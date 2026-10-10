package com.notnex.myday.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notnex.myday.data.MyDayEntity
import com.notnex.myday.data.MyDayRepository
import com.notnex.myday.data.ScheduleEntity
import com.notnex.myday.neuralnetwork.ScheduleItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class MyDayViewModel @Inject constructor(
    private val repository: MyDayRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(
        savedStateHandle.get<String>(KEY_DATE)?.let(LocalDate::parse) ?: LocalDate.now()
    )
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
        savedStateHandle[KEY_DATE] = date.toString()
    }

    fun observeDay(date: LocalDate): Flow<MyDayEntity?> = repository.observeDay(date)

    fun observeSchedule(date: LocalDate): Flow<List<ScheduleEntity>> = repository.observeSchedule(date)

    suspend fun getScheduleItem(id: String): ScheduleEntity? = repository.getScheduleItem(id)

    fun setRating(date: LocalDate, score: Double) {
        viewModelScope.launch { repository.saveRating(date, score) }
    }

    fun saveAiFeedback(date: LocalDate, feedback: String) {
        viewModelScope.launch { repository.saveAiFeedback(date, feedback) }
    }

    // Заметка сохраняется с задержкой после последнего символа. Если экран закрыли раньше —
    // сохраняем сразу в onCleared, иначе последние 1.5 с набранного текста терялись бы.
    private var pendingNote: Pair<LocalDate, String>? = null
    private var noteJob: Job? = null

    fun onNoteChanged(date: LocalDate, note: String) {
        pendingNote = date to note
        noteJob?.cancel()
        noteJob = viewModelScope.launch {
            delay(NOTE_SAVE_DELAY_MS.milliseconds)
            flushNote()
        }
    }

    private fun flushNote() {
        val (date, note) = pendingNote ?: return
        pendingNote = null
        repository.saveNoteInBackground(date, note)
    }

    override fun onCleared() {
        flushNote() //вызов функции чтобы сохранилось все равно сообщение
    }

    fun addScheduleItems(date: LocalDate, items: List<ScheduleItem>) {
        viewModelScope.launch { repository.addScheduleItems(date, items) }
    }

    fun updateScheduleItem(id: String, title: String, time: LocalTime, note: String) {
        viewModelScope.launch { repository.updateScheduleItem(id, title, time, note) }
    }

    fun deleteScheduleItem(id: String) {
        viewModelScope.launch { repository.deleteScheduleItem(id) }
    }

    companion object {
        const val KEY_DATE = "selected_date"
        const val NOTE_SAVE_DELAY_MS = 1500L
    }
}