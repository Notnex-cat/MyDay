package com.notnex.myday.neuralnetwork

import com.notnex.myday.data.AiSettingsRepository
import com.notnex.myday.data.TIME_FORMAT
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
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
import java.time.DateTimeException
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class AiRepository @Inject constructor(
    private val client: OkHttpClient,
    private val aiSettingsRepository: AiSettingsRepository
) {
    private val json = Json { ignoreUnknownKeys = true }

    /** Генерирует расписание дня по свободному описанию пользователя. */
    suspend fun generateSchedule(
        userInput: String,
        date: LocalDate,
        existing: List<ScheduleItem>
    ): List<ScheduleItem> {
        val provider = aiSettingsRepository.provider.first()
        val apiKey = aiSettingsRepository.apiKey(provider).first()
            ?: throw AiKeyMissingException()

        val planned = existing.joinToString("\n") { "${it.time.format(TIME_FORMAT)} — ${it.task}" }
        val fullInput = if (existing.isEmpty()) userInput
        else "$userInput\n\nУже запланировано:\n$planned\nНе повторяй эти пункты, верни только новые."
        val request =
            buildRequest(SCHEDULE_SYSTEM_PROMPT, fullInput, stream = false, provider, apiKey)
        val body = client.newCall(request).await()


        val items = parseScheduleResponse(json, body)

        return items.map { item ->
            ScheduleItem(
                id = UUID.randomUUID().toString(),
                date = date,
                time = item.time,
                task = item.task
            )
        }
    }

    /** Стримит ответ по кусочкам. Отмена подписки обрывает HTTP-запрос. */
    fun streamFeedback(note: String): Flow<String> = callbackFlow {
        val provider = aiSettingsRepository.provider.first()
        val apiKey = aiSettingsRepository.apiKey(provider).first()
            ?: throw AiKeyMissingException()
        val call = client.newCall(
            buildRequest(
                FEEDBACK_SYSTEM_PROMPT,
                note,
                stream = true,
                provider,
                apiKey
            )
        )

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                close(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    try {
                        if (!it.isSuccessful) throw AiHttpException(it.code, it.body.string())
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

    private fun parseDelta(data: String): String? = try {
        json.parseToJsonElement(data).jsonObject["choices"]
            ?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("delta")
            ?.jsonObject?.get("content")
            ?.jsonPrimitive?.contentOrNull
            ?.takeIf { it.isNotEmpty() }
    } catch (e: IllegalArgumentException) {
        // SerializationException — тоже IllegalArgumentException.
        throw AiResponseFormatException("Unexpected stream chunk", e)
    }

    // Тело собираем через JSON-билдер: пользовательский текст с кавычками и переводами строк
    // не ломает запрос (раньше он подставлялся в строку руками).
    private fun buildRequest(
        systemPrompt: String,
        userInput: String,
        stream: Boolean,
        provider: AiProvider,
        apiKey: String
    ): Request {
        val body: JsonObject = buildJsonObject {
            put("model", provider.model)
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
            .url(provider.endpoint)
            .post(body.toString().toRequestBody(JSON_MEDIA_TYPE))
            .addHeader("Authorization", "Bearer $apiKey")
            .apply { if (stream) addHeader("Accept", "text/event-stream") }
            .build()
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
                        if (!it.isSuccessful) throw AiHttpException(it.code, it.body.string())
                        continuation.resume(it.body.string())
                    } catch (e: IOException) {
                        continuation.resumeWithException(e)
                    }
                }
            }
        })
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json".toMediaType()

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

class AiHttpException(val code: Int, body: String) : IOException("HTTP $code: $body")

/** В настройках нет ключа выбранного провайдера: запрос даже не отправлялся. */
class AiKeyMissingException : IllegalStateException("AI key is not set")

/** Нейросеть ответила, но не тем, что можно разобрать: не JSON, не массив, кривое время. */
class AiResponseFormatException(message: String, cause: Throwable? = null) : IOException(message, cause)

/**
 * Достаёт пункты расписания из ответа в формате OpenAI chat completions.
 * Любая ошибка разбора становится [AiResponseFormatException]: человеку нужно сказать
 * «нейросеть ответила не то», а не «нет интернета».
 */
internal fun parseScheduleResponse(json: Json, body: String): List<ScheduleItemResponse> = try {
    val content = json.parseToJsonElement(body).jsonObject["choices"]
        ?.jsonArray?.firstOrNull()
        ?.jsonObject?.get("message")
        ?.jsonObject?.get("content")
        ?.jsonPrimitive?.contentOrNull
        ?: throw AiResponseFormatException("Response has no message content")
    json.decodeFromString<List<ScheduleItemResponse>>(extractJsonArray(content))
} catch (e: IllegalArgumentException) {
    // Сюда попадает и SerializationException: не JSON или не та структура.
    throw AiResponseFormatException("Response is not valid JSON", e)
} catch (e: DateTimeException) {
    throw AiResponseFormatException("Response has an invalid time", e)
}

// Нейросеть иногда оборачивает ответ в ```json ... ``` или добавляет пояснение вокруг массива.
internal fun extractJsonArray(content: String): String {
    val start = content.indexOf('[')
    val end = content.lastIndexOf(']')
    if (start == -1 || end <= start) throw AiResponseFormatException("Response does not contain a JSON array")
    return content.substring(start, end + 1)
}
