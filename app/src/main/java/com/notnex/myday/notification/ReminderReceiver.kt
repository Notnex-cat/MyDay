package com.notnex.myday.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.notnex.myday.data.MyDayRepository
import com.notnex.myday.di.ApplicationScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver: BroadcastReceiver() {
    @Inject lateinit var notifier: ReminderNotifier
    @Inject lateinit var reminderScheduler: ReminderScheduler
    @Inject @ApplicationScope lateinit var appScope: CoroutineScope
    @Inject lateinit var myDayRepository: MyDayRepository

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        appScope.launch {
            try {
                reminderScheduler.scheduleFromSettings()
                val day = myDayRepository.observeDay(LocalDate.now()).first()
                val message = reminderMessage(day)
                if (message != null) {
                    notifier.show(message)
                }
            } catch (e: Exception) {
                Log.w("ReminderReceiver", "Reminder setting failed: ${e.message}", e)
            } finally {
                pending.finish()   // в finally — чтобы вызвался и при ошибке
            }
        }
    }
}