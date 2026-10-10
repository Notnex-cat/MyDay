package com.notnex.myday.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** Формат "HH:mm"; при чтении терпим "7:30" и "07:30:00", которые иногда отдаёт нейросеть. */
object LocalTimeSerializer : KSerializer<LocalTime> {
    private val lenientFormatter = DateTimeFormatter.ofPattern("H:mm[:ss]")

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalTime) {
        encoder.encodeString(value.format(TIME_FORMAT))
    }

    override fun deserialize(decoder: Decoder): LocalTime {
        return LocalTime.parse(decoder.decodeString().trim(), lenientFormatter)
    }
}

val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
