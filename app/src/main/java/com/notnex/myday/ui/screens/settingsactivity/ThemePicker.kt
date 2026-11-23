package com.notnex.myday.ui.screens.settingsactivity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notnex.myday.R
import com.notnex.myday.ui.theme.MyDayTheme
import com.notnex.myday.ui.theme.ThemeStyle

/**
 * Выбор темы оформления: у каждой темы миниатюра — её фон и солнце фирменного цвета.
 * Миниатюры берут вариант (светлый или тёмный) тот же, что сейчас у системы.
 * Выбор не хранится здесь: выбранную тему передают снаружи, нажатие только сообщает о нём.
 */
@Composable
fun ThemePicker(
    selected: ThemeStyle,
    onSelect: (ThemeStyle) -> Unit,
) {
    val dark = isSystemInDarkTheme()
    ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MyDayTheme.spacing.md, vertical = MyDayTheme.spacing.xs),
    ) {
        Column(modifier = Modifier.padding(MyDayTheme.spacing.md)) {
            Text(stringResource(R.string.theme_style), style = MaterialTheme.typography.titleMedium)
            Row(
                horizontalArrangement = Arrangement.spacedBy(MyDayTheme.spacing.sm),
                modifier = Modifier.padding(top = MyDayTheme.spacing.sm),
            ) {
                ThemeStyle.entries.forEach { style ->
                    val palette = style.palette(dark)
                    val isSelected = style == selected
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .selectable(selected = isSelected, role = Role.RadioButton) { onSelect(style) }
                            .padding(MyDayTheme.spacing.xxs),
                    ) {
                        ThemeSwatch(
                            background = palette.background,
                            card = palette.card,
                            accent = palette.accent,
                            border = if (isSelected) MaterialTheme.colorScheme.primary else palette.line,
                            borderWidth = if (isSelected) 3 else 1,
                        )
                        Text(
                            text = stringResource(style.title),
                            // Мельче и в одну строку: жирное «Солнечный» в треть ширины не помещалось.
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            modifier = Modifier.padding(top = MyDayTheme.spacing.xs),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeSwatch(background: Color, card: Color, accent: Color, border: Color, borderWidth: Int) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(shape)
            .background(background)
            .border(borderWidth.dp, border, shape),
    ) {
        // Солнце садится за «карточку» — как на иконке.
        Box(
            Modifier
                .align(Alignment.Center)
                .offset(y = 4.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .aspectRatio(2.6f)
                .background(card)
        )
    }
}
