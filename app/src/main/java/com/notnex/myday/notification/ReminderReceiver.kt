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
        val pending = goAsync()
        val typeName = intent.getStringExtra("reminder_type")
        val type = ReminderType.entries.find { it.name == typeName} ?: ReminderType.DAY_NOTE
        appScope.launch {
            try {
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