package com.notnex.myday.firebase

data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)