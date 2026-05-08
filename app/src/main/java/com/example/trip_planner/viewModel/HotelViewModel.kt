package com.example.trip_planner.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.trip_planner.network.model.AgentResult
import com.example.trip_planner.network.model.HotelData
import com.example.trip_planner.network.model.HotelInfoDto
import com.example.trip_planner.ui.screens.PoiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HotelViewModel(application: Application) : BaseAgentViewModel(application) {
    
    private val TAG = "HotelViewModel"
    
    private val _uiState = MutableStateFlow<UiState<List<PoiModel>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<PoiModel>>> = _uiState.asStateFlow()
    
    private val _hotelData = MutableStateFlow<List<PoiModel>>(emptyList())
    val hotelData: StateFlow<List<PoiModel>> = _hotelData.asStateFlow()
    
    private val _hotelInfoList = MutableStateFlow<List<HotelInfoDto>>(emptyList())
    val hotelInfoList: StateFlow<List<HotelInfoDto>> = _hotelInfoList.asStateFlow()
    
    fun fetchHotels() {
        if (getDestinationValue().isBlank()) {
            Log.w(TAG, "⚠️ 用户未输入目的地")
            _uiState.value = UiState.Error("请先输入目的地！")
            return
        }
        
        cancelPreviousJob()
        currentJob = viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            val result = cachedRepository.fetchHotels(
                destination = getDestinationValue(),
                days = calculateDays(),
                startDate = getStartDateValue(),
                endDate = getEndDateValue(),
                preferences = getPreferencesValue()
            )
            
            when (result) {
                is AgentResult.Success -> {
                    processHotelResponse(result.data)
                    _uiState.value = UiState.Success(_hotelData.value)
                    Log.i(TAG, "✅ 酒店数据获取成功: ${_hotelData.value.size} 家")
                }
                is AgentResult.Error -> {
                    _uiState.value = UiState.Error(result.message)
                    Log.e(TAG, "❌ 酒店数据获取失败: ${result.message}")
                }
                is AgentResult.Loading -> {
                    Log.i(TAG, "⏳ 酒店数据加载中...")
                }
            }
        }
    }
    
    private fun processHotelResponse(response: HotelData) {
        _hotelInfoList.value = response.hotelInfoList
        _hotelData.value = response.poiList
    }
    
    fun resetState() {
        _uiState.value = UiState.Idle
        _hotelData.value = emptyList()
        _hotelInfoList.value = emptyList()
        Log.i(TAG, "🔄 已重置酒店状态")
    }
}