package com.example.f329723_app.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun BottomNavBar(currentRoute: String?, onItemSelected: (String) -> Unit) {
    NavigationBar {
        AppNavigation.allScreens.forEach { screen ->
            NavigationBarItem(
                selected = screen.route == currentRoute,
                onClick = { onItemSelected(screen.route) },
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) }
            )
        }
    }
}
