package com.example.trip_planner.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trip_planner.data.local.TripDatabase
import com.example.trip_planner.data.local.entity.ExpenseEntity
import com.example.trip_planner.data.local.entity.TripBudgetEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 费用统计 ViewModel
 * 负责管理旅行预算和花费记录
 */
class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val database = TripDatabase.getDatabase(application)
    private val expenseDao = database.expenseDao()
    private val budgetDao = database.tripBudgetDao()

    private val _currentTripId = MutableStateFlow<String?>(null)
    val currentTripId: StateFlow<String?> = _currentTripId.asStateFlow()

    val expenses: StateFlow<List<ExpenseEntity>> = _currentTripId
        .filterNotNull()
        .flatMapLatest { tripId ->
            expenseDao.getExpensesByTrip(tripId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalExpense: StateFlow<Double> = _currentTripId
        .filterNotNull()
        .flatMapLatest { tripId ->
            expenseDao.getTotalExpenseByTrip(tripId).map { it ?: 0.0 }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val budget: StateFlow<TripBudgetEntity?> = _currentTripId
        .filterNotNull()
        .flatMapLatest { tripId ->
            budgetDao.getBudgetFlowByTrip(tripId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val expensesByCategory: StateFlow<Map<String, Double>> = _currentTripId
        .filterNotNull()
        .flatMapLatest { tripId ->
            expenses.map { expenseList ->
                expenseList
                    .groupBy { it.category }
                    .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun setTripId(tripId: String) {
        _currentTripId.value = tripId
    }

    fun addExpense(
        tripId: String,
        userId: Long = 0,
        category: String,
        amount: Double,
        description: String = "",
        tags: String = "",
        date: String = ""
    ) {
        viewModelScope.launch {
            val expense = ExpenseEntity(
                userId = userId,
                tripId = tripId,
                category = category,
                amount = amount,
                description = description,
                tags = tags,
                date = date
            )
            expenseDao.insertExpense(expense)
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            expenseDao.updateExpense(expense)
        }
    }

    fun deleteExpense(expenseId: Long) {
        viewModelScope.launch {
            expenseDao.deleteExpenseById(expenseId)
        }
    }

    fun clearTripExpenses(tripId: String) {
        viewModelScope.launch {
            expenseDao.deleteExpensesByTrip(tripId)
        }
    }

    fun updateBudget(tripId: String, totalBudget: Double) {
        viewModelScope.launch {
            val existing = budgetDao.getBudgetByTrip(tripId)
            if (existing != null) {
                budgetDao.updateBudget(existing.copy(totalBudget = totalBudget, updatedAt = System.currentTimeMillis()))
            } else {
                budgetDao.insertBudget(TripBudgetEntity(tripId = tripId, totalBudget = totalBudget))
            }
        }
    }

    fun deleteBudget(tripId: String) {
        viewModelScope.launch {
            budgetDao.deleteBudgetByTrip(tripId)
        }
    }
}
