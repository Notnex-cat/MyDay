package com.notnex.myday.ui.screens.mainactivity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.notnex.myday.R
import com.notnex.myday.data.TIME_FORMAT
import com.notnex.myday.ui.theme.AppIcons
import com.notnex.myday.ui.theme.MyDayTheme
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleItemSheet(
    title: String,
    name: String,
    initialTime: LocalTime,
    initialNote: String?,
    onSave: (String, LocalTime, String) -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)?
) {

    var input by rememberSaveable { mutableStateOf(name) }
    var time by rememberSaveable { mutableStateOf(initialTime) }
    var note by rememberSaveable { mutableStateOf(initialNote.orEmpty()) }

    var showTimePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MyDayTheme.spacing.md)
                .padding(bottom = MyDayTheme.spacing.md)
                // Шторка доходит до низа экрана: кнопки не должны прятаться под панелью навигации.
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(MyDayTheme.spacing.sm),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text(stringResource(R.string.schedule_item_title)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(AppIcons.Schedule, contentDescription = null)
                Text(
                    text = stringResource(R.string.schedule_item_time, time.format(TIME_FORMAT)),
                    modifier = Modifier.padding(start = MyDayTheme.spacing.xs),
                )
            }

            if (initialNote != null) {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.schedule_item_note)) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text(
                            text = stringResource(R.string.delete),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                Button(
                    enabled = input.isNotBlank(),
                    onClick = { onSave(input.trim(), time, note.trim()) },
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }

    if (showTimePicker) {
        val pickerState = rememberTimePickerState(
            initialHour = time.hour,
            initialMinute = time.minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    time = LocalTime.of(pickerState.hour, pickerState.minute)
                    showTimePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            text = { TimePicker(state = pickerState) }
        )
    }

}