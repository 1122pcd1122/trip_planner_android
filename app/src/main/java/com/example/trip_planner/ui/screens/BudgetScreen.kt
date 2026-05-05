package com.example.trip_planner.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trip_planner.ui.components.ExpenseTracker
import com.example.trip_planner.ui.theme.LocalAppColors
import com.example.trip_planner.viewModel.ExpenseViewModel
import java.text.NumberFormat
import java.util.*

/**
 * 预算管理页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    tripId: String,
    tripName: String = "",
    userId: Long = 0,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    expenseViewModel: ExpenseViewModel = viewModel()
) {
    val appColors = LocalAppColors.current
    val expenses by expenseViewModel.expenses.collectAsState()
    val totalExpense by expenseViewModel.totalExpense.collectAsState()
    val expensesByCategory by expenseViewModel.expensesByCategory.collectAsState()
    val budget by expenseViewModel.budget.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(tripId) {
        if (tripId.isNotEmpty()) {
            expenseViewModel.setTripId(tripId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("预算管理", fontSize = 16.sp)
                        if (tripName.isNotEmpty()) {
                            Text(tripName, fontSize = 11.sp, color = appColors.textSecondary)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = appColors.softBackground)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(appColors.softBackground)
                .verticalScroll(scrollState)
        ) {
            BudgetSummary(
                budget = budget,
                totalExpense = totalExpense,
                appColors = appColors
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (expensesByCategory.isNotEmpty()) {
                CategoryPieChart(
                    expensesByCategory = expensesByCategory,
                    totalExpense = totalExpense,
                    appColors = appColors
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            CategoryBreakdown(
                expensesByCategory = expensesByCategory,
                totalExpense = totalExpense,
                appColors = appColors
            )

            Spacer(modifier = Modifier.height(12.dp))

            SpendingInsights(
                expenses = expenses,
                totalExpense = totalExpense,
                budget = budget,
                appColors = appColors
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExpenseTracker(
                expenses = expenses,
                totalExpense = totalExpense,
                expensesByCategory = expensesByCategory,
                budget = budget,
                onAddExpense = { category, amount, description, tags, date ->
                    expenseViewModel.addExpense(tripId, userId, category, amount, description, tags, date)
                },
                onDeleteExpense = { expenseId ->
                    expenseViewModel.deleteExpense(expenseId)
                },
                onUpdateBudget = { amount ->
                    expenseViewModel.updateBudget(tripId, amount)
                },
                appColors = appColors
            )
        }
    }
}

/**
 * 预算概览头部
 */
