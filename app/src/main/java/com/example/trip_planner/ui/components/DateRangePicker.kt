package com.example.trip_planner.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
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
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var isSelectingStart by remember { mutableStateOf(true) }
    
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val displayText = when {
        startDate.isNotEmpty() && endDate.isNotEmpty() -> "$startDate 至 $endDate"
        startDate.isNotEmpty() -> "$startDate 起"
        else -> "选择日期范围"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }
        ) {
            OutlinedTextField(
                value = displayText,
                onValueChange = {},
                placeholder = { Text("选择日期范围", color = appColors.textSecondary, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = appColors.brandTeal, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (startDate.isNotEmpty()) {
                        IconButton(onClick = { 
                            onDateRangeSelected("", "")
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "清除", tint = appColors.textSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = appColors.divider,
                    unfocusedBorderColor = appColors.divider,
                    focusedContainerColor = appColors.cardBackground,
                    unfocusedContainerColor = appColors.cardBackground
                ),
                singleLine = true,
                textStyle = TextStyle(fontSize = 13.sp),
                readOnly = true,
                enabled = false
            )
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()

            AlertDialog(
                onDismissRequest = { 
                    showDatePicker = false
                    isSelectingStart = true
                },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSelectingStart) "选择开始日期" else "选择结束日期",
                            fontSize = 16.sp,
                            color = appColors.textPrimary
                        )
                        TextButton(onClick = { 
                            showDatePicker = false
                            isSelectingStart = true
                        }) {
                            Text("取消", color = appColors.textSecondary)
                        }
                    }
                },
                text = {
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = false
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val selectedDate = dateFormat.format(Date(millis))
                                if (isSelectingStart) {
                                    onDateRangeSelected(selectedDate, endDate)
                                    isSelectingStart = false
                                } else {
                                    if (startDate.isNotEmpty()) {
                                        onDateRangeSelected(startDate, selectedDate)
                                    } else {
                                        onDateRangeSelected(selectedDate, "")
                                    }
                                    isSelectingStart = true
                                    showDatePicker = false
                                }
                            }
                        }
                    ) {
                        Text("确定", color = appColors.brandTeal)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showDatePicker = false
                        isSelectingStart = true
                    }) {
                        Text("取消", color = appColors.textSecondary)
                    }
                },
                containerColor = appColors.cardBackground,
                shape = MaterialTheme.shapes.medium
            )
        }
    }
}
