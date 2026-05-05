package com.example.trip_planner.data.local.dao

import androidx.room.*
import com.example.trip_planner.data.local.entity.TripNoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * 旅行笔记 DAO
 */
@Dao
interface TripNoteDao {

    @Query("SELECT * FROM trip_notes WHERE tripId = :tripId ORDER BY timestamp DESC")
    fun getNotesByTrip(tripId: String): Flow<List<TripNoteEntity>>

    @Query("SELECT * FROM trip_notes WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotesByUser(userId: Long): Flow<List<TripNoteEntity>>

    @Query("SELECT * FROM trip_notes WHERE tripId = :tripId AND date = :date ORDER BY timestamp DESC")
    fun getNotesByDate(tripId: String, date: String): Flow<List<TripNoteEntity>>

    @Query("SELECT * FROM trip_notes WHERE id = :id")
    suspend fun getNoteById(id: Long): TripNoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: TripNoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<TripNoteEntity>)

    @Update
    suspend fun updateNote(note: TripNoteEntity)

    @Delete
    suspend fun deleteNote(note: TripNoteEntity)

    @Query("DELETE FROM trip_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    @Query("DELETE FROM trip_notes WHERE tripId = :tripId")
    suspend fun deleteNotesByTrip(tripId: String)

    @Query("SELECT COUNT(*) FROM trip_notes WHERE tripId = :tripId")
    fun getNoteCountByTrip(tripId: String): Flow<Int>

    @Query("SELECT * FROM trip_notes ORDER BY timestamp DESC")
    suspend fun getAllNotesSync(): List<TripNoteEntity>

    @Query("DELETE FROM trip_notes")
    suspend fun deleteAllNotes()
}
