package com.notnex.myday.neuralnetwork

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalTime

class AiResponseParsingTest {

    private val json = Json { ignoreUnknownKeys = true }

    // Ответ в формате OpenAI chat completions с заданным текстом сообщения.
    private fun completion(content: String): String = buildJsonObject {
        putJsonArray("choices") {
            addJsonObject {
                putJsonObject("message") { put("content", content) }
            }
        }
    }.toString()

    @Test
    fun `plain array is parsed`() {
        val items = parseScheduleResponse(
            json,
            completion("""[{"time": "07:30", "task": "Подъём"}, {"time": "08:00", "task": "Завтрак"}]""")
        )
        assertEquals(
            listOf(
                ScheduleItemResponse(LocalTime.of(7, 30), "Подъём"),
                ScheduleItemResponse(LocalTime.of(8, 0), "Завтрак"),
            ),
            items
        )
    }

    @Test
    fun `markdown fence and comments around the array are ignored`() {
        val items = parseScheduleResponse(
            json,
            completion("Вот расписание:\n```json\n[{\"time\": \"09:00\", \"task\": \"Работа\"}]\n```\nУдачи!")
        )
        assertEquals(listOf(ScheduleItemResponse(LocalTime.of(9, 0), "Работа")), items)
    }

    @Test
    fun `time without leading zero and with seconds is accepted`() {
        val items = parseScheduleResponse(
            json,
            completion("""[{"time": "7:30", "task": "a"}, {"time": "07:45:00", "task": "b"}]""")
        )
        assertEquals(listOf(LocalTime.of(7, 30), LocalTime.of(7, 45)), items.map { it.time })
    }

    @Test
    fun `unknown fields are ignored`() {
        val items = parseScheduleResponse(
            json,
            completion("""[{"time": "10:00", "task": "a", "duration": 30}]""")
        )
        assertEquals(1, items.size)
    }

    @Test
    fun `text without an array is a format error`() {
        assertThrows(AiResponseFormatException::class.java) {
            parseScheduleResponse(json, completion("Не могу составить расписание"))
        }
    }

    @Test
    fun `invalid time is a format error`() {
        assertThrows(AiResponseFormatException::class.java) {
            parseScheduleResponse(json, completion("""[{"time": "утром", "task": "a"}]"""))
        }
    }

    @Test
    fun `body that is not json is a format error`() {
        assertThrows(AiResponseFormatException::class.java) {
            parseScheduleResponse(json, "<html>Bad gateway</html>")
        }
    }

    @Test
    fun `body without choices is a format error`() {
        assertThrows(AiResponseFormatException::class.java) {
            parseScheduleResponse(json, """{"error": "overloaded"}""")
        }
    }

    @Test
    fun `extractJsonArray cuts from the first to the last bracket`() {
        assertEquals("[1, [2], 3]", extractJsonArray("x [1, [2], 3] y"))
    }
}
