package com.notnex.myday.neuralnetwork

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notnex.myday.data.AiSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class NNViewModel @Inject constructor(
    private val aiRepository: AiRepository,
    aiSettingsRepository: AiSettingsRepository,
) : ViewModel() {

    /** Включена ли нейросеть в настройках; null — настройка ещё не прочитана. */
    val aiEnabled: StateFlow<Boolean?> = aiSettingsRepository.aiEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), null)

    private val _scheduleState = MutableStateFlow<NNResult<List<ScheduleItem>>>(NNResult.Idle)
    val scheduleState: StateFlow<NNResult<List<ScheduleItem>>> = _scheduleState.asStateFlow()

    private val _feedbackState = MutableStateFlow<NNResult<Unit>>(NNResult.Idle)
    val feedbackState: StateFlow<NNResult<Unit>> = _feedbackState.asStateFlow()

    /** Текст ответа, накапливаемый по мере стриминга. */
    private val _feedbackText = MutableStateFlow("")
    val feedbackText: StateFlow<String> = _feedbackText.asStateFlow()

    private var scheduleJob: Job? = null
    private var feedbackJob: Job? = null

    fun requestSchedule(userInput: String, date: LocalDate, draft: List<ScheduleItem>) {
        scheduleJob?.cancel()
        scheduleJob = viewModelScope.launch {
            _scheduleState.value = NNResult.Loading
            _scheduleState.value = try {
                NNResult.Success(aiRepository.generateSchedule(userInput, date, draft))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Schedule request failed", e)
                NNResult.Error(e)
            }
        }
    }

    /**
     * Экран забрал готовое расписание в черновик. Без сброса результат применился бы повторно
     * после поворота экрана: состояние ViewModel переживает поворот.
     */
    fun scheduleConsumed() {
        if (_scheduleState.value is NNResult.Success) _scheduleState.value = NNResult.Idle
    }

    /** @param onComplete вызывается один раз с полным текстом, когда стрим успешно завершился. */
    fun requestFeedback(note: String, onComplete: (String) -> Unit) {
        feedbackJob?.cancel()
        feedbackJob = viewModelScope.launch {
            _feedbackText.value = ""
            _feedbackState.value = NNResult.Loading
            var failure: Throwable? = null
            aiRepository.streamFeedback(note)
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
