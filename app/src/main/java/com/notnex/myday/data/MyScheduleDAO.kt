package com.notnex.myday.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MyScheduleDAO {

    @Upsert
    suspend fun upsert(item: ScheduleEntity)

    @Query("SELECT * FROM schedule_entries WHERE scheduleDate = :date ORDER BY time, id")
    fun observeByDate(date: LocalDate): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedule_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ScheduleEntity?

    @Query("SELECT * FROM schedule_entries")
    suspend fun getAll(): List<ScheduleEntity>

    @Query("DELETE FROM schedule_entries WHERE id = :id")
    suspend fun deleteById(id: String)
}
