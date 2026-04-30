package com.example.trip_planner.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trip_planner.data.local.TripDatabase
import com.example.trip_planner.data.local.entity.TripPlanEntity
import com.example.trip_planner.data.repository.TripPlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 行程规划 ViewModel
 * 
 * 负责管理行程规划的业务逻辑和数据状态
 */
class TripPlanViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TripPlanRepository

    private val _allTripPlans = MutableStateFlow<List<TripPlanEntity>>(emptyList())
    val allTripPlans: StateFlow<List<TripPlanEntity>> = _allTripPlans.asStateFlow()

    init {
        val database = TripDatabase.getDatabase(application)
        repository = TripPlanRepository(database.tripPlanDao())
        loadAllTripPlans()
    }

    /**
     * 加载所有行程规划
     */
    private fun loadAllTripPlans() {
        viewModelScope.launch {
            repository.getAllTripPlans().collect { plans ->
                _allTripPlans.value = plans
            }
        }
    }

    /**
     * 保存行程规划
     */
    fun saveTripPlan(
        destination: String,
        days: Int,
        preferences: String,
        hotelJson: String,
        dayPlansJson: String,
        overallTips: String
    ) {
        viewModelScope.launch {
            val tripPlan = TripPlanEntity(
                destination = destination,
                days = days,
                preferences = preferences,
                hotelJson = hotelJson,
                dayPlansJson = dayPlansJson,
                overallTips = overallTips
            )
            repository.saveTripPlan(tripPlan)
        }
    }

    /**
     * 删除行程规划
     */
    fun deleteTripPlan(id: Long) {
        viewModelScope.launch {
            repository.deleteTripPlanById(id)
        }
    }

    /**
     * 清空所有行程规划
     */
    fun deleteAllTripPlans() {
        viewModelScope.launch {
            repository.deleteAllTripPlans()
        }
    }
}
