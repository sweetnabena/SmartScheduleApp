package com.example.f329723_app.ui.screens.schedule

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.widget.CalendarView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi // only if needed
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import kotlinx.datetime.*
import java.time.DayOfWeek
import androidx.compose.runtime.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.example.f329723_app.ui.screens.schedule.HighlightedCalendar
import kotlinx.datetime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.*
import com.kizitonwose.calendar.core.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.f329723_app.ui.screens.schedule.ScheduleViewModel
import com.example.f329723_app.data.model.Task
import com.example.f329723_app.ui.screens.tasks.TaskViewModel
import kotlinx.coroutines.flow.collectLatest
import androidx.navigation.NavController
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime
import com.example.f329723_app.R
import com.example.f329723_app.data.model.Appointment
import java.text.SimpleDateFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreenContent(
    navController: NavController,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // State to track currently selected calendar date
    var selectedDate by remember {
        mutableStateOf(
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        )
    }

    // Collect appointments from ViewModel
    val allAppointments by viewModel.appointments.collectAsState()

    // Snackbar to display UI messages
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Highlighted dates on calendar based on existing appointments
    val markedDates = remember(allAppointments) {
        allAppointments.map {
            Instant.fromEpochMilliseconds(it.dateTime)
                .toLocalDateTime(TimeZone.currentSystemDefault()).date
        }.toSet()
    }

    // Filtered list of appointments matching the selected date
    val appointmentsForSelectedDate = remember(allAppointments, selectedDate) {
        allAppointments.filter {
            val apptDate = Instant.fromEpochMilliseconds(it.dateTime)
                .toLocalDateTime(TimeZone.currentSystemDefault()).date
            apptDate == selectedDate
        }
    }

    // Layout scaffold including FAB
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("addEditAppointment/${selectedDate}")
                Toast.makeText(context, "FAB clicked", Toast.LENGTH_SHORT).show()
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Event")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Display selected month and year
            Text(
                text = "${selectedDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${selectedDate.year}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Calendar with highlighted appointment days
            Box(modifier = Modifier.height(360.dp)) {
                HighlightedCalendar(
                    markedDates = markedDates.map {
                        java.time.LocalDate.of(it.year, it.monthNumber, it.dayOfMonth)
                    }.toSet(),
                    onDateClick = {
                        selectedDate = kotlinx.datetime.LocalDate(it.year, it.monthValue, it.dayOfMonth)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display list of appointments or message
            if (appointmentsForSelectedDate.isEmpty()) {
                Text("No appointments on this day.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(appointmentsForSelectedDate) { appointment ->
                        AppointmentCard(
                            appointment = appointment,
                            viewModel = viewModel,
                            snackbarHostState = snackbarHostState,
                            coroutineScope = coroutineScope
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppointmentCard(
    appointment: Appointment,
    viewModel: ScheduleViewModel,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    taskViewModel: TaskViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // Control expansion of task view
    var expanded by remember { mutableStateOf(false) }

    // Hold list of linked tasks
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }

    // Load tasks when appointment ID changes
    LaunchedEffect(appointment.id) {
        taskViewModel.getTasksForAppointment(appointment.id).collectLatest {
            tasks = it
        }
    }

    // Card layout to show appointment details
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {

            // Delete appointment icon
            IconButton(
                onClick = {
                    viewModel.deleteAppointment(appointment)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Appointment deleted")
                    }
                },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }

            // Appointment main details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(appointment.title, style = MaterialTheme.typography.titleMedium)

                // Format and show appointment time
                val formatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
                val timeString = formatter.format(Date(appointment.dateTime))
                Text("Time: $timeString")

                // Expanded content including location, reminders, and tasks
                AnimatedVisibility(visible = expanded) {
                    Column {
                        // Show location and launch Google Maps
                        appointment.location?.let { location ->
                            Text(
                                text = "📍 $location",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.clickable {
                                    val gmmIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("geo:0,0?q=${Uri.encode(location)}")
                                    )
                                    gmmIntent.setPackage("com.google.android.apps.maps")
                                    context.startActivity(gmmIntent)
                                }
                            )
                        }

                        // Reminder count
                        Text("🔔 Reminders: ${if (appointment.reminderOffsets.isNotEmpty()) "${appointment.reminderOffsets.size} active" else "None"}")

                        // Show tasks linked to the appointment
                        if (tasks.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("📝 Tasks:", style = MaterialTheme.typography.titleSmall)
                            tasks.forEach { task ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = task.completed,
                                        onCheckedChange = {
                                            taskViewModel.toggleCompletion(task)
                                        }
                                    )
                                    Text(task.content)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}






