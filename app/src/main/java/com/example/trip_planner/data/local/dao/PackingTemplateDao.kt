package com.example.trip_planner.data.local.dao

import androidx.room.*
import com.example.trip_planner.data.local.entity.PackingTemplateEntity
import kotlinx.coroutines.flow.Flow

/**
 * 打包清单模板 DAO
 */
@Dao
interface PackingTemplateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: PackingTemplateEntity)

    @Update
    suspend fun updateTemplate(template: PackingTemplateEntity)

    @Delete
    suspend fun deleteTemplate(template: PackingTemplateEntity)

    @Query("SELECT * FROM packing_templates ORDER BY createdAt DESC")
    fun getAllTemplates(): Flow<List<PackingTemplateEntity>>

    @Query("SELECT * FROM packing_templates WHERE id = :templateId")
    suspend fun getTemplateById(templateId: Long): PackingTemplateEntity?

    @Query("DELETE FROM packing_templates WHERE id = :templateId")
    suspend fun deleteTemplateById(templateId: Long)
}
