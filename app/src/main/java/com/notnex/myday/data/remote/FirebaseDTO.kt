package com.notnex.myday.data.remote

// DTO для Firestore. Все поля с дефолтами — Firestore требует конструктор без аргументов.
// Пакет закрыт keep-правилом в proguard-rules.pro, поэтому имена полей нельзя менять без миграции.

data class MyDayFirebaseDTO(
    val date: String = "",          // YYYY-MM-DD
    val score: Double = 0.0,
    val note: String = "",
    val lastUpdated: Long = 0L,
    val aiFeedback: String = "",
    val mood: Int = 0
)

data class MyScheduleFirebaseDTO(
    val id: String = "",
    val date: String = "",          // YYYY-MM-DD
    val time: String = "",          // HH:mm
    val scheduleItem: String = "",
    val note: String = "",
    val score: Double = 0.0,
    val lastUpdated: Long = 0L,
    val aiFeedback: String = "",
)
