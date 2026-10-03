package com.example.f329723_app.data.repository

import com.example.f329723_app.data.model.DailyLog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Repository class for managing DailyLog entries stored in Firebase Firestore
class DailyLogRepositoryFirebase @Inject constructor() {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("dailyLogs") // Reference to "dailyLogs" collection

    // Returns a Flow of all daily logs, kept in sync using a Firestore snapshot listener
    fun getAllLogs(): Flow<List<DailyLog>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                trySend(emptyList()) // Emit empty list if there's an error or no data
                return@addSnapshotListener
            }

            // Map documents to DailyLog objects, include Firestore document ID
            val logs = snapshot.documents.mapNotNull { doc ->
                doc.toObject(DailyLog::class.java)?.copy(id = doc.id)
            }
            trySend(logs) // Emit the list of logs
        }

        awaitClose { listener.remove() } // Stop listening when the flow is closed
    }

    // Inserts or updates a daily log in Firestore
    suspend fun insert(log: DailyLog) {
        if (log.id.isNotEmpty()) {
            // If log has an ID, update the existing document
            collection.document(log.id).set(log).await()
        } else {
            // If no ID, add a new document and update it with the generated ID
            val docRef = collection.add(log).await()
            val newLogWithId = log.copy(id = docRef.id)
            collection.document(docRef.id).set(newLogWithId).await()
        }
    }

    // Deletes a daily log from Firestore by ID
    suspend fun delete(log: DailyLog) {
        if (log.id.isNotEmpty()) {
            collection.document(log.id).delete()
        }
    }
}
