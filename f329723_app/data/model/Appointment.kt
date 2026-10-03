package com.example.f329723_app.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Appointment(
    @DocumentId
    val id: String = "", // where the Firestore document ID goes

    val title: String = "",
    val dateTime: Long = 0L,
    val location: String? = null,
    val reminderOffsets: List<Long> = emptyList(),
    val isRecurring: Boolean = false,
    val recurrenceType: String? = null
)

