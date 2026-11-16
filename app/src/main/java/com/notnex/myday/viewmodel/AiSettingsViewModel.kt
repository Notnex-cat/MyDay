package com.notnex.myday.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notnex.myday.data.AiSettingsRepository
import com.notnex.myday.neuralnetwork.AiProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiSettingsViewModel @Inject constructor(
    private val aiSettingsRepository: AiSettingsRepository,
) : ViewModel() {
    val selectedAi = aiSettingsRepository.provider

    val providersWithKey = aiSettingsRepository.providersWithKey

    fun selectProvider(provider: AiProvider) {
        viewModelScope.launch {
            aiSettingsRepository.saveProvider(provider)
        }
    }

    fun saveApiKey(provider: AiProvider, key: String) {
        viewModelScope.launch {
            aiSettingsRepository.saveApiKey(provider, key)
        }
    }

    fun clearApiKey(provider: AiProvider) {
        viewModelScope.launch {
            aiSettingsRepository.clearApiKey(provider)
        }
    }
}