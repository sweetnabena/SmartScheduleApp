package com.example.f329723_app.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.f329723_app.data.model.Appointment
import com.example.f329723_app.notification.ReminderReceiver
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Repository to manage appointment data using Firebase Firestore
class AppointmentRepositoryFirebase @Inject constructor(
    private val context: Context
) {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("appointments")

    // Returns a Flow of all appointments, updated in real-time via snapshot listener
    fun getAllAppointments(): Flow<List<Appointment>> = callbackFlow {
        val listener = collection.orderBy("dateTime").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                close(error) // Closes the Flow if error occurs
                return@addSnapshotListener
            }

            val appointments = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Appointment::class.java) // Deserialize Firestore documents
            }

            trySend(appointments) // Emits appointments to the Flow
        }

        awaitClose { listener.remove() } // Stops listening when Flow is cancelled
    }

    // Inserts or updates an appointment, and schedules reminders
    fun insert(appointment: Appointment) {
        if (appointment.id.isNotEmpty()) {
            // Update existing appointment
            val appointmentData = appointment.copy(id = "") // Avoid duplicating Firestore doc ID in fields
            collection.document(appointment.id)
                .set(appointmentData)
                .addOnSuccessListener {
                    Log.d("Firestore", "Updated appointment: ${appointment.id}")
                    scheduleReminder(appointment) // Set reminders after successful update
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to update appointment", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Create a new appointment document
            val appointmentData = appointment.copy(id = "")
            collection.add(appointmentData)
                .addOnSuccessListener { docRef ->
                    Log.d("Firestore", "Saved new appointment: ${docRef.id}")
                    val newAppt = appointment.copy(id = docRef.id)
                    scheduleReminder(newAppt) // Set reminders after save
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to save appointment", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Deletes an appointment document by ID
    fun delete(appointment: Appointment) {
        if (appointment.id.isNotEmpty()) {
            collection.document(appointment.id).delete()
        }
    }

    // Suspended function to fetch a single appointment by ID
    suspend fun getAppointmentById(appointmentId: String): Appointment? {
        return try {
            val snapshot = collection.document(appointmentId).get().await()
            snapshot.toObject(Appointment::class.java)
        } catch (e: Exception) {
            Log.e("Firestore", "Failed to fetch appointment", e)
            null
        }
    }

    // Inserts an appointment and returns the generated Firestore document ID
    suspend fun insertAndReturnId(appointment: Appointment): String {
        if (appointment.recurrenceType == "Daily") {
            Toast.makeText(context, "Daily repetition is not supported", Toast.LENGTH_SHORT).show()
            return ""
        }

        val docRef = collection.add(appointment.copy(id = "")).await()
        return docRef.id
    }

    // Schedules reminders using AlarmManager for each configured offset
    private fun scheduleReminder(appointment: Appointment) {
        appointment.reminderOffsets.forEach { offset ->
            val reminderTime = appointment.dateTime - offset * 60 * 1000 // Calculate time before appointment

            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("appointmentId", appointment.id)
                putExtra("offset", offset)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                (appointment.id + offset.toString()).hashCode(), // Ensure unique request code
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent) // Schedule alarm
        }
    }
}
