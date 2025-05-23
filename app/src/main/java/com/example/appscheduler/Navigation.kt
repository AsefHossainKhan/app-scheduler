package com.example.appscheduler

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.appscheduler.ui.BottomNavItem
import com.example.appscheduler.ui.Screen
import com.example.appscheduler.ui.screens.HomeScreen
import com.example.appscheduler.ui.screens.LogScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val items = listOf(
                    BottomNavItem.Home,
                    BottomNavItem.Log,
                )
                items.forEach { item ->
                    val title = when (item) {
                        BottomNavItem.Home -> stringResource(R.string.home_bottom_nav)
                        BottomNavItem.Log -> stringResource(R.string.log_bottom_nav)
                    }
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = title) },
                        label = { Text(text = title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                navController.graph.startDestinationRoute?.let { route ->
                                    popUpTo(route) {
                                        saveState = true
                                    }
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.HomeScreen.route,
        ) {
            composable(route = Screen.HomeScreen.route) {
                Box(Modifier.padding(innerPadding)) {
                    HomeScreen()
                }
            }
            composable(route = Screen.LogScreen.route) {
                Box(Modifier.padding(innerPadding)) {
                    LogScreen()
                }
            }
        }
    }
}