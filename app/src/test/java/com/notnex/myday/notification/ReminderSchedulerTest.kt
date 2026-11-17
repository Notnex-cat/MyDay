package com.notnex.myday.notification

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime

class ReminderSchedulerTest {

    private fun at(hour: Int, minute: Int, second: Int = 0) =
        LocalDateTime.of(2026, 11, 17, hour, minute, second)

    @Test
    fun `time later today is counted from now`() {
        assertEquals(2 * HOUR, millisUntil(LocalTime.of(21, 0), at(19, 0)))
    }

    @Test
    fun `time that already passed moves to tomorrow`() {
        assertEquals(23 * HOUR, millisUntil(LocalTime.of(21, 0), at(22, 0)))
    }

    // Будильник, сработавший в 21:00, ставит следующий на завтра, а не на ту же минуту.
    @Test
    fun `the same minute moves to tomorrow`() {
        assertEquals(24 * HOUR, millisUntil(LocalTime.of(21, 0), at(21, 0, 40)))
    }

    @Test
    fun `seconds of the current time are ignored`() {
        assertEquals(MINUTE, millisUntil(LocalTime.of(21, 1), at(21, 0, 59)))
    }

    @Test
    fun `time just after midnight is reached from the evening`() {
        assertEquals(35 * MINUTE, millisUntil(LocalTime.of(0, 5), at(23, 30)))
    }

    private companion object {
        const val MINUTE = 60_000L
        const val HOUR = 60 * MINUTE
    }
}
