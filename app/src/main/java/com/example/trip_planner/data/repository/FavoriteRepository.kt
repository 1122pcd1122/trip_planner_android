package com.example.trip_planner.data.repository

import com.example.trip_planner.data.local.dao.FavoriteDao
import com.example.trip_planner.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

/**
 * 收藏仓库
 * 
 * 负责收藏数据的业务逻辑，封装 DAO 操作
 */
class FavoriteRepository(private val favoriteDao: FavoriteDao) {

    /**
     * 获取所有收藏
     */
    fun getAllFavorites(): Flow<List<FavoriteEntity>> = favoriteDao.getAllFavorites()

    /**
     * 按类型获取收藏
     */
    fun getFavoritesByType(type: String): Flow<List<FavoriteEntity>> = favoriteDao.getFavoritesByType(type)

    /**
     * 检查是否已收藏
     */
    suspend fun isFavorite(itemId: String): Boolean = favoriteDao.isFavorite(itemId)

    /**
     * 添加收藏
     */
    suspend fun addFavorite(favorite: FavoriteEntity) = favoriteDao.insertFavorite(favorite)

    /**
     * 移除收藏
     */
    suspend fun removeFavorite(itemId: String) = favoriteDao.deleteFavorite(itemId)

    /**
     * 切换收藏状态
     * 返回新的收藏状态（true=已收藏，false=未收藏）
     */
    suspend fun toggleFavorite(favorite: FavoriteEntity): Boolean {
        val isFav = favoriteDao.isFavorite(favorite.itemId)
        if (isFav) {
            favoriteDao.deleteFavorite(favorite.itemId)
        } else {
            favoriteDao.insertFavorite(favorite)
        }
        return !isFav
    }

    /**
     * 清空所有收藏
     */
    suspend fun clearAll() = favoriteDao.deleteAllFavorites()
}
