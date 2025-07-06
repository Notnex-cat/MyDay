package com.notnex.myday.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "day_entries")
data class MyDayEntity(
    @PrimaryKey val date: LocalDate = LocalDate.now(),
    val score: Double = 0.0,
    val note: String = ""
)
data class MyDayFirebaseDTO(
    val date: String = "",
    val score: Double = 0.0,
    val note: String = ""
)