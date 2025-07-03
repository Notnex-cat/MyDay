package com.notnex.myday.viewmodel

import androidx.collection.buildIntIntMap
import java.time.LocalDate

sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")
    object DayNote : Screen("day_note")
    object Settings : Screen("setting")
}