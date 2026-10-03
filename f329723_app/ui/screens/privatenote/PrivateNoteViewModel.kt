package com.example.f329723_app.ui.screens.privatenote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.f329723_app.data.repository.PrivateNoteDao
import com.example.f329723_app.data.model.PrivateNote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrivateNoteViewModel @Inject constructor(
    private val dao: PrivateNoteDao
) : ViewModel() {
    val notes = dao.getAllNotes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addNote(content: String) {
        viewModelScope.launch {
            dao.insert(PrivateNote(content = content))
        }
    }

    fun deleteNote(note: PrivateNote) {
        viewModelScope.launch {
            dao.delete(note)
        }
    }
}
