package com.example.f329723_app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.f329723_app.data.model.PrivateNote
import com.example.f329723_app.data.repository.PrivateNoteDao

@Database(entities = [PrivateNote::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun privateNoteDao(): PrivateNoteDao
}
