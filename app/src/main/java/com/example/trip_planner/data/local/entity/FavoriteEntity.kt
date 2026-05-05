package com.example.trip_planner.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 收藏类型枚举
 */
enum class FavoriteType(val displayName: String) {
    HOTEL("酒店"),
    RESTAURANT("餐厅"),
    ATTRACTION("景点"),
    TRIP_PLAN("行程规划")
}

/**
 * 收藏实体类
 *
 * 对应数据库中的收藏表，存储用户收藏的各种数据
 */
@Entity(
    tableName = "favorites",
    indices = [
        Index(value = ["type"], name = "idx_favorite_type"),
        Index(value = ["timestamp"], name = "idx_favorite_timestamp"),
        Index(value = ["itemId"], name = "idx_favorite_item_id")
    ]
)
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
    val extraData: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
