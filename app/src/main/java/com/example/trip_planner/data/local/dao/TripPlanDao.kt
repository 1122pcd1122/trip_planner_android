package com.example.trip_planner.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.trip_planner.data.local.entity.TripPlanEntity
import kotlinx.coroutines.flow.Flow

/**
 * 行程规划数据访问对象
 * 
 * 提供对行程规划表的增删改查操作
 */
@Dao
interface TripPlanDao {

    /**
     * 获取所有行程规划（按时间倒序）
     */
    @Query("SELECT * FROM trip_plans ORDER BY timestamp DESC")
    fun getAllTripPlans(): Flow<List<TripPlanEntity>>

    /**
     * 获取单个行程规划
     */
    @Query("SELECT * FROM trip_plans WHERE id = :id")
    suspend fun getTripPlanById(id: Long): TripPlanEntity?

    /**
     * 保存行程规划
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTripPlan(tripPlan: TripPlanEntity): Long

    /**
     * 删除行程规划
     */
    @Delete
    suspend fun deleteTripPlan(tripPlan: TripPlanEntity)

    /**
     * 删除指定 ID 的行程规划
     */
    @Query("DELETE FROM trip_plans WHERE id = :id")
    suspend fun deleteTripPlanById(id: Long)

    /**
     * 清空所有行程规划
     */
    @Query("DELETE FROM trip_plans")
    suspend fun deleteAllTripPlans()

    /**
     * 获取行程规划数量
     */
    @Query("SELECT COUNT(*) FROM trip_plans")
    suspend fun getTripPlanCount(): Int
}
