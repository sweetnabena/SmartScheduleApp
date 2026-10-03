package com.example.f329723_app.data.repository

import android.database.Cursor
import androidx.room.*
import com.example.f329723_app.data.model.PrivateNote
import kotlinx.coroutines.flow.Flow
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Query


@Dao
interface PrivateNoteDao {
    @Query("SELECT * FROM private_notes")
    fun getAllNotes(): Flow<List<PrivateNote>>

    @Insert
    suspend fun insert(note: PrivateNote)

    @Delete
    suspend fun delete(note: PrivateNote)

    @Query("DELETE FROM private_notes WHERE id = :noteId")
    fun deleteById(noteId: Long): Int

    @Insert
    fun insertRaw(note: PrivateNote): Long

    @Query("SELECT id as _id, content FROM private_notes")
    fun getAllNotesCursor(): Cursor




}
