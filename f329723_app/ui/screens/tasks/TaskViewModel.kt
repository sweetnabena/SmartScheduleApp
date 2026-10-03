package com.example.f329723_app.ui.screens.tasks

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.f329723_app.data.model.Task
import com.example.f329723_app.data.repository.TaskRepositoryFirebase
import com.example.f329723_app.data.repository.DailyLogRepositoryFirebase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TaskFilter { ALL, PENDING, COMPLETED }

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepositoryFirebase,
    private val logRepo: DailyLogRepositoryFirebase
) : ViewModel() {

    // StateFlow to track current filter (All, Pending, Completed)
    private val _filter = MutableStateFlow(TaskFilter.ALL)
    val filter: StateFlow<TaskFilter> = _filter

    // Get all tasks as a Flow from repository
    private val allTasksFlow = repository.getAllTasks()

    // Apply filter to task list
    val tasks: StateFlow<List<Task>> = combine(allTasksFlow, _filter) { tasks, filter ->
        println("Filter: $filter | Tasks: ${tasks.map { "${it.content} -> ${it.completed}" }}")
        when (filter) {
            TaskFilter.ALL -> tasks
            TaskFilter.PENDING -> tasks.filter { !it.completed }
            TaskFilter.COMPLETED -> tasks.filter { it.completed }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Update the active task filter
    fun setFilter(filter: TaskFilter) {
        _filter.value = filter
    }

    // Toggle completion status of a task
    fun toggleCompletion(task: Task) {
        viewModelScope.launch {
            repository.update(task.copy(completed = !task.completed))
        }
    }

    // Calculate completion rate for today's tasks
    fun getTodayCompletionRate(): StateFlow<Float> {
        return tasks.map { taskList ->
            val today = LocalDate.now().toString()
            val todayTasks = taskList.filter { task ->
                Log.d("Tasks", "Today: $today vs Task.createdOn: ${task.createdOn}")
                task.createdOn == today
            }
            if (todayTasks.isEmpty()) 0f else todayTasks.count { it.completed }.toFloat() / todayTasks.size
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0f)
    }

    // Create and insert a new task
    fun addTask(content: String) {
        val newTask = Task(
            content = content,
            createdOn = LocalDate.now().toString()
        )
        viewModelScope.launch {
            repository.insert(newTask)
        }
    }

    // Delete a task
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.delete(task)
        }
    }

    // Update an existing task
    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.update(task)
        }
    }

    // Link multiple tasks to an appointment
    fun linkTasksToAppointment(taskIds: List<String>, appointmentId: String) {
        viewModelScope.launch {
            val allTasks = tasks.value
            taskIds.forEach { taskId ->
                val task = allTasks.find { it.id == taskId }
                if (task != null) {
                    val updatedTask = task.copy(linkedAppointmentId = appointmentId)
                    updateTask(updatedTask)
                }
            }
        }
    }

    // Retrieve all tasks linked to a specific appointment
    fun getTasksForAppointment(appointmentId: String): Flow<List<Task>> {
        return repository.getAllTasks().map { tasks ->
            tasks.filter { it.linkedAppointmentId == appointmentId }
        }
    }
}

