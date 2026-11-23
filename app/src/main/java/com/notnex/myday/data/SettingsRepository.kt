package com.notnex.myday.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.notnex.myday.notification.ReminderType
import com.notnex.myday.ui.theme.ThemeStyle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

// Время напоминания хранится как минуты от полуночи: 21:00 → 1260.
private fun minutesKey(type: ReminderType) = intPreferencesKey("${type.keyPrefix}_minutes")
private fun enabledKey(type: ReminderType) = booleanPreferencesKey("${type.keyPrefix}_enabled")

private val selectedTheme = stringPreferencesKey("appTheme")

/**
 * Настройки напоминаний. Отсюда их читают экран настроек, планировщик и оба приёмника:
 * время напоминания хранится только здесь, тут хранится и выбранная тема.
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

    suspend fun changeTheme(theme: ThemeStyle) {
        context.dataStore.edit { prefs ->
            prefs[selectedTheme] = theme.name
        }
    }

    val appTheme: Flow<ThemeStyle> = context.dataStore.data.map { prefs ->
        val themeApp: String? = prefs[selectedTheme]
        ThemeStyle.fromName(themeApp)
    }
}