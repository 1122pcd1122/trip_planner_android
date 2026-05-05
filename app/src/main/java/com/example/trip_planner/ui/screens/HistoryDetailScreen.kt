package com.example.trip_planner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trip_planner.data.local.entity.TripPlanEntity
import com.example.trip_planner.network.model.DayPlan
import com.example.trip_planner.network.model.ItineraryItem
import com.example.trip_planner.network.model.MealInfo
import com.example.trip_planner.network.model.PlanHotel
import com.example.trip_planner.utils.PdfExportUtils
import com.example.trip_planner.utils.ShareUtils
import com.example.trip_planner.utils.CalendarExportUtils
import com.google.gson.Gson
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)

/**
 * 历史记录详情页
 * 
 * 独立展示历史行程的详细信息
 * 支持查看每日行程、酒店推荐和出行建议
 */
@Composable
fun HistoryDetailScreen(
    plan: TripPlanEntity,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val appColors = com.example.trip_planner.ui.theme.LocalAppColors.current
    val gson = remember { Gson() }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var dayPlans by remember { mutableStateOf<List<DayPlan>>(emptyList()) }
    var planHotels by remember { mutableStateOf<List<PlanHotel>>(emptyList()) }

    LaunchedEffect(plan) {
        try {
            if (plan.dayPlansJson.isNotBlank()) {
                dayPlans = gson.fromJson(plan.dayPlansJson, Array<DayPlan>::class.java).toList()
            }
            if (plan.hotelJson.isNotBlank()) {
                planHotels = gson.fromJson(plan.hotelJson, Array<PlanHotel>::class.java).toList()
            }
        } catch (e: Exception) {
            dayPlans = emptyList()
            planHotels = emptyList()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "${plan.destination} ${plan.days}日游",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "创建于 ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(plan.timestamp))}",
                            fontSize = 12.sp,
                            color = appColors.textSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        ShareUtils.shareTripPlan(context, plan)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "分享行程",
                            tint = appColors.brandTeal
                        )
                    }
                    IconButton(onClick = {
                        ShareUtils.copyDetailedTripPlan(context, plan)
                    }) {
                        Icon(
                            imageVector = Icons.Default.TextSnippet,
                            contentDescription = "复制文本",
                            tint = appColors.brandTeal
                        )
                    }
                    IconButton(onClick = {
                        CalendarExportUtils.addTripToCalendar(context, plan)
                    }) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "导出到日历",
                            tint = appColors.brandTeal
                        )
                    }
                    IconButton(onClick = {
                        scope.launch {
                            try {
                                val pdfFile = PdfExportUtils.exportTripPlan(context, plan)
                                PdfExportUtils.sharePdfFile(context, pdfFile)
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("导出失败: ${e.message}")
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "导出PDF",
                            tint = appColors.brandTeal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = appColors.cardBackground
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors.softBackground)
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 出行建议
            if (plan.overallTips.isNotBlank()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "出行建议",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = appColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            plan.overallTips,
                            color = appColors.textSecondary,
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // 酒店推荐
            if (planHotels.isNotEmpty()) {
                item {
                    Text(
                        "酒店推荐",
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(planHotels) { hotel ->
                    HotelCard(hotel = hotel, appColors = appColors)
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // 每日行程
            if (dayPlans.isNotEmpty()) {
                item {
                    Text(
                        "每日行程",
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(dayPlans) { day ->
                    DayPlanCardReadOnly(day = day, appColors = appColors)
                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // 空状态
            if (dayPlans.isEmpty() && planHotels.isEmpty()) {
                item {
                    EmptyState(message = "暂无行程数据", appColors = appColors)
                }
            }
        }
    }
}

/**
 * 只读的每日行程卡片（不支持编辑）
 */
@Composable
fun DayPlanCardReadOnly(
    day: DayPlan,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "第${day.dayNum}天 - ${day.date}",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = appColors.textPrimary
            )
            Text(
                day.weather,
                color = appColors.textSecondary,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        day.itinerary.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    item.time,
                    color = appColors.brandTeal,
                    fontSize = 13.sp,
                    modifier = Modifier.width(60.dp)
                )
                Text(
                    item.spot,
                    fontSize = 14.sp,
                    color = appColors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        day.meals?.let { meals ->
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            meals.lunch?.let {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text("午餐: ", color = appColors.textSecondary, fontSize = 13.sp)
                    Text(it.name, color = appColors.textPrimary, fontSize = 13.sp)
                }
            }
            meals.dinner?.let {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text("晚餐: ", color = appColors.textSecondary, fontSize = 13.sp)
                    Text(it.name, color = appColors.textPrimary, fontSize = 13.sp)
                }
            }
        }

        if (day.tips.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                day.tips,
                color = appColors.textSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}
