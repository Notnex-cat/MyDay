package com.notnex.myday.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notnex.myday.data.SettingsRepository
import com.notnex.myday.notification.ReminderScheduler
import com.notnex.myday.notification.ReminderType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

/**
 * Напоминания на экране настроек. Flow для каждого типа — свойства, а не функция reminderTime(type):
 * функция, вызванная в теле composable, создавала бы новый Flow при каждой перерисовке.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val savedStateHandle: SavedStateHandle,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    val dayNoteTime = repository.reminderTime(ReminderType.DAY_NOTE)
    val dayNoteEnabled = repository.reminderEnabled(ReminderType.DAY_NOTE)

    val bedtimeTime = repository.reminderTime(ReminderType.BEDTIME)
    val bedtimeEnabled = repository.reminderEnabled(ReminderType.BEDTIME)


    fun enableReminder(time: LocalTime, type: ReminderType){
        viewModelScope.launch{
            repository.saveReminderTime( type, time)
            reminderScheduler.scheduleNext( type, time)
        }
    }

    // Флаг пишется раньше отмены: если будильник сработает в этот момент, приёмник
    // уже увидит «выключено» и не поставит новый.
    fun disableReminder(type: ReminderType){
        viewModelScope.launch{
            repository.disableReminder( type)
            reminderScheduler.cancelReminder( type)
        }
    }
}