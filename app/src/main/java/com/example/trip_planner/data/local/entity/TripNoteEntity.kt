package com.example.trip_planner.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 旅行笔记实体
 * 用于记录旅行中的照片和文字笔记
 */
@Entity(
    tableName = "trip_notes",
    indices = [
        Index(value = ["tripId"], name = "idx_note_trip"),
        Index(value = ["date"], name = "idx_note_date"),
        Index(value = ["timestamp"], name = "idx_note_timestamp"),
        Index(value = ["userId"], name = "idx_note_user")
    ]
)
data class TripNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 0,
    val tripId: String,
    val title: String = "",
    val content: String = "",
    val photoPaths: String = "",
    val date: String = "",
    val location: String = "",
    val mood: String = "",
    val tags: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 心情枚举
 */
enum class TravelMood(val emoji: String, val label: String) {
    HAPPY("😊", "开心"),
    EXCITED("🤩", "兴奋"),
    RELAXED("😌", "放松"),
    TIRED("😴", "疲惫"),
    SURPRISED("😲", "惊喜"),
    NORMAL("😐", "一般")
}
