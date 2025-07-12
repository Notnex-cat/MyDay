package com.notnex.myday.data

import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate


class MyDayRepository @Inject constructor(val dao: MyDayDAO) {
    fun getScoreByDate(date: LocalDate): Flow<MyDayEntity?> = dao.getEntry(date)

    suspend fun saveOrUpdateDayScore(date: LocalDate, score: Double, note: String) {
        val existing = dao.getEntryOnce(date)

        val newTimestamp = System.currentTimeMillis()

        val updated = when {
            existing == null -> MyDayEntity(date, score, note, newTimestamp)
            existing.score != score || existing.note != note ->
                existing.copy(score = score, note = note, lastUpdated = newTimestamp)
            else -> return // ничего не изменилось — выходим
        }

        dao.insert(updated)
    }

    suspend fun getAllLocalEntries(): List<MyDayEntity> = dao.getFullEntry()


    suspend fun deleteEntry(entry: MyDayEntity) = dao.delete(entry)
}