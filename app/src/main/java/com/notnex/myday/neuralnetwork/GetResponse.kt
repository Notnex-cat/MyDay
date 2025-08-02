package com.notnex.myday.neuralnetwork

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


fun getResponse(question: String, apiKey: String, onStreamUpdate: (String) -> Unit) {
    val client = OkHttpClient()
    val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

    val content = question.replace(Regex("\\s+"), " ").trim()
    val requestBody = """
        {
          "model": "mistral-medium-latest",
          "messages": [
            {
              "role": "user",
              "content": "ты персональный ассистент-помощник по саморазвитию который помогает пользователю улучшить его показатели. Отвечай на языке на котором задается вопрос: $question"
            }
          ],
          "stream": true
        }
        """.trimIndent()


    val request = Request.Builder()
        .url("https://api.mistral.ai/v1/chat/completions")
        .post(requestBody.toRequestBody("application/json".toMediaType()))
        .addHeader("Authorization", "Bearer $apiKey")
        .addHeader("Accept", "text/event-stream")
        .build()

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

                    //Log.d("STREAM", content)

                    if (content.isNotEmpty()) {
                        mainHandler.post {
                            onStreamUpdate(content)
                        }
                    }
                }
            }
        }
    })
}