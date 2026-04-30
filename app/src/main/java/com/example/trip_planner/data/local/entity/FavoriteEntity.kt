package com.example.trip_planner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 收藏实体类
 * 
 * 对应数据库中的收藏表，存储用户收藏的酒店、景点、餐厅等数据
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemId: String,
    val type: String,
    val name: String,
    val rating: String = "",
    val price: String = "",
    val address: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
