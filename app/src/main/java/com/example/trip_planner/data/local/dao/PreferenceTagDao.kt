package com.example.trip_planner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.trip_planner.data.local.entity.PreferenceTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferenceTagDao {

    @Query("SELECT * FROM preference_tags ORDER BY category, label")
    fun getAllTags(): Flow<List<PreferenceTagEntity>>

    @Query("SELECT * FROM preference_tags WHERE label LIKE '%' || :keyword || '%' OR keywords LIKE '%' || :keyword || '%' ORDER BY category, label")
    fun searchTags(keyword: String): Flow<List<PreferenceTagEntity>>

    @Query("SELECT * FROM preference_tags WHERE category = :category ORDER BY label")
    fun getTagsByCategory(category: String): Flow<List<PreferenceTagEntity>>

    @Query("SELECT * FROM preference_tags WHERE label = :label LIMIT 1")
    suspend fun getTagByLabel(label: String): PreferenceTagEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<PreferenceTagEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleTag(tag: PreferenceTagEntity)

    @Query("DELETE FROM preference_tags")
    suspend fun clearAllTags()

    @Query("SELECT COUNT(*) FROM preference_tags")
    suspend fun getTagCount(): Int
}
