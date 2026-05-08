package com.example.trip_planner.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trip_planner.data.repository.CachedTripRepository
import com.example.trip_planner.utils.DateUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseAgentViewModel(application: Application) : AndroidViewModel(application) {
    
    protected val cachedRepository by lazy { CachedTripRepository(getApplication()) }
    protected var currentJob: Job? = null
    
    private val _destination = MutableStateFlow("成都")
    val destination: StateFlow<String> = _destination.asStateFlow()
    
    private val _startDate = MutableStateFlow("")
    val startDate: StateFlow<String> = _startDate.asStateFlow()
    
    private val _endDate = MutableStateFlow("")
    val endDate: StateFlow<String> = _endDate.asStateFlow()
    
    private val _preferences = MutableStateFlow("")
    val preferences: StateFlow<String> = _preferences.asStateFlow()
    
    fun setDestination(value: String) {
        _destination.value = value
    }
    
    fun setStartDate(value: String) {
        _startDate.value = value
    }
    
    fun setEndDate(value: String) {
        _endDate.value = value
    }
    
    fun setPreferences(value: String) {
        _preferences.value = value
    }
    
    protected fun getDestinationValue(): String = _destination.value
    protected fun getStartDateValue(): String = _startDate.value
    protected fun getEndDateValue(): String = _endDate.value
    protected fun getPreferencesValue(): String = _preferences.value
    
    protected fun calculateDays(): String {
        return DateUtils.calculateDays(_startDate.value, _endDate.value)
    }
    
    fun cancelRequest() {
        currentJob?.cancel()
    }
    
    protected fun cancelPreviousJob() {
        currentJob?.cancel()
    }
}