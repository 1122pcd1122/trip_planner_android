package com.example.trip_planner.data.local.dao

import androidx.room.*
import com.example.trip_planner.data.local.entity.WeatherCacheEntity
import kotlinx.coroutines.flow.Flow

/**
 * 天气缓存 DAO
 * 提供天气缓存的数据库操作方法
 */
@Dao
interface WeatherCacheDao {

    @Query("SELECT * FROM weather_cache WHERE cityName = :cityName ORDER BY date ASC")
    fun getWeatherByCity(cityName: String): Flow<List<WeatherCacheEntity>>

    @Query("SELECT * FROM weather_cache WHERE cityName = :cityName AND date = :date LIMIT 1")
    suspend fun getWeatherByCityAndDate(cityName: String, date: String): WeatherCacheEntity?

    @Query("SELECT * FROM weather_cache WHERE cityName = :cityName AND timestamp > :validTime")
    suspend fun getValidWeather(cityName: String, validTime: Long): List<WeatherCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherCacheEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherList(weatherList: List<WeatherCacheEntity>)

    @Query("DELETE FROM weather_cache WHERE cityName = :cityName")
    suspend fun deleteWeatherByCity(cityName: String)

    @Query("DELETE FROM weather_cache WHERE timestamp < :expireTime")
    suspend fun deleteExpiredWeather(expireTime: Long)

    @Query("DELETE FROM weather_cache")
    suspend fun clearAll()
}
