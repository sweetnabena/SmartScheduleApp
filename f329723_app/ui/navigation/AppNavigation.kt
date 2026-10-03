package com.example.f329723_app.ui.navigation


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AppNavigation(val route: String, val label: String, val icon: ImageVector) {
    object Home : AppNavigation("home", "Home", Icons.Default.Home)
    object Schedule : AppNavigation("schedule", "Schedule", Icons.Default.DateRange)
    object Tasks : AppNavigation("tasks", "Tasks", Icons.Default.Check)
    object Settings : AppNavigation("settings", "Settings", Icons.Default.Settings)

    object Splash : AppNavigation("splash", "", Icons.Default.Home) // not used in BottomNavBar

    companion object {
        val allScreens = listOf(Home, Schedule, Tasks, Settings)
    }
}


