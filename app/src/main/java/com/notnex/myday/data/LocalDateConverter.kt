package com.notnex.myday.data

import androidx.room.TypeConverter
import java.time.LocalDate

class LocalDateConverter {

    @TypeConverter
    fun fromString(value: String): LocalDate = LocalDate.parse(value)

    @TypeConverter
    fun toString(date: LocalDate): String = date.toString()
}