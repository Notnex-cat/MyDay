package com.notnex.myday.notification

import com.notnex.myday.R
import com.notnex.myday.data.MyDayEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ReminderMessageTest {

    private val date = LocalDate.of(2026, 11, 17)

    @Test
    fun `no record asks to rate and write`() {
        assertEquals(R.string.rate_the_day, reminderMessage(null))
    }

    // Заглушка от расписания (ensureDays) — тоже незаполненный день.
    @Test
    fun `empty stub asks to rate and write`() {
        assertEquals(R.string.rate_the_day, reminderMessage(MyDayEntity(date, lastUpdated = 0L)))
    }

    @Test
    fun `note without score asks for the score`() {
        assertEquals(R.string.day_score, reminderMessage(MyDayEntity(date, note = "Хороший день")))
    }

    @Test
    fun `score without note asks for the note`() {
        assertEquals(R.string.tell_how_day_gone, reminderMessage(MyDayEntity(date, score = 4.0, note = "  ")))
    }

    @Test
    fun `filled day needs no reminder`() {
        assertNull(reminderMessage(MyDayEntity(date, score = 4.5, note = "Хороший день")))
    }
}
