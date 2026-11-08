package com.notnex.myday.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.notnex.myday.R
import com.notnex.myday.data.MyDayRepository
import com.notnex.myday.di.ApplicationScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Срабатывает по будильнику, даже когда приложение закрыто. Создаёт его система конструктором
 * без параметров, поэтому зависимости Hilt кладёт в поля, а не в конструктор.
 */
@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {
    @Inject
    lateinit var notifier: ReminderNotifier
    @Inject
    lateinit var reminderScheduler: ReminderScheduler
    @Inject
    @ApplicationScope
    lateinit var appScope: CoroutineScope
    @Inject
    lateinit var myDayRepository: MyDayRepository


    override fun onReceive(context: Context, intent: Intent) {
        // onReceive должен вернуться быстро, а DataStore и Room читаются асинхронно.
        // goAsync просит систему не убивать процесс, пока не вызван finish().
        val pending = goAsync()
        // У будильника от версии до ReminderType типа нет — это было вечернее напоминание.
        val typeName = intent.getStringExtra("reminder_type")
        val type = ReminderType.entries.find { it.name == typeName} ?: ReminderType.DAY_NOTE
        appScope.launch {
            try {
                // Сначала завтрашний будильник: если ниже что-то упадёт, цепочка не оборвётся.
                reminderScheduler.scheduleFromSettings(type)

                val message = when (type){
                    ReminderType.DAY_NOTE -> reminderMessage(myDayRepository.observeDay(LocalDate.now()).first())
                    ReminderType.BEDTIME -> R.string.time_to_sleep
                }
                if (message != null) {
                    notifier.show(message, type)
                }
            } catch (e: Exception) {
                Log.w("ReminderReceiver", "Reminder setting failed: ${e.message}", e)
            } finally {
                pending.finish()   // в finally — чтобы вызвался и при ошибке
            }
        }
    }
}