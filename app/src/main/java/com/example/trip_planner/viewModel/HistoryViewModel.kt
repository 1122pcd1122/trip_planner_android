package com.example.trip_planner.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trip_planner.data.local.TripDatabase
import com.example.trip_planner.data.local.entity.TripPlanEntity
import com.example.trip_planner.data.repository.TripPlanRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 历史记录 ViewModel
 * 
 * 负责管理已保存行程的业务逻辑和数据状态
 * 提供行程列表的加载、删除等功能
 * 
 * 主要功能：
 * - 从本地数据库加载所有已保存的行程
 * - 使用 Flow 实现数据自动更新
 * - 提供删除行程的方法
 */
class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    /** 行程仓库实例，负责数据库操作 */
    private val repository: TripPlanRepository

    /**
     * 所有已保存的行程列表
     * 使用 stateIn 将冷 Flow 转换为热 Flow，订阅者断开 5 秒后自动停止收集
     */
    val allPlans: StateFlow<List<TripPlanEntity>>

    init {
        // 初始化数据库和仓库
        val database = TripDatabase.getDatabase(application)
        repository = TripPlanRepository(database.tripPlanDao())
        // 启动收集行程列表数据
        allPlans = repository.getAllTripPlans()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    /**
     * 获取指定用户的行程列表
     */
    fun getPlansByUserId(userId: Long): StateFlow<List<TripPlanEntity>> {
        return repository.getTripPlansByUserId(userId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    /**
     * 删除指定行程
     * @param planId 行程 ID
     */
    fun deletePlan(planId: Long) {
        viewModelScope.launch {
            repository.deleteTripPlanById(planId)
        }
    }

    /**
     * 插入行程（用于撤销删除）
     * @param plan 行程实体
     */
    fun insertPlan(plan: TripPlanEntity) {
        viewModelScope.launch {
            repository.saveTripPlan(plan)
        }
    }
}
