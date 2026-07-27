package com.notnex.myday.neuralnetwork

import kotlinx.serialization.Serializable
import java.time.LocalTime

data class ScheduleItem(
    val id: String,
    val date: String,
    val time: LocalTime,
    val task: String
)