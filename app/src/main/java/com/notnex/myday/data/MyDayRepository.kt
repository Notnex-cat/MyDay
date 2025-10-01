package com.notnex.myday.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class MyDayRepository @Inject constructor(val dao: MyDayDAO) {
    fun getEntityByDate(date: LocalDate): Flow<MyDayEntity?> = dao.getEntry(date)

    suspend fun saveOrUpdateDayEntity(
        date: LocalDate,
        score: Double,
        note: String,
        aiFeedback: String,
        lastUpdated: Long
    ) {
        val existing = dao.getEntryOnce(date)

        val updated = when {
            existing == null -> MyDayEntity(date, score, note, lastUpdated, aiFeedback)
            existing.score != score || existing.note != note || existing.aiFeedback != aiFeedback ->
                existing.copy(score = score, note = note, lastUpdated = lastUpdated, aiFeedback = aiFeedback)
            else -> return // ничего не изменилось — выходим
        }

        dao.insertOfDay(updated)
    }

    suspend fun getAllLocalEntries(): List<MyDayEntity> = dao.getFullEntry()

    suspend fun deleteEntry(entry: MyDayEntity) = dao.delete(entry) //рудимент
}