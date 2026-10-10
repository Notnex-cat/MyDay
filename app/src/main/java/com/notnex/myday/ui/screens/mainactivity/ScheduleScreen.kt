package com.notnex.myday.ui.screens.mainactivity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.notnex.myday.R
import com.notnex.myday.data.TIME_FORMAT
import com.notnex.myday.neuralnetwork.NNResult
import com.notnex.myday.neuralnetwork.NNViewModel
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

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                label = { Text(stringResource(R.string.describe_your_day)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { nnViewModel.requestSchedule(userInput, date) },
                enabled = userInput.isNotBlank() && state !is NNResult.Loading
            ) {
                Text(stringResource(R.string.get_schedule))
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val current = state) {
                is NNResult.Idle -> Unit
                is NNResult.Loading -> CircularProgressIndicator()
                is NNResult.Success -> {
                    // Черновик, который можно прорядить перед сохранением
                    val draft = remember(current.data) { current.data.toMutableStateList() }

                    LazyColumn {
                        items(items = draft, key = { it.id }) { item ->
                            Card(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        "${item.time.format(TIME_FORMAT)} — ${item.task}",
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(14.dp)
                                    )
                                    IconButton(onClick = { draft.remove(item) }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.delete)
                                        )
                                    }
                                }
                            }
                        }
                        item {
                            Button(
                                enabled = draft.isNotEmpty(),
                                onClick = {
                                    viewModel.addScheduleItems(date, draft.toList())
                                    navController.popBackStack()
                                }
                            ) {
                                Text(stringResource(R.string.save))
                            }
                        }
                    }
                }
                is NNResult.Error -> Text(
                    text = stringResource(R.string.error_with_message, current.exception.message.orEmpty()),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
