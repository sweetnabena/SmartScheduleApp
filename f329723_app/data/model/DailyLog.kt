package com.example.f329723_app.data.model

import java.time.LocalDate

data class DailyLog(
    val id: String = "",
    val createdOn: String = LocalDate.now().toString(),
    val mood: Int = 0,
    val productivity: Int = 0
)
