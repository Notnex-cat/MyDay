package com.notnex.myday.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notnex.myday.data.AiSettingsRepository
import com.notnex.myday.data.SettingsRepository
import com.notnex.myday.notification.ReminderScheduler
import com.notnex.myday.notification.ReminderType
import com.notnex.myday.ui.theme.ThemeStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

/**
 * Напоминания на экране настроек. Flow для каждого типа — свойства, а не функция reminderTime(type):
 * функция, вызванная в теле composable, создавала бы новый Flow при каждой перерисовке.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val aiSettingsRepository: AiSettingsRepository,
    private val reminderScheduler: ReminderScheduler,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val dayNoteTime = settingsRepository.reminderTime(ReminderType.DAY_NOTE)
    val dayNoteEnabled = settingsRepository.reminderEnabled(ReminderType.DAY_NOTE)

    val bedtimeTime = settingsRepository.reminderTime(ReminderType.BEDTIME)
    val bedtimeEnabled = settingsRepository.reminderEnabled(ReminderType.BEDTIME)

    val aiEnabled = aiSettingsRepository.aiEnabled
    val selectedAi = aiSettingsRepository.provider

    val selectedTheme = settingsRepository.appTheme

    fun enableReminder(time: LocalTime, type: ReminderType) {
        viewModelScope.launch {
            settingsRepository.saveReminderTime(type, time)
            reminderScheduler.scheduleNext(type, time)
        }
    }

    // Флаг пишется раньше отмены: если будильник сработает в этот момент, приёмник
    // уже увидит «выключено» и не поставит новый.
    fun disableReminder(type: ReminderType) {
        viewModelScope.launch {
            settingsRepository.disableReminder(type)
            reminderScheduler.cancelReminder(type)
        }
    }

    fun enableAI(enabled: Boolean) {
        viewModelScope.launch {
            aiSettingsRepository.setAiEnabledOrDisabled(enabled)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val hasApiKey: Flow<Boolean> = selectedAi
        .flatMapLatest { provider -> aiSettingsRepository.apiKey(provider) }
        .map { key -> key != null }

    fun setTheme(seletedTheme: ThemeStyle){
        viewModelScope.launch {
            settingsRepository.changeTheme(seletedTheme)
        }
    }
}