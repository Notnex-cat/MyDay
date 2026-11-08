package com.notnex.myday.ui.screens.settingsactivity

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notnex.myday.ui.theme.MyDayTheme
import java.time.LocalTime

/**
 * Строка настроек напоминания. О ViewModel ничего не знает: данные приходят параметрами,
 * нажатия уходят наружу лямбдами, решает экран. Полоса делит строку на две цели нажатия:
 * текст открывает выбор времени, переключатель только включает и выключает.
 */
@Composable
fun ReminderRow(
    time: LocalTime?,
    title: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    onEnabledChange: (Boolean) -> Unit,
) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onTertiary
        ), modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),   // чтобы полоса знала высоту строки
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                Modifier
                    .weight(1f)   // колонка забирает всё свободное место и прижимает переключатель вправо
                    .clickable {  // нажимается только левая часть; padding после clickable, чтобы отступы тоже нажимались
                        onClick()
                    }
                    .padding(MyDayTheme.spacing.md)) {
                Text(
                    stringResource(title),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = time?.toString() ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        // 38 % — так в Material 3 выглядит неактивный элемент
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    },
                )
            }

            VerticalDivider(Modifier.padding(vertical = MyDayTheme.spacing.sm))

            Switch(
                checked = enabled, onCheckedChange = {
                    onEnabledChange(it)
                }, modifier = Modifier.padding(horizontal = MyDayTheme.spacing.md)
            )
        }
    }

}