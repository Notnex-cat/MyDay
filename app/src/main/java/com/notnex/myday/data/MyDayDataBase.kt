package com.notnex.myday.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [MyDayEntity::class], version = 1)
@TypeConverters(LocalDateConverter::class)
abstract class MyDayDataBase: RoomDatabase() {
    abstract fun myDayDao(): MyDayDAO
}