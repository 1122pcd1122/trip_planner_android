package com.example.trip_planner.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.trip_planner.network.model.AttractionData
import com.example.trip_planner.network.model.AgentResult
import com.example.trip_planner.network.model.SpotInfo
import com.example.trip_planner.ui.screens.PoiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AttractionViewModel(application: Application) : BaseAgentViewModel(application) {
    
    private val TAG = "AttractionViewModel"
    
    private val _uiState = MutableStateFlow<UiState<List<PoiModel>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<PoiModel>>> = _uiState.asStateFlow()
    
    private val _attractionData = MutableStateFlow<List<PoiModel>>(emptyList())
    val attractionData: StateFlow<List<PoiModel>> = _attractionData.asStateFlow()
    
    private val _spotInfoList = MutableStateFlow<List<SpotInfo>>(emptyList())
    val spotInfoList: StateFlow<List<SpotInfo>> = _spotInfoList.asStateFlow()
    
    fun fetchAttractions() {
        if (getDestinationValue().isBlank()) {
            Log.w(TAG, "⚠️ 用户未输入目的地")
            _uiState.value = UiState.Error("请先输入目的地！")
            return
        }
        
        cancelPreviousJob()
        currentJob = viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            val result = cachedRepository.fetchAttractions(
                destination = getDestinationValue(),
                days = calculateDays(),
                startDate = getStartDateValue(),
                endDate = getEndDateValue(),
                preferences = getPreferencesValue()
            )
            
            when (result) {
                is AgentResult.Success -> {
                    processAttractionResponse(result.data)
                    _uiState.value = UiState.Success(_attractionData.value)
                    Log.i(TAG, "✅ 景点数据获取成功: ${_attractionData.value.size} 个")
                }
                is AgentResult.Error -> {
                    _uiState.value = UiState.Error(result.message)
                    Log.e(TAG, "❌ 景点数据获取失败: ${result.message}")
                }
                is AgentResult.Loading -> {
                    Log.i(TAG, "⏳ 景点数据加载中...")
                }
            }
        }
    }
    
    private fun processAttractionResponse(response: AttractionData) {
        _spotInfoList.value = response.spotInfoList
        _attractionData.value = response.poiList
    }
    
    fun resetState() {
        _uiState.value = UiState.Idle
        _attractionData.value = emptyList()
        _spotInfoList.value = emptyList()
        Log.i(TAG, "🔄 已重置景点状态")
    }
}