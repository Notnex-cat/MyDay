package com.notnex.myday.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

// Один файл настроек на всё приложение. Свойство объявлено вне класса,
// чтобы DataStore создавался один раз, а не при каждом новом объекте.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// Время напоминания хранится как минуты от полуночи: 21:00 → 1260.
private val REMINDER_MINUTES = intPreferencesKey("reminder_minutes")
private val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    val reminderTime: Flow<LocalTime> = context.dataStore.data.map { prefs ->
        val minutes: Int? = prefs[REMINDER_MINUTES]   // null, если ещё ни разу не сохраняли
        LocalTime.of(minutes?.div(60) ?: 21, minutes?.rem(60) ?: 0)
    }

    val reminderEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[REMINDER_ENABLED] ?: false
    }

    suspend fun saveReminderTime(time: LocalTime) {
        context.dataStore.edit { prefs ->
            prefs[REMINDER_ENABLED] = true
            prefs[REMINDER_MINUTES] = time.hour * 60 + time.minute
        }
    }

    suspend fun disableReminder(){
        context.dataStore.edit { prefs ->
            prefs[REMINDER_ENABLED] = false
        }
    }
}