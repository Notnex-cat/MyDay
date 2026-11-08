package com.notnex.myday.ui.screens.settingsactivity

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import java.time.LocalTime

/**
 * Окно выбора времени. Вызывается внутри условия, поэтому состояние пикера создаётся заново
 * при каждом открытии и берёт актуальное initialTime, а не то, что было при прошлом открытии.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderTimeDialog(
    initialTime: LocalTime?,
    onConfirm: (LocalTime) -> Unit,   // «пользователь выбрал время»
    onDismiss: () -> Unit,            // «пользователь закрыл окно»
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime?.hour ?: 0,
        initialMinute = initialTime?.minute ?: 0,
        is24Hour = true,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Установить время") },
        text = {
            TimeInput(
                state = timePickerState,
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(LocalTime.of(timePickerState.hour, timePickerState.minute))
            }) { Text("Установить") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) { Text("Отмена") }
        }
    )
}