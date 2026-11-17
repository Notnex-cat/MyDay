package com.notnex.myday.ui.screens.mainactivity

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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.notnex.myday.R
import com.notnex.myday.ui.navigation.toScheduleRoute
import com.notnex.myday.ui.theme.AppIcons
import com.notnex.myday.ui.theme.MyDayTheme
import java.time.LocalDate


@Composable
fun CustomFloatingActionButton(
    navController: NavController,
    selectedDate: LocalDate,
    fabIcon: ImageVector
) {
    var isExpanded by remember { mutableStateOf(false) }

    val fabSize = 64.dp
    val expandedFabWidth by animateDpAsState(
        targetValue = if (isExpanded) 200.dp else fabSize,
        animationSpec = spring(dampingRatio = 2f)
    )
    val expandedFabHeight by animateDpAsState(
        targetValue = if (isExpanded) 58.dp else fabSize,
        animationSpec = spring(dampingRatio = 2f)
    )

    // Сдвиги и прозрачность читаются в лямбдах offset { } и graphicsLayer { }: значение анимации
    // меняется каждый кадр, и так Compose только передвигает и перекрашивает готовое,
    // а не выполняет заново всю функцию кнопки.
    val iconOffset by animateDpAsState(
        targetValue = if (isExpanded) (-70).dp else 0.dp,
        animationSpec = spring(dampingRatio = 3f)
    )
    val textOffset by animateDpAsState(
        targetValue = if (isExpanded) 10.dp else 50.dp,
        animationSpec = spring(dampingRatio = 3f)
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (isExpanded) 100 else 50,
            delayMillis = if (isExpanded) 50 else 0,
            easing = EaseInOut
        )
    )

    Column(
        horizontalAlignment = Alignment.End
    ) {

        // Expanded options
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .padding(bottom = MyDayTheme.spacing.xs)
                    .background(
                        MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(vertical = MyDayTheme.spacing.sm)
            ) {
                TextButton(onClick = {
                    isExpanded = false
                    navController.navigate(selectedDate.toScheduleRoute())
                }
                ) {
                    Icon(AppIcons.MenuOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(MyDayTheme.spacing.xs))
                    Text(stringResource(R.string.create_schedule))
                }

                TextButton(onClick = { /* TODO: Handle New Reminder */ }) {
                    Icon(AppIcons.Alarm, contentDescription = null)
                    Spacer(modifier = Modifier.width(MyDayTheme.spacing.xs))
                    Text(stringResource(R.string.new_reminder))
                }
            }
        }

        // FAB itself
        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
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
                    .offset { IntOffset(iconOffset.roundToPx(), 0) }
            )

            Text(
                text = stringResource(R.string.create_reminder),
                softWrap = false,
                modifier = Modifier
                    .offset { IntOffset(textOffset.roundToPx(), 0) }
                    .graphicsLayer { alpha = textAlpha }
            )
        }
    }
}