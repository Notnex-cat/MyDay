package com.notnex.myday.data

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/** Значение score для дня/пункта, который пользователь ещё не оценил. */
const val NOT_RATED = 0.0

val Double.isRated: Boolean get() = this > NOT_RATED

@Entity(tableName = "day_entries")
@Immutable
data class MyDayEntity(
    @PrimaryKey val date: LocalDate,
    val score: Double = NOT_RATED,
    val note: String = "",
    val lastUpdated: Long = System.currentTimeMillis(),
    val aiFeedback: String = "",
    val mood: Int = 0
)
