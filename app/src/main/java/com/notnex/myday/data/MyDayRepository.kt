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

        if (existing == null) {
            // Создаем новую запись
            val newEntity = MyDayEntity(date, score, note, lastUpdated, aiFeedback)
            dao.insertOfDay(newEntity)
        } else {
            // Обновляем существующую запись, чтобы не срабатывал CASCADE
            val updated = existing.copy(score = score, note = note, lastUpdated = lastUpdated, aiFeedback = aiFeedback)
            // Проверяем, действительно ли что-то изменилось
            if (updated != existing) {
                dao.update(updated)
            }
            // Если ничего не изменилось, просто выходим
        }
    }

    suspend fun getAllLocalEntries(): List<MyDayEntity> = dao.getFullEntry()

    suspend fun deleteEntry(entry: MyDayEntity) = dao.delete(entry) //рудимент
}