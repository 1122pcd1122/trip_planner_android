package com.example.trip_planner.data.local.dao

import androidx.room.*
import com.example.trip_planner.data.local.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * 搜索历史 DAO
 * 
 * 提供搜索历史数据的本地存储操作接口
 */
@Dao
interface SearchHistoryDao {

    /**
     * 查询所有搜索历史（按时间倒序）
     */
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT :limit")
    fun getSearchHistory(limit: Int = 20): Flow<List<SearchHistoryEntity>>

    /**
     * 查询所有搜索历史（非 Flow 版本）
     */
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getSearchHistoryList(limit: Int = 20): List<SearchHistoryEntity>

    /**
     * 插入搜索历史
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: SearchHistoryEntity)

    /**
     * 删除指定搜索历史
     */
    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteHistory(id: Long)

    /**
     * 清空所有搜索历史
     */
    @Query("DELETE FROM search_history")
    suspend fun clearAllHistory()

    /**
     * 删除过期的搜索历史（超过指定时间）
     * @param maxAgeMillis 最大保存时长（毫秒）
     */
    @Query("DELETE FROM search_history WHERE :currentTime - timestamp > :maxAgeMillis")
    suspend fun deleteExpiredHistory(currentTime: Long, maxAgeMillis: Long)
}
