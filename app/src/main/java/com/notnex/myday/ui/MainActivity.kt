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
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import com.notnex.myday.ui.screens.mainactivity.DayNote
import com.notnex.myday.ui.screens.mainactivity.EditScheduleScreen
import com.notnex.myday.ui.screens.mainactivity.MainScreen
import com.notnex.myday.ui.screens.mainactivity.ScheduleScreen
import com.notnex.myday.ui.theme.MyDayTheme
import com.notnex.myday.viewmodel.Screen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter

const val CARD_EXPLODE_BOUNDS_KEY = "CARD_EXPLODE_BOUNDS_KEY"

@AndroidEntryPoint
class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyDayTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE // или другой формат
                    val navController = rememberNavController()
                    SharedTransitionLayout {
                        NavHost(
                            navController = navController,
                            startDestination = Screen.MainScreen.route
                        ) {
                            composable(
                                route = Screen.MainScreen.route
                            ) {
                                MainScreen(
                                    navController = navController,
                                    animatedVisibilityScope = this,
                                )
                            }
                            composable(
                                route = Screen.DayNote.route + "/{date}",
                                arguments = listOf(
                                    navArgument("date") {
                                        type = NavType.StringType
                                        nullable = false
                                    },
                                )
                            ) { entry ->
                                val dateString = entry.arguments?.getString("date")
                                val date = try {
                                    LocalDate.parse(requireNotNull(dateString), dateFormatter)
                                } catch (_: Exception) {
                                    LocalDate.now()
                                }
                                DayNote(
                                    navController = navController,
                                    date = date,
                                    //currentRating = currentRating,
                                    //note = note,
                                    animatedVisibilityScope = this
                                )
                            }
                            composable(
                                route = Screen.ScheduleScreen.route + "/{date}",
                                arguments = listOf(
                                    navArgument("date") {
                                        type = NavType.StringType
                                        nullable = false
                                    },
                                )
                            )
                            { entry ->
                                val dateString = entry.arguments?.getString("date")
                                val date = try {
                                    LocalDate.parse(requireNotNull(dateString), dateFormatter)
                                } catch (_: Exception) {
                                    LocalDate.now()
                                }
                                ScheduleScreen(
                                    navController = navController,
                                    date = date,
                                    //currentRating = currentRating,
                                    //note = note,
                                )
                            }

                            composable<ScreenEditSchedule> { backStackEntry ->
                                val args = backStackEntry.toRoute<ScreenEditSchedule>()
                                EditScheduleScreen(navController, args.item)
                            }

                        }
                    }
                }
            }
        }
    }
}

@Serializable
object ScreenSchedule

@Serializable
data class ScreenEditSchedule(
    val item: String
)