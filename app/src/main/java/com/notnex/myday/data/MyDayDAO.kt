package com.notnex.myday.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MyDayDAO {
    @Query("SELECT * FROM day_entries")
    suspend fun getFullEntry(): List<MyDayEntity>

    @Query("SELECT * FROM day_entries WHERE date = :date LIMIT 1")
    fun getEntry(date: LocalDate): Flow<MyDayEntity?>

    @Query("SELECT * FROM day_entries WHERE date = :date LIMIT 1")
    suspend fun getEntryOnce(date: LocalDate): MyDayEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfDay(entry: MyDayEntity)

    @Update
    suspend fun update(entry: MyDayEntity)

    @Delete
    suspend fun delete(entry: MyDayEntity)
}