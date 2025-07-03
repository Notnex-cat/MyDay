package com.notnex.myday.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.notnex.myday.R
import com.notnex.myday.viewmodel.MyDayViewModel
import com.notnex.myday.viewmodel.Screen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MyDayViewModel = hiltViewModel()
    ) {

    val context = LocalContext.current
    val weekOffsets = rememberPagerState(5, 0f) { 10 }

    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now()) }

    val dayOfWeekFormatter = DateTimeFormatter.ofPattern("E", Locale.getDefault()) // "Пн"
    val dayOfMonthFormatter = DateTimeFormatter.ofPattern("d") // "26"

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
            )
        }) { innerPadding ->
        Column {
            HorizontalPager(
                state = weekOffsets,
                modifier = Modifier.padding(innerPadding)
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
                        val isSelected = date == selectedDate // это такие сложные условия оба
                        val isToday = date == LocalDate.now() //

                        Column(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else if (isToday) MaterialTheme.colorScheme.secondaryContainer
                                    else Color.Transparent
                                )
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

            var text = note?.note ?: ""

            Column {
                //Text(text = "$note")

                ElevatedCard( // текст о дне
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            navController.navigate("${Screen.DayNote.route}/${selectedDate}/${currentRating}/${text}")
                        }
                ) {
                    Text(
                        text = text.ifEmpty { stringResource(R.string.write_something) },
                        color = if (text.isEmpty()) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(16.dp),
                        maxLines = 10
                    )
                }

                RatingBar( // рейтинг дня
                    modifier = Modifier
                        .size(60.dp),
                    rating = currentRating,
                    onRatingChanged = {
                        viewModel.saveDayEntry(selectedDate, it, text)
                    },
                    starsColor = when {
                        currentRating >= 4.0 -> Color.Green
                        currentRating >= 2.5 -> colorResource(R.color.orange)
                        else -> Color.Red
                    }
                )
            }
        }
    }
}