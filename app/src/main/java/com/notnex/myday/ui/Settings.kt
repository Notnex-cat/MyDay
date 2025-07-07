package com.notnex.myday.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.identity.Identity
import com.notnex.myday.auth.AuthViewModel
import com.notnex.myday.ui.screens.AuthScreen
import com.notnex.myday.ui.screens.SettingsScreen
import com.notnex.myday.ui.theme.MyDayTheme
import com.notnex.myday.viewmodel.Screen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Settings() : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val viewModel: AuthViewModel = viewModel()
            val authState by viewModel.authState.collectAsState()
            val context = this
            val oneTapClient = remember { Identity.getSignInClient(context) }
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult(),
                onResult = { result ->
                    if (result.resultCode == RESULT_OK) {
                        val intent = result.data ?: return@rememberLauncherForActivityResult
                        viewModel.signInWithGoogle(intent)
                    }
                }
            )
            MyDayTheme {
                Surface(
                    modifier = Modifier.Companion.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = Screen.SettingScreen.route
                    ) {
                        composable("settings_screen"){
                            SettingsScreen(
                                state = authState,
                                onSignOut = {
                                    viewModel.signOut()
                                    this@Settings.finish()
                                },
                                navController = navController
                            )
                        }
                        composable("auth_screen") {
                            AuthScreen(
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}