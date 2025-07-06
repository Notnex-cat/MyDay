package com.notnex.myday.data

import androidx.room.TypeConverter
import java.time.LocalDate

class LocalDateConverter {

    @TypeConverter
    fun fromLocalDate(date: LocalDate): String {
        return date.toString() // формат YYYY-MM-DD
    }

    @TypeConverter
    fun toLocalDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString)
    }
}