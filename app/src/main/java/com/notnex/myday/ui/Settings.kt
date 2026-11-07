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
import com.notnex.myday.ui.navigation.AuthRoute
import com.notnex.myday.ui.navigation.SettingsRoute
import com.notnex.myday.ui.screens.settingsactivity.AuthScreen
import com.notnex.myday.ui.screens.settingsactivity.SettingsScreen
import com.notnex.myday.ui.theme.MyDayTheme
import com.notnex.myday.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Settings : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyDayTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = SettingsRoute
                    ) {
                        composable<SettingsRoute> {
                            val authViewModel: AuthViewModel = hiltViewModel()
                            val settingsViewModel: SettingsViewModel = hiltViewModel()
                            val authState by authViewModel.authState.collectAsState()
                            // key пересоздаёт экран при смене пользователя, сбрасывая его локальное состояние
                            key(authState.user?.userId) {
                                SettingsScreen(
                                    state = authState,
                                    onSignOut = authViewModel::signOut,
                                    navController = navController,
                                    settingsViewModel = settingsViewModel
                                )
                            }
                        }
                        composable<AuthRoute> {
                            AuthScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}
