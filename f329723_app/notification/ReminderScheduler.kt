package com.example.f329723_app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.example.f329723_app.data.model.Appointment

// Utility object responsible for scheduling alarm-based reminders
object ReminderScheduler {

    // Schedules a one-time reminder alarm at the specified time
    fun scheduleReminder(
        context: Context,
        appointmentId: String,
        triggerAtMillis: Long
    ): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // On Android 12+ (S), check if the app is allowed to set exact alarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(
                    context,
                    "Exact alarms are disabled. Enable them in system settings.",
                    Toast.LENGTH_LONG
                ).show()

                // Open settings page to request exact alarm permission
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(context, intent, null)

                return false // Permission not granted; abort scheduling
            }
        }

        // Intent to trigger the ReminderReceiver with appointment ID
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("appointmentId", appointmentId)
        }

        // Create a unique PendingIntent using appointment ID
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appointmentId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return try {
            // Schedule the alarm to fire exactly, even while idle
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            true
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(
                context,
                "Could not schedule alarm due to missing permission.",
                Toast.LENGTH_LONG
            ).show()
            false
        }
    }

    // Schedules multiple reminders based on a list of offsets for a given appointment
    fun scheduleReminders(context: Context, appointment: Appointment) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Android 12+ requires user approval to schedule exact alarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            Toast.makeText(
                context,
                "Cannot schedule reminders: exact alarms are disabled.",
                Toast.LENGTH_LONG
            ).show()

            // Launch system intent to allow user to enable exact alarms
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(context, intent, null)
            return
        }

        // Loop through each reminder offset (in minutes) and schedule an alarm
        appointment.reminderOffsets.forEach { offset ->
            val reminderTime = appointment.dateTime - offset * 60 * 1000 // Convert to milliseconds

            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("appointmentId", appointment.id)
                putExtra("offset", offset)
            }

            // Create a unique PendingIntent using appointment ID and offset
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                (appointment.id + offset.toString()).hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            try {
                // Schedule exact alarm at calculated reminder time
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
            } catch (e: SecurityException) {
                e.printStackTrace()
                Toast.makeText(
                    context,
                    "Could not set reminder due to missing permission.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
