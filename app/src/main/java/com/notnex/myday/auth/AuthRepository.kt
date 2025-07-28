package com.notnex.myday.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.notnex.myday.firebase.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

data class AuthState(
    val isAuthenticated: Boolean = false,
    val user: UserData? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthRepository(
    private val context: Context,
    private val oneTapClient: SignInClient
) {
    private val auth = Firebase.auth
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    init {
        val user = getCurrentUser()
        if (user != null) {
            _authState.value = AuthState(isAuthenticated = true, user = user)
        }
    }

    fun getCurrentUser(): UserData? = auth.currentUser?.let {
        UserData(
            userId = it.uid,
            username = it.displayName ?: it.email?.substringBefore("@"),
            profilePictureUrl = it.photoUrl?.toString(),
            email = it.email
        )
    }

    suspend fun signInWithEmail(email: String, password: String) {
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        try {
            auth.signInWithEmailAndPassword(email, password).await()
            _authState.value = AuthState(
                isAuthenticated = true,
                user = getCurrentUser()
            )
        } catch (e: Exception) {
            _authState.value = AuthState(error = e.message)
        }
    }

    suspend fun signUpWithEmail(email: String, password: String) {
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        try {
            auth.createUserWithEmailAndPassword(email, password).await()
            _authState.value = AuthState(
                isAuthenticated = true,
                user = getCurrentUser()
            )
        } catch (e: Exception) {
            _authState.value = AuthState(error = e.message)
        }
    }

    suspend fun signInWithGoogle(intent: Intent) {
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(intent)
            val googleIdToken = credential.googleIdToken
            val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
            auth.signInWithCredential(googleCredentials).await()
            _authState.value = AuthState(
                isAuthenticated = true,
                user = getCurrentUser()
            )
        } catch (e: Exception) {
            _authState.value = AuthState(error = e.message)
        }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState()
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}