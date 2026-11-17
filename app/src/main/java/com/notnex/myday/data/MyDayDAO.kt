package com.notnex.myday.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MyDayDAO {
    @Query("SELECT * FROM day_entries")
    suspend fun getAll(): List<MyDayEntity>

    @Query("SELECT * FROM day_entries WHERE date = :date LIMIT 1")
    fun observe(date: LocalDate): Flow<MyDayEntity?>

    @Query("SELECT * FROM day_entries WHERE date = :date LIMIT 1")
    suspend fun getOnce(date: LocalDate): MyDayEntity?

    // Один запрос на всю пачку из облака вместо запроса на каждый день.
    @Query("SELECT * FROM day_entries WHERE date IN (:dates)")
    suspend fun getByDates(dates: List<LocalDate>): List<MyDayEntity>

    // @Upsert обновляет строку на месте. INSERT OR REPLACE удалил бы строку дня
    // и через ForeignKey.CASCADE снёс бы всё его расписание.
    @Upsert
    suspend fun upsert(entry: MyDayEntity)

    @Upsert
    suspend fun upsertAll(entries: List<MyDayEntity>)

    // IGNORE не трогает уже существующую строку: настоящий день заглушкой не затирается.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(entries: List<MyDayEntity>)
}
