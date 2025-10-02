package com.notnex.myday.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class ScheduleRepository @Inject constructor(val dao: MyScheduleDAO) {
    fun getEntityByDate(date: LocalDate): Flow<ScheduleEntity?> = dao.getDayWithSchedules(date)

    suspend fun saveOrUpdateScheduleEntity(
        date: LocalDate,
        score: Double,
        note: String,
        aiFeedback: String,
        lastUpdated: Long
    ) {}

    suspend fun getAllLocalEntries(): List<MyDayEntity> = dao.getFullEntry()

    suspend fun deleteEntry(entry: MyDayEntity) = dao.delete(entry) //рудимент
}