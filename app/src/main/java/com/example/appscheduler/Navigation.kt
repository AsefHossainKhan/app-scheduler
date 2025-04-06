package com.example.appscheduler

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appscheduler.ui.Screen
import com.example.appscheduler.ui.SharedViewModel
import com.example.appscheduler.ui.screens.HomeScreen
import com.example.appscheduler.ui.screens.ShowInstalledApps

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
        composable(route = Screen.ShowInstalledAppsScreen.route) {
            ShowInstalledApps(modifier = Modifier)
        }
    }
}