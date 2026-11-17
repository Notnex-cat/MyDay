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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.notnex.myday.R
import com.notnex.myday.data.TIME_FORMAT
import com.notnex.myday.data.toScheduleItem
import com.notnex.myday.neuralnetwork.NNResult
import com.notnex.myday.neuralnetwork.NNViewModel
import com.notnex.myday.neuralnetwork.ScheduleItem
import com.notnex.myday.neuralnetwork.aiErrorText
import com.notnex.myday.ui.theme.MyDayTheme
import com.notnex.myday.viewmodel.MyDayViewModel
import java.time.LocalDate

@Composable
fun ScheduleScreen(
    navController: NavController,
    date: LocalDate,
    nnViewModel: NNViewModel = hiltViewModel(),
    viewModel: MyDayViewModel = hiltViewModel()
) {
    var userInput by rememberSaveable { mutableStateOf("") }
    val state by nnViewModel.scheduleState.collectAsStateWithLifecycle()
    val aiEnabled by nnViewModel.aiEnabled.collectAsStateWithLifecycle()

    // Черновик дня: сначала сохранённые пункты, потом к ним добавляются предложения нейросети.
    // «Сохранить» делает день ровно таким, как черновик. rememberSaveable — чтобы начатую
    // правку не сбрасывал поворот экрана.
    val draft = rememberSaveable(date, saver = stateListSaver()) { mutableStateListOf<ScheduleItem>() }
    val newIds = rememberSaveable(date, saver = stateListSaver()) { mutableStateListOf<String>() }

    // Сохранённые пункты читаются один раз: обновление из базы не должно затирать начатую правку,
    // а флаг (в отличие от проверки «черновик пуст») не вернёт пункты, которые человек удалил все.
    var draftLoaded by rememberSaveable(date) { mutableStateOf(false) }
    LaunchedEffect(date) {
        if (!draftLoaded) {
            draft.addAll(viewModel.getSchedule(date).map { it.toScheduleItem() })
            draftLoaded = true
        }
    }

    LaunchedEffect(state) {
        val result = state
        if (result is NNResult.Success) {
            // Повторный запрос заменяет прошлые предложения нейросети, сохранённые пункты не трогает.
            draft.removeAll { it.id in newIds }
            newIds.clear()
            draft.addAll(result.data)
            newIds.addAll(result.data.map { it.id })
            draft.sortBy { it.time }
            nnViewModel.scheduleConsumed()
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = MyDayTheme.spacing.md)
        ) {
            when (aiEnabled) {
                true -> {
                    OutlinedTextField(
                        value = userInput,
                        onValueChange = { userInput = it },
                        label = { Text(stringResource(R.string.describe_your_day)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(MyDayTheme.spacing.xs))

                    Button(
                        onClick = { nnViewModel.requestSchedule(userInput, date, draft.toList()) },
                        enabled = userInput.isNotBlank() && state !is NNResult.Loading
                    ) {
                        Text(stringResource(R.string.get_schedule))
                    }
                }
                // Без нейросети экран остаётся редактором дня: пункты можно удалять и сохранять.
                false -> Text(
                    text = stringResource(R.string.ai_disabled_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                null -> Unit // настройка ещё читается
            }

            Spacer(modifier = Modifier.height(MyDayTheme.spacing.md))

            when (val current = state) {
                is NNResult.Loading -> CircularProgressIndicator()
                is NNResult.Error -> Text(
                    text = aiErrorText(current.exception),
                    color = MaterialTheme.colorScheme.error
                )
                else -> Unit
            }

            LazyColumn {
                items(items = draft, key = { it.id }) { item ->
                    val isNew = item.id in newIds
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = MyDayTheme.spacing.xxs),
                        colors = if (isNew) {
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        } else {
                            CardDefaults.cardColors()
                        },
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "${item.time.format(TIME_FORMAT)} — ${item.task}",
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(MyDayTheme.spacing.md)
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
                    // Пустой черновик тоже сохраняется: так день можно очистить.
                    Button(
                        onClick = {
                            viewModel.replaceDaySchedule(date, draft.toList())
                            navController.popBackStack()
                        }
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}

// Список состояния Compose сам в Bundle не кладётся: сохраняем обычным списком и собираем обратно.
private fun <T : Any> stateListSaver() = listSaver<SnapshotStateList<T>, T>(
    save = { it.toList() },
    restore = { it.toMutableStateList() }
)
