package com.notnex.myday.notification

import java.time.LocalTime

/**
 * Встроенные напоминания. Всё, чем они отличаются, собрано здесь, а планировщик,
 * хранилище и приёмник работают с любым типом одинаково: новое напоминание — новая строка.
 *
 * @property requestCode номер будильника и уведомления. У каждого типа свой: с одинаковым
 *   номером второй будильник заменил бы первый, а уведомление затёрло бы соседнее.
 * @property defaultTime время, пока человек не выбрал своё.
 * @property keyPrefix начало имён ключей в DataStore. Имена уже сохранённых ключей менять нельзя:
 *   настройки под старым именем станут невидимы. Поэтому у DAY_NOTE остался исходный «reminder».
 */
enum class ReminderType(val requestCode: Int, val defaultTime: LocalTime, val keyPrefix: String) {
    DAY_NOTE(0, LocalTime.of(21, 0), "reminder"),
    BEDTIME(1, LocalTime.of(22, 30),  "bedtime"),
}