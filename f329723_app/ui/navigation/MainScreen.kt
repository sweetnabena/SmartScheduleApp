package com.example.f329723_app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.example.f329723_app.ui.screens.home.HomeScreen
import com.example.f329723_app.ui.screens.tasks.TasksScreenContent
import com.example.f329723_app.ui.screens.settings.SettingsScreen
import com.example.f329723_app.ui.screens.addappointment.AddEditAppointmentScreen
import com.example.f329723_app.ui.screens.schedule.ScheduleViewModel
import com.example.f329723_app.ui.screens.schedule.ScheduleScreenContent
import com.example.f329723_app.ui.screens.privatenote.PrivateNoteScreen
import com.example.f329723_app.ui.screens.splashscreen.SplashScreen
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MainScreen() {
    val navController = rememberNavController() // Controller to manage navigation stack
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    Scaffold(
        bottomBar = {
            // Show BottomNavBar only if we're not on the splash screen
            if (currentRoute !in listOf(AppNavigation.Splash.route)) {
                BottomNavBar(currentRoute = currentRoute) { route ->
                    if (route != currentRoute) {
                        navController.navigate(route) {
                            // Avoid stacking destinations, keep state
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
        }
    ) { padding ->
        // Navigation host that defines all composable screen destinations
        NavHost(
            navController = navController,
            startDestination = AppNavigation.Splash.route, // Initial screen
            modifier = Modifier.padding(padding)
        ) {
            // Home screen route
            composable(AppNavigation.Home.route) {
                HomeScreen(navController = navController)
            }

            // Schedule/calendar screen
            composable(AppNavigation.Schedule.route) {
                ScheduleScreenContent(navController = navController)
            }

            // Settings screen
            composable(AppNavigation.Settings.route) {
                SettingsScreen.Content()
            }

            // Tasks list screen
            composable(AppNavigation.Tasks.route) {
                TasksScreenContent()
            }

            // Private notes screen
            composable("private_notes") {
                PrivateNoteScreen(navController = navController)
            }

            // Splash screen route
            composable("splash") {
                SplashScreen(navController = navController)
            }

            // Add/Edit Appointment screen with optional appointmentId parameter
            composable("addEditAppointment/{selectedDate}?appointmentId={appointmentId}") { backStackEntry ->
                val selectedDateArg = backStackEntry.arguments?.getString("selectedDate")
                val selectedDate = selectedDateArg?.let { LocalDate.parse(it) } ?: LocalDate.now()

                val appointmentId = backStackEntry.arguments?.getString("appointmentId")
                val viewModel: ScheduleViewModel = hiltViewModel()
                val context = LocalContext.current

                // Load appointment details when appointmentId is provided (edit mode)
                LaunchedEffect(appointmentId) {
                    if (appointmentId != null) {
                        viewModel.loadAppointmentById(appointmentId)
                    }
                }

                val editingAppointment = viewModel.currentEditingAppointment

                AddEditAppointmentScreen(
                    selectedDate = selectedDate,
                    onSave = { appointment, taskIds ->
                        viewModel.saveAppointment(appointment, taskIds, context)
                        navController.popBackStack() // Navigate back after saving
                    },
                    onCancel = {
                        navController.popBackStack()
                    },
                    onDelete = {
                        editingAppointment?.let {
                            viewModel.deleteAppointment(it)
                        }
                        navController.popBackStack()
                    },
                    isEditMode = editingAppointment != null // Indicates update vs. create mode
                )
            }
        }
    }
}
