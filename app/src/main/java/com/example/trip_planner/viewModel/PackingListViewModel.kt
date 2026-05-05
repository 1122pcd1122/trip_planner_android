package com.example.trip_planner.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trip_planner.data.local.TripDatabase
import com.example.trip_planner.data.local.entity.PackingItemEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 打包清单 ViewModel
 */
class PackingListViewModel(application: Application) : AndroidViewModel(application) {

    private val database = TripDatabase.getDatabase(application)
    private val packingDao = database.packingItemDao()

    private val _currentTripId = MutableStateFlow<String?>(null)
    val currentTripId: StateFlow<String?> = _currentTripId.asStateFlow()

    val items: StateFlow<List<PackingItemEntity>> = _currentTripId
        .filterNotNull()
        .flatMapLatest { tripId ->
            packingDao.getItemsByTrip(tripId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val packedCount: StateFlow<Int> = _currentTripId
        .filterNotNull()
        .flatMapLatest { tripId ->
            packingDao.getPackedCountByTrip(tripId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalItemCount: StateFlow<Int> = _currentTripId
        .filterNotNull()
        .flatMapLatest { tripId ->
            packingDao.getItemCountByTrip(tripId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setTripId(tripId: String) {
        _currentTripId.value = tripId
    }

    fun addItem(tripId: String, userId: Long = 0, name: String, category: String, tags: String = "", quantity: Int = 1) {
        viewModelScope.launch {
            val item = PackingItemEntity(
                userId = userId,
                tripId = tripId,
                name = name,
                category = category,
                tags = tags,
                quantity = quantity
            )
            packingDao.insertItem(item)
        }
    }

    fun togglePacked(itemId: Long, isPacked: Boolean) {
        viewModelScope.launch {
            packingDao.updatePackedStatus(itemId, isPacked)
        }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            packingDao.deleteItemById(itemId)
        }
    }

    fun deleteItemsByTrip(tripId: String) {
        viewModelScope.launch {
            packingDao.deleteItemsByTrip(tripId)
        }
    }

    fun addDefaultItems(tripId: String, userId: Long = 0) {
        viewModelScope.launch {
            val defaultItems = listOf(
                PackingItemEntity(userId = userId, tripId = tripId, name = "换洗衣物", category = "衣物"),
                PackingItemEntity(userId = userId, tripId = tripId, name = "内衣裤", category = "衣物"),
                PackingItemEntity(userId = userId, tripId = tripId, name = "牙刷", category = "洗漱"),
                PackingItemEntity(userId = userId, tripId = tripId, name = "牙膏", category = "洗漱"),
                PackingItemEntity(userId = userId, tripId = tripId, name = "毛巾", category = "洗漱"),
                PackingItemEntity(userId = userId, tripId = tripId, name = "充电器", category = "电子"),
                PackingItemEntity(userId = userId, tripId = tripId, name = "身份证", category = "证件"),
                PackingItemEntity(userId = userId, tripId = tripId, name = "常用药品", category = "药品")
            )
            packingDao.insertItems(defaultItems)
        }
    }
}
