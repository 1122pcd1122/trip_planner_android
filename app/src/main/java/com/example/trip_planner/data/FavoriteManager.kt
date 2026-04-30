package com.example.trip_planner.data

import android.content.Context
import android.content.SharedPreferences
import com.example.trip_planner.ui.content.FavoriteItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 收藏管理器
 * 
 * 负责收藏数据的本地存储和管理，使用 SharedPreferences + Gson 实现
 */
class FavoriteManager(context: Context) {

    companion object {
        private const val PREF_NAME = "trip_planner_favorites"
        private const val KEY_FAVORITES = "favorites"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    /**
     * 获取所有收藏
     */
    fun getAllFavorites(): List<FavoriteItem> {
        val json = prefs.getString(KEY_FAVORITES, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<FavoriteItem>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 检查是否已收藏
     */
    fun isFavorite(itemId: String): Boolean {
        return getAllFavorites().any { it.id == itemId }
    }

    /**
     * 添加收藏
     */
    fun addFavorite(item: FavoriteItem) {
        val favorites = getAllFavorites().toMutableList()
        if (favorites.none { it.id == item.id }) {
            favorites.add(item.copy(timestamp = System.currentTimeMillis()))
            saveFavorites(favorites)
        }
    }

    /**
     * 移除收藏
     */
    fun removeFavorite(itemId: String) {
        val favorites = getAllFavorites().toMutableList()
        favorites.removeAll { it.id == itemId }
        saveFavorites(favorites)
    }

    /**
     * 切换收藏状态
     */
    fun toggleFavorite(item: FavoriteItem): Boolean {
        return if (isFavorite(item.id)) {
            removeFavorite(item.id)
            false
        } else {
            addFavorite(item)
            true
        }
    }

    /**
     * 清空所有收藏
     */
    fun clearAll() {
        prefs.edit().remove(KEY_FAVORITES).apply()
    }

    /**
     * 保存收藏列表
     */
    private fun saveFavorites(favorites: List<FavoriteItem>) {
        val json = gson.toJson(favorites)
        prefs.edit().putString(KEY_FAVORITES, json).apply()
    }
}
