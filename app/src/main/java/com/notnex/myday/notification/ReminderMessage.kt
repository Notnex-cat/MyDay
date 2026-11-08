package com.notnex.myday.notification

import androidx.annotation.StringRes
import com.notnex.myday.R
import com.notnex.myday.data.MyDayEntity
import com.notnex.myday.data.isRated

/** Какой текст напоминания показать для этого дня; null — день заполнен, напоминать не о чем. */
@StringRes
fun reminderMessage(day: MyDayEntity?): Int? {
    if (day == null) return R.string.rate_the_day

    return when {
        !day.score.isRated && day.note.isBlank() -> R.string.rate_the_day
        !day.score.isRated -> R.string.day_score
        day.note.isBlank() -> R.string.tell_how_day_gone
        else -> {
            null
        }
    }
}