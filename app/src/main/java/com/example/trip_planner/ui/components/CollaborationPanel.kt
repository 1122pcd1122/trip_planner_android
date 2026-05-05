package com.example.trip_planner.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trip_planner.data.local.entity.TripPlanEntity
import com.example.trip_planner.ui.theme.AppColors
import com.example.trip_planner.utils.CollaborationUtils

/**
 * 协作组件
 * 支持行程共享和协同编辑
 */
@Composable
fun CollaborationPanel(
    tripPlan: TripPlanEntity,
    onImportTrip: (TripPlanEntity) -> Unit,
    appColors: AppColors,
    modifier: Modifier = Modifier
) {
    var showImportDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
                "协作",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = appColors.textPrimary
            )
            Row {
                TextButton(onClick = {
                    CollaborationUtils.shareTrip(context, tripPlan)
                }) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "分享",
                        tint = appColors.brandTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("分享", color = appColors.brandTeal, fontSize = 12.sp)
                }
                TextButton(onClick = { showImportDialog = true }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "导入",
                        tint = appColors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("导入", color = appColors.textSecondary, fontSize = 12.sp)
                }
            }
        }
    }

    if (showImportDialog) {
        ImportTripDialog(
            onDismiss = { showImportDialog = false },
            onImport = { trip ->
                onImportTrip(trip)
                showImportDialog = false
            },
            appColors = appColors
        )
    }
}

/**
 * 导入行程对话框
 */
@Composable
fun ImportTripDialog(
    onDismiss: () -> Unit,
    onImport: (TripPlanEntity) -> Unit,
    appColors: AppColors
) {
    var shareCode by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导入行程", fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "请输入分享码",
                    fontSize = 12.sp,
                    color = appColors.textSecondary
                )
                OutlinedTextField(
                    value = shareCode,
                    onValueChange = { shareCode = it; error = null },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("粘贴分享码", fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.brandTeal,
                        unfocusedBorderColor = appColors.softBackground
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                    minLines = 3,
                    maxLines = 5
                )
                if (error != null) {
                    Text(
                        error!!,
                        fontSize = 12.sp,
                        color = appColors.error,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (shareCode.isBlank()) {
                        error = "请输入分享码"
                        return@TextButton
                    }
                    val trip = CollaborationUtils.importTripFromCode(shareCode.trim())
                    if (trip != null) {
                        onImport(trip)
                    } else {
                        error = "分享码无效，请检查后重试"
                    }
                },
                enabled = shareCode.isNotBlank()
            ) {
                Text("导入")
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
