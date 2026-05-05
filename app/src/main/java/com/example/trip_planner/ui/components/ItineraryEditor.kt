package com.example.trip_planner.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.mutableFloatStateOf
import com.example.trip_planner.network.model.DayPlan
import com.example.trip_planner.network.model.PlanHotel
import com.example.trip_planner.ui.theme.AppColors
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 行程编辑器
 * 支持对每日行程进行增删改查操作
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItineraryEditor(
    dayPlans: List<DayPlan>,
    planHotels: List<PlanHotel>,
    overallTips: String,
    onPlansUpdated: (List<DayPlan>, List<PlanHotel>, String) -> Unit,
    appColors: AppColors
) {
    var editingDayIndex by remember { mutableStateOf<Int?>(null) }
    var editingHotelIndex by remember { mutableStateOf<Int?>(null) }
    var editingTips by remember { mutableStateOf(false) }

    var mutableDayPlans by remember { mutableStateOf(dayPlans.toMutableList()) }
    var mutableHotels by remember { mutableStateOf(planHotels.toMutableList()) }
    var mutableTips by remember { mutableStateOf(overallTips) }
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var targetIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(dayPlans) {
        mutableDayPlans = dayPlans.toMutableList()
    }

    LaunchedEffect(planHotels) {
        mutableHotels = planHotels.toMutableList()
    }

    LaunchedEffect(overallTips) {
        mutableTips = overallTips
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // 行程概览
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "行程概览",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = appColors.textPrimary
                )
                Text(
                    "${mutableDayPlans.size}天 · ${mutableHotels.size}家酒店",
                    fontSize = 12.sp,
                    color = appColors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 每日行程列表（支持拖拽排序）
            mutableDayPlans.forEachIndexed { index, dayPlan ->
                DayPlanItem(
                    dayIndex = index,
                    dayPlan = dayPlan,
                    isDragging = draggedIndex == index,
                    onEdit = { editingDayIndex = index },
                    onDelete = {
                        if (mutableDayPlans.size > 1) {
                            mutableDayPlans = mutableDayPlans.toMutableList().apply { removeAt(index) }
                            onPlansUpdated(mutableDayPlans, mutableHotels, mutableTips)
                        }
                    },
                    onDragStart = { draggedIndex = index },
                    onDragEnd = {
                        draggedIndex?.let { fromIndex ->
                            targetIndex?.let { toIndex ->
                                if (fromIndex != toIndex) {
                                    val newList = mutableDayPlans.toMutableList()
                                    val item = newList.removeAt(fromIndex)
                                    newList.add(toIndex, item)
                                    mutableDayPlans = newList
                                    onPlansUpdated(mutableDayPlans, mutableHotels, mutableTips)
                                }
                            }
                        }
                        draggedIndex = null
                        targetIndex = null
                    },
                    onDragMove = { toIndex -> targetIndex = toIndex },
                    appColors = appColors
                )
                if (index < mutableDayPlans.size - 1) {
                    HorizontalDivider(color = appColors.textSecondary.copy(alpha = 0.08f))
                }
            }

            // 添加新的一天
            TextButton(
                onClick = {
                    val newDay = DayPlan(
                        dayNum = mutableDayPlans.size + 1,
                        date = "Day ${mutableDayPlans.size + 1}",
                        weather = "",
                        itinerary = emptyList(),
                        meals = null,
                        tips = "新的一天行程"
                    )
                    mutableDayPlans = mutableDayPlans.toMutableList().apply { add(newDay) }
                    onPlansUpdated(mutableDayPlans, mutableHotels, mutableTips)
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = appColors.brandTeal, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("添加新的一天", color = appColors.brandTeal, fontSize = 13.sp)
            }
        }

        HorizontalDivider()

        // 酒店列表编辑
        if (mutableHotels.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    "推荐酒店",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = appColors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                mutableHotels.forEachIndexed { index, hotel ->
                    HotelItem(
                        hotelIndex = index,
                        hotel = hotel,
                        onEdit = { editingHotelIndex = index },
                        onDelete = {
                            mutableHotels = mutableHotels.toMutableList().apply { removeAt(index) }
                            onPlansUpdated(mutableDayPlans, mutableHotels, mutableTips)
                        },
                        appColors = appColors
                    )
                    if (index < mutableHotels.size - 1) {
                        HorizontalDivider(color = appColors.textSecondary.copy(alpha = 0.08f))
                    }
                }
            }

            HorizontalDivider()
        }

        // 整体建议编辑
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "整体建议",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = appColors.textPrimary
                )
                IconButton(onClick = { editingTips = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "编辑", tint = appColors.textSecondary, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                mutableTips,
                fontSize = 13.sp,
                color = appColors.textSecondary,
                lineHeight = 20.sp
            )
        }
    }

    // 编辑日程对话框
    if (editingDayIndex != null) {
        DayPlanEditorDialog(
            dayPlan = mutableDayPlans[editingDayIndex!!],
            onDismiss = { editingDayIndex = null },
            onSave = { updatedDay ->
                mutableDayPlans = mutableDayPlans.toMutableList().apply {
                    set(editingDayIndex!!, updatedDay)
                }
                onPlansUpdated(mutableDayPlans, mutableHotels, mutableTips)
                editingDayIndex = null
            },
            appColors = appColors
        )
    }

    // 编辑酒店对话框
    if (editingHotelIndex != null) {
        HotelEditorDialog(
            hotel = mutableHotels[editingHotelIndex!!],
            onDismiss = { editingHotelIndex = null },
            onSave = { updatedHotel ->
                mutableHotels = mutableHotels.toMutableList().apply {
                    set(editingHotelIndex!!, updatedHotel)
                }
                onPlansUpdated(mutableDayPlans, mutableHotels, mutableTips)
                editingHotelIndex = null
            },
            appColors = appColors
        )
    }

    // 编辑建议对话框
    if (editingTips) {
        TipsEditorDialog(
            currentTips = mutableTips,
            onDismiss = { editingTips = false },
            onSave = { newTips ->
                mutableTips = newTips
                onPlansUpdated(mutableDayPlans, mutableHotels, mutableTips)
                editingTips = false
            },
            appColors = appColors
        )
    }
}

