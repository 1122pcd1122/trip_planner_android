package com.example.trip_planner.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trip_planner.data.local.entity.TripPlanEntity
import com.example.trip_planner.utils.SearchUtils
import com.example.trip_planner.utils.ShareUtils
import com.example.trip_planner.utils.UndoManager
import com.example.trip_planner.viewModel.HistoryViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 历史记录页面（极简现代风）
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    onPlanClick: (TripPlanEntity) -> Unit = {},
    viewModel: HistoryViewModel = viewModel()
) {
    val allPlans by viewModel.allPlans.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedTimeFilter by remember { mutableStateOf<TimeFilter>(TimeFilter.ALL) }
    val appColors = com.example.trip_planner.ui.theme.LocalAppColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val undoManager = remember { UndoManager(scope) }
    var deletedPlan by remember { mutableStateOf<TripPlanEntity?>(null) }

    val filteredPlans by remember {
        derivedStateOf {
            var filtered = allPlans
            
            val now = System.currentTimeMillis()
            filtered = when (selectedTimeFilter) {
                TimeFilter.ALL -> filtered
                TimeFilter.TODAY -> filtered.filter { 
                    isSameDay(it.timestamp, now) 
                }
                TimeFilter.WEEK -> filtered.filter { 
                    isWithinWeek(it.timestamp, now) 
                }
                TimeFilter.MONTH -> filtered.filter { 
                    isWithinMonth(it.timestamp, now) 
                }
            }
            
            if (searchQuery.isNotBlank()) {
                filtered = SearchUtils.filterAndSort(
                    items = filtered,
                    query = searchQuery,
                    getText = { "${it.destination} ${it.preferences}" }
                )
            }
            
            filtered
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(appColors.softBackground)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                placeholder = { Text("搜索目的地或偏好", color = appColors.textSecondary, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = appColors.textSecondary, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = appColors.divider,
                    unfocusedBorderColor = appColors.divider.copy(alpha = 0.5f),
                    focusedContainerColor = appColors.cardBackground,
                    unfocusedContainerColor = appColors.cardBackground
                )
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(TimeFilter.entries) { filter ->
                    FilterChip(
                        selected = selectedTimeFilter == filter,
                        onClick = { selectedTimeFilter = filter },
                        label = { Text(filter.label, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = appColors.brandTeal,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (filteredPlans.isEmpty()) {
                EmptyHistoryState(
                    appColors = appColors, 
                    hasQuery = searchQuery.isNotBlank(),
                    hasFilter = selectedTimeFilter != TimeFilter.ALL
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .animateContentSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(
                        items = filteredPlans,
                        key = { it.id },
                        contentType = { "history_plan_card" }
                    ) { plan ->
                        SwipeableHistoryCard(
                            plan = plan,
                            onClick = { onPlanClick(plan) },
                            onDelete = {
                                deletedPlan = plan
                                undoManager.deleteWithUndo(
                                    id = plan.id.toString(),
                                    type = UndoManager.ActionType.DELETE_HISTORY,
                                    data = plan,
                                    deleteAction = { viewModel.deletePlan(plan.id) },
                                    undoAction = {
                                        viewModel.insertPlan(plan)
                                    }
                                )
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "已删除行程",
                                        actionLabel = "撤销",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        undoManager.undo()
                                    }
                                }
                            },
                            onShare = { ShareUtils.shareTripPlan(context, plan) },
                            appColors = appColors
                        )
                        HorizontalDivider(color = appColors.divider.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

/**
 * 时间筛选枚举
 */
enum class TimeFilter(val label: String) {
    ALL("全部"),
    TODAY("今天"),
    WEEK("本周"),
    MONTH("本月")
}

/**
 * 判断是否为同一天
 */
fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

/**
 * 判断是否在一周内
 */
fun isWithinWeek(timestamp: Long, now: Long): Boolean {
    val oneWeekMs = 7 * 24 * 60 * 60 * 1000L
    return now - timestamp < oneWeekMs
}

/**
 * 判断是否在一个月内
 */
fun isWithinMonth(timestamp: Long, now: Long): Boolean {
    val oneMonthMs = 30 * 24 * 60 * 60 * 1000L
    return now - timestamp < oneMonthMs
}

/**
 * 历史行程卡片（极简现代风）
 */
@Composable
fun HistoryPlanCard(
    plan: TripPlanEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    val context = LocalContext.current
    val dateFormat = remember { java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()) }
    val dateStr = remember(plan.timestamp) { dateFormat.format(java.util.Date(plan.timestamp)) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    plan.destination,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = appColors.textPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "${plan.days}天",
                        color = appColors.textSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        dateStr,
                        color = appColors.textSecondary,
                        fontSize = 13.sp
                    )
                }
                if (plan.preferences.isNotEmpty()) {
                    Text(
                        plan.preferences,
                        color = appColors.textSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Row {
                TextButton(onClick = { ShareUtils.shareTripPlan(context, plan) }) {
                    Text("分享", color = appColors.brandTeal, fontSize = 12.sp)
                }
                TextButton(onClick = onDelete) {
                    Text("删除", color = appColors.error.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * 可滑动历史行程卡片（极简现代风）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableHistoryCard(
    plan: TripPlanEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onShare()
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color = when {
                direction == SwipeToDismissBoxValue.StartToEnd -> appColors.brandTeal.copy(alpha = 0.15f)
                direction == SwipeToDismissBoxValue.EndToStart -> appColors.error.copy(alpha = 0.15f)
                else -> Color.Transparent
            }
            val icon = when {
                direction == SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Share
                direction == SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                else -> Icons.Default.Share
            }
            val alignment = when {
                direction == SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                direction == SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.CenterStart
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (direction == SwipeToDismissBoxValue.EndToStart) appColors.error else appColors.brandTeal,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        content = {
            HistoryPlanCard(
                plan = plan,
                onClick = onClick,
                onDelete = onDelete,
                appColors = appColors
            )
        }
    )
}

/**
 * 空历史状态（极简现代风）
 */
@Composable
fun EmptyHistoryState(
    appColors: com.example.trip_planner.ui.theme.AppColors,
    hasQuery: Boolean,
    hasFilter: Boolean,
    onNavigateToPlan: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                when {
                    hasQuery -> "未找到匹配的行程"
                    hasFilter -> "该时间范围内暂无行程"
                    else -> "暂无历史记录"
                },
                color = appColors.textSecondary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                when {
                    hasQuery -> "试试其他关键词"
                    hasFilter -> "试试其他时间范围"
                    else -> "规划您的第一个行程吧"
                },
                color = appColors.textSecondary,
                fontSize = 12.sp
            )
            if (!hasQuery && !hasFilter && onNavigateToPlan != null) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onNavigateToPlan) {
                    Text(
                        "去规划行程",
                        color = appColors.brandTeal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
