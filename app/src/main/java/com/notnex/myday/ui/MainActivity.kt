@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.notnex.myday.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.notnex.myday.auth.AuthViewModel
import com.notnex.myday.ui.navigation.AuthRoute
import com.notnex.myday.ui.navigation.DayNoteRoute
import com.notnex.myday.ui.navigation.EditScheduleRoute
import com.notnex.myday.ui.navigation.MainRoute
import com.notnex.myday.ui.navigation.PracticeRoute
import com.notnex.myday.ui.navigation.ScheduleRoute
import com.notnex.myday.ui.navigation.SettingsRoute
import com.notnex.myday.ui.navigation.toLocalDateOrToday
import com.notnex.myday.ui.screens.PracticeScreen
import com.notnex.myday.ui.screens.mainactivity.DayNote
import com.notnex.myday.ui.screens.mainactivity.EditScheduleScreen
import com.notnex.myday.ui.screens.mainactivity.MainScreen
import com.notnex.myday.ui.screens.mainactivity.ScheduleScreen
import com.notnex.myday.ui.screens.settingsactivity.AuthScreen
import com.notnex.myday.ui.screens.settingsactivity.SettingsScreen
import com.notnex.myday.ui.theme.MyDayTheme
import dagger.hilt.android.AndroidEntryPoint

const val CARD_EXPLODE_BOUNDS_KEY = "CARD_EXPLODE_BOUNDS_KEY"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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
                    SharedTransitionLayout {
                        NavHost(
                            navController = navController,
                            startDestination = MainRoute
                        ) {
                            composable<MainRoute> {
                                MainScreen(
                                    navController = navController,
                                    animatedVisibilityScope = this
                                )
                            }
                            composable<DayNoteRoute> { entry ->
                                DayNote(
                                    navController = navController,
                                    date = entry.toRoute<DayNoteRoute>().date.toLocalDateOrToday(),
                                    animatedVisibilityScope = this
                                )
                            }
                            composable<ScheduleRoute> { entry ->
                                ScheduleScreen(
                                    navController = navController,
                                    date = entry.toRoute<ScheduleRoute>().date.toLocalDateOrToday()
                                )
                            }
                            composable<EditScheduleRoute> { entry ->
                                EditScheduleScreen(
                                    navController = navController,
                                    id = entry.toRoute<EditScheduleRoute>().id
                                )
                            }
                            composable<SettingsRoute> {
                                val authViewModel: AuthViewModel = hiltViewModel()
                                val authState by authViewModel.authState.collectAsState()
                                // key пересоздаёт экран при смене пользователя, сбрасывая его локальное состояние
                                key(authState.user?.userId) {
                                    SettingsScreen(
                                        state = authState,
                                        onSignOut = authViewModel::signOut,
                                        navController = navController
                                    )
                                }
                            }
                            composable<PracticeRoute> {
                                PracticeScreen(
                                    navController = navController
                                )
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
}