/**
 * 单日行程项
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DayPlanItem(
    dayIndex: Int,
    dayPlan: DayPlan,
    isDragging: Boolean = false,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDragMove: (Int) -> Unit,
    appColors: AppColors
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onEdit,
                onLongClick = onDragStart
            )
            .graphicsLayer {
                translationX = offsetX
                scaleX = if (isDragging) 1.02f else 1f
                scaleY = if (isDragging) 1.02f else 1f
                shadowElevation = if (isDragging) 8f else 0f
            }
            .pointerInput(dayIndex) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { onDragStart() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        onDragMove(dayIndex)
                    },
                    onDragEnd = {
                        offsetX = 0f
                        onDragEnd()
                    },
                    onDragCancel = {
                        offsetX = 0f
                        onDragEnd()
                    }
                )
            }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.DragHandle,
            contentDescription = "拖拽排序",
            tint = if (isDragging) appColors.brandTeal else appColors.textSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Day ${dayIndex + 1}: ${dayPlan.date}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = appColors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                dayPlan.weather,
                fontSize = 12.sp,
                color = appColors.textSecondary
            )
        }
        Row {
            TextButton(onClick = onEdit, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text("编辑", color = appColors.brandTeal, fontSize = 12.sp)
            }
            TextButton(onClick = onDelete, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text("删除", color = appColors.error, fontSize = 12.sp)
            }
        }
    }
}

/**
 * 酒店项
 */
@Composable
fun HotelItem(
    hotelIndex: Int,
    hotel: PlanHotel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    appColors: AppColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                hotel.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = appColors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                hotel.price,
                fontSize = 12.sp,
                color = appColors.textSecondary
            )
        }
        Row {
            TextButton(onClick = onEdit, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text("编辑", color = appColors.brandTeal, fontSize = 12.sp)
            }
            TextButton(onClick = onDelete, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text("删除", color = appColors.error, fontSize = 12.sp)
            }
        }
    }
}

/**
 * 日程编辑对话框
 */
@Composable
fun DayPlanEditorDialog(
    dayPlan: DayPlan,
    onDismiss: () -> Unit,
    onSave: (DayPlan) -> Unit,
    appColors: AppColors
) {
    var editedDate by remember { mutableStateOf(dayPlan.date) }
    var editedWeather by remember { mutableStateOf(dayPlan.weather) }
    var editedTips by remember { mutableStateOf(dayPlan.tips) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑行程", fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = editedDate,
                    onValueChange = { editedDate = it },
                    label = { Text("日期", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.brandTeal,
                        unfocusedBorderColor = appColors.softBackground
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )
                OutlinedTextField(
                    value = editedWeather,
                    onValueChange = { editedWeather = it },
                    label = { Text("天气", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.brandTeal,
                        unfocusedBorderColor = appColors.softBackground
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )
                OutlinedTextField(
                    value = editedTips,
                    onValueChange = { editedTips = it },
                    label = { Text("提示", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.brandTeal,
                        unfocusedBorderColor = appColors.softBackground
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(dayPlan.copy(date = editedDate, weather = editedWeather, tips = editedTips))
            }) {
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

/**
 * 酒店编辑对话框
 */
@Composable
fun HotelEditorDialog(
    hotel: PlanHotel,
    onDismiss: () -> Unit,
    onSave: (PlanHotel) -> Unit,
    appColors: AppColors
) {
    var editedName by remember { mutableStateOf(hotel.name) }
    var editedPrice by remember { mutableStateOf(hotel.price) }
    var editedAddress by remember { mutableStateOf(hotel.address) }
    var editedAdvantage by remember { mutableStateOf(hotel.advantage) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑酒店", fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("酒店名称", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.brandTeal,
                        unfocusedBorderColor = appColors.softBackground
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )
                OutlinedTextField(
                    value = editedPrice,
                    onValueChange = { editedPrice = it },
                    label = { Text("价格", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.brandTeal,
                        unfocusedBorderColor = appColors.softBackground
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )
                OutlinedTextField(
                    value = editedAddress,
                    onValueChange = { editedAddress = it },
                    label = { Text("地址", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.brandTeal,
                        unfocusedBorderColor = appColors.softBackground
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )
                OutlinedTextField(
                    value = editedAdvantage,
                    onValueChange = { editedAdvantage = it },
                    label = { Text("优势", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.brandTeal,
                        unfocusedBorderColor = appColors.softBackground
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(hotel.copy(name = editedName, price = editedPrice, address = editedAddress, advantage = editedAdvantage))
            }) {
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

/**
 * 建议编辑对话框
 */
@Composable
fun TipsEditorDialog(
    currentTips: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    appColors: AppColors
) {
    var editedTips by remember { mutableStateOf(currentTips) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑整体建议", fontSize = 16.sp) },
        text = {
            OutlinedTextField(
                value = editedTips,
                onValueChange = { editedTips = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = appColors.brandTeal,
                    unfocusedBorderColor = appColors.softBackground
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(editedTips) }) {
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
