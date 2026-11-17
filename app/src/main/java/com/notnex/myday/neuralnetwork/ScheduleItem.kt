package com.notnex.myday.neuralnetwork

import java.time.LocalDate
import java.time.LocalTime

// Serializable — чтобы черновик расписания переживал поворот экрана (rememberSaveable).
data class ScheduleItem(
    val id: String,
    val date: LocalDate,
    val time: LocalTime,
    val task: String
) : java.io.Serializable
