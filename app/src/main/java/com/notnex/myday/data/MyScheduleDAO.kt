package com.notnex.myday.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MyScheduleDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: MyDayEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleItem(item: ScheduleEntity)

    @Transaction
    @Query("SELECT * FROM schedule_entries WHERE scheduleDate = :date")
    fun getDayWithSchedules(date: LocalDate): Flow<List<ScheduleEntity>>
}
