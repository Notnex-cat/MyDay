package com.notnex.myday.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.notnex.myday.neuralnetwork.AiProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private val PROVIDER_KEY = stringPreferencesKey("ai_provider")

private fun apiKeyKey(provider: AiProvider) = stringPreferencesKey("ai_api_key_${provider.name}")

@Singleton
class AiSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val secretStore: SecretStore,
) {
    val provider: Flow<AiProvider> = context.dataStore.data.map { prefs ->
        AiProvider.entries.find { it.name == prefs[PROVIDER_KEY] } ?: AiProvider.GEMINI
    }

    suspend fun saveProvider(provider: AiProvider) {
        context.dataStore.edit { prefs -> prefs[PROVIDER_KEY] = provider.name }
    }

    fun apiKey(provider: AiProvider): Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[apiKeyKey(provider)] }
        .distinctUntilChanged()
        .map { encrypted -> encrypted?.let { secretStore.decrypt(it) } }
        .flowOn(Dispatchers.Default)

    suspend fun saveApiKey(provider: AiProvider, key: String) {
        val encrypted = withContext(Dispatchers.Default) { secretStore.encrypt(key.trim()) }
        context.dataStore.edit { prefs -> prefs[apiKeyKey(provider)] = encrypted }
    }

    suspend fun clearApiKey(provider: AiProvider) {
        context.dataStore.edit { prefs -> prefs.remove(apiKeyKey(provider)) }
    }
}
