package com.example.trip_planner.viewModel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trip_planner.data.FavoriteManager
import com.example.trip_planner.data.local.TripDatabase
import com.example.trip_planner.data.repository.TripPlanRepository
import com.example.trip_planner.network.TripRepository
import com.example.trip_planner.network.model.AgentResult
import com.example.trip_planner.network.model.DayPlan
import com.example.trip_planner.network.model.PlanHotel
import com.example.trip_planner.network.model.WeatherResponse
import com.example.trip_planner.ui.content.AgentType
import com.example.trip_planner.ui.content.FavoriteItem
import com.example.trip_planner.ui.content.PoiModel
import com.example.trip_planner.ui.content.PoiType
import com.google.gson.Gson
import kotlinx.coroutines.launch

/**
 * 主 ViewModel
 * 
 * 负责管理旅行规划应用的核心业务逻辑和 UI 状态
 * 
 * 主要职责：
 * - 管理用户输入（目的地、天数、偏好）
 * - 管理 UI 状态（加载、成功、错误）
 * - 协调各 Agent 的数据获取
 * - 处理业务逻辑和异常
 * 
 * 使用方式：通过 ViewModelProvider 创建实例，Compose 界面观察状态变化
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "MainViewModel"
    private val gson = Gson()

    /**
     * 收藏管理器
     * 延迟初始化，在首次使用时创建
     */
    private var favoriteManager: FavoriteManager? = null

    /**
     * 行程规划仓库
     */
    private val tripPlanRepository: TripPlanRepository

    init {
        val database = TripDatabase.getDatabase(application)
        tripPlanRepository = TripPlanRepository(database.tripPlanDao())
    }

    /**
     * 初始化收藏管理器
     * 需要传入 Application Context
     */
    fun initFavoriteManager(context: Context) {
        if (favoriteManager == null) {
            favoriteManager = FavoriteManager(context)
        }
    }

    /**
     * 获取收藏列表
     */
    fun getFavorites(): List<FavoriteItem> {
        return favoriteManager?.getAllFavorites() ?: emptyList()
    }

    /**
     * 检查是否已收藏
     */
    fun isFavorite(itemId: String): Boolean {
        return favoriteManager?.isFavorite(itemId) ?: false
    }

    /**
     * 切换收藏状态
     * 返回新的收藏状态（true=已收藏，false=未收藏）
     */
    fun toggleFavorite(item: FavoriteItem): Boolean {
        return favoriteManager?.toggleFavorite(item) ?: false
    }

    /**
     * 目的地输入
     * 默认值：成都
     */
    var destination = mutableStateOf("成都")

    /**
     * 旅行天数输入
     * 默认值：3天
     */
    var days = mutableStateOf("3")

    /**
     * 用户偏好输入
     * 默认值：不吃辣
     */
    var preferences = mutableStateOf("不吃辣")

    /**
     * UI 状态
     * 
     * 可选值：
     * - "Idle": 初始状态
     * - "Loading": 加载中
     * - "Success": 加载成功
     * - "Error": 加载失败
     */
    var uiState = mutableStateOf("Idle")

    /**
     * 结果数据
     * 用于显示错误信息或文本结果
     */
    var resultData = mutableStateOf("")
    
    /**
     * 当前选中的 Agent 类型
     * 默认值：WEATHER（天气查询）
     */
    var selectedAgent = mutableStateOf(AgentType.WEATHER)
    
    /**
     * 天气数据列表（支持多天）
     * fetchWeather 成功后填充
     */
    var weatherData = mutableStateOf<List<WeatherResponse>>(emptyList())

    /**
     * 酒店数据列表
     * fetchHotels 成功后填充
     */
    var hotelData = mutableStateOf<List<PoiModel>>(emptyList())

    /**
     * 餐厅数据列表
     * fetchRestaurants 成功后填充
     */
    var restaurantData = mutableStateOf<List<PoiModel>>(emptyList())

    /**
     * 景点数据列表
     * fetchAttractions 成功后填充
     */
    var attractionData = mutableStateOf<List<PoiModel>>(emptyList())

    /**
     * 日程数据（文本内容）
     * fetchItinerary 成功后填充
     */
    var itineraryData = mutableStateOf<String>("")

    /**
     * 旅行总汇总
     * ALL 模式下一键生成后填充
     */
    var totalSummary = mutableStateOf<String>("")

    /**
     * 每日行程列表
     * ALL 模式下一键生成后填充
     */
    var dayPlans = mutableStateOf<List<DayPlan>>(emptyList())

    /**
     * 行程规划酒店列表
     * ALL 模式下一键生成后填充
     */
    var planHotels = mutableStateOf<List<PlanHotel>>(emptyList())

    /**
     * 整体出行建议
     * ALL 模式下一键生成后填充
     */
    var overallTips = mutableStateOf<String>("")

    /**
     * 数据仓库实例
     * 负责处理所有网络请求
     */
    private val repository = TripRepository()

    /**
     * 触发生成旅行规划
     * 
     * 根据当前选中的 Agent 类型调用对应的数据获取方法
     * 入口函数，由 UI 层点击事件触发
     */
    fun generateTripPlan() {
        if (destination.value.isBlank()) {
            Log.w(TAG, "⚠️ 用户未输入目的地就点击了生成")
            resultData.value = "请先输入目的地！"
            return
        }

        val agentType = selectedAgent.value
        Log.i(TAG, "🎯 触发 [$agentType] Agent 请求")
        
        uiState.value = "Loading"
        resultData.value = ""

        when (agentType) {
            AgentType.WEATHER -> fetchWeather()
            AgentType.HOTEL -> fetchHotels()
            AgentType.RESTAURANT -> fetchRestaurants()
            AgentType.ATTRACTION -> fetchAttractions()
            AgentType.ALL -> fetchAllInOne()
            else -> {}
        }
    }

    /**
     * 一键生成完整旅行规划
     * 
     * 使用统一 API 一次请求获取所有数据
     * 包含天气、景点、酒店、餐厅和行程汇总
     */
    private fun fetchAllInOne() {
        viewModelScope.launch {
            uiState.value = "Loading"
            val result = repository.fetchAllInOne(destination.value, days.value, preferences.value)
            when (result) {
                is AgentResult.Success -> {
                    val planData = result.data
                    
                    // 设置每日行程
                    dayPlans.value = planData.days
                    Log.i(TAG, "✅ 每日行程解析: ${planData.days.size} 天")
                    
                    // 设置酒店列表
                    planHotels.value = planData.hotel
                    Log.i(TAG, "✅ 酒店解析: ${planData.hotel.size} 家")
                    
                    // 设置整体出行建议
                    overallTips.value = planData.overallTips
                    Log.i(TAG, "✅ 整体建议: ${planData.overallTips}")
                    
                    // 构建天气数据（从每日行程中提取）
                    val weatherList = planData.days.map { day ->
                        WeatherResponse(
                            cityName = destination.value,
                            latitude = "",
                            longitude = "",
                            date = day.date,
                            weather = day.weather,
                            temperature = "",
                            tips = day.tips
                        )
                    }
                    weatherData.value = weatherList
                    
                    // 将酒店转换为 PoiModel
                    val hotelPois = planData.hotel.map { hotel ->
                        PoiModel(
                            name = hotel.name,
                            rating = "",
                            price = hotel.price,
                            distance = "",
                            latLng = com.amap.api.maps.model.LatLng(
                                hotel.latitude.toDoubleOrNull() ?: 0.0,
                                hotel.longitude.toDoubleOrNull() ?: 0.0
                            ),
                            desc = "${hotel.address}\n${hotel.advantage}",
                            priceRange = hotel.price,
                            featureDish = "",
                            poiType = PoiType.HOTEL
                        )
                    }
                    hotelData.value = hotelPois
                    
                    // 从每日行程中提取餐厅（餐饮）
                    val restaurantPois = mutableListOf<PoiModel>()
                    planData.days.forEach { day ->
                        day.meals?.let { meals ->
                            meals.lunch?.let { lunch ->
                                restaurantPois.add(
                                    PoiModel(
                                        name = lunch.name,
                                        rating = "",
                                        price = lunch.dish,
                                        distance = "",
                                        latLng = com.amap.api.maps.model.LatLng(0.0, 0.0),
                                        desc = lunch.address,
                                        priceRange = "",
                                        featureDish = lunch.dish,
                                        poiType = PoiType.RESTAURANT
                                    )
                                )
                            }
                            meals.dinner?.let { dinner ->
                                restaurantPois.add(
                                    PoiModel(
                                        name = dinner.name,
                                        rating = "",
                                        price = dinner.dish,
                                        distance = "",
                                        latLng = com.amap.api.maps.model.LatLng(0.0, 0.0),
                                        desc = dinner.address,
                                        priceRange = "",
                                        featureDish = dinner.dish,
                                        poiType = PoiType.RESTAURANT
                                    )
                                )
                            }
                        }
                    }
                    restaurantData.value = restaurantPois
                    Log.i(TAG, "✅ 餐厅解析: ${restaurantPois.size} 家")
                    
                    // 从每日行程中提取景点
                    val attractionPois = mutableListOf<PoiModel>()
                    planData.days.forEach { day ->
                        day.itinerary.forEach { item ->
                            attractionPois.add(
                                PoiModel(
                                    name = item.spot,
                                    rating = "",
                                    price = "",
                                    distance = "",
                                    latLng = com.amap.api.maps.model.LatLng(
                                        item.latitude.toDoubleOrNull() ?: 0.0,
                                        item.longitude.toDoubleOrNull() ?: 0.0
                                    ),
                                    desc = item.address,
                                    priceRange = "",
                                    featureDish = "",
                                    poiType = PoiType.ATTRACTION
                                )
                            )
                        }
                    }
                    attractionData.value = attractionPois
                    Log.i(TAG, "✅ 景点解析: ${attractionPois.size} 个")
                    
                    uiState.value = "Success"
                    Log.i(TAG, "✅ 一键生成完成")
                }
                is AgentResult.Error -> {
                    uiState.value = "Error"
                    resultData.value = result.message
                    Log.e(TAG, "❌ 一键生成失败: ${result.message}")
                }

                else -> {
                    Log.w(TAG, "⚠️ 一键生成返回未处理状态: $result")
                }
            }
        }
    }

    /**
     * 获取天气数据
     * 
     * 调用 repository 获取天气信息
     * 成功：更新 weatherData 状态
     * 失败：更新 resultData 显示错误信息
     */
    private fun fetchWeather() {
        viewModelScope.launch {
            uiState.value = "Loading"
            val result = repository.fetchWeather(destination.value, days.value, preferences.value)
            when (result) {
                is AgentResult.Success -> {
                    weatherData.value = result.data
                    uiState.value = "Success"
                    Log.i(TAG, "✅ 天气数据获取成功: ${result.data.size} 天")
                }
                is AgentResult.Error -> {
                    uiState.value = "Error"
                    resultData.value = result.message
                    Log.e(TAG, "❌ 天气数据获取失败: ${result.message}")
                }
                is AgentResult.Loading -> {
                    Log.i(TAG, "⏳ 天气数据加载中...")
                }
            }
        }
    }


    /**
     * 获取酒店数据
     * 
     * 调用 repository 获取酒店推荐
     * 成功：更新 hotelData 状态
     * 失败：更新 resultData 显示错误信息
     */
    private fun fetchHotels() {
        viewModelScope.launch {
            uiState.value = "Loading"
            val result = repository.fetchHotels(destination.value, days.value, preferences.value)
            when (result) {
                is AgentResult.Success -> {
                    hotelData.value = result.data
                    uiState.value = "Success"
                    Log.i(TAG, "✅ 酒店数据获取成功: ${result.data.size} 家酒店")
                }
                is AgentResult.Error -> {
                    uiState.value = "Error"
                    resultData.value = result.message
                    Log.e(TAG, "❌ 酒店数据获取失败: ${result.message}")
                }
                is AgentResult.Loading -> {
                    Log.i(TAG, "⏳ 酒店数据加载中...")
                }
            }
        }
    }

    /**
     * 获取餐厅数据
     * 
     * 调用 repository 获取餐厅推荐
     * 成功：更新 restaurantData 状态
     * 失败：更新 resultData 显示错误信息
     */
    private fun fetchRestaurants() {
        viewModelScope.launch {
            uiState.value = "Loading"
            val result = repository.fetchRestaurants(destination.value, days.value, preferences.value)
            when (result) {
                is AgentResult.Success -> {
                    restaurantData.value = result.data
                    uiState.value = "Success"
                    Log.i(TAG, "✅ 餐厅数据获取成功: ${result.data.size} 家餐厅")
                }
                is AgentResult.Error -> {
                    uiState.value = "Error"
                    resultData.value = result.message
                    Log.e(TAG, "❌ 餐厅数据获取失败: ${result.message}")
                }
                is AgentResult.Loading -> {
                    Log.i(TAG, "⏳ 餐厅数据加载中...")
                }
            }
        }
    }

    /**
     * 获取景点数据
     * 
     * 调用 repository 获取景点推荐
     * 成功：更新 attractionData 状态
     * 失败：更新 resultData 显示错误信息
     */
    private fun fetchAttractions() {
        viewModelScope.launch {
            uiState.value = "Loading"
            val result = repository.fetchAttractions(destination.value, days.value, preferences.value)
            when (result) {
                is AgentResult.Success -> {
                    attractionData.value = result.data
                    uiState.value = "Success"
                    Log.i(TAG, "✅ 景点数据获取成功: ${result.data.size} 个景点")
                }
                is AgentResult.Error -> {
                    uiState.value = "Error"
                    resultData.value = result.message
                    Log.e(TAG, "❌ 景点数据获取失败: ${result.message}")
                }
                is AgentResult.Loading -> {
                    Log.i(TAG, "⏳ 景点数据加载中...")
                }
            }
        }
    }

    /**
     * 切换 Agent 类型
     * 
     * @param agentType 目标 Agent 类型
     */
    fun selectAgent(agentType: AgentType) {
        selectedAgent.value = agentType
        Log.i(TAG, "🔄 切换 Agent: $agentType")
    }

    /**
     * 重置为输入状态
     * 
     * 清空所有数据，恢复初始状态
     * 用于返回输入界面时调用
     */
    fun resetToInput() {
        Log.i(TAG, "🔙 重置为输入状态")
        uiState.value = "Idle"
        resultData.value = ""
        weatherData.value = emptyList()
        hotelData.value = emptyList()
        restaurantData.value = emptyList()
        attractionData.value = emptyList()
        itineraryData.value = ""
        totalSummary.value = ""
        dayPlans.value = emptyList()
        planHotels.value = emptyList()
        overallTips.value = ""
    }

    /**
     * 手动保存当前行程规划到收藏
     * 由 UI 层用户点击触发
     */
    fun saveCurrentTripPlan() {
        if (dayPlans.value.isEmpty() && planHotels.value.isEmpty()) {
            Log.w(TAG, "⚠️ 没有可保存的行程规划")
            return
        }
        
        viewModelScope.launch {
            try {
                val hotelJson = gson.toJson(planHotels.value)
                val dayPlansJson = gson.toJson(dayPlans.value)
                
                val tripPlan = com.example.trip_planner.data.local.entity.TripPlanEntity(
                    destination = destination.value,
                    days = days.value.toIntOrNull() ?: 3,
                    preferences = preferences.value,
                    hotelJson = hotelJson,
                    dayPlansJson = dayPlansJson,
                    overallTips = overallTips.value
                )
                
                tripPlanRepository.saveTripPlan(tripPlan)
                Log.i(TAG, "✅ 行程规划已收藏: ${destination.value} - ${days.value}天")
            } catch (e: Exception) {
                Log.e(TAG, "❌ 收藏行程规划失败: ${e.message}")
            }
        }
    }
}
