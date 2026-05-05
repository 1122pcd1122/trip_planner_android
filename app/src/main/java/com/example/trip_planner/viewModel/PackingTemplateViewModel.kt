package com.example.trip_planner.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trip_planner.data.local.TripDatabase
import com.example.trip_planner.data.local.entity.PackingTemplateEntity
import com.example.trip_planner.data.local.entity.PackingTemplateItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 打包清单模板 ViewModel
 */
class PackingTemplateViewModel(application: Application) : AndroidViewModel(application) {

    private val database = TripDatabase.getDatabase(application)
    private val templateDao = database.packingTemplateDao()

    val templates: StateFlow<List<PackingTemplateEntity>> = templateDao.getAllTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveTemplate(name: String, items: List<PackingTemplateItem>) {
        viewModelScope.launch {
            val itemsJson = Json.encodeToString(items)
            val template = PackingTemplateEntity(
                name = name,
                itemsJson = itemsJson
            )
            templateDao.insertTemplate(template)
        }
    }

    fun deleteTemplate(templateId: Long) {
        viewModelScope.launch {
            templateDao.deleteTemplateById(templateId)
        }
    }

    suspend fun getTemplateItems(templateId: Long): List<PackingTemplateItem>? {
        val template = templateDao.getTemplateById(templateId)
        return template?.let {
            try {
                Json.decodeFromString<List<PackingTemplateItem>>(it.itemsJson)
            } catch (e: Exception) {
                null
            }
        }
    }
}
