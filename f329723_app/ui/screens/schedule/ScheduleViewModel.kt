package com.example.f329723_app.ui.screens.schedule

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.f329723_app.data.model.Appointment
import com.example.f329723_app.data.repository.AppointmentRepositoryFirebase
import com.example.f329723_app.data.repository.TaskRepositoryFirebase
import com.example.f329723_app.notification.ReminderScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.content.Context
import javax.inject.Inject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: AppointmentRepositoryFirebase, // Firebase repo for appointments
    private val taskRepository: TaskRepositoryFirebase      // Firebase repo for tasks
) : ViewModel() {

    // Holds the list of appointments fetched from Firebase
    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    init {
        // Collect appointments from Firebase in a coroutine
        viewModelScope.launch {
            repository.getAllAppointments()
                .catch { e -> e.printStackTrace() } // Log any errors during collection
                .collect { fetched ->
                    _appointments.value = fetched // Update local state with fetched data
                }
        }
    }

    // Filters appointments to match a specific calendar date
    @RequiresApi(Build.VERSION_CODES.O)
    fun getAppointmentsForDate(date: LocalDate): List<Appointment> {
        return appointments.value.filter {
            val apptDate = Instant.ofEpochMilli(it.dateTime)
                .atZone(ZoneId.systemDefault()).toLocalDate()
            apptDate == date // Check if appointment is on the target date
        }
    }

    // Saves a new or updated appointment and links selected tasks to it
    fun saveAppointment(
        appointment: Appointment,
        selectedTaskIds: List<String>,
        context: Context
    ) {
        viewModelScope.launch {
            // Insert or update the appointment in Firebase
            repository.insert(appointment)

            // For each selected task, link it to this appointment
            selectedTaskIds.forEach { taskId ->
                taskRepository.updateTaskWithAppointment(taskId, appointment.id)
            }

            // Schedule reminders if any reminder offset is set
            if (appointment.reminderOffsets.isNotEmpty()) {
                ReminderScheduler.scheduleReminders(
                    context = context,
                    appointment = appointment
                )
            }
        }
    }

    // Holds the currently loaded appointment (for editing)
    var currentEditingAppointment: Appointment? by mutableStateOf(null)
        private set

    // Sets an appointment into the editing state
    fun setEditingAppointment(appointment: Appointment) {
        currentEditingAppointment = appointment
    }

    // Deletes an appointment from the repository
    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            repository.delete(appointment)
        }
    }

    // Loads an appointment by its ID and stores it for editing
    fun loadAppointmentById(appointmentId: String) {
        viewModelScope.launch {
            repository.getAppointmentById(appointmentId)?.let {
                currentEditingAppointment = it
            }
        }
    }
}

