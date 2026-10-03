package com.example.f329723_app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.f329723_app.ui.screens.dailylog.DailyLogViewModel
import com.example.f329723_app.ui.screens.tasks.TaskViewModel
import androidx.compose.material3.Text
import androidx.compose.material3.Slider
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.navigation.NavController
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.saveable.rememberSaveable
import java.util.Date
import java.util.Locale

import com.example.f329723_app.data.model.Appointment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController,
               viewModel: HomeViewModel = hiltViewModel()) {

    // Observing state from ViewModel
    val appointments by viewModel.appointments.collectAsState()
    val logViewModel: DailyLogViewModel = hiltViewModel()
    val todayLog by logViewModel.currentLog.collectAsState()

    // Remember mood and productivity values locally with saveable state
    val mood = rememberSaveable { mutableStateOf(3) }
    val productivity = rememberSaveable { mutableStateOf(3) }

    // Percentage of productivity (calculated in ViewModel)
    val productivityPercent by viewModel.todayProductivity.collectAsState()

    // When today's log is fetched, set local state values
    LaunchedEffect(todayLog) {
        todayLog?.let {
            mood.value = it.mood
            productivity.value = it.productivity
        }
    }

    // Scroll support for screen content
    val scrollState = rememberScrollState()

    // Fetch today's task completion from TaskViewModel
    val taskViewModel: TaskViewModel = hiltViewModel()
    val todayCompletion by taskViewModel.getTodayCompletionRate().collectAsState()

    // Scaffold provides basic screen structure including top app bar
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // App icon and name
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "App Icon",
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp)
                        )
                        Text(
                            text = "SmartSchedule",
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
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(scrollState)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section showing upcoming appointments
            UpcomingAppointmentsSection(appointments)

            // Productivity visualisation section
            SectionCard(title = "Productivity") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProductivityCircle(percent = productivityPercent)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "You're ${(productivityPercent * 100).toInt()}% productive today.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Text(
                    text = "Today's Task Completion: ${(todayCompletion * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            // Daily mood and productivity slider section
            SectionCard(title = "Mood & Productivity Log") {
                Text("Mood: ${mood.value}")
                Slider(
                    value = mood.value.toFloat(),
                    onValueChange = {
                        mood.value = it.toInt()
                        logViewModel.mood.value = it.toInt()
                    },
                    valueRange = 1f..5f,
                    steps = 3
                )

                Text("Productivity: ${productivity.value}")
                Slider(
                    value = productivity.value.toFloat(),
                    onValueChange = {
                        productivity.value = it.toInt()
                        logViewModel.productivity.value = it.toInt()
                    },
                    valueRange = 1f..5f,
                    steps = 3
                )

                Spacer(Modifier.height(12.dp))

                // Save log button to commit daily values
                Button(onClick = { logViewModel.saveLog() }) {
                    Text("Save Today's Log")
                }
            }

            // Navigation to notes screen
            SectionCard(title = "Notes") {
                Button(
                    onClick = { navController.navigate("private_notes") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Notes")
                }
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    // Reusable card component for clean section layout
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
fun ProductivityCircle(percent: Float) {
    // Visual progress bar indicating productivity percentage
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(120.dp)
    ) {
        CircularProgressIndicator(
            progress = percent.coerceIn(0f, 1f),
            strokeWidth = 8.dp,
            modifier = Modifier.fillMaxSize()
        )
        Text(
            "${(percent * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun UpcomingAppointmentsSection(appointments: List<Appointment>) {
    val context = LocalContext.current
    val now = System.currentTimeMillis()

    // Filter upcoming appointments based on current time
    val upcoming = appointments.filter { it.dateTime > now }.sortedBy { it.dateTime }

    SectionCard(title = "📋 Upcoming Appointments") {
        if (upcoming.isEmpty()) {
            Text("No upcoming appointments.", style = MaterialTheme.typography.bodyMedium)
        } else {
            upcoming.forEach { appointment ->
                val date = java.text.SimpleDateFormat("EEE, MMM d 'at' h:mm a", Locale.getDefault())
                    .format(Date(appointment.dateTime))
                Text("• ${appointment.title} — $date", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Share button to export schedule as plain text
            Button(onClick = {
                val shareText = buildString {
                    append("📅 My Upcoming Appointments:\n\n")
                    upcoming.forEach {
                        val formatted = java.text.SimpleDateFormat(
                            "EEE, MMM d 'at' h:mm a",
                            Locale.getDefault()
                        ).format(Date(it.dateTime))
                        append("• ${it.title} — $formatted\n")
                    }
                }

                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, "Share your schedule")
                context.startActivity(shareIntent)
            }) {
                Text("Share All")
            }
        }
    }
}

