package com.notnex.myday.neuralnetwork

import com.notnex.myday.data.LocalTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalTime

/** Элемент JSON-массива, который возвращает нейросеть. */
@Serializable
data class ScheduleItemResponse(
    @Serializable(with = LocalTimeSerializer::class)
    val time: LocalTime,
    val task: String
)
