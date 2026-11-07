package com.notnex.myday.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notnex.myday.data.SettingsRepository
import com.notnex.myday.notification.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val savedStateHandle: SavedStateHandle,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    val reminderTime = repository.reminderTime
    val reminderEnabled = repository.reminderEnabled


    fun enableReminder(time: LocalTime){
        viewModelScope.launch{
            repository.saveReminderTime(time)
            reminderScheduler.scheduleNext(time)
        }
    }

    fun disableReminder(){
        viewModelScope.launch{
            repository.disableReminder()
            reminderScheduler.cancelReminder()
        }
    }
}