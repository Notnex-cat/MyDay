package com.notnex.myday.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.notnex.myday.R
import com.notnex.myday.viewmodel.MyDayViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDate


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayNote(
    navController: NavController,
    viewModel: MyDayViewModel = hiltViewModel(),
    currentRating: Double,
    date: LocalDate,
    note: String
){
    var text by remember { mutableStateOf(note) }

    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    var saveJob by remember { mutableStateOf<Job?>(null) }

    var aiResponse by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = { Text("Опишите свой день") },
                navigationIcon = {
                    IconButton(onClick = {navController.popBackStack()}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .imePadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            TextField(
                value = text,
                minLines = 5,
                //maxLines = 5,
                placeholder = { Text(stringResource(R.string.write_something)) },
                onValueChange = {
                    text = it
                    saveJob?.cancel() // отменяем предыдущую задачу
                    saveJob = coroutineScope.launch {
                        delay(1000) // 500 мс после последнего ввода
                        viewModel.saveDayEntry(date, currentRating, it)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 20.sp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,  // Отключаем линию при фокусе
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )

            Button(
                modifier = Modifier
                    .padding(top = 16.dp),
                onClick = {
                    aiResponse = "" // обнуляем перед новым ответом
                    coroutineScope.launch {
                        getResponse(
                            question = text,
                            apiKey = "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", // твой ключ
                            onStreamUpdate = { chunk ->
                                aiResponse += chunk
                            }
                        )
                    }
                }
            ) {
                Text(text = "Спросить")
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Text(
                        text = aiResponse,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

suspend fun getResponse(question: String, apiKey: String, onStreamUpdate: (String) -> Unit) {
    val client = OkHttpClient()
    val url = "https://api.groq.com/openai/v1/chat/completions"
    val cleanedQuestion = question.replace(Regex("[\\u0000-\\u001F]"), "")

    val requestBody = """
        {
          "model": "mistral-saba-24b",
          "stream": true,
          "messages": [
            {"role": "user", "content": "ты персональный ассистент-помощник по саморазвитию который помогает пользователю улучшить его показатели. Отвечай на языке на котором задается вопрос: $cleanedQuestion"}
          ]
        }
    """.trimIndent()

    val request = Request.Builder()
        .url(url)
        .addHeader("Content-Type", "application/json")
        .addHeader("Authorization", "Bearer $apiKey")
        .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("GroqError", "Request failed", e)
            onStreamUpdate("Ошибка: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            val source = response.body?.source() ?: return
            val buffer = okio.Buffer()

            try {
                while (!source.exhausted()) {
                    source.read(buffer, 8192)
                    val raw = buffer.readUtf8()
                    raw.split("\n").forEach { line ->
                        if (line.startsWith("data:")) {
                            val jsonPart = line.removePrefix("data:").trim()
                            if (jsonPart.isNotEmpty() && jsonPart != "[DONE]") {
                                try {
                                    val obj = JSONObject(jsonPart)
                                    val delta = obj.getJSONArray("choices")
                                        .getJSONObject(0)
                                        .getJSONObject("delta")
                                    if (delta.has("content")) {
                                        val chunk = delta.getString("content")
                                        onStreamUpdate(chunk)
                                    }
                                } catch (_: Exception) {}
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("GroqStreamError", "Stream parsing failed", e)
                onStreamUpdate("Ошибка потока: ${e.message}")
            }
        }
    })
}