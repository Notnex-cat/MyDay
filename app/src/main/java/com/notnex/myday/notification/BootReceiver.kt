package com.notnex.myday.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.notnex.myday.di.ApplicationScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Возвращает будильники, которые система стёрла при перезагрузке телефона или обновлении
 * приложения. BOOT_COMPLETED приходит только после первой разблокировки: до неё данные
 * приложения, включая DataStore, зашифрованы.
 */
@AndroidEntryPoint
class BootReceiver: BroadcastReceiver() {
    @Inject lateinit var reminderScheduler: ReminderScheduler
    @Inject @ApplicationScope
    lateinit var appScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        // Приёмник exported: напрямую его может вызвать любое приложение с любым действием.
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) {
            return
        }
        val pending = goAsync()
        appScope.launch {
            try {
                reminderScheduler.scheduleAllFromSettings()
            } catch (e: Exception) {
                Log.w("BootReceiver", "Reminder setting failed: ${e.message}", e)
            } finally {
                pending.finish()   // в finally — чтобы вызвался и при ошибке
            }
        }
    }
}