package com.example.trip_planner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePicker(
    startDate: String,
    endDate: String,
    onDateRangeSelected: (String, String) -> Unit,
    appColors: com.example.trip_planner.ui.theme.AppColors,
    key: String = ""
) {
    var showDatePicker by remember(key) { mutableStateOf(false) }
    var tempStartDate by remember { mutableStateOf("") }
    var tempEndDate by remember { mutableStateOf("") }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // 同步外部状态
    LaunchedEffect(startDate, endDate) {
        tempStartDate = startDate
        tempEndDate = endDate
    }

    val displayText = when {
        tempStartDate.isNotEmpty() && tempEndDate.isNotEmpty() -> "$tempStartDate ~ $tempEndDate"
        tempStartDate.isNotEmpty() -> "$tempStartDate 起"
        else -> "选择日期范围"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { 
                    // 如果日期范围已完整，点击时重置
                    if (tempStartDate.isNotEmpty() && tempEndDate.isNotEmpty()) {
                        tempStartDate = ""
                        tempEndDate = ""
                    }
                    showDatePicker = true 
                }
        ) {
            OutlinedTextField(
                value = displayText,
                onValueChange = {},
                placeholder = { Text("选择日期范围", color = appColors.textSecondary, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = appColors.brandTeal, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (tempStartDate.isNotEmpty() || tempEndDate.isNotEmpty()) {
                        IconButton(onClick = { 
                            tempStartDate = ""
                            tempEndDate = ""
                            onDateRangeSelected("", "")
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "清除", tint = appColors.textSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = appColors.brandTeal,
                    unfocusedBorderColor = appColors.divider,
                    focusedContainerColor = appColors.cardBackground,
                    unfocusedContainerColor = appColors.cardBackground
                ),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                readOnly = true,
                enabled = false
            )
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = System.currentTimeMillis()
            )

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = dateFormat.format(Date(millis))
                            
                            // 第一次选择 → 设置开始日期
                            if (tempStartDate.isEmpty() || tempEndDate.isNotEmpty()) {
                                tempStartDate = selectedDate
                                tempEndDate = ""
                            } 
                            // 第二次选择，且日期晚于开始日期 → 设置结束日期
                            else if (millis > (dateFormat.parse(tempStartDate)?.time ?: 0)) {
                                tempEndDate = selectedDate
                                onDateRangeSelected(tempStartDate, tempEndDate)
                            } 
                            // 第二次选择，但日期早于开始日期 → 交换
                            else {
                                tempEndDate = tempStartDate
                                tempStartDate = selectedDate
                                onDateRangeSelected(tempStartDate, tempEndDate)
                            }
                        }
                        
                        // 如果日期范围完整，关闭对话框
                        if (tempStartDate.isNotEmpty() && tempEndDate.isNotEmpty()) {
                            showDatePicker = false
                        }
                    }) {
                        Text("确定", color = appColors.brandTeal)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("取消", color = appColors.textSecondary)
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false
                )
            }
        }
    }
}
