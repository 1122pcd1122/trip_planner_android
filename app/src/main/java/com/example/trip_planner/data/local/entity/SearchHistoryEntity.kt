package com.example.trip_planner.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 搜索历史实体
 * 
 * 存储用户的搜索记录，方便快速重新搜索
 */
@Entity(
    tableName = "search_history",
    indices = [
        Index(value = ["query"], name = "idx_search_query"),
        Index(value = ["timestamp"], name = "idx_search_timestamp")
    ]
)
data class SearchHistoryEntity(
    /** 自增主键 */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** 搜索关键词（目的地） */
    val query: String,
    /** 搜索时间戳 */
    val timestamp: Long = System.currentTimeMillis()
)
