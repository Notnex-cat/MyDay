package com.notnex.myday.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.notnex.myday.data.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
) {

    private fun reminderPendingIntent(): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return pendingIntent
    }

    fun scheduleNext(reminderTime: LocalTime) {
        val localTimeAndDate = LocalDateTime.now()
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.setWindow(
            AlarmManager.RTC_WAKEUP,
            getTime(reminderTime, localTimeAndDate) + System.currentTimeMillis(),
            600000,
            reminderPendingIntent()
        )
    }

    fun cancelReminder(){
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.cancel(reminderPendingIntent())
    }

    fun getTime(time: LocalTime, localDateTime: LocalDateTime): Long{
        val currentTime = localDateTime.hour * 3600000L + localDateTime.minute * 60000L
        val reminderTime = time.hour * 3600000L + time.minute * 60000L
        return if (reminderTime > currentTime){
            reminderTime - currentTime
        } else{
            reminderTime - currentTime + 86400000L
        }
    }

    suspend fun scheduleFromSettings(){
        if (!settingsRepository.reminderEnabled.first()) return
        val value = settingsRepository.reminderTime.first()
        scheduleNext(value)
    }
}