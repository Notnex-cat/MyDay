package com.notnex.myday.ui.screens.mainactivity

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
import com.notnex.myday.R
import com.notnex.myday.neuralnetwork.NNResult
import com.notnex.myday.neuralnetwork.NNViewModel
import com.notnex.myday.ui.CARD_EXPLODE_BOUNDS_KEY
import com.notnex.myday.viewmodel.MyDayViewModel
import kotlinx.coroutines.flow.first
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.DayNote(
    navController: NavController,
    viewModel: MyDayViewModel = hiltViewModel(),
    nnViewModel: NNViewModel = hiltViewModel(),
    date: LocalDate,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val day by remember(date) { viewModel.observeDay(date) }.collectAsState(initial = null)

    var noteText by remember { mutableStateOf("") }

    // Текст подставляем один раз при открытии: дальше источник правды — поле ввода,
    // иначе каждое автосохранение перезаписывало бы то, что пользователь печатает.
    LaunchedEffect(date) {
        noteText = viewModel.observeDay(date).first()?.note.orEmpty()
    }

    val feedbackState by nnViewModel.feedbackState.collectAsState()
    val streamedFeedback by nnViewModel.feedbackText.collectAsState()
    val isLoading = feedbackState is NNResult.Loading

    // Пока идёт (или только что закончился) запрос — показываем стрим, иначе сохранённый ответ
    val feedback = when (feedbackState) {
        is NNResult.Loading, is NNResult.Success -> streamedFeedback
        else -> day?.aiFeedback.orEmpty()
    }

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
                    containerColor = Color.Transparent
                ),
                title = { Text(stringResource(R.string.describe_your_day_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .imePadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            TextField(
                value = noteText,
                minLines = 5,
                maxLines = 10,
                placeholder = { Text(stringResource(R.string.write_something)) },
                onValueChange = {
                    noteText = it
                    viewModel.onNoteChanged(date, it)
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 20.sp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )

            Button(
                modifier = Modifier.padding(top = 16.dp),
                enabled = noteText.isNotBlank() && !isLoading,
                onClick = {
                    nnViewModel.requestFeedback(noteText) { fullText ->
                        viewModel.saveAiFeedback(date, fullText)
                    }
                }
            ) {
                Text(text = stringResource(R.string.ask_ai))
            }

            LazyColumn( // нужно, чтобы длинный ответ листался
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    if (isLoading && streamedFeedback.isEmpty()) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    }
                    if (feedbackState is NNResult.Error) {
                        Text(
                            text = stringResource(
                                R.string.error_with_message,
                                (feedbackState as NNResult.Error).exception.message.orEmpty()
                            ),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Text(
                        text = feedback,
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
