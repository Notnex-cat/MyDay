package com.notnex.myday.auth

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(
        app.applicationContext,
        Identity.getSignInClient(app.applicationContext)
    )
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