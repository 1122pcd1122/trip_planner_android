package com.example.trip_planner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 行程规划实体类
 * 
 * 存储完整的旅行规划数据，包括目的地、天数、酒店、每日行程等
 */
@Entity(tableName = "trip_plans")
data class TripPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val destination: String,
    val days: Int,
    val preferences: String = "",
    val hotelJson: String = "",
    val dayPlansJson: String = "",
    val overallTips: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
