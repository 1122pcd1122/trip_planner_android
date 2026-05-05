package com.example.trip_planner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "preference_tags")
data class PreferenceTagEntity(
    @PrimaryKey
    val id: String,
    val label: String,
    val icon: String,
    val keywords: String,
    val category: String = "general"
)
