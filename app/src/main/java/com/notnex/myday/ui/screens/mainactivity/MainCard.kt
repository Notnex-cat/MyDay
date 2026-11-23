package com.notnex.myday.ui.screens.mainactivity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.notnex.myday.ui.theme.MyDayTheme

@Composable
fun MainCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    // Карточке с заголовком нужен отступ сверху поменьше, чтобы на глаз он совпадал с остальными.
    topPadding: Dp = MyDayTheme.spacing.sm,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()                                   // 1
            .padding(
                start = MyDayTheme.spacing.md,
                end = MyDayTheme.spacing.md,
                bottom = MyDayTheme.spacing.sm,
            )                                                 // 2
            .clip(RoundedCornerShape(22.dp))                  // 3
            .background(containerColor)                       // 4
            .then(modifier)                                   // 5
            .padding(
                start = MyDayTheme.spacing.md,
                end = MyDayTheme.spacing.md,
                top = topPadding,
                bottom = MyDayTheme.spacing.sm,
            ),                                                // 6
        content = content,                                    // 7
    )
}