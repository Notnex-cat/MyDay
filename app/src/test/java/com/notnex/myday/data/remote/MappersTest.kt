package com.notnex.myday.data.remote

import com.notnex.myday.data.MyDayEntity
import com.notnex.myday.data.ScheduleEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class MappersTest {

    @Test
    fun `day survives the trip to the cloud and back`() {
        val day = MyDayEntity(
            date = LocalDate.of(2026, 11, 17),
            score = 4.5,
            note = "Заметка",
            lastUpdated = 1_700_000_000_000,
            aiFeedback = "Отзыв"
        )
        assertEquals(day, day.toDto().toEntityOrNull())
    }

    @Test
    fun `schedule item survives the trip to the cloud and back`() {
        val item = ScheduleEntity(
            id = "id-1",
            scheduleDate = LocalDate.of(2026, 11, 17),
            time = LocalTime.of(7, 5),
            scheduleItem = "Подъём",
            note = "Не проспать",
            score = 3.0,
            lastUpdated = 1_700_000_000_000,
            aiFeedback = ""
        )
        assertEquals(item, item.toDto().toEntityOrNull())
    }

    // Формат полей совпадает с уже сохранёнными документами Firestore.
    @Test
    fun `dto uses the stored formats`() {
        val dto = ScheduleEntity(
            id = "id-1",
            scheduleDate = LocalDate.of(2026, 1, 5),
            time = LocalTime.of(7, 5),
            scheduleItem = "Подъём"
        ).toDto()
        assertEquals("2026-01-05", dto.date)
        assertEquals("07:05", dto.time)
    }

    @Test
    fun `broken date or time in the cloud gives null instead of a crash`() {
        assertNull(MyDayFirebaseDTO(date = "вчера").toEntityOrNull())
        assertNull(MyScheduleFirebaseDTO(id = "x", date = "2026-13-40", time = "07:00").toEntityOrNull())
        assertNull(MyScheduleFirebaseDTO(id = "x", date = "2026-11-17", time = "").toEntityOrNull())
    }
}
