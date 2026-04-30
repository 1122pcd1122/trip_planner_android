package com.example.trip_planner.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.trip_planner.data.local.dao.FavoriteDao
import com.example.trip_planner.data.local.dao.TripPlanDao
import com.example.trip_planner.data.local.entity.FavoriteEntity
import com.example.trip_planner.data.local.entity.TripPlanEntity

/**
 * 旅行规划应用数据库
 * 
 * 包含收藏表、行程规划表等数据表
 */
@Database(
    entities = [FavoriteEntity::class, TripPlanEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TripDatabase : RoomDatabase() {

    abstract fun favoriteDao(): FavoriteDao
    abstract fun tripPlanDao(): TripPlanDao

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
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
