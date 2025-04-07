package com.example.appscheduler.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(var title: String, var icon: ImageVector, var route: String) {
    object Home : BottomNavItem("Home", Icons.Filled.Home, Screen.HomeScreen.route)
    object Log : BottomNavItem("Log", Icons.AutoMirrored.Filled.List, Screen.LogScreen.route)
}