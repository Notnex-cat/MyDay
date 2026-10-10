package com.notnex.myday.auth

import android.content.Intent
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await

data class AuthState(
    val isAuthenticated: Boolean = false,
    val user: UserData? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthRepository(
    private val oneTapClient: SignInClient
) {
    private val auth = Firebase.auth
    private val _authState = MutableStateFlow(stateFor(auth.currentUser))
    val authState: StateFlow<AuthState> = _authState

    val currentUserId: String? get() = auth.currentUser?.uid

    init {
        // Единственный источник правды о том, кто вошёл: и вход, и выход (в том числе
        // сделанный не через этот класс) приходят сюда.
        auth.addAuthStateListener { firebaseAuth ->
            _authState.update {
                it.copy(
                    isAuthenticated = firebaseAuth.currentUser != null,
                    user = firebaseAuth.currentUser?.toUserData()
                )
            }
        }
    }

    suspend fun signInWithEmail(email: String, password: String) = authenticate {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun signUpWithEmail(email: String, password: String) = authenticate {
        auth.createUserWithEmailAndPassword(email, password).await()
    }

    suspend fun signInWithGoogle(intent: Intent) = authenticate {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleCredentials = GoogleAuthProvider.getCredential(credential.googleIdToken, null)
        auth.signInWithCredential(googleCredentials).await()
    }

    fun signOut() {
        auth.signOut()
        _authState.update { it.copy(error = null) }
    }

    fun clearError() {
        _authState.update { it.copy(error = null) }
    }

    private suspend fun authenticate(block: suspend () -> Unit) {
        _authState.update { it.copy(isLoading = true, error = null) }
        try {
            block()
            // Подтянем displayName/photoUrl, которых может не быть сразу после входа
            auth.currentUser?.reload()?.await()
            _authState.update { it.copy(isLoading = false, user = auth.currentUser?.toUserData()) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Неудачная попытка входа не должна «разлогинивать» уже вошедшего пользователя
            _authState.update { it.copy(isLoading = false, error = e.message) }
        }
    }

    private fun stateFor(user: FirebaseUser?) = AuthState(
        isAuthenticated = user != null,
        user = user?.toUserData()
    )

    private fun FirebaseUser.toUserData() = UserData(
        userId = uid,
        username = displayName ?: email?.substringBefore("@"),
        profilePictureUrl = photoUrl?.toString(),
        email = email
    )
}
