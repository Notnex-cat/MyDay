package com.notnex.myday.neuralnetwork

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class NNViewModel @Inject constructor(
    private val mistral: MistralRepository
) : ViewModel() {

    private val _scheduleState = MutableStateFlow<NNResult<List<ScheduleItem>>>(NNResult.Idle)
    val scheduleState: StateFlow<NNResult<List<ScheduleItem>>> = _scheduleState.asStateFlow()

    private val _feedbackState = MutableStateFlow<NNResult<Unit>>(NNResult.Idle)
    val feedbackState: StateFlow<NNResult<Unit>> = _feedbackState.asStateFlow()

    /** Текст ответа, накапливаемый по мере стриминга. */
    private val _feedbackText = MutableStateFlow("")
    val feedbackText: StateFlow<String> = _feedbackText.asStateFlow()

    private var scheduleJob: Job? = null
    private var feedbackJob: Job? = null

    fun requestSchedule(userInput: String, date: LocalDate) {
        scheduleJob?.cancel()
        scheduleJob = viewModelScope.launch {
            _scheduleState.value = NNResult.Loading
            _scheduleState.value = try {
                NNResult.Success(mistral.generateSchedule(userInput, date))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Schedule request failed", e)
                NNResult.Error(e)
            }
        }
    }

    /** @param onComplete вызывается один раз с полным текстом, когда стрим успешно завершился. */
    fun requestFeedback(note: String, onComplete: (String) -> Unit) {
        feedbackJob?.cancel()
        feedbackJob = viewModelScope.launch {
            _feedbackText.value = ""
            _feedbackState.value = NNResult.Loading
            var failure: Throwable? = null
            mistral.streamFeedback(note)
                .catch { failure = it }
                .collect { chunk -> _feedbackText.value += chunk }

            val error = failure
            if (error != null) {
                Log.e(TAG, "Feedback request failed", error)
                _feedbackState.value = NNResult.Error(error)
            } else {
                _feedbackState.value = NNResult.Success(Unit)
                onComplete(_feedbackText.value)
            }
        }
    }

    private companion object {
        const val TAG = "NNViewModel"
    }
}
