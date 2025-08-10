package com.notnex.myday.ui.screens

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
import androidx.compose.material3.CircularProgressIndicator
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
import com.notnex.myday.neuralnetwork.NNResult
import com.notnex.myday.neuralnetwork.NNViewModel
import com.notnex.myday.ui.CARD_EXPLODE_BOUNDS_KEY
import com.notnex.myday.viewmodel.MyDayViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.DayNote(
    navController: NavController,
    viewModel: MyDayViewModel = hiltViewModel(),
    nnViewModel: NNViewModel = hiltViewModel(),
    date: LocalDate,
    animatedVisibilityScope: AnimatedVisibilityScope,
){
    val coroutineScope = rememberCoroutineScope()

    var saveJob by remember { mutableStateOf<Job?>(null) }

    val fullDB by viewModel.getScore(date).collectAsState(initial = null)

    val currentRating = fullDB?.score ?: 4.5

    var localtext by remember { mutableStateOf("") }

    var aiResponse by remember { mutableStateOf("") }

    val state by nnViewModel.responseState.collectAsState()

    LaunchedEffect(fullDB) {
        if (fullDB != null && localtext.isEmpty()) {
            localtext = fullDB!!.note
        }
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
                value = localtext,
                minLines = 5,
                maxLines = 10,
                placeholder = { Text(stringResource(R.string.write_something)) },
                onValueChange = {
                    localtext = it
                    saveJob?.cancel() // отменяем предыдущую задачу
                    saveJob = coroutineScope.launch {
                        delay(1500) // 1500 мс после последнего ввода
                        viewModel.saveDayEntry(date, currentRating, it, aiResponse)
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
                    nnViewModel.requestFromDayNote(
                        userInput = localtext,
                        onStreamUpdate = { chunk ->
                            aiResponse += chunk
                            saveJob?.cancel()
                            saveJob = coroutineScope.launch {
                                delay(1500)
                                viewModel.saveDayEntry(date, currentRating, localtext, aiResponse)
                            }
                        }
                    )
                }
            ) {
                Text(text = "Спросить")
            }

            LazyColumn( // нужено чтобы ответ листался
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    when (state) {
                        is NNResult.Loading -> CircularProgressIndicator()
                        is NNResult.Success, NNResult.Idle -> {
                            Text(
                                text = aiResponse,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                            )
                        }
                        is NNResult.Error -> {
                            Text("Ошибка: ${(state as NNResult.Error).exception.message}")
                        }
                    }
                }
            }
        }
    }
}