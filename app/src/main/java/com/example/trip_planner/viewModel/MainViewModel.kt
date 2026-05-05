package com.example.trip_planner.viewModel

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trip_planner.data.local.TripDatabase
import com.example.trip_planner.data.local.entity.TripPlanEntity
import com.example.trip_planner.data.repository.AuthRepository
import com.example.trip_planner.data.repository.CachedTripRepository
import com.example.trip_planner.data.repository.TripPlanRepository
import com.example.trip_planner.network.TripRepository
import com.example.trip_planner.network.model.AgentResult
import com.example.trip_planner.network.model.DayPlan
import com.example.trip_planner.network.model.HotelInfoDto
import com.example.trip_planner.network.model.PlanHotel
import com.example.trip_planner.network.model.RestaurantInfoDto
import com.example.trip_planner.network.model.SpotInfo
import com.example.trip_planner.network.model.WeatherResponse
import com.example.trip_planner.ui.screens.AgentType
import com.example.trip_planner.ui.screens.PoiModel
import com.example.trip_planner.ui.screens.PoiType
import com.example.trip_planner.utils.NetworkMonitor
import com.example.trip_planner.utils.UserPreferences
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "MainViewModel"

    private val tripPlanRepository: TripPlanRepository
    private val authRepository = AuthRepository()

    init {
        val database = TripDatabase.getDatabase(application)
        tripPlanRepository = TripPlanRepository(database.tripPlanDao())
    }

    private val _destination = MutableStateFlow("成都")
    val destination: StateFlow<String> = _destination.asStateFlow()
    
    private val _days = MutableStateFlow("3")
    val days: StateFlow<String> = _days.asStateFlow()
    
    private val _startDate = MutableStateFlow("")
    val startDate: StateFlow<String> = _startDate.asStateFlow()
    
    private val _endDate = MutableStateFlow("")
    val endDate: StateFlow<String> = _endDate.asStateFlow()
    
    private val _preferences = MutableStateFlow("")
    val preferences: StateFlow<String> = _preferences.asStateFlow()

    val agentUiStates = mutableStateMapOf<AgentType, String>(
        AgentType.ALL to "Idle",
        AgentType.WEATHER to "Idle",
        AgentType.HOTEL to "Idle",
        AgentType.RESTAURANT to "Idle",
        AgentType.ATTRACTION to "Idle"
    )

    fun getCurrentAgentUiState(): String {
        return agentUiStates[selectedAgent.value] ?: "Idle"
    }

    fun setAgentUiState(agentType: AgentType, state: String) {
        agentUiStates[agentType] = state
    }

    private val _resultData = MutableStateFlow("")
    val resultData: StateFlow<String> = _resultData.asStateFlow()
    
    private val _selectedAgent = MutableStateFlow(AgentType.ALL)
    val selectedAgent: StateFlow<AgentType> = _selectedAgent.asStateFlow()
    
    private val _weatherState = MutableStateFlow<UiState<List<WeatherResponse>>>(UiState.Idle)
    val weatherState: StateFlow<UiState<List<WeatherResponse>>> = _weatherState.asStateFlow()
    private val _weatherData = MutableStateFlow<List<WeatherResponse>>(emptyList())
    val weatherData: StateFlow<List<WeatherResponse>> = _weatherData.asStateFlow()

    private val _hotelState = MutableStateFlow<UiState<List<PoiModel>>>(UiState.Idle)
    val hotelState: StateFlow<UiState<List<PoiModel>>> = _hotelState.asStateFlow()
    private val _hotelData = MutableStateFlow<List<PoiModel>>(emptyList())
    val hotelData: StateFlow<List<PoiModel>> = _hotelData.asStateFlow()
    private val _hotelInfoList = MutableStateFlow<List<HotelInfoDto>>(emptyList())
    val hotelInfoList: StateFlow<List<HotelInfoDto>> = _hotelInfoList.asStateFlow()

    private val _restaurantState = MutableStateFlow<UiState<List<PoiModel>>>(UiState.Idle)
    val restaurantState: StateFlow<UiState<List<PoiModel>>> = _restaurantState.asStateFlow()
    private val _restaurantData = MutableStateFlow<List<PoiModel>>(emptyList())
    val restaurantData: StateFlow<List<PoiModel>> = _restaurantData.asStateFlow()
    private val _restaurantInfoList = MutableStateFlow<List<RestaurantInfoDto>>(emptyList())
    val restaurantInfoList: StateFlow<List<RestaurantInfoDto>> = _restaurantInfoList.asStateFlow()

    private val _attractionState = MutableStateFlow<UiState<List<PoiModel>>>(UiState.Idle)
    val attractionState: StateFlow<UiState<List<PoiModel>>> = _attractionState.asStateFlow()
    private val _attractionData = MutableStateFlow<List<PoiModel>>(emptyList())
    val attractionData: StateFlow<List<PoiModel>> = _attractionData.asStateFlow()
    private val _spotInfoList = MutableStateFlow<List<SpotInfo>>(emptyList())
    val spotInfoList: StateFlow<List<SpotInfo>> = _spotInfoList.asStateFlow()

    private val _allInOneState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val allInOneState: StateFlow<UiState<Unit>> = _allInOneState.asStateFlow()
    private val _dayPlans = MutableStateFlow<List<DayPlan>>(emptyList())
    val dayPlans: StateFlow<List<DayPlan>> = _dayPlans.asStateFlow()
    private val _planHotels = MutableStateFlow<List<PlanHotel>>(emptyList())
    val planHotels: StateFlow<List<PlanHotel>> = _planHotels.asStateFlow()
    private val _overallTips = MutableStateFlow("")
    val overallTips: StateFlow<String> = _overallTips.asStateFlow()

    private val _isPlanSaved = MutableStateFlow(false)
    val isPlanSaved: StateFlow<Boolean> = _isPlanSaved.asStateFlow()
    private val _currentSavedPlan = MutableStateFlow<TripPlanEntity?>(null)
    val currentSavedPlan: StateFlow<TripPlanEntity?> = _currentSavedPlan.asStateFlow()

    private val repository = TripRepository()
    private val cachedRepository by lazy { CachedTripRepository(getApplication()) }
    private val networkMonitor by lazy { NetworkMonitor(getApplication()) }

    val isNetworkAvailable: Boolean
        get() = networkMonitor.isNetworkAvailable()

    fun setDestination(value: String) { _destination.value = value }
    fun setDays(value: String) { _days.value = value }
    fun setStartDate(value: String) { _startDate.value = value }
    fun setEndDate(value: String) { _endDate.value = value }
    fun setPreferences(value: String) { _preferences.value = value }
    fun setSelectedAgent(value: AgentType) { _selectedAgent.value = value }
    fun setResultData(value: String) { _resultData.value = value }
    fun setIsPlanSaved(value: Boolean) { _isPlanSaved.value = value }

    private var weatherJob: Job? = null
    private var hotelJob: Job? = null
    private var restaurantJob: Job? = null
    private var attractionJob: Job? = null
    private var allInOneJob: Job? = null

    fun generateTripPlan() {
        if (_destination.value.isBlank()) {
            Log.w(TAG, "⚠️ 用户未输入目的地就点击了生成")
            setResultData("请先输入目的地！")
            return
        }

        setIsPlanSaved(false)

        val agentType = _selectedAgent.value
        Log.i(TAG, "🎯 触发 [$agentType] Agent 请求")
        
        setAgentUiState(agentType, "Loading")
        setResultData("")

        when (agentType) {
            AgentType.WEATHER -> fetchWeather()
            AgentType.HOTEL -> fetchHotels()
            AgentType.RESTAURANT -> fetchRestaurants()
            AgentType.ATTRACTION -> fetchAttractions()
            AgentType.ALL -> fetchAllInOne()
        }
    }

    fun cancelCurrentRequest() {
        weatherJob?.cancel()
        hotelJob?.cancel()
        restaurantJob?.cancel()
        attractionJob?.cancel()
        allInOneJob?.cancel()
        Log.i(TAG, "🛑 已取消当前请求")
    }

    private fun fetchAllInOne() {
        allInOneJob?.cancel()
        allInOneJob = viewModelScope.launch {
            _allInOneState.value = UiState.Loading
            setAgentUiState(AgentType.ALL, "Loading")
            val result = cachedRepository.fetchAllInOne(
                destination = _destination.value,
                days = _days.value,
                startDate = _startDate.value,
                endDate = _endDate.value,
                preferences = _preferences.value
            )
            when (result) {
                is AgentResult.Success -> {
                    val planData = result.data
                    
                    _dayPlans.value = planData.days
                    Log.i(TAG, "✅ 每日行程解析: ${planData.days.size} 天")
                    
                    _planHotels.value = planData.hotel
                    Log.i(TAG, "✅ 酒店解析: ${planData.hotel.size} 家")
                    
                    _overallTips.value = planData.overallTips
                    Log.i(TAG, "✅ 整体建议: ${planData.overallTips}")
                    
                    val weatherList = planData.days.map { day ->
                        WeatherResponse(
                            cityName = _destination.value,
                            latitude = "",
                            longitude = "",
                            date = day.date,
                            weather = day.weather,
                            temperature = "",
                            tips = day.tips
                        )
                    }
                    _weatherData.value = weatherList
                    _weatherState.value = UiState.Success(weatherList)
                    
                    val hotelPois = planData.hotel.map { hotel ->
                        PoiModel(
                            name = hotel.name,
                            rating = "",
                            price = hotel.price,
                            distance = "",
                            latLng = LatLng(
                                hotel.latitude.toDoubleOrNull() ?: 0.0,
                                hotel.longitude.toDoubleOrNull() ?: 0.0
                            ),
                            desc = "${hotel.address}\n${hotel.advantage}",
                            priceRange = hotel.price,
                            featureDish = "",
                            poiType = PoiType.HOTEL
                        )
                    }
                    _hotelData.value = hotelPois
                    _hotelState.value = UiState.Success(hotelPois)
                    
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
                                        latLng = LatLng(0.0, 0.0),
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
                                        latLng = LatLng(0.0, 0.0),
                                        desc = dinner.address,
                                        priceRange = "",
                                        featureDish = dinner.dish,
                                        poiType = PoiType.RESTAURANT
                                    )
                                )
                            }
                        }
                    }
                    _restaurantData.value = restaurantPois
                    _restaurantState.value = UiState.Success(restaurantPois)
                    Log.i(TAG, "✅ 餐厅解析: ${restaurantPois.size} 家")
                    
                    val attractionPois = mutableListOf<PoiModel>()
                    planData.days.forEach { day ->
                        day.itinerary.forEach { item ->
                            attractionPois.add(
                                PoiModel(
                                    name = item.spot,
                                    rating = "",
                                    price = "",
                                    distance = "",
                                    latLng = LatLng(
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
                    _attractionData.value = attractionPois
                    _attractionState.value = UiState.Success(attractionPois)
                    Log.i(TAG, "✅ 景点解析: ${attractionPois.size} 个")
                    
                    setIsPlanSaved(false)
                    setAgentUiState(AgentType.ALL, "Success")
                    _allInOneState.value = UiState.Success(Unit)
                    Log.i(TAG, "✅ 一键生成完成")
                }
                is AgentResult.Error -> {
                    setAgentUiState(AgentType.ALL, "Error")
                    setResultData(result.message)
                    _allInOneState.value = UiState.Error(result.message)
                    Log.e(TAG, "❌ 一键生成失败: ${result.message}")
                }

                else -> {
                    Log.w(TAG, "⚠️ 一键生成返回未处理状态: $result")
                }
            }
        }
    }

    private fun fetchWeather() {
        weatherJob?.cancel()
        weatherJob = viewModelScope.launch {
            _weatherState.value = UiState.Loading
            setAgentUiState(AgentType.WEATHER, "Loading")
            val result = cachedRepository.fetchWeather(
                destination = _destination.value,
                days = _days.value,
                startDate = _startDate.value,
                endDate = _endDate.value,
                preferences = _preferences.value
            )
            when (result) {
                is AgentResult.Success -> {
                    _weatherData.value = result.data
                    _weatherState.value = UiState.Success(result.data)
                    setAgentUiState(AgentType.WEATHER, "Success")
                    Log.i(TAG, "✅ 天气数据获取成功: ${result.data.size} 天")
                }
                is AgentResult.Error -> {
                    _weatherState.value = UiState.Error(result.message)
                    setAgentUiState(AgentType.WEATHER, "Error")
                    setResultData(result.message)
                    Log.e(TAG, "❌ 天气数据获取失败: ${result.message}")
                }
                is AgentResult.Loading -> {
                    Log.i(TAG, "⏳ 天气数据加载中...")
                }
            }
        }
    }


    private fun fetchHotels() {
        hotelJob?.cancel()
        hotelJob = viewModelScope.launch {
            _hotelState.value = UiState.Loading
            setAgentUiState(AgentType.HOTEL, "Loading")
            val result = cachedRepository.fetchHotels(
                destination = _destination.value,
                days = _days.value,
                startDate = _startDate.value,
                endDate = _endDate.value,
                preferences = _preferences.value
            )
            when (result) {
                is AgentResult.Success -> {
                    val hotelData = result.data
                    _hotelInfoList.value = hotelData.hotelInfoList
                    _hotelData.value = hotelData.poiList
                    _hotelState.value = UiState.Success(hotelData.poiList)
                    setAgentUiState(AgentType.HOTEL, "Success")
                    Log.i(TAG, "✅ 酒店数据获取成功: ${hotelData.poiList.size} 家酒店")
                }
                is AgentResult.Error -> {
                    _hotelState.value = UiState.Error(result.message)
                    setAgentUiState(AgentType.HOTEL, "Error")
                    setResultData(result.message)
                    Log.e(TAG, "❌ 酒店数据获取失败: ${result.message}")
                }
                is AgentResult.Loading -> {
                    Log.i(TAG, "⏳ 酒店数据加载中...")
                }
            }
        }
    }

    private fun fetchRestaurants() {
        restaurantJob?.cancel()
        restaurantJob = viewModelScope.launch {
            _restaurantState.value = UiState.Loading
            setAgentUiState(AgentType.RESTAURANT, "Loading")
            val result = cachedRepository.fetchRestaurants(
                destination = _destination.value,
                days = _days.value,
                startDate = _startDate.value,
                endDate = _endDate.value,
                preferences = _preferences.value
            )
            when (result) {
                is AgentResult.Success -> {
                    val restaurantData = result.data
                    _restaurantInfoList.value = restaurantData.restaurantInfoList
                    _restaurantData.value = restaurantData.poiList
                    _restaurantState.value = UiState.Success(restaurantData.poiList)
                    setAgentUiState(AgentType.RESTAURANT, "Success")
                    Log.i(TAG, "✅ 餐厅数据获取成功: ${restaurantData.poiList.size} 家餐厅")
                }
                is AgentResult.Error -> {
                    _restaurantState.value = UiState.Error(result.message)
                    setAgentUiState(AgentType.RESTAURANT, "Error")
                    setResultData(result.message)
                    Log.e(TAG, "❌ 餐厅数据获取失败: ${result.message}")
                }
                is AgentResult.Loading -> {
                    Log.i(TAG, "⏳ 餐厅数据加载中...")
                }
            }
        }
    }

    private fun fetchAttractions() {
        attractionJob?.cancel()
        attractionJob = viewModelScope.launch {
            _attractionState.value = UiState.Loading
            setAgentUiState(AgentType.ATTRACTION, "Loading")
            val result = cachedRepository.fetchAttractions(
                destination = _destination.value,
                days = _days.value,
                startDate = _startDate.value,
                endDate = _endDate.value,
                preferences = _preferences.value
            )
            when (result) {
                is AgentResult.Success -> {
                    val attractionData = result.data
                    _spotInfoList.value = attractionData.spotInfoList
                    _attractionData.value = attractionData.poiList
                    _attractionState.value = UiState.Success(attractionData.poiList)
                    setAgentUiState(AgentType.ATTRACTION, "Success")
                    Log.i(TAG, "✅ 景点数据获取成功: ${attractionData.poiList.size} 个景点")
                }
                is AgentResult.Error -> {
                    _attractionState.value = UiState.Error(result.message)
                    setAgentUiState(AgentType.ATTRACTION, "Error")
                    setResultData(result.message)
                    Log.e(TAG, "❌ 景点数据获取失败: ${result.message}")
                }
                is AgentResult.Loading -> {
                    Log.i(TAG, "⏳ 景点数据加载中...")
                }
            }
        }
    }

    /**
     * 手动保存行程到历史记录
     * 由用户点击保存按钮触发
     * @return 保存是否成功
     */
    fun savePlanToHistory(): Boolean {
        if (_dayPlans.value.isEmpty() && _planHotels.value.isEmpty()) {
            Log.w(TAG, "⚠️ 无行程数据，无法保存")
            return false
        }
        
        viewModelScope.launch {
            try {
                val dayPlansJson = Json.encodeToString(_dayPlans.value)
                val hotelJson = Json.encodeToString(_planHotels.value)
                
                val plan = TripPlanEntity(
                    destination = _destination.value,
                    days = _days.value.toIntOrNull() ?: 1,
                    preferences = _preferences.value,
                    hotelJson = hotelJson,
                    dayPlansJson = dayPlansJson,
                    overallTips = _overallTips.value
                )
                tripPlanRepository.saveTripPlan(plan)
                setIsPlanSaved(true)
                _currentSavedPlan.value = plan
                
                val tripId = "trip_${System.currentTimeMillis()}"
                val tripData = Json.encodeToString(
                    TripPlanSyncData(
                        destination = _destination.value,
                        days = _days.value,
                        startDate = _startDate.value,
                        endDate = _endDate.value,
                        preferences = _preferences.value,
                        dayPlansJson = dayPlansJson,
                        hotelJson = hotelJson,
                        overallTips = _overallTips.value
                    )
                )
                saveTripToCloud(
                    tripId = tripId,
                    destination = _destination.value,
                    days = _days.value.toIntOrNull() ?: 1,
                    startDate = _startDate.value,
                    endDate = _endDate.value,
                    preferences = _preferences.value,
                    tripData = tripData
                )
                
                Log.i(TAG, "✅ 已保存到历史记录，包含 ${_dayPlans.value.size} 天行程和 ${_planHotels.value.size} 家酒店")
            } catch (e: Exception) {
                Log.e(TAG, "❌ 保存历史记录失败: ${e.message}")
            }
        }
        return true
    }

    fun saveTripToCloud(
        tripId: String,
        destination: String,
        days: Int,
        startDate: String,
        endDate: String,
        preferences: String,
        tripData: String
    ) {
        viewModelScope.launch {
            try {
                val token = UserPreferences.getToken(getApplication())
                if (token.isNotEmpty()) {
                    authRepository.saveTripToCloud(
                        token = token,
                        tripId = tripId,
                        destination = destination,
                        days = days,
                        startDate = startDate,
                        endDate = endDate,
                        preferences = preferences,
                        tripData = tripData
                    )
                    Log.i(TAG, "✅ 行程已同步到云端")
                }
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ 云端同步失败: ${e.message}")
            }
        }
    }

    /**
     * 重置保存状态
     * 当用户重新规划行程时调用
     */
    fun resetSaveState() {
        setIsPlanSaved(false)
        _currentSavedPlan.value = null
    }

    /**
     * 删除指定天的指定景点
     * @param dayIndex 天的索引（从0开始）
     * @param itemIndex 景点的索引（从0开始）
     */
    fun removeItineraryItem(dayIndex: Int, itemIndex: Int) {
        val updatedPlans = _dayPlans.value.toMutableList()
        if (dayIndex in updatedPlans.indices) {
            val day = updatedPlans[dayIndex]
            val updatedItinerary = day.itinerary.toMutableList()
            if (itemIndex in updatedItinerary.indices) {
                updatedItinerary.removeAt(itemIndex)
                updatedPlans[dayIndex] = day.copy(itinerary = updatedItinerary)
                _dayPlans.value = updatedPlans
                Log.i(TAG, "✅ 已删除第${dayIndex + 1}天第${itemIndex + 1}个景点")
            }
        }
    }

    /**
     * 删除指定天的午餐
     * @param dayIndex 天的索引（从0开始）
     */
    fun removeLunch(dayIndex: Int) {
        val updatedPlans = _dayPlans.value.toMutableList()
        if (dayIndex in updatedPlans.indices) {
            val day = updatedPlans[dayIndex]
            val updatedMeals = day.meals?.copy(lunch = null)
            updatedPlans[dayIndex] = day.copy(meals = updatedMeals)
            _dayPlans.value = updatedPlans
            Log.i(TAG, "✅ 已删除第${dayIndex + 1}天午餐")
        }
    }

    /**
     * 删除指定天的晚餐
     * @param dayIndex 天的索引（从0开始）
     */
    fun removeDinner(dayIndex: Int) {
        val updatedPlans = _dayPlans.value.toMutableList()
        if (dayIndex in updatedPlans.indices) {
            val day = updatedPlans[dayIndex]
            val updatedMeals = day.meals?.copy(dinner = null)
            updatedPlans[dayIndex] = day.copy(meals = updatedMeals)
            _dayPlans.value = updatedPlans
            Log.i(TAG, "✅ 已删除第${dayIndex + 1}天晚餐")
        }
    }

    /**
     * 移动景点位置
     * @param dayIndex 天的索引（从0开始）
     * @param fromIndex 原位置
     * @param toIndex 目标位置
     */
    fun moveItineraryItem(dayIndex: Int, fromIndex: Int, toIndex: Int) {
        val updatedPlans = _dayPlans.value.toMutableList()
        if (dayIndex in updatedPlans.indices) {
            val day = updatedPlans[dayIndex]
            val updatedItinerary = day.itinerary.toMutableList()
            if (fromIndex in updatedItinerary.indices && toIndex in updatedItinerary.indices) {
                val item = updatedItinerary.removeAt(fromIndex)
                updatedItinerary.add(toIndex, item)
                updatedPlans[dayIndex] = day.copy(itinerary = updatedItinerary)
                _dayPlans.value = updatedPlans
                Log.i(TAG, "✅ 已移动第${dayIndex + 1}天景点：$fromIndex -> $toIndex")
            }
        }
    }

    /**
     * 更新行程计划（用于编辑器保存）
     */
    fun updatePlans(updatedDays: List<DayPlan>, updatedHotels: List<PlanHotel>, updatedTips: String) {
        _dayPlans.value = updatedDays
        _planHotels.value = updatedHotels
        _overallTips.value = updatedTips
        Log.i(TAG, "✅ 已更新行程计划: ${updatedDays.size}天, ${updatedHotels.size}家酒店")
    }

    /**
     * 从历史记录加载行程规划
     */
    fun loadTripPlanFromHistory(planId: Long) {
        viewModelScope.launch {
            try {
                val plan = tripPlanRepository.getTripPlanById(planId)
                if (plan != null) {
                    setDestination(plan.destination)
                    setDays(plan.days.toString())
                    setPreferences(plan.preferences)
                    _overallTips.value = plan.overallTips
                    
                    if (plan.dayPlansJson.isNotEmpty()) {
                        _dayPlans.value = Json.decodeFromString(plan.dayPlansJson)
                        Log.i(TAG, "✅ 加载历史记录: ${_dayPlans.value.size} 天行程")
                    }
                    
                    if (plan.hotelJson.isNotEmpty()) {
                        _planHotels.value = Json.decodeFromString(plan.hotelJson)
                        Log.i(TAG, "✅ 加载历史记录: ${_planHotels.value.size} 家酒店")
                    }
                    
                    setAgentUiState(AgentType.ALL, "Success")
                    setSelectedAgent(AgentType.ALL)
                } else {
                    Log.e(TAG, "❌ 未找到历史记录: $planId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ 加载历史记录失败: ${e.message}")
            }
        }
    }

    /**
     * 清除所有缓存
     */
    fun clearCache() {
        viewModelScope.launch {
            cachedRepository.clearAllCache()
            Log.i(TAG, "✅ 已清除所有缓存")
        }
    }

    /**
     * 获取缓存统计信息
     */
    suspend fun getCacheStats(): String {
        return cachedRepository.getCacheStats()
    }
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class TripPlanSyncData(
    val destination: String,
    val days: String,
    val startDate: String,
    val endDate: String,
    val preferences: String,
    val dayPlansJson: String,
    val hotelJson: String,
    val overallTips: String
)
