package com.example.trip_planner.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 打包清单模板实体
 */
@Entity(
    tableName = "packing_templates",
    indices = [
        Index(value = ["name"], name = "idx_template_name")
    ]
)
data class PackingTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val itemsJson: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 模板项数据类
 */
data class PackingTemplateItem(
    val name: String,
    val category: String,
    val tags: String,
    val quantity: Int
)
