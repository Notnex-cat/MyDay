package com.notnex.myday.ui.theme

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput


@Composable
fun RatingBar(
    modifier: Modifier = Modifier,
    rating: Double = 0.0,
    stars: Int = 5,
    onRatingChanged: (Double) -> Unit,
    starsColor: Color = Color.Yellow
) {
    // pointerInput(Unit) запускается один раз и запомнил бы первую лямбду навсегда.
    // rememberUpdatedState подменяет её на свежую, не перезапуская обработку жестов.
    val currentOnRatingChanged by rememberUpdatedState(onRatingChanged)
    Row {
        for (index in 1..stars) {
            Box(
                modifier = modifier
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
                Icon(
                    imageVector = when {
                        index <= rating -> Icons.Rounded.Star
                        index - 0.5 == rating -> AppIcons.StarHalf
                        else -> AppIcons.StarOutline
                    },
                    contentDescription = null,
                    tint = starsColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
