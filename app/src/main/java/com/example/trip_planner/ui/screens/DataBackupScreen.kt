package com.example.trip_planner.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trip_planner.ui.theme.LocalAppColors
import com.example.trip_planner.utils.DataBackupUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataBackupScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appColors = LocalAppColors.current
    val scope = rememberCoroutineScope()
    var pendingImportUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showReplaceConfirm by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                isProcessing = true
                val result = DataBackupUtils.exportData(context, it)
                isProcessing = false
                if (result.isSuccess) {
                    Toast.makeText(context, "导出成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "导出失败: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            pendingImportUri = it
            showReplaceConfirm = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据备份", fontSize = 16.sp) },
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
                .verticalScroll(rememberScrollState())
        ) {
            BackupActionCard(
                icon = Icons.Default.Upload,
                title = "导出数据",
                description = "将所有旅行数据导出为 JSON 文件",
                buttonText = "导出",
                onClick = { exportLauncher.launch("trip_backup_${System.currentTimeMillis()}.json") },
                appColors = appColors
            )

            BackupActionCard(
                icon = Icons.Default.Download,
                title = "导入数据",
                description = "从 JSON 文件恢复旅行数据",
                buttonText = "导入",
                onClick = { importLauncher.launch(arrayOf("application/json")) },
                appColors = appColors
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(appColors.cardBackground)
                    .padding(16.dp)
            ) {
                Text("说明", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = appColors.textPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("• 导出的数据包含：行程规划、旅行笔记、费用记录、预算设置、打包清单", fontSize = 12.sp, color = appColors.textSecondary)
                Text("• 导入时可选择覆盖现有数据或追加到现有数据", fontSize = 12.sp, color = appColors.textSecondary)
                Text("• 建议定期备份重要数据", fontSize = 12.sp, color = appColors.textSecondary)
            }
        }
    }

    if (showReplaceConfirm) {
        AlertDialog(
            onDismissRequest = { showReplaceConfirm = false },
            title = { Text("导入方式", fontSize = 16.sp) },
            text = { Text("请选择导入方式：\n• 覆盖：清空现有数据后导入\n• 追加：保留现有数据并添加新数据", fontSize = 14.sp) },
            confirmButton = {
                TextButton(onClick = {
                    showReplaceConfirm = false
                    pendingImportUri?.let { uri ->
                        scope.launch {
                            isProcessing = true
                            val result = DataBackupUtils.importData(context, uri, true)
                            isProcessing = false
                            if (result.isSuccess) {
                                Toast.makeText(context, "导入成功", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "导入失败: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }) {
                    Text("覆盖")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { showReplaceConfirm = false }) {
                        Text("取消")
                    }
                    TextButton(onClick = {
                        showReplaceConfirm = false
                        pendingImportUri?.let { uri ->
                            scope.launch {
                                isProcessing = true
                                val result = DataBackupUtils.importData(context, uri, false)
                                isProcessing = false
                                if (result.isSuccess) {
                                    Toast.makeText(context, "导入成功", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "导入失败: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }) {
                        Text("追加")
                    }
                }
            },
            containerColor = appColors.cardBackground
        )
    }

    if (isProcessing) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors.softBackground.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = appColors.brandTeal)
        }
    }
}

@Composable
fun BackupActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(appColors.cardBackground)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = appColors.brandTeal, modifier = Modifier.size(24.dp))
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = appColors.textPrimary)
                Text(description, fontSize = 12.sp, color = appColors.textSecondary)
            }
        }
        TextButton(onClick = onClick) {
            Text(buttonText, fontSize = 14.sp, color = appColors.brandTeal)
        }
    }
}
