package com.example.f329723_app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.f329723_app.R
import com.example.f329723_app.data.repository.TaskRepositoryFirebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import android.util.Log
import kotlinx.coroutines.tasks.await
import com.example.f329723_app.data.model.Task

// BroadcastReceiver triggered by AlarmManager to show appointment reminders
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        Toast.makeText(context, "ReminderReceiver fired!", Toast.LENGTH_LONG).show()
        Log.d("ReminderReceiver", "Alarm received!")

        // Check if user has enabled notifications in settings
        if (!areNotificationsEnabled(context)) return

        // Retrieve the appointment ID from the intent
        val appointmentId = intent.getStringExtra("appointmentId") ?: return

        // Launch a coroutine to fetch tasks and build a reminder message
        CoroutineScope(Dispatchers.IO).launch {
            val repository = TaskRepositoryFirebase()
            val tasks = repository.getTasksForAppointment(appointmentId)

            val incompleteTasks = tasks.filter { !it.completed } // Only show pending tasks

            val offset = intent.getLongExtra("offset", -1) // Time offset from intent
            val message = if (incompleteTasks.isNotEmpty()) {
                "Reminder: ${offset} minutes left. ${incompleteTasks.size} task(s) still pending!"
            } else if (offset > 0) {
                "Reminder: ${offset} minutes until your appointment."
            } else {
                "You have an upcoming appointment."
            }

            showNotification(context, message) // Display the notification
        }
    }

    // Builds and displays the notification
    private fun showNotification(context: Context, message: String) {
        // Android 13+ requires POST_NOTIFICATIONS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionGranted = context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED

            if (!permissionGranted) {
                // If permission not granted, don't show the notification
                return
            }
        }

        val builder = NotificationCompat.Builder(context, "reminder_channel")
            .setSmallIcon(R.drawable.ic_notification) // App icon for notification
            .setContentTitle("Appointment Reminder")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // High importance for visibility

        NotificationManagerCompat.from(context).notify(1001, builder.build())
    }

    // Reads user's notification preference from SharedPreferences
    fun areNotificationsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("notifications_enabled", true)
    }
}
