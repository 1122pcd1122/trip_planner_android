package com.example.trip_planner.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trip_planner.data.local.TripDatabase
import com.example.trip_planner.data.local.entity.DetailCacheEntity
import com.example.trip_planner.network.TripRepository
import com.example.trip_planner.network.model.AgentResult
                                                                                                                                                                                                                                                                                  import com.example.trip_planner.network.model.HotelDetailInfo
import com.example.trip_planner.network.model.AttractionDetailInfo
import com.example.trip_planner.network.model.RestaurantDetailInfo
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 详情页 ViewModel
 * 负责加载酒店、景点、餐厅的详细信息，并管理加载状态
 * 通过 TripRepository 发起网络请求，将返回的 JSON 解析为结构化数据
 * 内置内存缓存 + Room 持久化缓存，避免重复请求相同数据，支持离线访问
 */
class DetailViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        /** 缓存过期时间：24小时（毫秒） */
        private const val CACHE_EXPIRY_MILLIS = 24 * 60 * 60 * 1000L
    }

    /** 网络请求仓库实例 */
    private val repository = TripRepository()
    /** JSON 解析器，用于将 API 返回的 JSON 字符串解析为数据类 */
    private val gson = Gson()
    /** 详情缓存 DAO，用于持久化缓存 */
    private val detailCacheDao = TripDatabase.getDatabase(application).detailCacheDao()

    /** 酒店详情缓存，键为酒店名称，避免重复网络请求 */
    private val hotelCache = mutableMapOf<String, HotelDetailInfo>()
    /** 景点详情缓存，键为景点名称，避免重复网络请求 */
    private val attractionCache = mutableMapOf<String, AttractionDetailInfo>()
    /** 餐厅详情缓存，键为餐厅名称，避免重复网络请求 */
    private val restaurantCache = mutableMapOf<String, RestaurantDetailInfo>()

    /** 内部状态流，存储详情页的加载状态 */
    private val _detailState = MutableStateFlow<DetailState>(DetailState.Loading)
    /** 对外暴露的只读状态流，供 UI 层收集 */
    val detailState: StateFlow<DetailState> = _detailState.asStateFlow()

    /**
     * 加载酒店详情
     * 优先从内存缓存读取，其次从持久化缓存读取，最后发起网络请求
     * @param name 酒店名称
     * @param latitude 酒店纬度
     * @param longitude 酒店经度
     */
    fun loadHotelDetail(name: String, latitude: String, longitude: String) {
        viewModelScope.launch {
            // 检查内存缓存
            val cachedData = hotelCache[name]
            if (cachedData != null) {
                _detailState.value = DetailState.HotelSuccess(cachedData)
                return@launch
            }

            // 检查持久化缓存
            val cacheKey = "hotel_$name"
            val persistentCache = detailCacheDao.getCacheByKey(cacheKey)
            if (persistentCache != null && isCacheValid(persistentCache.timestamp)) {
                val cachedDetail = parseHotelDetail(persistentCache.jsonData)
                hotelCache[name] = cachedDetail
                _detailState.value = DetailState.HotelSuccess(cachedDetail)
                return@launch
            }

            // 缓存未命中或已过期，发起网络请求
            _detailState.value = DetailState.Loading
            when (val result = repository.fetchHotelDetail(name, latitude, longitude)) {
                is AgentResult.Success -> {
                    val hotelDetail = parseHotelDetail(result.data)
                    hotelCache[name] = hotelDetail
                    // 写入持久化缓存
                    detailCacheDao.insertCache(
                        DetailCacheEntity(
                            cacheKey = cacheKey,
                            type = "hotel",
                            name = name,
                            jsonData = result.data
                        )
                    )
                    _detailState.value = DetailState.HotelSuccess(hotelDetail)
                }
                is AgentResult.Error -> {
                    _detailState.value = DetailState.Error(result.message)
                }
                is AgentResult.Loading -> {
                    _detailState.value = DetailState.Loading
                }
            }
        }
    }

    /**
     * 加载景点详情
     * 优先从内存缓存读取，其次从持久化缓存读取，最后发起网络请求
     * @param name 景点名称
     * @param latitude 景点纬度
     * @param longitude 景点经度
     */
    fun loadAttractionDetail(name: String, latitude: String, longitude: String) {
        viewModelScope.launch {
            // 检查内存缓存
            val cachedData = attractionCache[name]
            if (cachedData != null) {
                _detailState.value = DetailState.AttractionSuccess(cachedData)
                return@launch
            }

            // 检查持久化缓存
            val cacheKey = "attraction_$name"
            val persistentCache = detailCacheDao.getCacheByKey(cacheKey)
            if (persistentCache != null && isCacheValid(persistentCache.timestamp)) {
                val cachedDetail = parseAttractionDetail(persistentCache.jsonData)
                attractionCache[name] = cachedDetail
                _detailState.value = DetailState.AttractionSuccess(cachedDetail)
                return@launch
            }

            // 缓存未命中或已过期，发起网络请求
            _detailState.value = DetailState.Loading
            when (val result = repository.fetchAttractionDetail(name, latitude, longitude)) {
                is AgentResult.Success -> {
                    val attractionDetail = parseAttractionDetail(result.data)
                    attractionCache[name] = attractionDetail
                    // 写入持久化缓存
                    detailCacheDao.insertCache(
                        DetailCacheEntity(
                            cacheKey = cacheKey,
                            type = "attraction",
                            name = name,
                            jsonData = result.data
                        )
                    )
                    _detailState.value = DetailState.AttractionSuccess(attractionDetail)
                }
                is AgentResult.Error -> {
                    _detailState.value = DetailState.Error(result.message)
                }
                is AgentResult.Loading -> {
                    _detailState.value = DetailState.Loading
                }
            }
        }
    }

    /**
     * 加载餐厅详情
     * 优先从内存缓存读取，其次从持久化缓存读取，最后发起网络请求
     * @param name 餐厅名称
     * @param latitude 餐厅纬度
     * @param longitude 餐厅经度
     */
    fun loadRestaurantDetail(name: String, latitude: String, longitude: String) {
        viewModelScope.launch {
            // 检查内存缓存
            val cachedData = restaurantCache[name]
            if (cachedData != null) {
                _detailState.value = DetailState.RestaurantSuccess(cachedData)
                return@launch
            }

            // 检查持久化缓存
            val cacheKey = "restaurant_$name"
            val persistentCache = detailCacheDao.getCacheByKey(cacheKey)
            if (persistentCache != null && isCacheValid(persistentCache.timestamp)) {
                val cachedDetail = parseRestaurantDetail(persistentCache.jsonData)
                restaurantCache[name] = cachedDetail
                _detailState.value = DetailState.RestaurantSuccess(cachedDetail)
                return@launch
            }

            // 缓存未命中或已过期，发起网络请求
            _detailState.value = DetailState.Loading
            when (val result = repository.fetchRestaurantDetail(name, latitude, longitude)) {
                is AgentResult.Success -> {
                    val restaurantDetail = parseRestaurantDetail(result.data)
                    restaurantCache[name] = restaurantDetail
                    // 写入持久化缓存
                    detailCacheDao.insertCache(
                        DetailCacheEntity(
                            cacheKey = cacheKey,
                            type = "restaurant",
                            name = name,
                            jsonData = result.data
                        )
                    )
                    _detailState.value = DetailState.RestaurantSuccess(restaurantDetail)
                }
                is AgentResult.Error -> {
                    _detailState.value = DetailState.Error(result.message)
                }
                is AgentResult.Loading -> {
                    _detailState.value = DetailState.Loading
                }
            }
        }
    }

    /**
     * 判断缓存是否有效（未过期）
     */
    private fun isCacheValid(timestamp: Long): Boolean {
        return System.currentTimeMillis() - timestamp < CACHE_EXPIRY_MILLIS
    }

    /**
     * 清除指定名称的缓存数据
     * 同时清除内存缓存和持久化缓存
     * 用于下拉刷新时强制重新请求
     * @param name 数据名称（酒店/景点/餐厅名称）
     */
    fun clearCache(name: String) {
        hotelCache.remove(name)
        attractionCache.remove(name)
        restaurantCache.remove(name)
        viewModelScope.launch {
            detailCacheDao.deleteCache("hotel_$name")
            detailCacheDao.deleteCache("attraction_$name")
            detailCacheDao.deleteCache("restaurant_$name")
        }
    }

    /**
     * 清除所有缓存数据
     */
    fun clearAllCache() {
        hotelCache.clear()
        attractionCache.clear()
        restaurantCache.clear()
        viewModelScope.launch {
            detailCacheDao.clearAllCache()
        }
    }

    /**
     * 解析酒店详情 JSON 数据
     * 解析失败时返回空对象，避免崩溃
     */
    private fun parseHotelDetail(json: String): HotelDetailInfo {
        return try {
            gson.fromJson(json, HotelDetailInfo::class.java) ?: HotelDetailInfo()
        } catch (e: Exception) {
            HotelDetailInfo()
        }
    }

    /**
     * 解析景点详情 JSON 数据
     * 解析失败时返回空对象，避免崩溃
     */
    private fun parseAttractionDetail(json: String): AttractionDetailInfo {
        return try {
            gson.fromJson(json, AttractionDetailInfo::class.java) ?: AttractionDetailInfo()
        } catch (e: Exception) {
            AttractionDetailInfo()
        }
    }

    /**
     * 解析餐厅详情 JSON 数据
     * 解析失败时返回空对象，避免崩溃
     */
    private fun parseRestaurantDetail(json: String): RestaurantDetailInfo {
        return try {
            gson.fromJson(json, RestaurantDetailInfo::class.java) ?: RestaurantDetailInfo()
        } catch (e: Exception) {
            RestaurantDetailInfo()
        }
    }
}

/**
 * 详情页加载状态
 * 用于 UI 层根据不同状态展示不同内容
 */
sealed class DetailState {
    /** 加载中状态 */
    object Loading : DetailState()
    /** 酒店详情加载成功，携带解析后的数据 */
    data class HotelSuccess(val data: HotelDetailInfo) : DetailState()
    /** 景点详情加载成功，携带解析后的数据 */
    data class AttractionSuccess(val data: AttractionDetailInfo) : DetailState()
    /** 餐厅详情加载成功，携带解析后的数据 */
    data class RestaurantSuccess(val data: RestaurantDetailInfo) : DetailState()
    /** 加载失败状态，携带错误信息 */
    data class Error(val message: String) : DetailState()
}

/**
 * 详情页类型枚举
 * 用于标识当前展示的是哪种类型的详情页面
 */
enum class DetailType {
    /** 酒店详情页 */
    HOTEL,
    /** 景点详情页 */
    ATTRACTION,
    /** 餐厅详情页 */
    RESTAURANT
}
