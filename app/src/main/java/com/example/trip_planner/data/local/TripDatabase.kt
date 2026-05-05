package com.example.trip_planner.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.trip_planner.data.local.dao.DetailCacheDao
import com.example.trip_planner.data.local.dao.ExpenseDao
import com.example.trip_planner.data.local.dao.FavoriteDao
import com.example.trip_planner.data.local.dao.PackingItemDao
import com.example.trip_planner.data.local.dao.PackingTemplateDao
import com.example.trip_planner.data.local.dao.PreferenceTagDao
import com.example.trip_planner.data.local.dao.SearchHistoryDao
import com.example.trip_planner.data.local.dao.TripBudgetDao
import com.example.trip_planner.data.local.dao.TripNoteDao
import com.example.trip_planner.data.local.dao.TripPlanDao
import com.example.trip_planner.data.local.dao.UserDao
import com.example.trip_planner.data.local.dao.WeatherCacheDao
import com.example.trip_planner.data.local.entity.DetailCacheEntity
import com.example.trip_planner.data.local.entity.ExpenseEntity
import com.example.trip_planner.data.local.entity.FavoriteEntity
import com.example.trip_planner.data.local.entity.PackingItemEntity
import com.example.trip_planner.data.local.entity.PackingTemplateEntity
import com.example.trip_planner.data.local.entity.PreferenceTagEntity
import com.example.trip_planner.data.local.entity.SearchHistoryEntity
import com.example.trip_planner.data.local.entity.TripBudgetEntity
import com.example.trip_planner.data.local.entity.TripNoteEntity
import com.example.trip_planner.data.local.entity.TripPlanEntity
import com.example.trip_planner.data.local.entity.UserEntity
import com.example.trip_planner.data.local.entity.WeatherCacheEntity

/**
 * 旅行规划应用数据库
 * 
 * 包含收藏表、行程规划表、详情缓存表等数据表
 */
@Database(
    entities = [FavoriteEntity::class, TripPlanEntity::class, DetailCacheEntity::class, PreferenceTagEntity::class, SearchHistoryEntity::class, WeatherCacheEntity::class, ExpenseEntity::class, TripBudgetEntity::class, TripNoteEntity::class, PackingItemEntity::class, PackingTemplateEntity::class, UserEntity::class],
    version = 17,
    exportSchema = false
)
abstract class TripDatabase : RoomDatabase() {

    abstract fun favoriteDao(): FavoriteDao
    abstract fun tripPlanDao(): TripPlanDao
    abstract fun detailCacheDao(): DetailCacheDao
    abstract fun preferenceTagDao(): PreferenceTagDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun weatherCacheDao(): WeatherCacheDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun tripBudgetDao(): TripBudgetDao
    abstract fun tripNoteDao(): TripNoteDao
    abstract fun packingItemDao(): PackingItemDao
    abstract fun packingTemplateDao(): PackingTemplateDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: TripDatabase? = null

        fun getDatabase(context: Context): TripDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TripDatabase::class.java,
                    "trip_planner_database"
                )
                    .fallbackToDestructiveMigration()
                    .enableMultiInstanceInvalidation()
                    .setQueryExecutor(java.util.concurrent.Executors.newSingleThreadExecutor())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
