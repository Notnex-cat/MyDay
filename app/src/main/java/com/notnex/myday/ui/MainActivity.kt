package com.notnex.myday.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.notnex.myday.ui.theme.MyDayTheme
import com.notnex.myday.viewmodel.Screen
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyDayTheme {
                val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE // или другой формат
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Screen.MainScreen.route
                ) {
                    composable(
                        route = Screen.MainScreen.route
                    ) {
                        MainScreen(
                            navController = navController
                        )
                    }
                    composable(
                        route = Screen.DayNote.route + "/{date}/{currentRating}/{note}",
                        arguments = listOf(
                            navArgument("date") {
                                type = NavType.StringType
                                nullable = true
                            },
                            navArgument("currentRating") {
                                type = NavType.FloatType
                                nullable = false
                            },
                            navArgument("note") {
                                type = NavType.StringType
                                nullable = true
                            }
                        )
                    ) { entry ->
                        val dateString = entry.arguments?.getString("date")
                        val date = dateString.let { LocalDate.parse(it, dateFormatter) }
                        val currentRating = entry.arguments?.getFloat("currentRating")?.toDouble() ?: 0.0
                        val note = entry.arguments?.getString("note") ?: ""

                        DayNote(
                            navController = navController,
                            date = date,
                            currentRating = currentRating,
                            note = note
                        )
                    }
                }
            }
        }
    }
}
