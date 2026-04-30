package com.example.trip_planner.data.repository

import com.example.trip_planner.data.local.dao.TripPlanDao
import com.example.trip_planner.data.local.entity.TripPlanEntity
import kotlinx.coroutines.flow.Flow

/**
 * 行程规划仓库
 * 
 * 负责行程规划数据的业务逻辑，封装 DAO 操作
 */
class TripPlanRepository(private val tripPlanDao: TripPlanDao) {

    /**
     * 获取所有行程规划
     */
    fun getAllTripPlans(): Flow<List<TripPlanEntity>> = tripPlanDao.getAllTripPlans()

    /**
     * 获取单个行程规划
     */
    suspend fun getTripPlanById(id: Long): TripPlanEntity? = tripPlanDao.getTripPlanById(id)

    /**
     * 保存行程规划
     */
    suspend fun saveTripPlan(tripPlan: TripPlanEntity): Long = tripPlanDao.insertTripPlan(tripPlan)

    /**
     * 删除行程规划
     */
    suspend fun deleteTripPlan(tripPlan: TripPlanEntity) = tripPlanDao.deleteTripPlan(tripPlan)

    /**
     * 删除行程规划
     */
    suspend fun deleteTripPlanById(id: Long) = tripPlanDao.deleteTripPlanById(id)

    /**
     * 清空所有行程规划
     */
    suspend fun deleteAllTripPlans() = tripPlanDao.deleteAllTripPlans()
}
