package com.notnex.myday.data

import androidx.room.Embedded
import androidx.room.Relation

data class DayWithSchedules(
    @Embedded val day: MyDayEntity,
    @Relation(
        parentColumn = "date",
        entityColumn = "scheduleDate"
    )
    val schedules: List<ScheduleEntity>
)