package com.notnex.myday.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.notnex.myday.BuildConfig
import com.notnex.myday.R
import com.notnex.myday.ui.CARD_EXPLODE_BOUNDS_KEY
import com.notnex.myday.viewmodel.MyDayViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDate


@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.DayNote(
    navController: NavController,
    viewModel: MyDayViewModel = hiltViewModel(),
    currentRating: Double,
    date: LocalDate,
    note: String,
    animatedVisibilityScope: AnimatedVisibilityScope,
){
    var text by remember { mutableStateOf(note) }

    //val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    var saveJob by remember { mutableStateOf<Job?>(null) }

    val fullDB by viewModel.getScore(date).collectAsState(initial = null)

    var aiResponse by remember { mutableStateOf("") }

    LaunchedEffect(fullDB?.aiFeedback) {
        aiResponse = fullDB?.aiFeedback ?: ""
    }
    val apiKey = BuildConfig.NN_API_KEY

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .sharedBounds(
                sharedContentState = rememberSharedContentState(
                    key = CARD_EXPLODE_BOUNDS_KEY
                ),
                animatedVisibilityScope = animatedVisibilityScope
            ),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent //MaterialTheme.colorScheme.background
                ),
                title = { Text("Опишите свой день") },
                navigationIcon = {
                    IconButton(onClick = {navController.popBackStack()}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .imePadding()
                //.background(Color.Red)
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
                        delay(1500) // 500 мс после последнего ввода
                        viewModel.saveDayEntry(date, currentRating, it, aiResponse!!)
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
                    getResponse(
                        question = text,
                        apiKey = apiKey, // твой ключ
                        onStreamUpdate = { chunk ->
                            aiResponse += chunk
                            saveJob?.cancel()
                            saveJob = coroutineScope.launch {
                                delay(1000) // 500 мс после последнего ввода
                                viewModel.saveDayEntry(date, currentRating, text, aiResponse)
                            }
                        }
                    )
                }
            ) {
                Text(text = "Спросить")
            }

            // LazyColumn is used here to display the AI's response.
            // It's a vertically scrolling list that only composes and lays out the currently visible items.
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    // Text composable to display the AI's response.
                    Text(
                        text = aiResponse,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                    )
                }
            }
        }
    }
}

fun getResponse(question: String, apiKey: String, onStreamUpdate: (String) -> Unit) {
    val client = OkHttpClient()
    val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

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
