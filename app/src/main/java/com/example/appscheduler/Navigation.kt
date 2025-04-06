package com.example.appscheduler

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appscheduler.ui.Screen
import com.example.appscheduler.ui.SharedViewModel
import com.example.appscheduler.ui.screens.HomeScreen

@Composable
fun Navigation() {
    val sharedViewModel: SharedViewModel = hiltViewModel()
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.HomeScreen.route,
    ) {
        composable(route = Screen.HomeScreen.route) {
            HomeScreen(navController, sharedViewModel)
        }
    }
}