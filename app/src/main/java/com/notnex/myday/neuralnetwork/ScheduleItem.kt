package com.notnex.myday.neuralnetwork

import kotlinx.serialization.Serializable

@Serializable
data class ScheduleItem(
    val id: String,
    val date: String,
    val time: String,
    val task: String
)