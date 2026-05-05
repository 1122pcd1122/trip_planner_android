package com.example.trip_planner.utils

import android.content.Context
import android.net.Uri
import com.example.trip_planner.data.local.TripDatabase
import com.example.trip_planner.data.local.entity.ExpenseEntity
import com.example.trip_planner.data.local.entity.PackingItemEntity
import com.example.trip_planner.data.local.entity.TripBudgetEntity
import com.example.trip_planner.data.local.entity.TripNoteEntity
import com.example.trip_planner.data.local.entity.TripPlanEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 数据备份和恢复工具类
 */
object DataBackupUtils {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    @Serializable
    data class BackupTripPlan(
        val id: Long = 0,
        val userId: Long = 0,
        val destination: String,
        val days: Int,
        val preferences: String = "",
        val hotelJson: String = "",
        val dayPlansJson: String = "",
        val overallTips: String = "",
        val timestamp: Long = System.currentTimeMillis()
    )

    @Serializable
    data class BackupTripNote(
        val id: Long = 0,
        val userId: Long = 0,
        val tripId: String,
        val title: String = "",
        val content: String = "",
        val photoPaths: String = "",
        val date: String = "",
        val location: String = "",
        val mood: String = "",
        val tags: String = "",
        val timestamp: Long = System.currentTimeMillis()
    )

    @Serializable
    data class BackupExpense(
        val id: Long = 0,
        val userId: Long = 0,
        val tripId: String,
        val category: String = "其他",
        val amount: Double,
        val description: String = "",
        val tags: String = "",
        val date: String = "",
        val timestamp: Long = System.currentTimeMillis()
    )

    @Serializable
    data class BackupTripBudget(
        val id: Long = 0,
        val tripId: String,
        val totalBudget: Double,
        val currency: String = "¥",
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis()
    )

    @Serializable
    data class BackupPackingItem(
        val id: Long = 0,
        val userId: Long = 0,
        val tripId: String,
        val name: String,
        val category: String = "其他",
        val tags: String = "",
        val isPacked: Boolean = false,
        val quantity: Int = 1,
        val timestamp: Long = System.currentTimeMillis()
    )

    @Serializable
    data class BackupData(
        val version: Int = 1,
        val exportDate: Long = System.currentTimeMillis(),
        val tripPlans: List<BackupTripPlan> = emptyList(),
        val tripNotes: List<BackupTripNote> = emptyList(),
        val expenses: List<BackupExpense> = emptyList(),
        val tripBudgets: List<BackupTripBudget> = emptyList(),
        val packingItems: List<BackupPackingItem> = emptyList()
    )

    private fun TripPlanEntity.toBackup() = BackupTripPlan(
        id = id, userId = userId, destination = destination, days = days,
        preferences = preferences, hotelJson = hotelJson, dayPlansJson = dayPlansJson,
        overallTips = overallTips, timestamp = timestamp
    )

    private fun TripNoteEntity.toBackup() = BackupTripNote(
        id = id, userId = userId, tripId = tripId, title = title, content = content,
        photoPaths = photoPaths, date = date, location = location, mood = mood,
        tags = tags, timestamp = timestamp
    )

    private fun ExpenseEntity.toBackup() = BackupExpense(
        id = id, userId = userId, tripId = tripId, category = category, amount = amount,
        description = description, tags = tags, date = date, timestamp = timestamp
    )

    private fun TripBudgetEntity.toBackup() = BackupTripBudget(
        id = id, tripId = tripId, totalBudget = totalBudget, currency = currency,
        createdAt = createdAt, updatedAt = updatedAt
    )

    private fun PackingItemEntity.toBackup() = BackupPackingItem(
        id = id, userId = userId, tripId = tripId, name = name, category = category,
        tags = tags, isPacked = isPacked, quantity = quantity, timestamp = timestamp
    )

    private fun BackupTripPlan.toEntity() = TripPlanEntity(
        id = id, userId = userId, destination = destination, days = days,
        preferences = preferences, hotelJson = hotelJson, dayPlansJson = dayPlansJson,
        overallTips = overallTips, timestamp = timestamp
    )

    private fun BackupTripNote.toEntity() = TripNoteEntity(
        id = id, userId = userId, tripId = tripId, title = title, content = content,
        photoPaths = photoPaths, date = date, location = location, mood = mood,
        tags = tags, timestamp = timestamp
    )

    private fun BackupExpense.toEntity() = ExpenseEntity(
        id = id, userId = userId, tripId = tripId, category = category, amount = amount,
        description = description, tags = tags, date = date, timestamp = timestamp
    )

    private fun BackupTripBudget.toEntity() = TripBudgetEntity(
        id = id, tripId = tripId, totalBudget = totalBudget, currency = currency,
        createdAt = createdAt, updatedAt = updatedAt
    )

    private fun BackupPackingItem.toEntity() = PackingItemEntity(
        id = id, userId = userId, tripId = tripId, name = name, category = category,
        tags = tags, isPacked = isPacked, quantity = quantity, timestamp = timestamp
    )

    /**
     * 导出所有数据为 JSON 文件
     */
    suspend fun exportData(context: Context, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val database = TripDatabase.getDatabase(context)
            
            val backupData = BackupData(
                tripPlans = database.tripPlanDao().getAllTripPlansSync().map { it.toBackup() },
                tripNotes = database.tripNoteDao().getAllNotesSync().map { it.toBackup() },
                expenses = database.expenseDao().getAllExpensesSync().map { it.toBackup() },
                tripBudgets = database.tripBudgetDao().getAllBudgetsSync().map { it.toBackup() },
                packingItems = database.packingItemDao().getAllItemsSync().map { it.toBackup() }
            )

            val jsonString = json.encodeToString(backupData)
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 从 JSON 文件导入数据
     */
    suspend fun importData(context: Context, uri: Uri, replace: Boolean = false): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: throw RuntimeException("无法读取文件")

            val backupData = json.decodeFromString<BackupData>(jsonString)
            val database = TripDatabase.getDatabase(context)

            if (replace) {
                database.tripPlanDao().deleteAllTripPlans()
                database.tripNoteDao().deleteAllNotes()
                database.expenseDao().deleteAllExpenses()
                database.tripBudgetDao().deleteAllBudgets()
                database.packingItemDao().deleteAllItems()
            }

            database.tripPlanDao().insertTripPlans(backupData.tripPlans.map { it.toEntity() })
            database.tripNoteDao().insertNotes(backupData.tripNotes.map { it.toEntity() })
            database.expenseDao().insertExpenses(backupData.expenses.map { it.toEntity() })
            database.tripBudgetDao().insertBudgets(backupData.tripBudgets.map { it.toEntity() })
            database.packingItemDao().insertItems(backupData.packingItems.map { it.toEntity() })

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
