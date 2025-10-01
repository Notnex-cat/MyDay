package com.notnex.myday.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.notnex.myday.auth.AuthViewModel
import com.notnex.myday.ui.screens.settingsactivity.AuthScreen
import com.notnex.myday.ui.screens.settingsactivity.SettingsScreen
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
                            val viewModel: AuthViewModel = hiltViewModel()
                            val authState by viewModel.authState.collectAsState()
                            val userId = authState.user?.userId ?: "no_user"
                            key(userId) {
                                SettingsScreen(
                                    state = authState,
                                    onSignOut = {
                                        viewModel.signOut()
                                        //this@Settings.finish()
                                    },
                                    navController = navController
                                )
                            }
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