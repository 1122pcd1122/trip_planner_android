package com.example.trip_planner.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 打包清单项实体
 */
@Entity(
    tableName = "packing_items",
    indices = [
        Index(value = ["tripId"], name = "idx_packing_trip"),
        Index(value = ["category"], name = "idx_packing_category"),
        Index(value = ["userId"], name = "idx_packing_user")
    ]
)
data class PackingItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 0,
    val tripId: String,
    val name: String,
    val category: String = "其他",
    val tags: String = "",
    val isPacked: Boolean = false,
    val quantity: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 打包类别（已废弃，改为用户自定义）
 */
enum class PackingCategory(val label: String, val icon: String) {
    CLOTHING("衣物", "👕"),
    TOILETRIES("洗漱", "🧴"),
    ELECTRONICS("电子", "🔌"),
    DOCUMENTS("证件", "📄"),
    MEDICINE("药品", "💊"),
    FOOD("食品", "🍎"),
    OTHER("其他", "📦")
}
