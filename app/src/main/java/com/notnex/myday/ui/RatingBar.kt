package com.notnex.myday.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarHalf
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp


@Composable
fun RatingBar(
    modifier: Modifier = Modifier,
    rating: Double = 0.0,
    stars: Int = 5,
    onRatingChanged: (Double) -> Unit,
    starsColor: Color = Color.Yellow
) {
    Row {
        for (index in 1..stars) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val half = size.width / 2
                            if (offset.x <= half) {
                                onRatingChanged(index - 0.5)
                            } else {
                                onRatingChanged(index.toDouble())
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        index <= rating -> Icons.Rounded.Star
                        index - 0.5 == rating -> Icons.Rounded.StarHalf
                        else -> Icons.Rounded.StarOutline
                    },
                    contentDescription = null,
                    tint = starsColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
