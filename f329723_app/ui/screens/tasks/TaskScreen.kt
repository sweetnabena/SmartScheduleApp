package com.example.f329723_app.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.Alignment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.List

import androidx.compose.material3.ListItem
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.example.f329723_app.data.model.Task
import com.example.f329723_app.ui.screens.tasks.TaskViewModel
import com.example.f329723_app.ui.screens.tasks.TaskFilter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreenContent(viewModel: TaskViewModel = hiltViewModel()) {
    // Observe task list and filter from ViewModel
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsState()
    var newTaskText by remember { mutableStateOf("") } // For new task input

    Scaffold(
        topBar = {
            // Top app bar with title
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Task & Notes",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        // FAB to add new task
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (newTaskText.isNotBlank()) {
                    viewModel.addTask(newTaskText)
                    newTaskText = ""
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Task filter toggle buttons
            item {
                FilterToggle(filter = filter, onFilterChange = viewModel::setFilter)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Input field for new task
            item {
                OutlinedTextField(
                    value = newTaskText,
                    onValueChange = { newTaskText = it },
                    label = { Text("New Task") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Render each task in a Card
            items(tasks) { task ->
                TaskItem(
                    task = task,
                    onDelete = { viewModel.deleteTask(task) },
                    onToggleComplete = { viewModel.toggleCompletion(task) }
                )
            }
        }
    }

    // Debug print for visible tasks
    LaunchedEffect(tasks, filter) {
        println("Visible tasks (${filter.name}): ${tasks.map { it.content + " (" + it.completed + ")" }}")
    }
}

@Composable
fun FilterToggle(filter: TaskFilter, onFilterChange: (TaskFilter) -> Unit) {
    // Row with 3 filter buttons: All, Pending, Completed
    println("Current Filter: $filter")
    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
        FilterButton("All", filter == TaskFilter.ALL) { onFilterChange(TaskFilter.ALL) }
        FilterButton("Pending", filter == TaskFilter.PENDING) { onFilterChange(TaskFilter.PENDING) }
        FilterButton("Completed", filter == TaskFilter.COMPLETED) { onFilterChange(TaskFilter.COMPLETED) }
    }
}

@Composable
fun FilterButton(text: String, selected: Boolean, onClick: () -> Unit) {
    // Filter buttons change background color when selected
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
        )
    ) {
        Text(text)
    }
}

@Composable
fun TaskItem(
    task: Task,
    onDelete: () -> Unit,
    onToggleComplete: () -> Unit
) {
    // Task Card with checkbox and delete button
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = task.completed,
                    onCheckedChange = { onToggleComplete() }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = task.content)
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Task",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}








