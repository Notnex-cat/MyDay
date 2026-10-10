package com.notnex.myday.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notnex.myday.data.MyDayEntity
import com.notnex.myday.data.MyDayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PracticeViewModel @Inject constructor(
    private val repository: MyDayRepository
    ) : ViewModel() {

    private val _counter = MutableStateFlow<Int>(0)
    val counter = _counter.asStateFlow()
    private val _time = MutableStateFlow<Int>(0)
    val time = _time.asStateFlow()
    private var timeJob: Job? = null

    private val _text = MutableStateFlow("")
    val text = _text.asStateFlow()
    private var noteJob: Job? = null

    fun addCount() {
        _counter.update { it + 1 }
    }

    fun timeCounter() {
        if (timeJob?.isActive != true) {
            timeJob = viewModelScope.launch {
                while (true) {
                    delay(1000)
                    _time.update { it + 1 }
                }
            }
        }
    }

    fun onNoteChanged(note: String){
        noteJob?.cancel()
        noteJob = viewModelScope.launch {
            delay(1000)
            _text.value = note
        }
    }


    fun observeNote(): Flow<List<MyDayEntity>> = repository.observeNotes()

    fun setMood(date: LocalDate, mood: Int) {
        viewModelScope.launch { repository.saveMood(date, mood) }
    }

}