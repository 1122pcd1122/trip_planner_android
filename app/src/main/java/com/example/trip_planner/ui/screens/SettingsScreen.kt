package com.example.trip_planner.ui.screens

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trip_planner.ui.theme.LocalAppColors
import com.example.trip_planner.utils.CacheUtils
import kotlinx.coroutines.launch

/**
 * 设置页面
 * 包含隐私政策入口、版本号、清除缓存等功能
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onDataBackupClick: () -> Unit
) {
    val context = LocalContext.current
    val appColors = LocalAppColors.current
    val scope = rememberCoroutineScope()
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var cacheSize by remember { mutableStateOf(0L) }
    var isClearing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        cacheSize = CacheUtils.getCacheSize(context)
    }

    val packageInfo = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: Exception) {
            null
        }
    }
    val versionName = packageInfo?.versionName ?: "1.0"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSection(title = "关于", appColors = appColors) {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "版本号",
                    subtitle = "v$versionName",
                    appColors = appColors
                )
                SettingsItem(
                    icon = Icons.Default.Description,
                    title = "隐私政策",
                    onClick = onPrivacyPolicyClick,
                    appColors = appColors
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsSection(title = "存储", appColors = appColors) {
                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = "清除缓存",
                    subtitle = CacheUtils.formatCacheSize(cacheSize),
                    onClick = { showClearCacheDialog = true },
                    appColors = appColors
                )
                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = "数据备份",
                    subtitle = "导出或导入旅行数据",
                    onClick = onDataBackupClick,
                    appColors = appColors
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "旅行规划助手",
                fontSize = 12.sp,
                color = appColors.textSecondary.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }

    if (showClearCacheDialog) {
                AlertDialog(
                    onDismissRequest = { if (!isClearing) showClearCacheDialog = false },
                    title = { Text("清除缓存", fontSize = 16.sp) },
                    text = { Text("确定要清除所有缓存数据吗？此操作不可撤销。", fontSize = 14.sp) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                isClearing = true
                                scope.launch {
                                    CacheUtils.clearAppCache(context)
                                        .onSuccess {
                                            cacheSize = 0
                                            Toast.makeText(context, "缓存已清除", Toast.LENGTH_SHORT).show()
                                        }
                                        .onFailure {
                                            Toast.makeText(context, "清除失败: ${it.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    isClearing = false
                                    showClearCacheDialog = false
                                }
                            },
                            enabled = !isClearing
                        ) {
                            if (isClearing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = appColors.brandTeal
                                )
                            } else {
                                Text("确定")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showClearCacheDialog = false },
                            enabled = !isClearing
                        ) {
                            Text("取消")
                        }
                    },
                    containerColor = appColors.cardBackground
                )
            }
}

/**
 * 设置分组
 */
@Composable
fun SettingsSection(
    title: String,
    appColors: com.example.trip_planner.ui.theme.AppColors = com.example.trip_planner.ui.theme.LocalAppColors.current,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            title,
            fontSize = 12.sp,
            color = appColors.textSecondary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        Column(content = content)
        HorizontalDivider()
    }
}

/**
 * 设置项
 */
@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    appColors: com.example.trip_planner.ui.theme.AppColors = com.example.trip_planner.ui.theme.LocalAppColors.current
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) {
                        Modifier.clickable(onClick = onClick)
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = appColors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Normal)
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(subtitle, fontSize = 13.sp, color = appColors.textSecondary)
                }
            }
            if (onClick != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = appColors.textSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        HorizontalDivider(color = appColors.textSecondary.copy(alpha = 0.08f))
    }
}