@Composable
fun BudgetSummary(
    budget: com.example.trip_planner.data.local.entity.TripBudgetEntity?,
    totalExpense: Double,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    val format = NumberFormat.getNumberInstance(Locale.getDefault())
    format.maximumFractionDigits = 0
    val budgetAmount = budget?.totalBudget ?: 0.0
    val remaining = budgetAmount - totalExpense
    val usagePercent = if (budgetAmount > 0) (totalExpense / budgetAmount * 100).coerceIn(0.0, 100.0) else 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(appColors.cardBackground)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryItem("预算", "¥${format.format(budgetAmount)}", appColors)
            SummaryItem("已花费", "¥${format.format(totalExpense)}", appColors)
            SummaryItem(
                "剩余",
                "¥${format.format(remaining)}",
                appColors,
                if (remaining >= 0) appColors.brandTeal else appColors.error
            )
        }

        if (budgetAmount > 0) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { (usagePercent / 100).toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(MaterialTheme.shapes.extraSmall),
                    color = when {
                        usagePercent >= 90 -> appColors.error
                        usagePercent >= 70 -> Color(0xFFFFA500)
                        else -> appColors.brandTeal
                    },
                    trackColor = appColors.divider.copy(alpha = 0.3f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "已使用 ${usagePercent.toInt()}%",
                    fontSize = 10.sp,
                    color = appColors.textSecondary
                )
                if (usagePercent >= 80) {
                    Text(
                        "⚠️ 预算紧张",
                        fontSize = 10.sp,
                        color = appColors.error
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryItem(
    label: String,
    value: String,
    appColors: com.example.trip_planner.ui.theme.AppColors,
    valueColor: Color = appColors.textPrimary
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = appColors.textSecondary, letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

/**
 * 分类饼图
 */
@Composable
fun CategoryPieChart(
    expensesByCategory: Map<String, Double>,
    totalExpense: Double,
    appColors: com.example.trip_planner.ui.theme.AppColors,
    modifier: Modifier = Modifier
) {
    val categoryColors = remember {
        listOf(
            Color(0xFF4A90D9),
            Color(0xFF9B59B6),
            Color(0xFFE67E22),
            Color(0xFF2ECC71),
            Color(0xFFE74C3C),
            Color(0xFF95A5A6),
            Color(0xFF3498DB),
            Color(0xFF1ABC9C),
            Color(0xFFF39C12),
            Color(0xFFD35400)
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(appColors.cardBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("花费分布", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = appColors.textSecondary)
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            val categoriesWithExpenses = expensesByCategory.filter { it.value > 0 }
            if (categoriesWithExpenses.isEmpty()) {
                Canvas(modifier = Modifier.size(160.dp)) {
                    drawArc(
                        color = Color(0xFFE0E0E0),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Butt)
                    )
                }
            } else {
                Canvas(modifier = Modifier.size(160.dp)) {
                    var startAngle: Float = -90f
                    val sweepAngle: Float = 360f

                    categoriesWithExpenses.entries.toList().forEachIndexed { index, entry ->
                        val amount = entry.value
                        val percentage: Float = (amount / totalExpense).toFloat()
                        val sweep: Float = sweepAngle * percentage
                        val color = categoryColors[index % categoryColors.size]

                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Butt)
                        )

                        startAngle = startAngle + sweep
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "¥${NumberFormat.getNumberInstance(Locale.getDefault()).apply { maximumFractionDigits = 0 }.format(totalExpense)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimary
                )
                Text("总花费", fontSize = 10.sp, color = appColors.textSecondary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val categoriesWithExpenses = expensesByCategory.filter { it.value > 0 }
        if (categoriesWithExpenses.isEmpty()) {
            Text("暂无花费数据", fontSize = 11.sp, color = appColors.textSecondary)
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                categoriesWithExpenses.entries.toList().take(3).forEachIndexed { index, entry ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Canvas(modifier = Modifier.size(8.dp)) {
                            drawCircle(color = categoryColors[index % categoryColors.size])
                        }
                        Text(entry.key, fontSize = 9.sp, color = appColors.textSecondary)
                    }
                }
            }
        }
    }
}

/**
 * 分类明细
 */
@Composable
fun CategoryBreakdown(
    expensesByCategory: Map<String, Double>,
    totalExpense: Double,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    val format = NumberFormat.getNumberInstance(Locale.getDefault())
    format.maximumFractionDigits = 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(appColors.cardBackground)
            .padding(16.dp)
    ) {
        Text("分类明细", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = appColors.textSecondary)
        Spacer(modifier = Modifier.height(12.dp))

        expensesByCategory.forEach { (category, amount) ->
            if (amount > 0) {
                val percent = if (totalExpense > 0) (amount / totalExpense * 100).toInt() else 0
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(category, fontSize = 12.sp, color = appColors.textPrimary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("¥${format.format(amount)}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = appColors.textPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$percent%", fontSize = 10.sp, color = appColors.textSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (percent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(MaterialTheme.shapes.extraSmall),
                        color = appColors.brandTeal,
                        trackColor = appColors.divider.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

/**
 * 花费洞察
 */
@Composable
fun SpendingInsights(
    expenses: List<com.example.trip_planner.data.local.entity.ExpenseEntity>,
    totalExpense: Double,
    budget: com.example.trip_planner.data.local.entity.TripBudgetEntity?,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    val format = NumberFormat.getNumberInstance(Locale.getDefault())
    format.maximumFractionDigits = 0

    val dailyAverage = if (expenses.isNotEmpty()) {
        totalExpense / expenses.size
    } else 0.0

    val maxExpense = expenses.maxByOrNull { it.amount }
    val budgetAmount = budget?.totalBudget ?: 0.0
    val dailyBudget = if (budgetAmount > 0 && expenses.isNotEmpty()) {
        budgetAmount / expenses.size
    } else 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(appColors.cardBackground)
            .padding(16.dp)
    ) {
        Text("花费洞察", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = appColors.textSecondary)
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InsightCard(
                icon = "📊",
                title = "日均花费",
                value = "¥${format.format(dailyAverage)}",
                modifier = Modifier.weight(1f),
                appColors = appColors
            )
            InsightCard(
                icon = "💰",
                title = "最大单笔",
                value = if (maxExpense != null) "¥${format.format(maxExpense.amount)}" else "无",
                modifier = Modifier.weight(1f),
                appColors = appColors
            )
        }

        if (dailyBudget > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InsightCard(
                    icon = "🎯",
                    title = "日均预算",
                    value = "¥${format.format(dailyBudget)}",
                    modifier = Modifier.weight(1f),
                    appColors = appColors
                )
                val diff = dailyBudget - dailyAverage
                InsightCard(
                    icon = if (diff >= 0) "✅" else "⚠️",
                    title = "预算偏差",
                    value = "${if (diff >= 0) "节省" else "超支"} ¥${format.format(kotlin.math.abs(diff))}",
                    modifier = Modifier.weight(1f),
                    appColors = appColors
                )
            }
        }
    }
}

@Composable
fun InsightCard(
    icon: String,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(appColors.softBackground)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(title, fontSize = 9.sp, color = appColors.textSecondary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = appColors.textPrimary)
    }
}
