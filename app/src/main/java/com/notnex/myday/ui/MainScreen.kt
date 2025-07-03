package com.notnex.myday.ui

import android.R
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.notnex.myday.viewmodel.MyDayViewModel
import com.notnex.myday.viewmodel.Screen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: MyDayViewModel = hiltViewModel()
    ) {

    val context = LocalContext.current
    val weekOffsets = rememberPagerState(5, 0f) { 10 }

    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now()) }

    val dayOfWeekFormatter = DateTimeFormatter.ofPattern("E", Locale.getDefault()) // "Пн"
    val dayOfMonthFormatter = DateTimeFormatter.ofPattern("d") // "26"
    Column {
        HorizontalPager(
            state = weekOffsets,
            modifier = modifier.padding(vertical = 16.dp)
        ) { page ->

            val startOfWeek = LocalDate.now().minusWeeks(5)
                .with(WeekFields.of(Locale.getDefault()).firstDayOfWeek)
                .plusWeeks(page.toLong())

            val weekDates = (0..6).map { startOfWeek.plusDays(it.toLong()) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weekDates.forEach { date ->
                    val isSelected = date == selectedDate

                    Column(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .clickable { selectedDate = date }
                            .padding(vertical = 8.dp, horizontal = 11.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = date.format(dayOfWeekFormatter),
                            fontSize = 12.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = date.format(dayOfMonthFormatter),
                            fontSize = 18.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        val note by viewModel.getScore(selectedDate).collectAsState(initial = null)

        val currentRating = note?.score ?: 4.5

        val text = note?.note ?: ""

        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(),
            contentAlignment = Alignment.Center
        ) {
            Column {
                Text(text = "$note")

                Button(
                    onClick = {
                        navController.navigate("${Screen.DayNote.route}/${selectedDate}/${currentRating}")
                    }
                ) {
                    Text(text = text)
                }

                RatingBar(
                    modifier = Modifier
                        .size(50.dp),
                    rating = currentRating,
                    onRatingChanged = {
                        viewModel.saveDayEntry(selectedDate, it, text)
                    },
                    starsColor = when {
                        currentRating >= 4.0 -> Color.Yellow
                        currentRating >= 2.5 -> colorResource(R.color.holo_orange_dark)
                        else -> Color.Red
                    }
                )
            }
        }
    }
}