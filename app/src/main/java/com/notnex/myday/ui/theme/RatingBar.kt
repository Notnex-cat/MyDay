package com.notnex.myday.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Только звёзды оценки, без подписей: так их можно поставить и в карточку дня, и потом
 * в пункт расписания. Нажатие на левую половину звезды ставит половинку, на правую — целую.
 */
@Composable
fun RatingBar(
    modifier: Modifier = Modifier,
    rating: Double = 0.0,
    stars: Int = 5,
    starSize: Dp = 30.dp,
    onRatingChanged: (Double) -> Unit,
    starsColor: Color = Color.Yellow
) {
    // pointerInput(Unit) запускается один раз и запомнил бы первую лямбду навсегда.
    // rememberUpdatedState подменяет её на свежую, не перезапуская обработку жестов.
    val currentOnRatingChanged by rememberUpdatedState(onRatingChanged)
    val empty = MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MyDayTheme.spacing.xs),
    ) {
        for (index in 1..stars) {
            Box(
                modifier = Modifier
                    .size(starSize)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val half = size.width / 2
                            if (offset.x <= half) {
                                currentOnRatingChanged(index - 0.5)
                            } else {
                                currentOnRatingChanged(index.toDouble())
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    when {
                        index <= rating -> drawCircle(starsColor)
                        index - 0.5 == rating -> {
                            drawCircle(empty)
                            drawArc(
                                starsColor,
                                startAngle = 90f,
                                sweepAngle = 180f,
                                useCenter = true
                            )
                        }
                        else -> drawCircle(empty)
                    }
                }
            }
        }
    }
}

