package com.notnex.myday.neuralnetwork

import kotlinx.serialization.Serializable

@Serializable
data class ScheduleItemResponse(
    val time: String,
    val task: String
)
