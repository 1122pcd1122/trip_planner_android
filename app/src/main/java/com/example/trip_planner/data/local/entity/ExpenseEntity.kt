package com.example.trip_planner.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 费用记录实体
 * 用于跟踪旅行预算和实际花费
 */
@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["tripId"], name = "idx_expense_trip"),
        Index(value = ["category"], name = "idx_expense_category"),
        Index(value = ["timestamp"], name = "idx_expense_timestamp"),
        Index(value = ["userId"], name = "idx_expense_user")
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 0,
    val tripId: String,
    val category: String = "其他",
    val amount: Double,
    val description: String = "",
    val tags: String = "",
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
