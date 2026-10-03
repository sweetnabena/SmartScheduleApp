package com.example.f329723_app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.content.Context

// Annotation to enable Hilt dependency injection
@HiltAndroidApp
class SmartScheduleApp : Application() {

    // Called when the application is starting
    override fun onCreate() {
        super.onCreate()

        // Create the notification channel for reminders
        createReminderNotificationChannel(this)
    }
}

// Function to create a notification channel for reminder alerts (only needed on Android 8.0+)
fun createReminderNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Define the notification channel properties
        val channel = NotificationChannel(
            "reminder_channel", // Channel ID used throughout the app
            "Reminders",        // User-visible name of the channel
            NotificationManager.IMPORTANCE_HIGH // Importance level (makes sound, appears as heads-up)
        ).apply {
            description = "Reminders for appointments and tasks" // User-visible description
        }

        // Get the system NotificationManager and create the channel
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}

