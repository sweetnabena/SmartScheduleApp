package com.example.f329723_app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.f329723_app.data.repository.AppointmentRepositoryFirebase
import com.example.f329723_app.data.repository.DailyLogRepositoryFirebase
import com.example.f329723_app.data.repository.TaskRepositoryFirebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appointmentRepo: AppointmentRepositoryFirebase,
    private val logRepo: DailyLogRepositoryFirebase,
    private val taskRepo: TaskRepositoryFirebase
) : ViewModel() {

    // Collects and exposes all appointments from Firebase as a StateFlow
    val appointments = appointmentRepo.getAllAppointments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Collects all daily logs as StateFlow
    val logs = logRepo.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Collects all tasks as StateFlow
    val tasks = taskRepo.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Individual Scores ---

    // Calculates the percentage of today's tasks marked as completed
    val taskCompletionToday = tasks.map { taskList ->
        val today = LocalDate.now().toString()
        val todayTasks = taskList.filter { it.createdOn == today }

        println("DEBUG: Today's tasks = $todayTasks")

        val completion = if (todayTasks.isEmpty()) 0f
        else todayTasks.count { it.completed } / todayTasks.size.toFloat()

        println("DEBUG: Task completion rate = $completion")

        completion
    }

    // Extracts today's mood score from logs and normalises it to a value between 0 and 1
    val moodToday = logs.map { logList ->
        val todayLog = logList.find { it.createdOn == LocalDate.now().toString() }
        (todayLog?.mood ?: 0) / 5f
    }

    // Extracts today's productivity score from logs and normalises it to a value between 0 and 1
    val productivityRatingToday = logs.map { logList ->
        val todayLog = logList.find { it.createdOn == LocalDate.now().toString() }
        (todayLog?.productivity ?: 0) / 5f
    }

    // Calculates a score based on how many appointments are scheduled for today
    // Normalised against a target of 4 appointments per day
    val appointmentScoreToday = appointments.map { list ->
        val today = LocalDate.now()
        val todayAppointments = list.filter { appointment ->
            val date = java.time.Instant.ofEpochMilli(appointment.dateTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            date == today
        }
        val targetAppointmentsPerDay = 4f
        (todayAppointments.size / targetAppointmentsPerDay).coerceIn(0f, 1f)
    }

    // --- Combined Productivity Score ---

    // Combines task completion, mood, productivity rating, and appointment count into a single average score
    // Represents overall productivity for today on a 0 to 1 scale
    val todayProductivity: StateFlow<Float> = combine(
        taskCompletionToday,
        moodToday,
        productivityRatingToday,
        appointmentScoreToday
    ) { taskScore, moodScore, prodRatingScore, appointmentScore ->
        listOf(taskScore, moodScore, prodRatingScore, appointmentScore).average().toFloat()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)
}
