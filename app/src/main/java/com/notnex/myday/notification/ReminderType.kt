package com.notnex.myday.notification

import java.time.LocalTime

enum class ReminderType(val requestCode: Int, val defaultTime: LocalTime, val keyPrefix: String) {
    DAY_NOTE(0, LocalTime.of(21, 0), "reminder"),
    BEDTIME(1, LocalTime.of(22, 30),  "bedtime"),
}