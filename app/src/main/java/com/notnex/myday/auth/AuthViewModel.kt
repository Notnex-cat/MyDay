package com.notnex.myday.auth

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    val authState: StateFlow<AuthState> = repo.authState

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch { repo.signInWithEmail(email, password) }
    }

    fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch { repo.signUpWithEmail(email, password) }
    }

    fun signInWithGoogle(intent: Intent) {
        viewModelScope.launch { repo.signInWithGoogle(intent) }
    }

    fun signOut() = repo.signOut()
    fun clearError() = repo.clearError()
} 