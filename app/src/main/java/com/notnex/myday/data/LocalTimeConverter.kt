package com.notnex.myday.data

import androidx.room.TypeConverter
import java.time.LocalTime

class LocalTimeConverter {
    @TypeConverter
    fun fromLocalTime(time: LocalTime): String {
        return time.toString()
    }

    @TypeConverter
    fun toLocalTime(timeString: String): LocalTime {
        return LocalTime.parse(timeString)
    }
}