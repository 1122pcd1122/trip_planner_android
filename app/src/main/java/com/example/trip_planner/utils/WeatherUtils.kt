package com.example.trip_planner.utils

import android.util.Log
import com.example.trip_planner.data.local.entity.WeatherCacheEntity
import com.example.trip_planner.network.TripApiService
import com.example.trip_planner.network.model.TripPlanRequest
import com.example.trip_planner.network.model.WeatherResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

/**
 * 天气工具类
 * 负责获取和缓存天气数据
 */
object WeatherUtils {

    private const val TAG = "WeatherUtils"
    private const val CACHE_VALID_HOURS = 6

    /**
     * 获取天气数据（优先从缓存读取）
     */
    suspend fun getWeather(
        cityName: String,
        apiService: TripApiService,
        weatherCacheDao: com.example.trip_planner.data.local.dao.WeatherCacheDao
    ): List<WeatherCacheEntity> {
        val validTime = System.currentTimeMillis() - (CACHE_VALID_HOURS * 60 * 60 * 1000)
        val cachedWeather = weatherCacheDao.getValidWeather(cityName, validTime)

        if (cachedWeather.isNotEmpty()) {
            Log.d(TAG, "使用缓存的天气数据: $cityName")
            return cachedWeather
        }

        return try {
            Log.d(TAG, "从网络获取天气数据: $cityName")
            val request = TripPlanRequest(
                destination = cityName,
                preferences = ""
            )
            val response = apiService.getWeather(request)

            if (response.status == "success") {
                val weatherList = parseWeatherResponse(response.message)
                val cacheEntities = weatherList.map { w ->
                    WeatherCacheEntity(
                        cityName = cityName,
                        date = w.date,
                        weather = w.weather,
                        temperature = w.temperature,
                        tips = w.tips
                    )
                }
                weatherCacheDao.insertWeatherList(cacheEntities)
                cacheEntities
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取天气数据失败: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * 解析天气响应
     */
    private fun parseWeatherResponse(jsonString: String): List<WeatherResponse> {
        return try {
            val gson = Gson()
            val type = object : TypeToken<List<WeatherResponse>>() {}.type
            gson.fromJson(jsonString, type)
        } catch (e: Exception) {
            Log.e(TAG, "解析天气数据失败: ${e.message}")
            emptyList()
        }
    }

    /**
     * 格式化日期显示
     */
    fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MM/dd EEE", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            if (date != null) outputFormat.format(date) else dateString
        } catch (e: Exception) {
            dateString
        }
    }

    /**
     * 获取天气图标
     */
    fun getWeatherIcon(weather: String): String {
        return when {
            weather.contains("晴") -> "☀️"
            weather.contains("多云") -> "⛅"
            weather.contains("阴") -> "☁️"
            weather.contains("雨") -> "🌧️"
            weather.contains("雪") -> "❄️"
            weather.contains("雾") -> "🌫️"
            weather.contains("风") -> "💨"
            else -> "🌤️"
        }
    }
}
