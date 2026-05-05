package com.example.trip_planner.data.local.dao

import androidx.room.*
import com.example.trip_planner.data.local.entity.TripBudgetEntity
import kotlinx.coroutines.flow.Flow

/**
 * 行程预算 DAO
 */
@Dao
interface TripBudgetDao {

    @Query("SELECT * FROM trip_budgets WHERE tripId = :tripId")
    suspend fun getBudgetByTrip(tripId: String): TripBudgetEntity?

    @Query("SELECT * FROM trip_budgets WHERE tripId = :tripId")
    fun getBudgetFlowByTrip(tripId: String): Flow<TripBudgetEntity?>

    @Query("SELECT * FROM trip_budgets ORDER BY updatedAt DESC")
    fun getAllBudgets(): Flow<List<TripBudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: TripBudgetEntity)

    @Update
    suspend fun updateBudget(budget: TripBudgetEntity)

    @Delete
    suspend fun deleteBudget(budget: TripBudgetEntity)

    @Query("DELETE FROM trip_budgets WHERE tripId = :tripId")
    suspend fun deleteBudgetByTrip(tripId: String)

    @Query("SELECT * FROM trip_budgets ORDER BY updatedAt DESC")
    suspend fun getAllBudgetsSync(): List<TripBudgetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<TripBudgetEntity>)

    @Query("DELETE FROM trip_budgets")
    suspend fun deleteAllBudgets()
}
