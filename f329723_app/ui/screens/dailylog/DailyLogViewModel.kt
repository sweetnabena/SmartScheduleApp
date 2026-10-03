package com.example.f329723_app.ui.screens.dailylog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import androidx.compose.runtime.mutableStateOf
import com.example.f329723_app.data.model.DailyLog
import com.example.f329723_app.data.repository.DailyLogRepositoryFirebase
import java.time.LocalDate
import kotlinx.coroutines.flow.first

// ViewModel for managing the state of the daily log UI screen
@HiltViewModel
class DailyLogViewModel @Inject constructor(
    private val repository: DailyLogRepositoryFirebase // Injecting repository for data access
) : ViewModel() {

    // UI state for mood and productivity sliders (range typically 1–5)
    val mood = mutableStateOf(3)
    val productivity = mutableStateOf(3)

    // Holds the current log entry for today, if one exists
    val currentLog = MutableStateFlow<DailyLog?>(null)

    // Initialiser block to load today's log when ViewModel is created
    init {
        viewModelScope.launch {
            repository.getAllLogs().collect { logs ->
                // Look for a log matching today's date
                val log = logs.find { it.createdOn == LocalDate.now().toString() }
                currentLog.value = log
                log?.let {
                    // If found, populate the UI state
                    mood.value = it.mood
                    productivity.value = it.productivity
                }
            }
        }
    }

    // Saves or updates today's log entry
    fun saveLog() {
        val today = LocalDate.now().toString()

        viewModelScope.launch {
            // Get all logs to check if a log for today already exists
            val existingLogs = repository.getAllLogs().first()
            val existingLog = existingLogs.find { it.createdOn == today }

            // Create a new log or update the existing one
            val newLog = DailyLog(
                id = existingLog?.id ?: "", // Use existing ID if updating
                mood = mood.value,
                productivity = productivity.value,
                createdOn = today
            )

            // Persist the log in Firestore
            repository.insert(newLog)

            // Immediately update the UI state with the saved log
            currentLog.value = newLog
        }
    }
}
