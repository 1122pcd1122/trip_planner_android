package com.example.trip_planner.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.trip_planner.network.model.AgentResult
import com.example.trip_planner.network.model.WeatherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(application: Application) : BaseAgentViewModel(application) {
    
    private val TAG = "WeatherViewModel"
    
    private val _uiState = MutableStateFlow<UiState<List<WeatherResponse>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<WeatherResponse>>> = _uiState.asStateFlow()
    
    private val _weatherData = MutableStateFlow<List<WeatherResponse>>(emptyList())
    val weatherData: StateFlow<List<WeatherResponse>> = _weatherData.asStateFlow()
    
    fun fetchWeather() {
        if (getDestinationValue().isBlank()) {
            Log.w(TAG, "⚠️ 用户未输入目的地")
            _uiState.value = UiState.Error("请先输入目的地！")
            return
        }
        
        cancelPreviousJob()
        currentJob = viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            val result = cachedRepository.fetchWeather(
                destination = getDestinationValue(),
                days = calculateDays(),
                startDate = getStartDateValue(),
                endDate = getEndDateValue(),
                preferences = getPreferencesValue()
            )
            
            when (result) {
                is AgentResult.Success -> {
                    _weatherData.value = result.data
                    _uiState.value = UiState.Success(result.data)
                    Log.i(TAG, "✅ 天气数据获取成功: ${result.data.size} 条")
                }
                is AgentResult.Error -> {
                    _uiState.value = UiState.Error(result.message)
                    Log.e(TAG, "❌ 天气数据获取失败: ${result.message}")
                }
                is AgentResult.Loading -> {
                    Log.i(TAG, "⏳ 天气数据加载中...")
                }
            }
        }
    }
    
    fun resetState() {
        _uiState.value = UiState.Idle
        _weatherData.value = emptyList()
        Log.i(TAG, "🔄 已重置天气状态")
    }
}