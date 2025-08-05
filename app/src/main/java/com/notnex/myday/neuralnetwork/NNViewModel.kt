package com.notnex.myday.neuralnetwork

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notnex.myday.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class NNViewModel @Inject constructor(
    private val client: OkHttpClient
) : ViewModel() {
    private val _scheduleState = MutableStateFlow<NNResult<List<ScheduleItem>>>(NNResult.Idle)
    val scheduleState: StateFlow<NNResult<List<ScheduleItem>>> = _scheduleState

    private val _responseState = MutableStateFlow<NNResult<Unit>>(NNResult.Idle)
    val responseState: StateFlow<NNResult<Unit>> = _responseState

    val apiKey = BuildConfig.NN_API_KEY
    private val model = "mistral-medium-latest"
    private val endpoint = "https://api.mistral.ai/v1/chat/completions"
    private val mediaType = "application/json".toMediaType()

    fun requestSchedule(userInput: String) {
        viewModelScope.launch {
            _scheduleState.value = NNResult.Loading
            try {
                val scheduleList = withContext(Dispatchers.IO) {
                    getScheduleResponse(userInput)
                }
                _scheduleState.value = NNResult.Success(scheduleList)
            } catch (e: Exception) {
                _scheduleState.value = NNResult.Error(e)
                Log.e("ScheduleVM", "Error: $e")
            }
        }
    }

    fun requestFromDayNote(userInput: String, onStreamUpdate: (String) -> Unit) {
        viewModelScope.launch {
            _responseState.value = NNResult.Loading
            try {
                val nnrequest = withContext(Dispatchers.IO) {
                    getResponse(userInput, onStreamUpdate) // возвращает String
                }
                _responseState.value = NNResult.Success(nnrequest)
            } catch (e: Exception) {
                _responseState.value = NNResult.Error(e)
                Log.e("ScheduleVM", "Error: $e")
            }
        }
    }

    fun getScheduleResponse(userInput: String): List<ScheduleItem> {
        val prompt = buildPrompt(PromptType.Schedule, userInput)
        val request = buildRequest(prompt, false)

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw Exception("HTTP ${response.code}")

        val bodyString = response.body.string()
        val jsonObject = Json.Default.parseToJsonElement(bodyString).jsonObject
        val choices = jsonObject["choices"]?.jsonArray ?: throw Exception("Missing choices")
        val message = choices[0].jsonObject["message"]?.jsonObject ?: throw Exception("Missing message")
        val content = message["content"]?.jsonPrimitive?.content ?: throw Exception("Missing content")

        val cleanedJson = content
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        return Json.Default.decodeFromString(cleanedJson)
    }

    fun getResponse(userInput: String, onStreamUpdate: (String) -> Unit) {
        val content = userInput.replace(Regex("\\s+"), " ").trim()
        val prompt = buildPrompt(PromptType.NoteFeedback, content)
        val request = buildRequest(prompt, true)

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("HTTP", "Ошибка запроса: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val source = response.body.source()

                while (!source.exhausted()) {
                    val line = source.readUtf8Line()
                    if (line != null && line.startsWith("data: ")) {
                        val json = line.removePrefix("data: ").trim()

                        if (json == "[DONE]") break

                        val content = JSONObject(json)
                            .getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("delta")
                            .optString("content", "")

                        if (content.isNotEmpty()) {
                            onStreamUpdate(content) // 🔄 Стриминг в UI
                        }
                    }
                }
            }
        })
    }

    private fun buildRequest(content: String, stream: Boolean): Request {
        val body = """
        {
          "model": "$model",
          "messages": [
            {
              "role": "user",
              "content": "$content"
            }
          ],
          "stream": $stream
        }
    """.trimIndent()

        return Request.Builder()
            .url(endpoint)
            .post(body.toRequestBody(mediaType))
            .addHeader("Authorization", "Bearer $apiKey")
            .apply {
                if (stream) addHeader("Accept", "text/event-stream")
            }
            .build()
    }

    private fun buildPrompt(task: PromptType, input: String): String = when (task) {
        PromptType.Schedule -> "Ты — ассистент по составлению расписания дня. Пользователь сказал: $input Сформируй подробное расписание на основе этого, в формате JSON-массива с объектами: - time: время в формате \\\"HH:mm\\\" - task: описание задачи Пример ответа:[ {\\\"time\\\": \\\"07:30\\\", \\\"task\\\": \\\"Подъем и зарядка\\\"}, {\\\"time\\\": \\\"08:00\\\", \\\"task\\\": \\\"Завтрак\\\"}, {\\\"time\\\": \\\"09:00\\\", \\\"task\\\": \\\"Работа над проектом\\\"} ] Возвращай ТОЛЬКО JSON — никаких пояснений и комментариев."
        PromptType.NoteFeedback -> "ты персональный ассистент... Отвечай на языке на котором задан вопрос: ${input.replace(Regex("\\s+"), " ").trim()}"
    }

}

enum class PromptType { Schedule, NoteFeedback }
