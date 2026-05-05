package com.example.trip_planner.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trip_planner.data.local.TripDatabase
import com.example.trip_planner.data.local.entity.TripNoteEntity
import com.example.trip_planner.data.local.entity.TravelMood
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 旅行笔记 ViewModel
 */
class TripNoteViewModel(application: Application) : AndroidViewModel(application) {

    private val database = TripDatabase.getDatabase(application)
    private val noteDao = database.tripNoteDao()

    private val _currentTripId = MutableStateFlow<String?>(null)
    val currentTripId: StateFlow<String?> = _currentTripId.asStateFlow()

    val notes: StateFlow<List<TripNoteEntity>> = _currentTripId
        .filterNotNull()
        .flatMapLatest { tripId ->
            noteDao.getNotesByTrip(tripId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val noteCount: StateFlow<Int> = _currentTripId
        .filterNotNull()
        .flatMapLatest { tripId ->
            noteDao.getNoteCountByTrip(tripId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun getNotesByUser(userId: Long): StateFlow<List<TripNoteEntity>> {
        return noteDao.getNotesByUser(userId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun setTripId(tripId: String) {
        _currentTripId.value = tripId
    }

    fun addNote(
        tripId: String,
        userId: Long = 0,
        title: String,
        content: String,
        date: String,
        location: String,
        mood: String,
        photoPaths: String = "",
        tags: String = ""
    ) {
        viewModelScope.launch {
            val note = TripNoteEntity(
                userId = userId,
                tripId = tripId,
                title = title,
                content = content,
                date = date,
                location = location,
                mood = mood,
                photoPaths = photoPaths,
                tags = tags
            )
            noteDao.insertNote(note)
        }
    }

    fun updateNote(note: TripNoteEntity) {
        viewModelScope.launch {
            noteDao.updateNote(note.copy(timestamp = System.currentTimeMillis()))
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            noteDao.deleteNoteById(noteId)
        }
    }

    fun deleteNotesByTrip(tripId: String) {
        viewModelScope.launch {
            noteDao.deleteNotesByTrip(tripId)
        }
    }
}
