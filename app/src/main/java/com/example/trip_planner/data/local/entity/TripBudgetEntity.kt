package com.example.trip_planner.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 行程预算实体
 * 用于设置和跟踪每个行程的总预算
 */
@Entity(
    tableName = "trip_budgets",
    indices = [
        Index(value = ["tripId"], name = "idx_budget_trip", unique = true)
    ]
)
data class TripBudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tripId: String,
    val totalBudget: Double,
    val currency: String = "¥",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
