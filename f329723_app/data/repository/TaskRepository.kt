package com.example.f329723_app.data.repository

import com.example.f329723_app.data.model.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Repository for managing Task data in Firebase Firestore
class TaskRepositoryFirebase @Inject constructor() {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("tasks") // Reference to the "tasks" collection

    // Returns a real-time Flow of all tasks, ordered by ID
    fun getAllTasks(): Flow<List<Task>> = callbackFlow {
        val listener = collection.orderBy("id").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                close(error) // Close Flow if error occurs
                return@addSnapshotListener
            }

            // Convert each document into a Task object, including the Firestore document ID
            val tasks = snapshot.documents.mapNotNull { doc ->
                doc.toObject<Task>()?.copy(id = doc.id)
            }

            trySend(tasks) // Emit the list of tasks
        }

        awaitClose { listener.remove() } // Stop listening when Flow is closed
    }

    // Fetches all tasks linked to a specific appointment by its ID
    suspend fun getTasksForAppointment(appointmentId: String): List<Task> {
        return try {
            val snapshot = collection
                .whereEqualTo("linkedAppointmentId", appointmentId) // Filter by foreign key
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject<Task>()?.copy(id = it.id) }
        } catch (e: Exception) {
            emptyList() // Return empty list on error
        }
    }

    // Inserts a new task or updates an existing one if ID is provided
    suspend fun insert(task: Task) {
        if (task.id.isNotEmpty()) {
            // Update existing task
            collection.document(task.id).set(task)
        } else {
            // Add new task and let Firestore assign an ID
            val docRef = collection.add(task).await()
        }
    }

    // Updates a specific task by linking it to an appointment using appointmentId
    suspend fun updateTaskWithAppointment(taskId: String, appointmentId: String) {
        val snapshot = collection.document(taskId).get().await()
        val task = snapshot.toObject(Task::class.java)?.copy(id = taskId)
        if (task != null) {
            val updated = task.copy(linkedAppointmentId = appointmentId) // Set appointment reference
            collection.document(taskId).set(updated).await() // Save updated task
        }
    }

    // Updates a task by overwriting its document in Firestore
    suspend fun update(task: Task) {
        if (task.id.isNotEmpty()) {
            collection.document(task.id).set(task)
        }
    }

    // Deletes a task from Firestore
    suspend fun delete(task: Task) {
        if (task.id.isNotEmpty()) {
            collection.document(task.id).delete()
        }
    }
}
