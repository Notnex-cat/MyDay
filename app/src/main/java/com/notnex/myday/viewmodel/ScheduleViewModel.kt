package com.notnex.myday.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notnex.myday.BuildConfig
import com.notnex.myday.neuralnetwork.NNResult
import com.notnex.myday.neuralnetwork.ScheduleItem
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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor() : ViewModel() {

    val apiKey = BuildConfig.NN_API_KEY
    private val _scheduleState = MutableStateFlow<NNResult<List<ScheduleItem>>>(NNResult.Loading)
    val scheduleState: StateFlow<NNResult<List<ScheduleItem>>> = _scheduleState

    fun requestSchedule(userInput: String) {
        viewModelScope.launch {
            _scheduleState.value = NNResult.Loading
            try {
                val scheduleList = withContext(Dispatchers.IO) {
                    getScheduleResponse(apiKey, userInput)
                }
                _scheduleState.value = NNResult.Success(scheduleList)
            } catch (e: Exception) {
                _scheduleState.value = NNResult.Error(e)
                Log.e("ScheduleVM", "Error: $e")
            }
        }
    }


    fun getScheduleResponse(apiKey: String, userInput: String): List<ScheduleItem> {
        val client = OkHttpClient()
        val mediaType = "application/json".toMediaType()

        val prompt = """
            Ты — ассистент по составлению расписания дня.  
            Пользователь сказал: "$userInput"  
            Сформируй подробное расписание на основе этого, в формате JSON-массива с объектами:  
            - time: время в формате "HH:mm"  
            - task: описание задачи

            Пример ответа:  
            [
              {"time": "07:30", "task": "Подъем и зарядка"},
              {"time": "08:00", "task": "Завтрак"},
              {"time": "09:00", "task": "Работа над проектом"}
            ]

            Возвращай ТОЛЬКО JSON — никаких пояснений и комментариев.
        """.trimIndent()

        val requestBodyJson = """
        {
          "model": "mistral-medium-latest",
          "messages": [
            {
              "role": "user",
              "content": "Ты — ассистент по составлению расписания дня. Пользователь сказал: $userInput Сформируй подробное расписание на основе этого, в формате JSON-массива с объектами: - time: время в формате \"HH:mm\" - task: описание задачи Пример ответа:[ {\"time\": \"07:30\", \"task\": \"Подъем и зарядка\"}, {\"time\": \"08:00\", \"task\": \"Завтрак\"}, {\"time\": \"09:00\", \"task\": \"Работа над проектом\"} ] Возвращай ТОЛЬКО JSON — никаких пояснений и комментариев."
            }
          ],
          "stream": false
        }
    """.trimIndent()

        val request = Request.Builder()
            .url("https://api.mistral.ai/v1/chat/completions")
            .post(requestBodyJson.toRequestBody(mediaType))
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw Exception("HTTP ${response.code}")

        val bodyString = response.body.string()
        val jsonObject = Json.parseToJsonElement(bodyString).jsonObject
        val choices = jsonObject["choices"]?.jsonArray ?: throw Exception("Missing choices")
        val message = choices[0].jsonObject["message"]?.jsonObject ?: throw Exception("Missing message")
        val content = message["content"]?.jsonPrimitive?.content ?: throw Exception("Missing content")

        val cleanedJson = content
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        return Json.decodeFromString(cleanedJson)
    }
}

