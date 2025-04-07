package com.example.appscheduler.ui

sealed class Screen(val route: String) {
    data object HomeScreen : Screen("home_screen")
}