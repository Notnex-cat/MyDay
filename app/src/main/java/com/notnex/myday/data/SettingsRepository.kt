package com.notnex.myday.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.notnex.myday.notification.ReminderType
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
private fun minutesKey(type: ReminderType) = intPreferencesKey("${type.keyPrefix}_minutes")
private fun enabledKey(type: ReminderType) = booleanPreferencesKey("${type.keyPrefix}_enabled")

/**
 * Настройки напоминаний. Отсюда их читают экран настроек, планировщик и оба приёмника:
 * время напоминания хранится только здесь.
 */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun reminderTime(type: ReminderType): Flow<LocalTime> = context.dataStore.data.map { prefs ->
        val minutes: Int? = prefs[minutesKey(type)]   // null, если ещё ни разу не сохраняли
        minutes?.let { LocalTime.of(it / 60, it % 60) } ?: type.defaultTime
    }

    fun reminderEnabled(type: ReminderType): Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[enabledKey(type)] ?: false
    }

    // Выбор времени и есть включение: время и флаг записываются вместе, одним edit.
    suspend fun saveReminderTime(type: ReminderType, time: LocalTime) {
        context.dataStore.edit { prefs ->
            prefs[enabledKey(type)] = true
            prefs[minutesKey(type)] = time.hour * 60 + time.minute
        }
    }

    // Время не стираем: включив напоминание снова, человек получит прежнее.
    suspend fun disableReminder(type: ReminderType){
        context.dataStore.edit { prefs ->
            prefs[enabledKey(type)] = false
        }
    }
}