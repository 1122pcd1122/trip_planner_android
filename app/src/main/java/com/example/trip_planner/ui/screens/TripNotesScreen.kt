package com.example.trip_planner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trip_planner.data.local.entity.TripNoteEntity
import com.example.trip_planner.ui.components.TripNotesSection
import com.example.trip_planner.ui.theme.LocalAppColors
import com.example.trip_planner.viewModel.TripNoteViewModel

/**
 * 旅行笔记页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripNotesScreen(
    tripId: String,
    tripName: String = "",
    userId: Long = 0,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    noteViewModel: TripNoteViewModel = viewModel()
) {
    val appColors = LocalAppColors.current
    val notes by (if (tripId.isNotEmpty()) noteViewModel.notes else noteViewModel.getNotesByUser(userId)).collectAsState()
    var showEditor by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<TripNoteEntity?>(null) }

    LaunchedEffect(tripId) {
        if (tripId.isNotEmpty()) {
            noteViewModel.setTripId(tripId)
        }
    }

    if (showEditor || editingNote != null) {
        NoteEditorScreen(
            tripId = tripId,
            userId = userId,
            initialNote = editingNote,
            onBack = {
                showEditor = false
                editingNote = null
            },
            onSave = { title, content, date, location, mood, photos, tags, noteUserId ->
                if (editingNote != null) {
                    noteViewModel.updateNote(
                        editingNote!!.copy(
                            title = title,
                            content = content,
                            date = date,
                            location = location,
                            mood = mood,
                            photoPaths = photos,
                            tags = tags
                        )
                    )
                } else {
                    noteViewModel.addNote(tripId, noteUserId, title, content, date, location, mood, photos, tags)
                }
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("旅行笔记", fontSize = 16.sp)
                            if (tripName.isNotEmpty()) {
                                Text(tripName, fontSize = 11.sp, color = appColors.textSecondary)
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = appColors.softBackground)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(appColors.softBackground)
            ) {
                TripNotesSection(
                    notes = notes,
                    onNavigateToAdd = { showEditor = true },
                    onNavigateToEdit = { note -> editingNote = note },
                    onDeleteNote = { noteId ->
                        noteViewModel.deleteNote(noteId)
                    },
                    onUpdateNote = { note ->
                        noteViewModel.updateNote(note)
                    },
                    appColors = appColors,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
