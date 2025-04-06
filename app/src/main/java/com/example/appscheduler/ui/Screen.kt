package com.example.appscheduler.ui

sealed class Screen(val route: String) {
    data object HomeScreen : Screen("home_screen")
    data object ShowInstalledAppsScreen: Screen("show_installed_apps_screen")
}