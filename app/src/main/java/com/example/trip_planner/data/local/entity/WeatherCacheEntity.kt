package com.example.trip_planner.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 天气缓存实体
 * 用于缓存目的地的天气数据，减少重复请求
 */
@Entity(
    tableName = "weather_cache",
    indices = [
        Index(value = ["cityName", "date"], unique = true, name = "idx_weather_city_date")
    ]
)
data class WeatherCacheEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cityName: String,
    val date: String,
    val weather: String,
    val temperature: String,
    val tips: String,
    val timestamp: Long = System.currentTimeMillis()
)
