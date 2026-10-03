package com.example.f329723_app.ui.screens.addappointment

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.f329723_app.ui.screens.tasks.TaskViewModel
import androidx.fragment.app.FragmentActivity
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.*
import java.time.format.DateTimeFormatter
import com.example.f329723_app.data.model.Appointment
import com.example.f329723_app.data.repository.AppointmentRepositoryFirebase
import com.example.f329723_app.notification.ReminderScheduler
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember




@Composable
fun AddEditAppointmentScreen(

    selectedDate: LocalDate,
    onSave: (Appointment, List<String>) -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    taskViewModel: TaskViewModel = hiltViewModel(),
    isEditMode: Boolean = false

) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(selectedDate) }
    val initialTime = remember { LocalTime.now() }
    var time by remember { mutableStateOf(LocalTime.now()) }
    var location by remember { mutableStateOf("") }
    var reminderEnabled by remember { mutableStateOf(false) }
    var reminderOffsets by remember { mutableStateOf(mutableListOf<Long>(30)) }
    var newOffsetText by remember { mutableStateOf("") }
    var repetition by remember { mutableStateOf("None") }
    var selectedOffset by remember { mutableStateOf("30") }
    val allTasks by taskViewModel.tasks.collectAsState()
    val incompleteTasks = remember(allTasks) { allTasks.filter { !it.completed } }
    val selectedTasks = remember { mutableStateListOf<String>() }// stores selected taskids
    val coroutineScope = rememberCoroutineScope()
    val appointmentRepository = remember { AppointmentRepositoryFirebase(context) }
    val snackbarHostState = remember { SnackbarHostState() }



    val repetitionOptions = listOf("None", "Weekly", "Monthly")
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Add Appointment", style = MaterialTheme.typography.headlineSmall)

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(" Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
            DatePickerField(label = "📅 Date", date = date) {
                date = it
            }

            Spacer(Modifier.height(12.dp))
            TimePickerField(label = "⏰ Time", time = time) {
                time = it
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("📍 Location") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Link Tasks:")
            incompleteTasks.forEach { task ->
                val isChecked = selectedTasks.contains(task.id)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { checked ->
                            if (checked) selectedTasks.add(task.id)
                            else selectedTasks.remove(task.id)
                        }
                    )
                    Text(task.content)
                }
            }



            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                Text(" Reminder")
            }

            if (reminderEnabled) {
                val offsetOptions = listOf("10", "20", "30", "40", "50", "60")

                DropdownMenuField(
                    label = "Reminder Time (min before)",
                    options = offsetOptions,
                    selected = selectedOffset,
                    onSelect = { selectedOffset = it }
                )

            }


            Spacer(Modifier.height(12.dp))
            DropdownMenuField(
                label = "🔁 Repeat",
                options = repetitionOptions,
                selected = repetition,
                onSelect = {
                    repetition = it
                    focusManager.clearFocus()
                }
            )

            Spacer(Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(onClick = onCancel) {
                    Text(" Cancel")
                }
                Button(onClick = {
                    if (title.isBlank()) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Please enter a title")
                        }
                        return@Button
                    }

                    coroutineScope.launch {
                        val dateTime = LocalDateTime.of(date, time)
                            .atZone(ZoneId.systemDefault())
                            .toEpochSecond() * 1000

                        val reminderOffsetsFinal =
                            if (reminderEnabled) listOf(selectedOffset.toLong()) else emptyList()


                        val newAppointment = Appointment(
                            title = title,
                            dateTime = dateTime,
                            location = location.ifBlank { null },
                            reminderOffsets = reminderOffsetsFinal,
                            isRecurring = repetition != "None",
                            recurrenceType = if (repetition != "None") repetition else null
                        )


                        val repeatCount = if (repetition != "None") 5 else 1
                        val intervalDays = when (repetition) {
                            "Weekly" -> 7L
                            "Monthly" -> 30L
                            else -> 0L
                        }


                        val allAppointments = mutableListOf<Appointment>()

                        repeat(repeatCount) { i ->
                            val offsetDays = intervalDays * i
                            val repeatTime = LocalDateTime.of(date, time).plusDays(offsetDays)
                                .atZone(ZoneId.systemDefault())
                                .toEpochSecond() * 1000

                            val copy = Appointment(
                                title = title,
                                dateTime = repeatTime,
                                location = location.ifBlank { null },
                                reminderOffsets = reminderOffsetsFinal,
                                isRecurring = repetition != "None"
                            )

                            val insertedId = appointmentRepository.insertAndReturnId(copy)
                            val savedAppointment = copy.copy(id = insertedId)
                            ReminderScheduler.scheduleReminders(context, savedAppointment)
                            taskViewModel.linkTasksToAppointment(selectedTasks, insertedId)

                            allAppointments.add(savedAppointment)
                        }

                        // Return only the first created instance for callback
                        onSave(allAppointments.first(), selectedTasks)
                    }
                }) {
                    Text("✔ Save")
                }
            }
            // Show delete button only in edit mode
            if (isEditMode) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Appointment")
                }
            }
        }
        }

        // reminder test button
//        Spacer(Modifier.height(12.dp))
//        Button(
//            onClick = {
//                val triggerTime = System.currentTimeMillis() + 10_000
//                ReminderScheduler.scheduleReminder(
//                    context = context,
//                    appointmentId = "test123",
//                    triggerAtMillis = triggerTime
//                )
//            },
//            modifier = Modifier.align(Alignment.End)
//        ) {
//            Text("Test Reminder in 10s")
//        }
}

@Composable
fun DatePickerField(label: String, date: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val context = LocalContext.current
    val formattedDate = date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))

    val activity = context as? FragmentActivity

    OutlinedTextField(
        value = formattedDate,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (activity != null) {
                    val picker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Select Date")
                        .setSelection(date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .build()

                    picker.show(activity.supportFragmentManager, "datePicker")

                    picker.addOnPositiveButtonClickListener {
                        val selected = Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(selected)
                    }
                }
            }
    )
}

@Composable
fun TimePickerField(label: String, time: LocalTime, onTimeSelected: (LocalTime) -> Unit) {
    val context = LocalContext.current
    val formattedTime = time.format(DateTimeFormatter.ofPattern("hh:mm a"))

    Box(modifier = Modifier
        .fillMaxWidth()
        .clickable {
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    onTimeSelected(LocalTime.of(hour, minute))
                },
                time.hour,
                time.minute,
                false
            ).show()
        }
    ) {
        OutlinedTextField(
            value = formattedTime,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )
    }
}



@Composable
fun DropdownMenuField(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collectLatest {
            expanded = true
        }
    }

    Box {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            interactionSource = interactionSource,
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                focusManager.clearFocus()
            }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
