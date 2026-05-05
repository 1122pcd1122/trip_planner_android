package com.example.trip_planner.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trip_planner.data.local.TripDatabase
import com.example.trip_planner.data.local.entity.WeatherCacheEntity
import com.example.trip_planner.network.NetworkClient
import com.example.trip_planner.utils.WeatherUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 天气 ViewModel
 * 负责管理天气数据的获取和缓存
 */
class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val database = TripDatabase.getDatabase(application)
    private val weatherCacheDao = database.weatherCacheDao()
    private val apiService = NetworkClient.tripApiService

    private val _weatherData = MutableStateFlow<List<WeatherCacheEntity>>(emptyList())
    val weatherData: StateFlow<List<WeatherCacheEntity>> = _weatherData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * 获取指定城市的天气
     */
    fun fetchWeather(cityName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val weatherList = WeatherUtils.getWeather(
                    cityName = cityName,
                    apiService = apiService,
                    weatherCacheDao = weatherCacheDao
                )
                _weatherData.value = weatherList
            } catch (e: Exception) {
                _error.value = "获取天气数据失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 清除指定城市的天气缓存
     */
    fun clearWeatherCache(cityName: String) {
        viewModelScope.launch {
            weatherCacheDao.deleteWeatherByCity(cityName)
        }
    }

    /**
     * 清除所有天气缓存
     */
    fun clearAllCache() {
        viewModelScope.launch {
            weatherCacheDao.clearAll()
        }
    }
}
