package com.notnex.myday.data

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(
    tableName = "schedule_entries",
    foreignKeys = [
        ForeignKey(
            entity = MyDayEntity::class,
            parentColumns = ["date"],
            childColumns = ["scheduleDate"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["scheduleDate"])]
)
@Immutable
data class ScheduleEntity(
    @PrimaryKey val id: String,
    val scheduleDate: LocalDate,                       // внешний ключ на день
    val time: LocalTime,
    val scheduleItem: String,                          // название пункта (подъём, завтрак)
    val note: String = "",                             // комментарий
    val score: Double = NOT_RATED,                     // оценка выполнения
    val lastUpdated: Long = System.currentTimeMillis(),
    val aiFeedback: String = ""
)
