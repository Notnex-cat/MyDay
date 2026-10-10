package com.notnex.myday.data

import androidx.room.Dao
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

    // @Upsert обновляет строку на месте. INSERT OR REPLACE удалил бы строку дня
    // и через ForeignKey.CASCADE снёс бы всё его расписание.
    @Upsert
    suspend fun upsert(entry: MyDayEntity)



    @Query("SELECT * FROM day_entries WHERE note != '' ORDER BY date DESC")
    fun observeNotes(): Flow<List<MyDayEntity>>
}
