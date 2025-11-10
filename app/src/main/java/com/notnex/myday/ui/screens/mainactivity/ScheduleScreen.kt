package com.notnex.myday.ui.screens.mainactivity

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.notnex.myday.neuralnetwork.NNResult
import com.notnex.myday.neuralnetwork.NNViewModel
import com.notnex.myday.neuralnetwork.ScheduleItem
import com.notnex.myday.ui.ScreenEditSchedule
import com.notnex.myday.viewmodel.MyDayViewModel
import java.time.LocalDate

@Composable
fun ScheduleScreen(
    navController: NavController,
    date: LocalDate,
    nnViewModel: NNViewModel = hiltViewModel(),
    viewModel: MyDayViewModel = hiltViewModel()
) {
    var userInput by remember { mutableStateOf("") }
    val state by nnViewModel.scheduleState.collectAsState()

    val context = LocalContext.current
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
                            Card(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp)
                                    .clickable {
                                        navController.navigate(ScreenEditSchedule(item = item.task))
                                    }
                            ) {
                                Text(
                                    "${item.time} — ${item.task}",
                                    modifier = Modifier.padding(14.dp)
                                )
                            }

                        }
                        item {
                            Button(onClick = {
                                schedule.forEach { item ->
                                    val itemString = "${item.time} — ${item.task}"
                                    viewModel.saveDaySchedule(date, itemString, "", 4.5, "")
                                }
                                Toast.makeText(context, "Сохранено ${schedule.size} элементов", Toast.LENGTH_SHORT).show()
                            }) {
                                Text("Save")
                            }
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