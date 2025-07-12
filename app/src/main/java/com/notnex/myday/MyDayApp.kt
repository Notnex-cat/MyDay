package com.notnex.myday

import android.app.Application
import com.google.android.gms.auth.api.identity.Identity
import com.notnex.myday.auth.AuthRepository
import com.notnex.myday.auth.AuthViewModel
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyDayApp: Application() {
    // Создаём AuthViewModel как singleton
    val authViewModel by lazy {
        val authRepository = AuthRepository(
            applicationContext,
            Identity.getSignInClient(applicationContext)
        )
        AuthViewModel(authRepository)
    }
}


