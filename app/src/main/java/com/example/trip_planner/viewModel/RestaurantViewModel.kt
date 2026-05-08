package com.example.trip_planner.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.trip_planner.network.model.AgentResult
import com.example.trip_planner.network.model.RestaurantData
import com.example.trip_planner.network.model.RestaurantInfoDto
import com.example.trip_planner.ui.screens.PoiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RestaurantViewModel(application: Application) : BaseAgentViewModel(application) {
    
    private val TAG = "RestaurantViewModel"
    
    private val _uiState = MutableStateFlow<UiState<List<PoiModel>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<PoiModel>>> = _uiState.asStateFlow()
    
    private val _restaurantData = MutableStateFlow<List<PoiModel>>(emptyList())
    val restaurantData: StateFlow<List<PoiModel>> = _restaurantData.asStateFlow()
    
    private val _restaurantInfoList = MutableStateFlow<List<RestaurantInfoDto>>(emptyList())
    val restaurantInfoList: StateFlow<List<RestaurantInfoDto>> = _restaurantInfoList.asStateFlow()
    
    fun fetchRestaurants() {
        if (getDestinationValue().isBlank()) {
            Log.w(TAG, "⚠️ 用户未输入目的地")
            _uiState.value = UiState.Error("请先输入目的地！")
            return
        }
        
        cancelPreviousJob()
        currentJob = viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            val result = cachedRepository.fetchRestaurants(
                destination = getDestinationValue(),
                days = calculateDays(),
                startDate = getStartDateValue(),
                endDate = getEndDateValue(),
                preferences = getPreferencesValue()
            )
            
            when (result) {
                is AgentResult.Success -> {
                    processRestaurantResponse(result.data)
                    _uiState.value = UiState.Success(_restaurantData.value)
                    Log.i(TAG, "✅ 餐厅数据获取成功: ${_restaurantData.value.size} 家")
                }
                is AgentResult.Error -> {
                    _uiState.value = UiState.Error(result.message)
                    Log.e(TAG, "❌ 餐厅数据获取失败: ${result.message}")
                }
                is AgentResult.Loading -> {
                    Log.i(TAG, "⏳ 餐厅数据加载中...")
                }
            }
        }
    }
    
    private fun processRestaurantResponse(response: RestaurantData) {
        _restaurantInfoList.value = response.restaurantInfoList
        _restaurantData.value = response.poiList
    }
    
    fun resetState() {
        _uiState.value = UiState.Idle
        _restaurantData.value = emptyList()
        _restaurantInfoList.value = emptyList()
        Log.i(TAG, "🔄 已重置餐厅状态")
    }
}