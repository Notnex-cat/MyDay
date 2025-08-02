package com.notnex.myday.neuralnetwork

import kotlinx.serialization.Serializable

@Serializable
data class ScheduleResponse(
    val schedule: List<ScheduleItem>
)