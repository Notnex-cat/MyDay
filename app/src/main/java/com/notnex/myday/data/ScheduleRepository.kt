package com.notnex.myday.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import javax.inject.Inject


class ScheduleRepository @Inject constructor(val dao: MyScheduleDAO) {
    fun getScheduleByDate(date: LocalDate): Flow<List<ScheduleEntity>> = dao.getDayWithSchedules(date)

    suspend fun saveOrUpdateScheduleEntity(
        id: String,
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
            id = id,
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


    //suspend fun getAllLocalEntries(): List<MyDayEntity> = dao.getFullEntry()

   // suspend fun deleteEntry(entry: MyDayEntity) = dao.delete(entry) //рудимент
}