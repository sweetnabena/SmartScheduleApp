package com.example.f329723_app.ui.screens.privatenote

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.material.icons.filled.ArrowBack


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivateNoteScreen(
    navController: NavController,
    viewModel: PrivateNoteViewModel = hiltViewModel() // ViewModel injected using Hilt
) {
    val notes by viewModel.notes.collectAsState() // Observing the list of notes
    val context = LocalContext.current // Getting context for share intent
    var newNote by remember { mutableStateOf("") } // State for new note input

    // Scaffold provides structure with top app bar
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notes") },
                navigationIcon = {
                    // Back navigation icon to return to previous screen
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        // Main content column
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Text input field for new note
            OutlinedTextField(
                value = newNote,
                onValueChange = { newNote = it },
                label = { Text("Add Note") },
                modifier = Modifier.fillMaxWidth()
            )

            // Button to save the note and clear the input
            Button(onClick = {
                viewModel.addNote(newNote)
                newNote = ""
            }) {
                Text("Save Note")
            }

            Spacer(Modifier.height(16.dp))

            // Display each saved note inside a card
            notes.forEach { note ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        // Display the content of the note
                        Text(note.content)

                        // Action row: Share and Delete icons
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Share button
                            IconButton(onClick = {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    putExtra(Intent.EXTRA_TEXT, note.content)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share via"))
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "Share")
                            }

                            // Delete button
                            IconButton(onClick = { viewModel.deleteNote(note) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Note",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


