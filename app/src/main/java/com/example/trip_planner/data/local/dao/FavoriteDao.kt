package com.example.trip_planner.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.trip_planner.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

/**
 * 收藏数据访问对象
 * 
 * 提供对收藏表的增删改查操作，支持按类型筛选
 */
@Dao
interface FavoriteDao {

    /**
     * 获取所有收藏（按时间倒序）
     */
    @Query("SELECT * FROM favorites ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    /**
     * 按类型获取收藏
     */
    @Query("SELECT * FROM favorites WHERE type = :type ORDER BY timestamp DESC")
    fun getFavoritesByType(type: String): Flow<List<FavoriteEntity>>

    /**
     * 检查是否已收藏
     */
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE itemId = :itemId)")
    suspend fun isFavorite(itemId: String): Boolean

    /**
     * 添加收藏
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    /**
     * 移除收藏
     */
    @Query("DELETE FROM favorites WHERE itemId = :itemId")
    suspend fun deleteFavorite(itemId: String)

    /**
     * 清空所有收藏
     */
    @Query("DELETE FROM favorites")
    suspend fun deleteAllFavorites()

    /**
     * 获取收藏数量
     */
    @Query("SELECT COUNT(*) FROM favorites")
    suspend fun getFavoriteCount(): Int
}
