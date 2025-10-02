package com.notnex.myday.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

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

data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // PK каждой записи
    val scheduleDate: LocalDate,                       // внешний ключ
    val scheduleItem: String,                          // название пункта (подъём, завтрак)
    val note: String,                                  // комментарий
    val score: Double,                                    // оценка (например, насколько хорошо выполнил)
    val lastUpdated: Long = System.currentTimeMillis(),
    val aiFeedback: String
)