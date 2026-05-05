package com.example.trip_planner.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trip_planner.data.local.entity.ExpenseEntity
import com.example.trip_planner.data.local.entity.TripBudgetEntity
import com.example.trip_planner.ui.theme.AppColors
import java.text.NumberFormat
import java.util.*

/**
 * 费用统计组件（用户自定义分类/标签）
 */
@Composable
fun ExpenseTracker(
    expenses: List<ExpenseEntity>,
    totalExpense: Double,
    expensesByCategory: Map<String, Double>,
    budget: TripBudgetEntity?,
    onAddExpense: (String, Double, String, String, String) -> Unit,
    onDeleteExpense: (Long) -> Unit,
    onUpdateBudget: (Double) -> Unit,
    appColors: AppColors,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showTrendChart by remember { mutableStateOf(false) }
    val budgetAmount = budget?.totalBudget ?: 0.0
    val remaining = budgetAmount - totalExpense
    val usagePercent = if (budgetAmount > 0) (totalExpense / budgetAmount * 100).coerceIn(0.0, 100.0) else 0.0

    val dailyExpenses = remember(expenses) {
        expenses.groupBy { it.date }
            .mapValues { (_, items) -> items.sumOf { it.amount } }
            .toSortedMap { d1, d2 -> d1.compareTo(d2) }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "费用统计",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = appColors.textSecondary
            )
            Row {
                if (expenses.isNotEmpty()) {
                    IconButton(onClick = { showTrendChart = !showTrendChart }) {
                        Icon(
                            Icons.Default.ShowChart,
                            contentDescription = "趋势",
                            tint = appColors.brandTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (budgetAmount > 0) {
                    TextButton(onClick = { showBudgetDialog = true }) {
                        Text("预算", color = appColors.brandTeal, fontSize = 11.sp)
                    }
                }
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "添加费用",
                        tint = appColors.brandTeal,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (showTrendChart && expenses.isNotEmpty()) {
            ExpenseTrendChart(
                dailyExpenses = dailyExpenses,
                appColors = appColors
            )
            HorizontalDivider(color = appColors.divider.copy(alpha = 0.3f))
        }

        if (expenses.isEmpty() && budgetAmount == 0.0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(appColors.cardBackground),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "暂无费用记录",
                        fontSize = 12.sp,
                        color = appColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { showAddDialog = true }) {
                        Text("添加第一笔费用", color = appColors.brandTeal, fontSize = 11.sp)
                    }
                }
            }
        } else {
            if (budgetAmount > 0) {
                BudgetOverview(
                    budgetAmount = budgetAmount,
                    totalExpense = totalExpense,
                    remaining = remaining,
                    usagePercent = usagePercent,
                    appColors = appColors
                )
                HorizontalDivider(color = appColors.divider.copy(alpha = 0.3f))

                if (usagePercent >= 80) {
                    BudgetWarning(
                        usagePercent = usagePercent,
                        remaining = remaining,
                        appColors = appColors
                    )
                    HorizontalDivider(color = appColors.divider.copy(alpha = 0.3f))
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    TextButton(onClick = { showBudgetDialog = true }) {
                        Text("设置预算", color = appColors.brandTeal, fontSize = 11.sp)
                    }
                }
            }

            ExpenseSummary(
                totalExpense = totalExpense,
                expensesByCategory = expensesByCategory,
                appColors = appColors
            )

            if (expenses.isNotEmpty()) {
                HorizontalDivider(color = appColors.divider.copy(alpha = 0.3f))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 250.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(expenses, key = { it.id }) { expense ->
                        ExpenseItem(
                            expense = expense,
                            onDelete = { onDeleteExpense(expense.id) },
                            appColors = appColors
                        )
                        HorizontalDivider(color = appColors.divider.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { category, amount, desc, tags, date ->
                onAddExpense(category, amount, desc, tags, date)
                showAddDialog = false
            },
            existingCategories = expenses.map { it.category }.distinct(),
            appColors = appColors
        )
    }

    if (showBudgetDialog) {
        SetBudgetDialog(
            currentBudget = budgetAmount,
            onDismiss = { showBudgetDialog = false },
            onSave = { newBudget ->
                onUpdateBudget(newBudget)
                showBudgetDialog = false
            },
            appColors = appColors
        )
    }
}

/**
 * 预算概览
 */
@Composable
fun BudgetOverview(
    budgetAmount: Double,
    totalExpense: Double,
    remaining: Double,
    usagePercent: Double,
    appColors: AppColors
) {
    val format = NumberFormat.getNumberInstance(Locale.getDefault())
    format.maximumFractionDigits = 0
    val progressColor = when {
        usagePercent >= 90 -> appColors.error
        usagePercent >= 70 -> Color(0xFFFFA500)
        else -> appColors.brandTeal
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "预算",
                    fontSize = 10.sp,
                    color = appColors.textSecondary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    "¥${format.format(budgetAmount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = appColors.textPrimary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "剩余",
                    fontSize = 10.sp,
                    color = appColors.textSecondary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    "¥${format.format(remaining)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (remaining >= 0) appColors.brandTeal else appColors.error
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            LinearProgressIndicator(
                progress = (usagePercent / 100).toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(MaterialTheme.shapes.extraSmall),
                color = progressColor,
                trackColor = appColors.divider.copy(alpha = 0.3f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            "已使用 ${usagePercent.toInt()}%",
            fontSize = 10.sp,
            color = appColors.textSecondary,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
fun BudgetWarning(
    usagePercent: Double,
    remaining: Double,
    appColors: AppColors
) {
    val format = NumberFormat.getNumberInstance(Locale.getDefault())
    format.maximumFractionDigits = 2

    val warningColor = if (usagePercent >= 90) appColors.error else Color(0xFFFFA500)
    val warningMessage = if (usagePercent >= 90) {
        "预算即将超支！仅剩 ¥${format.format(remaining)}"
    } else {
        "已使用 ${usagePercent.toInt()}% 预算，请注意控制支出"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(warningColor.copy(alpha = 0.1f))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = warningColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                warningMessage,
                fontSize = 11.sp,
                color = warningColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 费用汇总
 */
@Composable
fun ExpenseSummary(
    totalExpense: Double,
    expensesByCategory: Map<String, Double>,
    appColors: AppColors
) {
    val format = NumberFormat.getNumberInstance(Locale.getDefault())
    format.maximumFractionDigits = 2

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "总花费",
                fontSize = 13.sp,
                color = appColors.textSecondary
            )
            Text(
                "¥${format.format(totalExpense)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = appColors.brandTeal
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        expensesByCategory.forEach { (category, amount) ->
            if (amount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        category,
                        fontSize = 12.sp,
                        color = appColors.textPrimary
                    )
                    Text(
                        "¥${format.format(amount)}",
                        fontSize = 12.sp,
                        color = appColors.textSecondary
                    )
                }
            }
        }
    }
}

/**
 * 费用项
 */
@Composable
fun ExpenseItem(
    expense: ExpenseEntity,
    onDelete: () -> Unit,
    appColors: AppColors
) {
    val format = NumberFormat.getNumberInstance(Locale.getDefault())
    format.maximumFractionDigits = 2

    val tags = remember(expense.tags) {
        if (expense.tags.isNotEmpty()) {
            expense.tags.split("|").filter { it.isNotEmpty() }
        } else {
            emptyList()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                expense.category,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = appColors.textPrimary
            )
            if (expense.description.isNotEmpty()) {
                Text(
                    expense.description,
                    fontSize = 11.sp,
                    color = appColors.textSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (tags.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tags.forEach { tag ->
                        ExpenseTagChip(tag = tag, appColors = appColors)
                    }
                }
            }
        }
        Text(
            "¥${format.format(expense.amount)}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = appColors.textPrimary
        )
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Default.Close,
                contentDescription = "删除",
                tint = appColors.error.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ExpenseTagChip(
    tag: String,
    appColors: AppColors
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(appColors.brandTeal.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(tag, fontSize = 9.sp, color = appColors.brandTeal)
    }
}

/**
 * 添加费用对话框（用户自定义分类/标签）
 */
@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Double, String, String, String) -> Unit,
    existingCategories: List<String>,
    appColors: AppColors
) {
    var category by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var useNewCategory by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加费用", fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("金额", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.brandTeal,
                        unfocusedBorderColor = appColors.softBackground
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )

                Text("分类", fontSize = 12.sp, color = appColors.textSecondary)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { useNewCategory = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (!useNewCategory) appColors.brandTeal.copy(alpha = 0.1f) else appColors.cardBackground
                        )
                    ) {
                        Text("选择已有", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { useNewCategory = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (useNewCategory) appColors.brandTeal.copy(alpha = 0.1f) else appColors.cardBackground
                        )
                    ) {
                        Text("新建分类", fontSize = 11.sp)
                    }
                }

                if (useNewCategory) {
                    OutlinedTextField(
                        value = newCategory,
                        onValueChange = { newCategory = it },
                        label = { Text("新分类名称", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = appColors.brandTeal,
                            unfocusedBorderColor = appColors.softBackground
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                    )
                } else if (existingCategories.isNotEmpty()) {
                    val scrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        existingCategories.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = 10.sp) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("备注（可选）", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.brandTeal,
                        unfocusedBorderColor = appColors.softBackground
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("标签（用 | 分隔，可选）", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.brandTeal,
                        unfocusedBorderColor = appColors.softBackground
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                    placeholder = { Text("例如：必要|可报销", fontSize = 11.sp, color = appColors.textSecondary) }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    val finalCategory = if (useNewCategory) newCategory else category
                    if (amountValue > 0 && finalCategory.isNotEmpty()) {
                        onAdd(finalCategory, amountValue, description, tags, "")
                    }
                },
                enabled = amount.toDoubleOrNull()?.let { it > 0 } == true && 
                          (if (useNewCategory) newCategory.isNotEmpty() else category.isNotEmpty())
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        containerColor = appColors.cardBackground
    )
}

@Composable
fun ExpenseTrendChart(
    dailyExpenses: Map<String, Double>,
    appColors: AppColors,
    modifier: Modifier = Modifier
) {
    val format = NumberFormat.getNumberInstance(Locale.getDefault())
    format.maximumFractionDigits = 2

    if (dailyExpenses.isEmpty()) return

    val dates = dailyExpenses.keys.toList()
    val amounts = dailyExpenses.values.toList()
    val maxAmount = amounts.maxOrNull() ?: 0.0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            "费用趋势",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = appColors.textPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val chartWidth = size.width
            val chartHeight = size.height
            val padding = 30.dp.toPx()
            val bottomPadding = 20.dp.toPx()

            val drawWidth = chartWidth - padding * 2
            val drawHeight = chartHeight - bottomPadding - 10.dp.toPx()

            if (maxAmount > 0 && dates.isNotEmpty()) {
                val points = mutableListOf<Pair<Float, Float>>()

                dates.forEachIndexed { index, date ->
                    val amount = dailyExpenses[date] ?: 0.0
                    val x = padding + (index.toFloat() / (dates.size - 1).coerceAtLeast(1)) * drawWidth
                    val y = drawHeight - (amount.toFloat() / maxAmount.toFloat()) * drawHeight
                    points.add(Pair(x, y))
                }

                if (points.size > 1) {
                    val path = Path()
                    path.moveTo(points[0].first, points[0].second)

                    for (i in 1 until points.size) {
                        path.lineTo(points[i].first, points[i].second)
                    }

                    drawPath(
                        path = path,
                        color = appColors.brandTeal,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    points.forEach { point ->
                        drawCircle(
                            color = appColors.brandTeal,
                            radius = 4.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(point.first, point.second)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (dates.isNotEmpty()) {
                Text(dates.first(), fontSize = 9.sp, color = appColors.textSecondary)
                if (dates.size > 1) {
                    Text(dates.last(), fontSize = 9.sp, color = appColors.textSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "日均: ¥${format.format(amounts.average())}",
                fontSize = 10.sp,
                color = appColors.textSecondary
            )
            Text(
                "最高: ¥${format.format(maxAmount)}",
                fontSize = 10.sp,
                color = appColors.textSecondary
            )
        }
    }
}

/**
 * 设置预算对话框
 */
@Composable
fun SetBudgetDialog(
    currentBudget: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit,
    appColors: AppColors
) {
    var budgetAmount by remember { mutableStateOf(if (currentBudget > 0) currentBudget.toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置预算", fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "为本次行程设置总预算，系统将自动跟踪花费情况",
                    fontSize = 11.sp,
                    color = appColors.textSecondary
                )
                OutlinedTextField(
                    value = budgetAmount,
                    onValueChange = { budgetAmount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("预算金额", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    prefix = { Text("¥", fontSize = 13.sp, color = appColors.textSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.brandTeal,
                        unfocusedBorderColor = appColors.softBackground
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = budgetAmount.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onSave(amount)
                    }
                },
                enabled = budgetAmount.toDoubleOrNull()?.let { it > 0 } == true
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        containerColor = appColors.cardBackground
    )
}
