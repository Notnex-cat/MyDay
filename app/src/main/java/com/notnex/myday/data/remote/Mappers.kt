package com.notnex.myday.data.remote

import com.notnex.myday.data.MyDayEntity
import com.notnex.myday.data.ScheduleEntity
import com.notnex.myday.data.TIME_FORMAT
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException

fun MyDayEntity.toDto() = MyDayFirebaseDTO(
    date = date.toString(),
    score = score,
    note = note,
    lastUpdated = lastUpdated,
    aiFeedback = aiFeedback,
    mood = mood
)

/** null, если документ в облаке повреждён (например, невалидная дата). */
fun MyDayFirebaseDTO.toEntityOrNull(): MyDayEntity? = try {
    MyDayEntity(
        date = LocalDate.parse(date),
        score = score,
        note = note,
        lastUpdated = lastUpdated,
        aiFeedback = aiFeedback,
        mood = mood
    )
} catch (_: DateTimeParseException) {
    null
}

fun ScheduleEntity.toDto() = MyScheduleFirebaseDTO(
    id = id,
    date = scheduleDate.toString(),
    time = time.format(TIME_FORMAT),
    scheduleItem = scheduleItem,
    note = note,
    score = score,
    lastUpdated = lastUpdated,
    aiFeedback = aiFeedback
)

fun MyScheduleFirebaseDTO.toEntityOrNull(): ScheduleEntity? = try {
    ScheduleEntity(
        id = id,
        scheduleDate = LocalDate.parse(date),
        time = LocalTime.parse(time),
        scheduleItem = scheduleItem,
        note = note,
        score = score,
        lastUpdated = lastUpdated,
        aiFeedback = aiFeedback
    )
} catch (_: DateTimeParseException) {
    null
}
