package com.example.trip_planner.network

import android.util.Log
import com.example.trip_planner.network.model.AgentResult
import com.example.trip_planner.network.model.AttractionData
import com.example.trip_planner.network.model.AttractionResponse
import com.example.trip_planner.network.model.DetailRequest
import com.example.trip_planner.network.model.HotelData
import com.example.trip_planner.network.model.HotelResponse
import com.example.trip_planner.network.model.RestaurantData
import com.example.trip_planner.network.model.RestaurantResponse
import com.example.trip_planner.network.model.TripPlanResponse
import com.example.trip_planner.network.model.TripPlanRequest
import com.example.trip_planner.network.model.WeatherListResponse
import com.example.trip_planner.network.model.WeatherResponse
import com.example.trip_planner.ui.screens.PoiModel
import com.example.trip_planner.ui.screens.PoiType
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 旅行数据仓库
 * 
 * 负责处理所有网络请求的业务逻辑
 * 位于网络层和 ViewModel 层之间，起到桥梁作用
 * 
 * 主要功能：
 * - 调用 NetworkClient 获取 API 服务
 * - 处理网络请求的异常和错误
 * - 将 API 返回的数据转换为 UI 所需的数据模型
 * - 使用协程确保网络请求在 IO 线程执行
 * 
 * 使用方式：在 ViewModel 中创建实例并调用对应的 fetch 方法
 */
class TripRepository {

    private val TAG = "TripRepository"
    
    // 通过 NetworkClient 获取 API 服务实例
    private val apiService = NetworkClient.tripApiService
    
    // Gson 用于解析 JSON 数据
    private val gson = Gson()

    /**
     * 获取天气信息
     * 
     * @param destination 目的地城市
     * @param days 天数（兼容旧版）
     * @param startDate 开始日期（新版）
     * @param endDate 结束日期（新版）
     * @param preferences 用户偏好
     * @return AgentResult<List<WeatherResponse>> 天气数据列表或多天数据
     */
    suspend fun fetchWeather(destination: String, days: String, startDate: String = "", endDate: String = "", preferences: String): AgentResult<List<WeatherResponse>> = withContext(Dispatchers.IO) {
        Log.i(TAG, "📡 开始请求天气数据: destination=$destination, days=$days, startDate=$startDate, endDate=$endDate, preferences=$preferences",)
        try {
            val request = TripPlanRequest(
                destination = destination,
                days = days,
                startDate = startDate,
                endDate = endDate,
                preferences = preferences
            )


            // 调用 API
            val response = apiService.getWeather(request)
            Log.i(TAG, "📥 天气 API 响应: status=${response.status}")
            Log.i(TAG,"📥 API message: status=${response.message}")
            
            if (response.status == "success") {
                try {
                    val weatherListResponse = gson.fromJson(response.message, WeatherListResponse::class.java)
                    if (weatherListResponse == null) {
                        Log.e(TAG, "❌ 天气数据解析失败")
                        return@withContext AgentResult.Error("天气数据解析失败")
                    }
                    AgentResult.Success(weatherListResponse.weatherList)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ 天气数据解析异常: ${e.message}")
                    AgentResult.Error("天气数据解析失败: ${e.message}")
                }
            } else {
                AgentResult.Error(response.message)
            }
        } catch (e: Exception) {
            AgentResult.Error("网络请求失败: ${e.message}")
        }
    }


