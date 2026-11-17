package com.notnex.myday

import android.app.Application
import android.util.Log
import com.notnex.myday.di.ApplicationScope
import com.notnex.myday.notification.ReminderScheduler
import com.notnex.myday.sync.SyncManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MyDayApp : Application() {

    @Inject
    lateinit var syncManager: SyncManager

    @Inject
    lateinit var reminderScheduler: ReminderScheduler
    @Inject @ApplicationScope
    lateinit var appScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        // Система стирает будильники при обновлении и принудительной остановке приложения —
        // при каждом запуске процесса ставим их заново.
        appScope.launch {
            try {
                reminderScheduler.scheduleAllFromSettings()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("MyDayApp", "Reminders were not scheduled: ${e.message}", e)
            }
        }
        syncManager.start()
    }
}
