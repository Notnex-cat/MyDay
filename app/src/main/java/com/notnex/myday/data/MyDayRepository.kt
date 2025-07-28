package com.notnex.myday.data

import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate


class MyDayRepository @Inject constructor(val dao: MyDayDAO) {
    fun getEntityByDate(date: LocalDate): Flow<MyDayEntity?> = dao.getEntry(date)

    suspend fun saveOrUpdateDayEntity(date: LocalDate, score: Double, note: String, aiFeedback: String) {
        val existing = dao.getEntryOnce(date)

        val newTimestamp = System.currentTimeMillis()

        val updated = when {
            existing == null -> MyDayEntity(date, score, note, newTimestamp, aiFeedback)
            existing.score != score || existing.note != note || existing.aiFeedback != aiFeedback ->
                existing.copy(score = score, note = note, lastUpdated = newTimestamp, aiFeedback = aiFeedback)
            else -> return // ничего не изменилось — выходим
        }

        dao.insert(updated)
    }

    suspend fun getAllLocalEntries(): List<MyDayEntity> = dao.getFullEntry()


    suspend fun deleteEntry(entry: MyDayEntity) = dao.delete(entry)
}