package com.example.trip_planner.data.local.dao

import androidx.room.*
import com.example.trip_planner.data.local.entity.PackingItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * 打包清单 DAO
 */
@Dao
interface PackingItemDao {

    @Query("SELECT * FROM packing_items WHERE tripId = :tripId ORDER BY category, timestamp DESC")
    fun getItemsByTrip(tripId: String): Flow<List<PackingItemEntity>>

    @Query("SELECT * FROM packing_items WHERE userId = :userId ORDER BY timestamp DESC")
    fun getItemsByUser(userId: Long): Flow<List<PackingItemEntity>>

    @Query("SELECT * FROM packing_items WHERE tripId = :tripId AND isPacked = :isPacked ORDER BY category, timestamp DESC")
    fun getItemsByStatus(tripId: String, isPacked: Boolean): Flow<List<PackingItemEntity>>

    @Query("SELECT COUNT(*) FROM packing_items WHERE tripId = :tripId")
    fun getItemCountByTrip(tripId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM packing_items WHERE tripId = :tripId AND isPacked = 1")
    fun getPackedCountByTrip(tripId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PackingItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<PackingItemEntity>)

    @Update
    suspend fun updateItem(item: PackingItemEntity)

    @Delete
    suspend fun deleteItem(item: PackingItemEntity)

    @Query("DELETE FROM packing_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("DELETE FROM packing_items WHERE tripId = :tripId")
    suspend fun deleteItemsByTrip(tripId: String)

    @Query("UPDATE packing_items SET isPacked = :isPacked WHERE id = :id")
    suspend fun updatePackedStatus(id: Long, isPacked: Boolean)

    @Query("SELECT * FROM packing_items ORDER BY timestamp DESC")
    suspend fun getAllItemsSync(): List<PackingItemEntity>

    @Query("DELETE FROM packing_items")
    suspend fun deleteAllItems()
}
