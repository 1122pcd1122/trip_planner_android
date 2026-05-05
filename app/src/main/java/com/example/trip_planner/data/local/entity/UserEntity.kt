package com.example.trip_planner.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 用户实体
 */
@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], name = "idx_user_username", unique = true),
        Index(value = ["email"], name = "idx_user_email", unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val email: String = "",
    val passwordHash: String,
    val avatar: String = "",
    val nickname: String = "",
    val phone: String = "",
    val bio: String = "",
    val gender: Int = 0, // 0: 未知, 1: 男, 2: 女
    val birthday: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = 0
)
