package com.example.f329723_app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.app.Activity


class SettingsScreen {
    companion object {

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun Content() {
            // Main scaffold for the Settings screen, includes a top app bar
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Settings",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header title for the preferences section
                    Text(
                        text = "Preferences",
                        style = MaterialTheme.typography.titleLarge
                    )

                    val context = LocalContext.current

                    // State for notification toggle
                    var notificationsEnabled by remember { mutableStateOf(areNotificationsEnabled(context)) }

                    // Toggle switch row for enabling/disabling notifications
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Enable notifications", modifier = Modifier.weight(1f))
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { isChecked ->
                                notificationsEnabled = isChecked
                                saveNotificationPreference(isChecked, context)
                            }
                        )
                    }
                }
            }
        }

        // Saves the user's notification preference in SharedPreferences
        fun saveNotificationPreference(enabled: Boolean, context: Context) {
            val prefs = context.getSharedPreferences("user_prefs", 0)
            prefs.edit().putBoolean("notifications_enabled", enabled).apply()
        }

        // Loads the user's notification preference from SharedPreferences
        fun areNotificationsEnabled(context: Context): Boolean {
            val prefs = context.getSharedPreferences("user_prefs", 0)
            return prefs.getBoolean("notifications_enabled", true)
        }

        // Reusable component for toggling notification setting (used elsewhere if needed)
        @Composable
        fun NotificationSettingsToggle() {
            val context = LocalContext.current
            var notificationsEnabled by remember {
                mutableStateOf(areNotificationsEnabled(context))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Enable notifications", modifier = Modifier.weight(1f))
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { isChecked ->
                        notificationsEnabled = isChecked
                        saveNotificationPreference(isChecked, context)

                        // If enabling notifications on Android 13+, check permission
                        if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                // Ask for permission if not already granted
                                val activity = context as? Activity
                                activity?.let {
                                    ActivityCompat.requestPermissions(
                                        it,
                                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                                        100 // request code
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

