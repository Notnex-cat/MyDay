package com.notnex.myday.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [MyDayEntity::class, ScheduleEntity::class], version = 4)
@TypeConverters(LocalDateConverter::class,
    LocalTimeConverter::class)
abstract class MyDayDataBase: RoomDatabase() {

    abstract fun myDayDao(): MyDayDAO

    abstract fun myScheduleDao(): MyScheduleDAO
}