    /**
     * 获取景点列表
     * 
     * @param destination 目的地城市
     * @param days 天数（兼容旧版）
     * @param startDate 开始日期（新版）
     * @param endDate 结束日期（新版）
     * @param preferences 用户偏好
     * @return AgentResult<List<PoiModel>> 景点列表（转换为 PoiModel）或错误信息
     */
    suspend fun fetchAttractions(destination: String, days: String, startDate: String = "", endDate: String = "", preferences: String): AgentResult<AttractionData> = withContext(Dispatchers.IO) {
        Log.i(TAG, "📡 开始请求景点数据: destination=$destination, days=$days, startDate=$startDate, endDate=$endDate, preferences=$preferences")
        try {
            val request = TripPlanRequest(
                destination = destination,
                days = days,
                startDate = startDate,
                endDate = endDate,
                preferences = preferences
            )
            val response = apiService.getAttractions(request)
            Log.i(TAG, "📥 景点 API 响应: status=${response.status}")
            Log.i(TAG,"📥 API message: status=${response.message}")
            
            if (response.status == "success") {
                try {
                    val attractionResponse = gson.fromJson(response.message, AttractionResponse::class.java)
                    if (attractionResponse == null) {
                        Log.e(TAG, "❌ 景点数据解析失败")
                        return@withContext AgentResult.Error("景点数据解析失败")
                    }
                    
                    val spotInfoList = attractionResponse.spotList
                    val allPois = spotInfoList.map { spot ->
                        PoiModel(
                            name = spot.name,
                            rating = spot.score,
                            price = "",
                            distance = "",
                            latLng = LatLng(
                                spot.latitude.toDoubleOrNull() ?: 0.0,
                                spot.longitude.toDoubleOrNull() ?: 0.0
                            ),
                            desc = "${spot.address}\n${spot.intro}",
                            priceRange = "",
                            featureDish = "",
                            poiType = PoiType.ATTRACTION
                        )
                    }
                    
                    Log.i(TAG, "✅ 景点数据解析完成: ${allPois.size} 个景点")
                    AgentResult.Success(AttractionData(spotInfoList, allPois))
                } catch (e: Exception) {
                    Log.e(TAG, "❌ 景点解析失败: ${e.message}")
                    AgentResult.Error("数据解析失败: ${e.message}")
                }
            } else {
                Log.e(TAG, "❌ 景点 API 返回失败: ${response.message}")
                AgentResult.Error(response.message)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 景点请求失败: ${e.message}")
            AgentResult.Error("网络请求失败: ${e.message}")
        }
    }

    /**
     * 获取酒店列表
     * 
     * @param destination 目的地城市
     * @param days 天数（兼容旧版）
     * @param startDate 开始日期（新版）
     * @param endDate 结束日期（新版）
     * @param preferences 用户偏好
     * @return AgentResult<List<PoiModel>> 酒店列表（转换为 PoiModel）或错误信息
     */
    suspend fun fetchHotels(destination: String, days: String, startDate: String = "", endDate: String = "", preferences: String): AgentResult<HotelData> = withContext(Dispatchers.IO) {
        Log.i(TAG, "📡 开始请求酒店数据: destination=$destination, days=$days, startDate=$startDate, endDate=$endDate, preferences=$preferences")
        try {

            val request = TripPlanRequest(
                destination = destination,
                days = days,
                startDate = startDate,
                endDate = endDate,
                preferences = preferences
            )
            val response = apiService.getHotels(request)
            Log.i(TAG, "📥 酒店 API 响应: status=${response.status}")
            Log.i(TAG,"📥 API message: status=${response.message}")
            
            if (response.status == "success") {
                try {
                    val hotelResponse = gson.fromJson(response.message, HotelResponse::class.java)
                    if (hotelResponse == null) {
                        Log.e(TAG, "❌ 酒店数据解析失败")
                        return@withContext AgentResult.Error("酒店数据解析失败")
                    }
                    
                    val hotelInfoList = hotelResponse.hotelList
                    val poiList = hotelInfoList.map { hotel ->
                        PoiModel(
                            name = hotel.name,
                            rating = "",
                            price = hotel.priceRange,
                            distance = "",
                            latLng = LatLng(
                                hotel.latitude.toDoubleOrNull() ?: 0.0,
                                hotel.longitude.toDoubleOrNull() ?: 0.0
                            ),
                            desc = "${hotel.address}\n${hotel.feature}",
                            priceRange = hotel.priceRange,
                            featureDish = "",
                            poiType = PoiType.HOTEL
                        )
                    }
                    
                    Log.i(TAG, "✅ 酒店数据解析完成: ${poiList.size} 家酒店")
                    AgentResult.Success(HotelData(hotelInfoList, poiList))
                } catch (e: Exception) {
                    Log.e(TAG, "❌ 酒店解析失败: ${e.message}")
                    AgentResult.Error("数据解析失败: ${e.message}")
                }
            } else {
                Log.e(TAG, "❌ 酒店 API 返回失败: ${response.message}")
                AgentResult.Error(response.message)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 酒店请求失败: ${e.message}")
            AgentResult.Error("网络请求失败: ${e.message}")
        }
    }

    /**
     * 获取餐厅列表
     * 
     * @param destination 目的地城市
     * @param days 天数（兼容旧版）
     * @param startDate 开始日期（新版）
     * @param endDate 结束日期（新版）
     * @param preferences 用户偏好
     * @return AgentResult<List<PoiModel>> 餐厅列表（转换为 PoiModel）或错误信息
     */
    suspend fun fetchRestaurants(destination: String, days: String, startDate: String = "", endDate: String = "", preferences: String): AgentResult<RestaurantData> = withContext(Dispatchers.IO) {
        Log.i(TAG, "📡 开始请求餐厅数据: destination=$destination, days=$days, startDate=$startDate, endDate=$endDate, preferences=$preferences")
        try {

            val request = TripPlanRequest(
                destination = destination,
                days = days,
                startDate = startDate,
                endDate = endDate,
                preferences = preferences
            )
            val response = apiService.getRestaurants(request)
            Log.i(TAG, "📥 餐厅 API 响应: status=${response.status}")
            Log.i(TAG,"📥 API message: status=${response.message}")

            
            if (response.status == "success") {
                try {
                    val restaurantResponse = gson.fromJson(response.message, RestaurantResponse::class.java)
                    if (restaurantResponse == null) {
                        Log.e(TAG, "❌ 餐厅数据解析失败")
                        return@withContext AgentResult.Error("餐厅数据解析失败")
                    }
                    
                    val restaurantInfoList = restaurantResponse.foodList
                    val poiList = restaurantInfoList.map { food ->
                        PoiModel(
                            name = food.name,
                            rating = food.score,
                            price = food.featureDish,
                            distance = "",
                            latLng = LatLng(
                                food.latitude.toDoubleOrNull() ?: 0.0,
                                food.longitude.toDoubleOrNull() ?: 0.0
                            ),
                            desc = food.address,
                            priceRange = "",
                            featureDish = food.featureDish,
                            poiType = PoiType.RESTAURANT
                        )
                    }
                    
                    Log.i(TAG, "✅ 餐厅数据解析完成: ${poiList.size} 家餐厅")
                    AgentResult.Success(RestaurantData(restaurantInfoList, poiList))
                } catch (e: Exception) {
                    Log.e(TAG, "❌ 餐厅解析失败: ${e.message}")
                    AgentResult.Error("数据解析失败: ${e.message}")
                }
            } else {
                Log.e(TAG, "❌ 餐厅 API 返回失败: ${response.message}")
                AgentResult.Error(response.message)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 餐厅请求失败: ${e.message}")
            AgentResult.Error("网络请求失败: ${e.message}")
        }
    }

    /**
     * 一键生成完整旅行规划
     * 
     * ALL 模式下的一次请求获取所有数据
     * 包含天气、景点、酒店、餐厅和行程汇总
     * 
     * @param destination 目的地城市
     * @param days 游玩天数（兼容旧版）
     * @param startDate 开始日期（新版）
     * @param endDate 结束日期（新版）
     * @param preferences 用户偏好
     * @return AgentResult<TripAllResponse> 统一响应结果
     */
    suspend fun fetchAllInOne(destination: String, days: String, startDate: String = "", endDate: String = "", preferences: String): AgentResult<TripPlanResponse> = withContext(Dispatchers.IO) {
        Log.i(TAG, "🚀 开始一键生成完整旅行规划: destination=$destination, days=$days, startDate=$startDate, endDate=$endDate, preferences=$preferences")
        try {
            val request = TripPlanRequest(
                destination = destination,
                days = days,
                startDate = startDate,
                endDate = endDate,
                preferences = preferences
            )
            val response = apiService.generateAllInOne(request)
            Log.i(TAG, "📥 统一规划 API 响应: status=${response.status}")
            Log.i(TAG, "📥 统一规划 API 响应: API message=${response.message}")
            
            if (response.status == "success") {
                try {
                    val planResponse = gson.fromJson(response.message, TripPlanResponse::class.java)
                    if (planResponse == null) {
                        Log.e(TAG, "❌ 统一规划解析失败")
                        return@withContext AgentResult.Error("统一规划解析失败")
                    }
                    Log.i(TAG, "✅ 统一规划解析完成: 共${planResponse.days.size}天, 酒店=${planResponse.hotel.size}家")
                    AgentResult.Success(planResponse)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ 统一规划解析异常: ${e.message}")
                    return@withContext AgentResult.Error("统一规划解析失败: ${e.message}")
                }
            } else {
                Log.e(TAG, "❌ 统一规划 API 返回失败: ${response.message}")
                AgentResult.Error(response.message)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 统一规划请求失败: ${e.message}")
            AgentResult.Error("网络请求失败: ${e.message}")
        }
    }

    /**
     * 获取酒店详情
     * 
     * @param name 酒店名称
     * @param latitude 酒店纬度
     * @param longitude 酒店经度
     * @return AgentResult<String> 酒店详情 JSON 数据
     */
    suspend fun fetchHotelDetail(name: String, latitude: String, longitude: String): AgentResult<String> = withContext(Dispatchers.IO) {
        Log.i(TAG, "🔍 开始查询酒店详情: name=$name")
        try {
            val request = DetailRequest(
                name = name,
                type = "hotel",
                latitude = latitude,
                longitude = longitude
            )
            val response = apiService.getHotelDetail(request)
            if (response.status == "success") {
                Log.i(TAG, "✅ 酒店详情获取成功")
                AgentResult.Success(response.message)
            } else {
                Log.e(TAG, "❌ 酒店详情 API 返回失败: ${response.message}")
                AgentResult.Error(response.message)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 酒店详情请求失败: ${e.message}")
            AgentResult.Error("网络请求失败: ${e.message}")
        }
    }

    /**
     * 获取景点详情
     * 
     * @param name 景点名称
     * @param latitude 景点纬度
     * @param longitude 景点经度
     * @return AgentResult<String> 景点详情 JSON 数据
     */
    suspend fun fetchAttractionDetail(name: String, latitude: String, longitude: String): AgentResult<String> = withContext(Dispatchers.IO) {
        Log.i(TAG, "🔍 开始查询景点详情: name=$name")
        try {
            val request = DetailRequest(name = name, type = "attraction", latitude = latitude, longitude = longitude)
            val response = apiService.getAttractionDetail(request)
            if (response.status == "success") {
                Log.i(TAG, "✅ 景点详情获取成功")
                AgentResult.Success(response.message)
            } else {
                Log.e(TAG, "❌ 景点详情 API 返回失败: ${response.message}")
                AgentResult.Error(response.message)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 景点详情请求失败: ${e.message}")
            AgentResult.Error("网络请求失败: ${e.message}")
        }
    }

    /**
     * 获取餐厅详情
     * 
     * @param name 餐厅名称
     * @param latitude 餐厅纬度
     * @param longitude 餐厅经度
     * @return AgentResult<String> 餐厅详情 JSON 数据
     */
    suspend fun fetchRestaurantDetail(name: String, latitude: String, longitude: String): AgentResult<String> = withContext(Dispatchers.IO) {
        Log.i(TAG, "🔍 开始查询餐厅详情: name=$name")
        try {
            val request = DetailRequest(name = name, type = "restaurant", latitude = latitude, longitude = longitude)
            val response = apiService.getRestaurantDetail(request)
            if (response.status == "success") {
                Log.i(TAG, "✅ 餐厅详情获取成功")
                AgentResult.Success(response.message)
            } else {
                Log.e(TAG, "❌ 餐厅详情 API 返回失败: ${response.message}")
                AgentResult.Error(response.message)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 餐厅详情请求失败: ${e.message}")
            AgentResult.Error("网络请求失败: ${e.message}")
        }
    }
}