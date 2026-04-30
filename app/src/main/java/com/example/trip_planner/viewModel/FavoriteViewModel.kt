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

    private val repository: FavoriteRepository

    private val _allFavorites = MutableStateFlow<List<FavoriteEntity>>(emptyList())
    val allFavorites: StateFlow<List<FavoriteEntity>> = _allFavorites.asStateFlow()

    private val _selectedType = MutableStateFlow<String?>(null)
    val selectedType: StateFlow<String?> = _selectedType.asStateFlow()

    private val _filteredFavorites = MutableStateFlow<List<FavoriteEntity>>(emptyList())
    val filteredFavorites: StateFlow<List<FavoriteEntity>> = _filteredFavorites.asStateFlow()

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

    /**
     * 按类型筛选
     */
    fun selectType(type: String?) {
        _selectedType.value = type
        filterByType(type)
    }

    private fun filterByType(type: String?) {
        _filteredFavorites.value = if (type == null) {
            _allFavorites.value
        } else {
            _allFavorites.value.filter { it.type == type }
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

    /**
     * 切换收藏状态
     */
    suspend fun toggleFavorite(favorite: FavoriteEntity): Boolean {
        return repository.toggleFavorite(favorite)
    }

    /**
     * 获取各类型收藏数量
     */
    fun getTypeCount(type: String): Int {
        return _allFavorites.value.count { it.type == type }
    }
}
