package com.example.trip_planner.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    val displayText = when {
        startDate.isNotEmpty() && endDate.isNotEmpty() -> "$startDate ~ $endDate"
        startDate.isNotEmpty() -> "$startDate 起"
        else -> "选择日期范围"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            placeholder = { Text("选择日期范围", color = appColors.textSecondary, fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = appColors.brandTeal, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                if (startDate.isNotEmpty()) {
                    IconButton(onClick = { onDateRangeSelected("", "") }) {
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
            enabled = true
        )

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
                            if (startDate.isEmpty() || endDate.isNotEmpty()) {
                                onDateRangeSelected(selectedDate, "")
                            } else if (millis > dateFormat.parse(startDate)?.time ?: 0) {
                                onDateRangeSelected(startDate, selectedDate)
                            } else {
                                onDateRangeSelected(selectedDate, startDate)
                            }
                        }
                        showDatePicker = false
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
