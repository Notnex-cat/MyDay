package com.notnex.myday.neuralnetwork

import kotlinx.serialization.Serializable

@Serializable
data class ScheduleItem(
    val time: String,
    val task: String
)