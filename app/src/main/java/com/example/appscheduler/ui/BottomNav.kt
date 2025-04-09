package com.example.appscheduler.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(var icon: ImageVector, var route: String) {
    object Home : BottomNavItem(Icons.Filled.Home, Screen.HomeScreen.route)
    object Log : BottomNavItem(Icons.AutoMirrored.Filled.List, Screen.LogScreen.route)
}