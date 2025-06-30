package com.notnex.myday.data

import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate


class MyDayRepository @Inject constructor(private val dao: MyDayDAO) {
    fun getScoreByDate(date: LocalDate): Flow<MyDayEntity?> = dao.getEntry(date)

    suspend fun saveOrUpdateDayScore(date: LocalDate, score: Double, note: String) {
        val existing = dao.getEntryOnce(date)

        val updated = existing?.copy(score = score, note = note) ?: MyDayEntity(date = date, score = score, note = note)

        dao.insert(updated)
    }

    suspend fun deleteEntry(entry: MyDayEntity) = dao.delete(entry)
}