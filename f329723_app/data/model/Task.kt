package com.example.f329723_app.data.model

import java.time.LocalDate

data class Task(
    var id: String = "",
    val content: String = "",
    val completed: Boolean = false,
    val linkedAppointmentId: String? = null,
    val createdOn: String = LocalDate.now().toString()
)
