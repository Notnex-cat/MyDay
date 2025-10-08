package com.notnex.myday.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import javax.inject.Inject


class ScheduleRepository @Inject constructor(val dao: MyScheduleDAO) {
    fun getEntityByDate(date: LocalDate): Flow<List<ScheduleEntity>> = dao.getDayWithSchedules(date)

    suspend fun saveOrUpdateScheduleEntity(
        date: LocalDate,
        item: String,
        score: Double,
        note: String,
        aiFeedback: String,
        lastUpdated: Long
    ) {
        val existing = dao.getDayWithSchedules(date)
            .firstOrNull()

        val entity = ScheduleEntity(
            scheduleDate = date,
            scheduleItem = item,
            score = score,
            note = note,
            lastUpdated = lastUpdated,
            aiFeedback = aiFeedback
        )

        if (existing == null) {
            dao.insertScheduleItem(entity)
        } else {
            dao.insertScheduleItem(entity)
        }
    }


    // suspend fun getAllLocalEntries(): List<MyDayEntity> = dao.getFullEntry()

   // suspend fun deleteEntry(entry: MyDayEntity) = dao.delete(entry) //рудимент
}