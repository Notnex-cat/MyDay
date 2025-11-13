package com.notnex.myday.data

import kotlinx.coroutines.flow.Flow
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

        val entity = ScheduleEntity(
            id = id,
            scheduleDate = date,
            scheduleItem = item,
            score = score,
            note = note,
            lastUpdated = lastUpdated,
            aiFeedback = aiFeedback
        )

        dao.insertScheduleItem(entity)

    }


    //suspend fun getAllLocalEntries(): List<MyDayEntity> = dao.getFullEntry()

   suspend fun deleteItem(id: String) = dao.deleteById(id, date)
}