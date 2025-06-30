package com.notnex.myday.ui

//noinspection SuspiciousImport
import android.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notnex.myday.viewmodel.MyDayViewModel
import java.time.LocalDate

@Composable
fun PageContent(date: LocalDate, modifier: Modifier = Modifier, viewModel: MyDayViewModel = hiltViewModel()) {

    val note by viewModel.getScore(date).collectAsState(initial = null)

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

            RatingBar(
                modifier = Modifier
                    .size(50.dp),
                rating = currentRating,
                onRatingChanged = {
                    viewModel.saveDayEntry(date, it, text)
                },
                starsColor = when {
                    currentRating >= 4.0 -> Color.Yellow
                    currentRating >= 2.5 -> colorResource(R.color.holo_orange_dark)
                    else -> Color.Red
                }
            )

            TextField(
                value = text,
                onValueChange = {
                    viewModel.saveDayEntry(date, currentRating, it)
                },
                label = {
                    Text("Мысли о дне")
                }
            )
        }
    }
}