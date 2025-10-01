package com.notnex.myday.data

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import java.time.LocalDate

@Entity(tableName = "day_entries")
@Immutable
data class ScheduleEntity(
    val date: LocalDate,
    val scheduleItem: String,
    val note: String,
    val score: Int,
    val lastUpdated: Long = System.currentTimeMillis(),
    val aiFeedback: String
)