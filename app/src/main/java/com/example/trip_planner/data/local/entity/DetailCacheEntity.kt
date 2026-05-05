package com.example.trip_planner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 详情缓存实体
 * 
 * 存储酒店、景点、餐厅的详情数据，用于离线访问
 * 避免重复网络请求，提升用户体验
 */
@Entity(tableName = "detail_cache")
data class DetailCacheEntity(
    /** 缓存键，格式：类型_名称 */
    @PrimaryKey
    val cacheKey: String,
    /** 数据类型：hotel/attraction/restaurant */
    val type: String,
    /** 数据名称 */
    val name: String,
    /** 缓存的 JSON 数据 */
    val jsonData: String,
    /** 缓存时间戳，用于过期判断 */
    val timestamp: Long = System.currentTimeMillis()
)
