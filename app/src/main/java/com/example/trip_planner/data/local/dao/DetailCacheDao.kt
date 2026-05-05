package com.example.trip_planner.data.local.dao

import androidx.room.*
import com.example.trip_planner.data.local.entity.DetailCacheEntity
import kotlinx.coroutines.flow.Flow

/**
 * 详情缓存 DAO
 * 
 * 提供详情数据的本地缓存操作接口
 */
@Dao
interface DetailCacheDao {

    /**
     * 查询所有缓存的详情数据
     */
    @Query("SELECT * FROM detail_cache ORDER BY timestamp DESC")
    fun getAllCache(): Flow<List<DetailCacheEntity>>

    /**
     * 根据缓存键查询单条详情数据
     */
    @Query("SELECT * FROM detail_cache WHERE cacheKey = :cacheKey")
    suspend fun getCacheByKey(cacheKey: String): DetailCacheEntity?

    /**
     * 根据类型查询所有缓存
     */
    @Query("SELECT * FROM detail_cache WHERE type = :type ORDER BY timestamp DESC")
    suspend fun getCacheByType(type: String): List<DetailCacheEntity>

    /**
     * 插入或更新缓存数据
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: DetailCacheEntity)

    /**
     * 批量插入缓存数据
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCacheList(cacheList: List<DetailCacheEntity>)

    /**
     * 删除指定缓存
     */
    @Query("DELETE FROM detail_cache WHERE cacheKey = :cacheKey")
    suspend fun deleteCache(cacheKey: String)

    /**
     * 删除指定类型的所有缓存
     */
    @Query("DELETE FROM detail_cache WHERE type = :type")
    suspend fun deleteCacheByType(type: String)

    /**
     * 删除过期缓存（超过指定时间）
     * @param maxAgeMillis 最大缓存时长（毫秒）
     */
    @Query("DELETE FROM detail_cache WHERE :currentTime - timestamp > :maxAgeMillis")
    suspend fun deleteExpiredCache(currentTime: Long, maxAgeMillis: Long)

    /**
     * 清空所有缓存
     */
    @Query("DELETE FROM detail_cache")
    suspend fun clearAllCache()

    /**
     * 获取所有缓存（非 Flow 版本）
     */
    @Query("SELECT * FROM detail_cache ORDER BY timestamp DESC")
    suspend fun getAllCaches(): List<DetailCacheEntity>
}
