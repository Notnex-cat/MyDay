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

            appScope.launch {
                try {
                reminderScheduler.scheduleAllFromSettings()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("Start", "Strt of reminder and sync stopped: ${e.message}", e)
                }
            }
        syncManager.start()
    }
}
