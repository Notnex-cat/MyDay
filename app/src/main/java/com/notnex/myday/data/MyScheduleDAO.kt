package com.notnex.myday.data

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface MyScheduleDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: MyDayEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleItem(item: ScheduleEntity)

    @Transaction
    @Query("SELECT * FROM day_entries WHERE date = :date")
    suspend fun getDayWithSchedules(date: LocalDate): Flow<ScheduleEntity?>
}