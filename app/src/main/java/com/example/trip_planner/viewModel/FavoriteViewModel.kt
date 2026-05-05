package com.example.trip_planner.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trip_planner.data.local.TripDatabase
import com.example.trip_planner.data.local.entity.FavoriteEntity
import com.example.trip_planner.data.repository.FavoriteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 收藏 ViewModel
 * 
 * 负责管理收藏页面的业务逻辑和数据状态
 */
class FavoriteViewModel(application: Application) : AndroidViewModel(application) {

    /** 收藏仓库实例，负责数据库操作 */
    private val repository: FavoriteRepository

    /** 所有收藏列表的原始数据流 */
    private val _allFavorites = MutableStateFlow<List<FavoriteEntity>>(emptyList())
    /** 对外暴露的只读收藏列表 */
    val allFavorites: StateFlow<List<FavoriteEntity>> = _allFavorites.asStateFlow()

    /** 当前选中的收藏类型（hotel/attraction/restaurant），null 表示全部 */
    private val _selectedType = MutableStateFlow<String?>(null)

    /** 过滤后的收藏列表，根据选中类型动态更新 */
    private val _filteredFavorites = MutableStateFlow<List<FavoriteEntity>>(emptyList())

    init {
        val database = TripDatabase.getDatabase(application)
        repository = FavoriteRepository(database.favoriteDao())
        loadAllFavorites()
    }

    /**
     * 加载所有收藏
     */
    private fun loadAllFavorites() {
        viewModelScope.launch {
            repository.getAllFavorites().collect { favorites ->
                _allFavorites.value = favorites
                filterByType(_selectedType.value)
            }
        }
    }

    private fun filterByType(type: String?) {
        _filteredFavorites.value = if (type == null) {
            _allFavorites.value
        } else {
            _allFavorites.value.filter { it.type == type }
        }
    }

    /**
     * 添加收藏
     */
    fun addFavorite(favorite: FavoriteEntity) {
        viewModelScope.launch {
            repository.addFavorite(favorite)
        }
    }

    /**
     * 移除收藏
     */
    fun removeFavorite(itemId: String) {
        viewModelScope.launch {
            repository.removeFavorite(itemId)
        }
    }

}
