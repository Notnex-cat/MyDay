package com.notnex.myday.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "day_entries")
data class MyDayEntity(
    @PrimaryKey val date: LocalDate,
    val score: Double,
    val note: String
)