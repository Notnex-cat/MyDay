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

/**
 * Ставит и снимает будильники напоминаний. Будильник одноразовый: сработав,
 * ReminderReceiver сам ставит следующий через [scheduleFromSettings].
 */
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
) {

    // Один и тот же PendingIntent для постановки и для отмены: система находит будильник
    // по получателю и requestCode. Тип кладётся до getBroadcast — тот копирует Intent в момент
    // вызова. FLAG_UPDATE_CURRENT обновляет extras и у будильника от старой версии, где типа не было.
    // Ключ "reminder_type" читает ReminderReceiver — менять в обоих местах.
    private fun reminderPendingIntent(type: ReminderType): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
        intent.putExtra("reminder_type", type.name)
        val pendingIntent = PendingIntent.getBroadcast(context, type.requestCode, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        return pendingIntent
    }

    // Неточный будильник с окном 10 минут: система может объединить его с чужими и разбудить
    // телефон один раз. Точное время ценой отдельного разрешения здесь не нужно.
    fun scheduleNext(type: ReminderType, reminderTime: LocalTime) {
        val localTimeAndDate = LocalDateTime.now()
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.setWindow(
            AlarmManager.RTC_WAKEUP,
            getTime(reminderTime, localTimeAndDate) + System.currentTimeMillis(),
            600000,
            reminderPendingIntent(type)
        )
    }

    fun cancelReminder(type: ReminderType){
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.cancel(reminderPendingIntent(type))
    }

    // Через сколько миллисекунд наступит time по местным часам. Если сегодня это время уже было
    // или наступает прямо сейчас — завтра: так сработавший в 21:00 будильник ставит следующий
    // на завтра, а не на ту же минуту. Секунды отброшены, сдвиг не больше минуты.
    fun getTime(time: LocalTime, localDateTime: LocalDateTime): Long{
        val currentTime = localDateTime.hour * 3600000L + localDateTime.minute * 60000L
        val reminderTime = time.hour * 3600000L + time.minute * 60000L
        return if (reminderTime > currentTime){
            reminderTime - currentTime
        } else{
            reminderTime - currentTime + 86400000L
        }
    }

    // Время и флаг берутся из DataStore — других мест, где они хранятся, нет. Выключенное
    // напоминание не ставится, поэтому его не вернут ни приёмник, ни перезагрузка.
    suspend fun scheduleFromSettings(type: ReminderType){
        if (!settingsRepository.reminderEnabled(type).first()) return
        val value = settingsRepository.reminderTime(type).first()
        scheduleNext(type, value)
    }

    // После перезагрузки телефона и обновления приложения система стирает все будильники.
    suspend fun scheduleAllFromSettings(){
        for (type in ReminderType.entries){
            scheduleFromSettings(type)
        }
    }
}