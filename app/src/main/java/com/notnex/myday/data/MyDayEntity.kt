package com.notnex.myday.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "day_entries")
data class MyDayEntity(
    @PrimaryKey val date: LocalDate,
    val score: Double,
    val note: String,
    val lastUpdated: Long = System.currentTimeMillis(),
    val aiFeedback: String
)

data class MyDayFirebaseDTO(
    val date: String = "",          // формат YYYY-MM-DD
    val score: Double = 0.0,
    val note: String = "",
    val lastUpdated: Long = 0L,
    val aiFeedback: String = ""
)