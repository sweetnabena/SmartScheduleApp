package com.example.f329723_app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "private_notes")
data class PrivateNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String
)
