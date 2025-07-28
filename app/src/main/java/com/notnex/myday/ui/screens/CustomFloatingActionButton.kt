package com.notnex.myday.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp


@Composable
fun CustomFloatingActionButton(
    expandable: Boolean,
    onFabClick: () -> Unit,
    fabIcon: ImageVector
) {
    var isExpanded by remember { mutableStateOf(false) }
    if (!expandable) {
        isExpanded = false
    }

    val fabSize = 64.dp
    val expandedFabWidth by animateDpAsState(
        targetValue = if (isExpanded) 200.dp else fabSize,
        animationSpec = spring(dampingRatio = 2f)
    )
    val expandedFabHeight by animateDpAsState(
        targetValue = if (isExpanded) 58.dp else fabSize,
        animationSpec = spring(dampingRatio = 2f)
    )

    Column(
        horizontalAlignment = Alignment.End
    ) {

        // Expanded options
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(vertical = 12.dp)
            ) {
                TextButton(onClick = { /* TODO: Handle New Note */ }) {
                    Icon(Icons.Default.NoteAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Новая заметка")
                }

                TextButton(onClick = { /* TODO: Handle New Reminder */ }) {
                    Icon(Icons.Default.Alarm, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Новое напоминание")
                }
            }
        }

        // FAB itself
        FloatingActionButton(
            onClick = {
                onFabClick()
                if (expandable) {
                    isExpanded = !isExpanded
                }
            },
            modifier = Modifier
                .width(expandedFabWidth)
                .height(expandedFabHeight),
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(
                imageVector = fabIcon,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .offset(x = animateDpAsState(if (isExpanded) (-70).dp else 0.dp, animationSpec = spring(dampingRatio = 3f)).value)
            )

            Text(
                text = "Create Reminder",
                softWrap = false,
                modifier = Modifier
                    .offset(x = animateDpAsState(if (isExpanded) 10.dp else 50.dp, animationSpec = spring(dampingRatio = 3f)).value)
                    .alpha(
                        animateFloatAsState(
                            targetValue = if (isExpanded) 1f else 0f,
                            animationSpec = tween(
                                durationMillis = if (isExpanded) 100 else 50,
                                delayMillis = if (isExpanded) 50 else 0,
                                easing = EaseInOut
                            )
                        ).value
                    )
            )
        }
    }
}