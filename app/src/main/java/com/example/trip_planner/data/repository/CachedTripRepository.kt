package com.example.trip_planner.data.repository

import android.content.Context
import android.util.Log
import com.example.trip_planner.data.local.TripDatabase
import com.example.trip_planner.data.local.entity.DetailCacheEntity
import com.example.trip_planner.network.TripRepository
import com.example.trip_planner.network.model.AgentResult
import com.example.trip_planner.network.model.AttractionData
import com.example.trip_planner.network.model.HotelData
import com.example.trip_planner.network.model.RestaurantData
import com.example.trip_planner.network.model.TripPlanResponse
import com.example.trip_planner.network.model.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CachedTripRepository(context: Context) {

    private val TAG = "CachedTripRepository"
    private val networkRepository = TripRepository()
    private val database = TripDatabase.getDatabase(context)
    private val cacheDao = database.detailCacheDao()
    private val json = Json { ignoreUnknownKeys = true }
    
    private val memoryCache = mutableMapOf<String, CacheEntry>()
    private val cacheMutex = Mutex()

    data class CacheEntry(
        val data: Any,
        val timestamp: Long = System.currentTimeMillis()
    )

    companion object {
        private const val WEATHER_CACHE_MS = 6 * 60 * 60 * 1000L
        private const val LIST_CACHE_MS = 24 * 60 * 60 * 1000L
        private const val DETAIL_CACHE_MS = 7 * 24 * 60 * 60 * 1000L
        private const val PLAN_CACHE_MS = 12 * 60 * 60 * 1000L
        private const val MAX_MEMORY_CACHE_SIZE = 50
    }

    private fun getCacheDuration(type: String): Long {
        return when {
            type.startsWith("weather") -> WEATHER_CACHE_MS
            type.startsWith("hotel_detail") || type.startsWith("attraction_detail") || type.startsWith("restaurant_detail") -> DETAIL_CACHE_MS
            type.startsWith("all_in_one") -> PLAN_CACHE_MS
            else -> LIST_CACHE_MS
        }
    }

    suspend fun fetchWeather(destination: String, days: String, startDate: String = "", endDate: String = "", preferences: String): AgentResult<List<WeatherResponse>> {
        val cacheKey = "weather_${destination}_${days}"
        
        val cached = getCachedData<List<WeatherResponse>>(cacheKey, WEATHER_CACHE_MS)
        if (cached != null) {
            return AgentResult.Success(cached)
        }

        return retryWithBackoff(
            maxRetries = 2,
            initialDelayMs = 1000,
            operation = {
                val result = networkRepository.fetchWeather(destination, days, startDate, endDate, preferences)
                if (result is AgentResult.Success) {
                    cacheData(cacheKey, result.data)
                }
                result
            }
        )
    }

    suspend fun fetchAttractions(destination: String, days: String, startDate: String = "", endDate: String = "", preferences: String): AgentResult<AttractionData> {
        val cacheKey = "attractions_${destination}"
        
        val cached = getCachedData<AttractionData>(cacheKey, LIST_CACHE_MS)
        if (cached != null) {
            return AgentResult.Success(cached)
        }

        return retryWithBackoff(
            maxRetries = 2,
            initialDelayMs = 1000,
            operation = {
                val result = networkRepository.fetchAttractions(destination, days, startDate, endDate, preferences)
                if (result is AgentResult.Success) {
                    cacheData(cacheKey, result.data)
                }
                result
            }
        )
    }

    suspend fun fetchHotels(destination: String, days: String, startDate: String = "", endDate: String = "", preferences: String): AgentResult<HotelData> {
        val cacheKey = "hotels_${destination}"
        
        val cached = getCachedData<HotelData>(cacheKey, LIST_CACHE_MS)
        if (cached != null) {
            return AgentResult.Success(cached)
        }

        return retryWithBackoff(
            maxRetries = 2,
            initialDelayMs = 1000,
            operation = {
                val result = networkRepository.fetchHotels(destination, days, startDate, endDate, preferences)
                if (result is AgentResult.Success) {
                    cacheData(cacheKey, result.data)
                }
                result
            }
        )
    }

    suspend fun fetchRestaurants(destination: String, days: String, startDate: String = "", endDate: String = "", preferences: String): AgentResult<RestaurantData> {
        val cacheKey = "restaurants_${destination}"
        
        val cached = getCachedData<RestaurantData>(cacheKey, LIST_CACHE_MS)
        if (cached != null) {
            return AgentResult.Success(cached)
        }

        return retryWithBackoff(
            maxRetries = 2,
            initialDelayMs = 1000,
            operation = {
                val result = networkRepository.fetchRestaurants(destination, days, startDate, endDate, preferences)
                if (result is AgentResult.Success) {
                    cacheData(cacheKey, result.data)
                }
                result
            }
        )
    }

    suspend fun fetchAllInOne(destination: String, days: String, startDate: String = "", endDate: String = "", preferences: String): AgentResult<TripPlanResponse> {
        val cacheKey = "all_in_one_${destination}_${days}"
        
        val cached = getCachedData<TripPlanResponse>(cacheKey, PLAN_CACHE_MS)
        if (cached != null) {
            return AgentResult.Success(cached)
        }

        return retryWithBackoff(
            maxRetries = 2,
            initialDelayMs = 1000,
            operation = {
                val result = networkRepository.fetchAllInOne(destination, days, startDate, endDate, preferences)
                if (result is AgentResult.Success) {
                    cacheData(cacheKey, result.data)
                }
                result
            }
        )
    }

    suspend fun fetchHotelDetail(name: String, latitude: String, longitude: String): AgentResult<String> {
        val cacheKey = "hotel_detail_${name}"
        
        val cached = getCachedData<String>(cacheKey, DETAIL_CACHE_MS)
        if (cached != null) {
            return AgentResult.Success(cached)
        }

        return retryWithBackoff(
            maxRetries = 2,
            initialDelayMs = 1000,
            operation = {
                val result = networkRepository.fetchHotelDetail(name, latitude, longitude)
                if (result is AgentResult.Success) {
                    cacheData(cacheKey, result.data)
                }
                result
            }
        )
    }

    suspend fun fetchAttractionDetail(name: String, latitude: String, longitude: String): AgentResult<String> {
        val cacheKey = "attraction_detail_${name}"
        
        val cached = getCachedData<String>(cacheKey, DETAIL_CACHE_MS)
        if (cached != null) {
            return AgentResult.Success(cached)
        }

        return retryWithBackoff(
            maxRetries = 2,
            initialDelayMs = 1000,
            operation = {
                val result = networkRepository.fetchAttractionDetail(name, latitude, longitude)
                if (result is AgentResult.Success) {
                    cacheData(cacheKey, result.data)
                }
                result
            }
        )
    }

    suspend fun fetchRestaurantDetail(name: String, latitude: String, longitude: String): AgentResult<String> {
        val cacheKey = "restaurant_detail_${name}"
        
        val cached = getCachedData<String>(cacheKey, DETAIL_CACHE_MS)
        if (cached != null) {
            return AgentResult.Success(cached)
        }

        return retryWithBackoff(
            maxRetries = 2,
            initialDelayMs = 1000,
            operation = {
                val result = networkRepository.fetchRestaurantDetail(name, latitude, longitude)
                if (result is AgentResult.Success) {
                    cacheData(cacheKey, result.data)
                }
                result
            }
        )
    }

    private suspend fun <T> retryWithBackoff(
        maxRetries: Int,
        initialDelayMs: Long,
        operation: suspend () -> AgentResult<T>
    ): AgentResult<T> {
        var currentDelay = initialDelayMs
        var lastError: Exception? = null

        for (attempt in 0..maxRetries) {
            try {
                if (attempt > 0) {
                    Log.i(TAG, "🔄 重试请求 (第 $attempt 次，等待 ${currentDelay}ms)")
                    kotlinx.coroutines.delay(currentDelay)
                    currentDelay *= 2
                }
                return operation()
            } catch (e: Exception) {
                lastError = e
                Log.w(TAG, "⚠️ 请求失败 (第 ${attempt + 1} 次): ${e.message}")
            }
        }

        Log.e(TAG, "❌ 所有重试均失败")
        return AgentResult.Error("网络请求失败: ${lastError?.message ?: "未知错误"}")
    }

    suspend fun clearAllCache() = withContext(Dispatchers.IO) {
        cacheDao.clearAllCache()
        Log.i(TAG, "🗑️ 已清除所有缓存")
    }

    suspend fun getCacheStats(): String = withContext(Dispatchers.IO) {
        try {
            val allCaches = cacheDao.getAllCaches()
            val totalSize = allCaches.sumOf { it.jsonData.length }
            val now = System.currentTimeMillis()
            val validCount = allCaches.count { 
                now - it.timestamp < getCacheDuration(it.type)
            }
            "缓存条目: ${allCaches.size} (有效: $validCount), 总大小: ${totalSize / 1024}KB"
        } catch (e: Exception) {
            "获取缓存统计失败: ${e.message}"
        }
    }

    suspend fun clearCache(key: String) = withContext(Dispatchers.IO) {
        cacheDao.deleteCache(key)
        Log.i(TAG, "🗑️ 已清除缓存: $key")
    }

    suspend fun clearExpiredCache() = withContext(Dispatchers.IO) {
        val allCaches = cacheDao.getAllCaches()
        val now = System.currentTimeMillis()
        var clearedCount = 0
        allCaches.forEach { cache ->
            val duration = getCacheDuration(cache.type)
            if (now - cache.timestamp > duration) {
                cacheDao.deleteCache(cache.cacheKey)
                clearedCount++
            }
        }
        Log.i(TAG, "🗑️ 已清除 $clearedCount 个过期缓存")
    }

    private suspend inline fun <reified T> getCachedData(key: String, durationMs: Long): T? = withContext(Dispatchers.IO) {
        try {
            cacheMutex.withLock {
                val memoryEntry = memoryCache[key]
                if (memoryEntry != null && System.currentTimeMillis() - memoryEntry.timestamp < durationMs) {
                    @Suppress("UNCHECKED_CAST")
                    return@withContext memoryEntry.data as T
                }
                memoryCache.remove(key)
            }

            val cacheEntity = cacheDao.getCacheByKey(key)
            if (cacheEntity != null) {
                val now = System.currentTimeMillis()
                if (now - cacheEntity.timestamp < durationMs) {
                    val data = json.decodeFromString<T>(cacheEntity.jsonData)
                    cacheMutex.withLock {
                        memoryCache[key] = CacheEntry(data as Any)
                        if (memoryCache.size > MAX_MEMORY_CACHE_SIZE) {
                            val oldestKey = memoryCache.entries.minByOrNull { it.value.timestamp }?.key
                            if (oldestKey != null) {
                                memoryCache.remove(oldestKey)
                            }
                        }
                    }
                    return@withContext data
                } else {
                    cacheDao.deleteCache(key)
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private suspend inline fun <reified T> cacheData(key: String, data: T) = withContext(Dispatchers.IO) {
        try {
            cacheMutex.withLock {
                memoryCache[key] = CacheEntry(data as Any)
                if (memoryCache.size > MAX_MEMORY_CACHE_SIZE) {
                    val oldestKey = memoryCache.entries.minByOrNull { it.value.timestamp }?.key
                    if (oldestKey != null) {
                        memoryCache.remove(oldestKey)
                    }
                }
            }

            val jsonData = json.encodeToString(data)
            val type = key.substringBefore("_")
            val name = key.substringAfter("_")
            val entity = DetailCacheEntity(
                cacheKey = key,
                type = type,
                name = name,
                jsonData = jsonData,
                timestamp = System.currentTimeMillis()
            )
            cacheDao.insertCache(entity)
        } catch (e: Exception) {
            // ignore
        }
    }
}
