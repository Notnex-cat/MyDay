package com.notnex.myday.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notnex.myday.data.MyDayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MyDayViewModel @Inject constructor(
    private val myDayRepository: MyDayRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    fun getScore(date: LocalDate) = myDayRepository.getScoreByDate(date)

    fun saveDayEntry(date: LocalDate, score: Double, note: String) {
        viewModelScope.launch {
            myDayRepository.saveOrUpdateDayScore(date, score, note)
        }
    }

}