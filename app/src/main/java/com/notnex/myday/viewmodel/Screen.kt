package com.notnex.myday.viewmodel

sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")
    object DayNote : Screen("day_note")
    object SettingScreen : Screen("settings_screen")
    data object Auth : Screen("auth_screen")
}