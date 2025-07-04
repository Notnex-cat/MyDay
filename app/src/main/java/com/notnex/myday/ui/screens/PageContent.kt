package com.notnex.myday.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.notnex.myday.R
import com.notnex.myday.ui.theme.RatingBar
import com.notnex.myday.viewmodel.MyDayViewModel
import com.notnex.myday.viewmodel.Screen
import java.time.LocalDate


@Composable
fun PageContent(
    selectedDate: LocalDate,
    navController: NavController,
    viewModel: MyDayViewModel
) {
    val note by viewModel.getScore(selectedDate).collectAsState(initial = null)

    val currentRating = note?.score ?: 4.5

    val text = note?.note ?: ""

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
                    navController.navigate("${Screen.DayNote.route}/${selectedDate}/${currentRating}/${text}") //преход на полный экран с описанием дня
                }
        ) {
            Text(
                text = text.ifEmpty { stringResource(R.string.write_something) },
                color = if (text.isEmpty()) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(16.dp),
                maxLines = 10
            )
        }
        Box(modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ){
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