package com.notnex.myday.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notnex.myday.neuralnetwork.NNResult
import com.notnex.myday.neuralnetwork.NNViewModel
import com.notnex.myday.neuralnetwork.ScheduleItem

@Composable
fun ScheduleScreen(
    nnViewModel: NNViewModel = hiltViewModel(),
) {
    var userInput by remember { mutableStateOf("") }
    val state by nnViewModel.scheduleState.collectAsState()
    Scaffold { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                label = { Text("Опиши свой день") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { nnViewModel.requestSchedule(userInput) },
                //enabled = userInput.isNotBlank()
            ) {
                Text("Получить расписание")
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (state) {
                is NNResult.Idle -> Text("")
                is NNResult.Loading -> CircularProgressIndicator()
                is NNResult.Success -> {
                    val schedule = (state as NNResult.Success<List<ScheduleItem>>).data
                    LazyColumn {
                        items(schedule) { item ->
                            Text("${item.time} — ${item.task}", modifier = Modifier.padding(4.dp))
                        }
                    }
                }
                is NNResult.Error -> {
                    Text("Ошибка: ${(state as NNResult.Error).exception.message}")
                }
            }
        }
    }
}