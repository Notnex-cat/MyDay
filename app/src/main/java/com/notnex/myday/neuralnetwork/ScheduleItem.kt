package com.notnex.myday.neuralnetwork

import java.time.LocalDate
import java.time.LocalTime

data class ScheduleItem(
    val id: String,
    val date: LocalDate,
    val time: LocalTime,
    val task: String
)
