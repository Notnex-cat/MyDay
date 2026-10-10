package com.notnex.myday.neuralnetwork

import com.notnex.myday.BuildConfig
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class MistralRepository @Inject constructor(
    private val client: OkHttpClient
) {
    private val json = Json { ignoreUnknownKeys = true }

    /** Генерирует расписание дня по свободному описанию пользователя. */
    suspend fun generateSchedule(userInput: String, date: LocalDate): List<ScheduleItem> {
        val request = buildRequest(SCHEDULE_SYSTEM_PROMPT, userInput, stream = false)
        val body = client.newCall(request).await()

        val content = json.parseToJsonElement(body).jsonObject["choices"]
            ?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("message")
            ?.jsonObject?.get("content")
            ?.jsonPrimitive?.contentOrNull
            ?: throw IOException("Unexpected response format")

        val items = json.decodeFromString<List<ScheduleItemResponse>>(extractJsonArray(content))

        // id = YYYYMMDD_HHmm: детерминированный, поэтому повторная генерация на то же время
        // обновляет пункт, а не плодит дубли.
        val datePart = date.format(DateTimeFormatter.BASIC_ISO_DATE)
        return items.map { item ->
            ScheduleItem(
                id = "${datePart}_${item.time.format(ID_TIME_FORMAT)}",
                date = date,
                time = item.time,
                task = item.task
            )
        }
    }

    /** Стримит ответ по кусочкам. Отмена подписки обрывает HTTP-запрос. */
    fun streamFeedback(note: String): Flow<String> = callbackFlow {
        val call = client.newCall(buildRequest(FEEDBACK_SYSTEM_PROMPT, note, stream = true))
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                close(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    try {
                        if (!it.isSuccessful) throw IOException("HTTP ${it.code}")
                        val source = it.body.source()
                        while (true) {
                            val line = source.readUtf8Line() ?: break
                            if (!line.startsWith("data:")) continue
                            val data = line.removePrefix("data:").trim()
                            if (data == "[DONE]") break
                            parseDelta(data)?.let { chunk -> trySendBlocking(chunk) }
                        }
                        close()
                    } catch (e: Exception) {
                        close(e)
                    }
                }
            }
        })
        awaitClose { call.cancel() }
    }.buffer(Channel.UNLIMITED)

    private fun parseDelta(data: String): String? =
        json.parseToJsonElement(data).jsonObject["choices"]
            ?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("delta")
            ?.jsonObject?.get("content")
            ?.jsonPrimitive?.contentOrNull
            ?.takeIf { it.isNotEmpty() }

    // Тело собираем через JSON-билдер: пользовательский текст с кавычками и переводами строк
    // не ломает запрос (раньше он подставлялся в строку руками).
    private fun buildRequest(systemPrompt: String, userInput: String, stream: Boolean): Request {
        val body: JsonObject = buildJsonObject {
            put("model", MODEL)
            put("stream", stream)
            putJsonArray("messages") {
                addJsonObject {
                    put("role", "system")
                    put("content", systemPrompt)
                }
                addJsonObject {
                    put("role", "user")
                    put("content", userInput)
                }
            }
        }
        return Request.Builder()
            .url(ENDPOINT)
            .post(body.toString().toRequestBody(JSON_MEDIA_TYPE))
            .addHeader("Authorization", "Bearer ${BuildConfig.NN_API_KEY}")
            .apply { if (stream) addHeader("Accept", "text/event-stream") }
            .build()
    }

    // Нейросеть иногда оборачивает ответ в ```json ... ``` или добавляет пояснение вокруг массива.
    private fun extractJsonArray(content: String): String {
        val start = content.indexOf('[')
        val end = content.lastIndexOf(']')
        if (start == -1 || end <= start) throw IOException("Response does not contain a JSON array")
        return content.substring(start, end + 1)
    }

    private suspend fun Call.await(): String = suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation { cancel() }
        enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    try {
                        if (!it.isSuccessful) throw IOException("HTTP ${it.code}")
                        continuation.resume(it.body.string())
                    } catch (e: IOException) {
                        continuation.resumeWithException(e)
                    }
                }
            }
        })
    }

    private companion object {
        const val MODEL = "mistral-small-latest"
        const val ENDPOINT = "https://api.mistral.ai/v1/chat/completions"
        val JSON_MEDIA_TYPE = "application/json".toMediaType()
        val ID_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HHmm")

        const val SCHEDULE_SYSTEM_PROMPT =
            "Ты — ассистент по составлению расписания дня. По описанию пользователя сформируй подробное " +
                "расписание в виде JSON-массива объектов с полями: time — время в формате \"HH:mm\", " +
                "task — описание задачи. Пример: " +
                "[{\"time\": \"07:30\", \"task\": \"Подъем и зарядка\"}, {\"time\": \"08:00\", \"task\": \"Завтрак\"}]. " +
                "Пиши задачи на языке пользователя. Возвращай ТОЛЬКО JSON — никаких пояснений и комментариев."

        const val FEEDBACK_SYSTEM_PROMPT =
            "Ты персональный ассистент по саморазвитию. Пользователь описал свой день. " +
                "Дай короткую обратную связь и совет. Отвечай на языке, на котором написана заметка."
    }
}
