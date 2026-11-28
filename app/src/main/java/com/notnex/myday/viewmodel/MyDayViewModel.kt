package com.notnex.myday.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notnex.myday.data.MyDayEntity
import com.notnex.myday.data.MyDayRepository
import com.notnex.myday.data.ScheduleEntity
import com.notnex.myday.neuralnetwork.ScheduleItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class MyDayViewModel @Inject constructor(
    private val repository: MyDayRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(
        savedStateHandle.get<String>(KEY_DATE)?.let(LocalDate::parse) ?: LocalDate.now()
    )
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
        savedStateHandle[KEY_DATE] = date.toString()
    }

    // День и расписание выбранной даты для главного экрана. flatMapLatest при смене даты бросает
    // прежний запрос, onStart сразу очищает экран от данных прошлой даты. WhileSubscribed(5000)
    // держит запрос к базе 5 секунд после ухода экрана: поворот его не перезапускает.
    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedDay: StateFlow<MyDayEntity?> = _selectedDate
        .flatMapLatest { date -> repository.observeDay(date).onStart { emit(null) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedSchedule: StateFlow<List<ScheduleEntity>> = _selectedDate
        .flatMapLatest { date -> repository.observeSchedule(date).onStart { emit(emptyList()) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    fun observeDay(date: LocalDate): Flow<MyDayEntity?> = repository.observeDay(date)

    suspend fun getDay(date: LocalDate): MyDayEntity? = repository.getDay(date)

    suspend fun getSchedule(date: LocalDate): List<ScheduleEntity> = repository.getSchedule(date)

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
        flushNote() // экран закрыли раньше, чем истекла задержка: дописываем остаток
    }

    fun updateScheduleItem(id: String, title: String, time: LocalTime, note: String) {
        viewModelScope.launch { repository.updateScheduleItem(id, title, time, note) }
    }

    fun deleteScheduleItem(id: String) {
        viewModelScope.launch { repository.deleteScheduleItem(id) }
    }

    fun replaceDaySchedule(date: LocalDate, items: List<ScheduleItem>){
        viewModelScope.launch {
            repository.replaceDaySchedule(date, items)
        }
    }

    companion object {
        const val KEY_DATE = "selected_date"
        const val NOTE_SAVE_DELAY_MS = 1500L
        const val STOP_TIMEOUT_MS = 5_000L
    }
}