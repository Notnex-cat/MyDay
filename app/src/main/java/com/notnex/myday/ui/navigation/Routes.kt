package com.notnex.myday.ui.navigation

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.format.DateTimeParseException

@Serializable
data object MainRoute


/** Адрес заметки дня: myday://day/2026-11-08. Им пользуются NavHost и уведомление — константа одна, чтобы адреса не разошлись. */
const val DAY_DEEP_LINK = "myday://day"

@Serializable
data class DayNoteRoute(val date: String)

@Serializable
data class ScheduleRoute(val date: String)

@Serializable
data class EditScheduleRoute(val id: String)

@Serializable
data object SettingsRoute

@Serializable
data object PracticeRoute


@Serializable
data object AuthRoute

fun LocalDate.toDayNoteRoute() = DayNoteRoute(toString())

fun LocalDate.toScheduleRoute() = ScheduleRoute(toString())

/** Дата из аргумента маршрута; при мусоре открываем сегодняшний день, а не падаем. */
fun String.toLocalDateOrToday(): LocalDate = try {
    LocalDate.parse(this)
} catch (_: DateTimeParseException) {
    LocalDate.now()
}
