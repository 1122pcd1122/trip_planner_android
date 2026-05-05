package com.example.trip_planner.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trip_planner.ui.theme.LocalAppColors
import com.example.trip_planner.viewModel.UserViewModel

/**
 * 个人中心页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    userViewModel: UserViewModel = viewModel(),
    onNavigateToBudget: (Long) -> Unit = {},
    onNavigateToNotes: (Long) -> Unit = {},
    onNavigateToPacking: (Long) -> Unit = {},
    onNavigateToProfileInfo: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onToggleTheme: () -> Unit = {},
    isDarkTheme: Boolean = false
) {
    val appColors = LocalAppColors.current
    val context = LocalContext.current
    val currentUser by userViewModel.currentUser.collectAsState()
    val currentUserId by userViewModel.currentUserId.collectAsState()
    var showPasswordDialog by remember { mutableStateOf(false) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("个人中心", fontSize = 16.sp) },
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(appColors.cardBackground)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(appColors.brandTeal.copy(alpha = 0.2f), shape = CircleShape)
                        .clickable { onNavigateToProfileInfo() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        currentUser?.nickname?.take(1) ?: currentUser?.username?.take(1) ?: "U",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.brandTeal
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    currentUser?.nickname?.takeIf { it.isNotEmpty() } ?: currentUser?.username ?: "用户",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = appColors.textPrimary
                )

                if (currentUser?.email?.isNotEmpty() == true) {
                    Text(
                        currentUser?.email ?: "",
                        fontSize = 12.sp,
                        color = appColors.textSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ProfileMenuItem(
                icon = Icons.Default.Person,
                title = "个人信息",
                subtitle = "查看和编辑个人资料",
                onClick = onNavigateToProfileInfo,
                appColors = appColors
            )

            ProfileMenuItem(
                icon = Icons.Default.Lock,
                title = "修改密码",
                subtitle = "定期修改密码以保护账号安全",
                onClick = { showPasswordDialog = true },
                appColors = appColors
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileMenuItem(
                icon = Icons.Default.Flight,
                title = "我的旅行",
                onClick = { onNavigateToBudget(currentUserId) },
                appColors = appColors
            )

            ProfileMenuItem(
                icon = Icons.AutoMirrored.Filled.Note,
                title = "我的笔记",
                onClick = { onNavigateToNotes(currentUserId) },
                appColors = appColors
            )

            ProfileMenuItem(
                icon = Icons.AutoMirrored.Filled.ListAlt,
                title = "我的清单",
                onClick = { onNavigateToPacking(currentUserId) },
                appColors = appColors
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileMenuItem(
                icon = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                title = if (isDarkTheme) "切换亮色模式" else "切换暗色模式",
                onClick = onToggleTheme,
                appColors = appColors
            )

            ProfileMenuItem(
                icon = Icons.Default.Settings,
                title = "设置",
                onClick = onNavigateToSettings,
                appColors = appColors
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    userViewModel.logout()
                    onLogout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = appColors.error)
            ) {
                Text("退出登录", fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            oldPassword = oldPassword,
            onOldPasswordChange = { oldPassword = it },
            newPassword = newPassword,
            onNewPasswordChange = { newPassword = it },
            confirmPassword = confirmPassword,
            onConfirmPasswordChange = { confirmPassword = it },
            onDismiss = { showPasswordDialog = false },
            onChangePassword = {
                if (newPassword != confirmPassword) {
                    Toast.makeText(context, "两次密码输入不一致", Toast.LENGTH_SHORT).show()
                    return@ChangePasswordDialog
                }
                if (newPassword.length < 6) {
                    Toast.makeText(context, "密码长度不能少于6位", Toast.LENGTH_SHORT).show()
                    return@ChangePasswordDialog
                }
                userViewModel.changePassword(oldPassword, newPassword,
                    onSuccess = {
                        Toast.makeText(context, "密码修改成功", Toast.LENGTH_SHORT).show()
                        oldPassword = ""
                        newPassword = ""
                        confirmPassword = ""
                        showPasswordDialog = false
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            appColors = appColors
        )
    }
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(appColors.cardBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = appColors.brandTeal, modifier = Modifier.size(20.dp))
            Column {
                Text(title, fontSize = 14.sp, color = appColors.textPrimary)
                if (subtitle != null) {
                    Text(subtitle, fontSize = 12.sp, color = appColors.textSecondary, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = appColors.textSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
    HorizontalDivider(color = appColors.divider.copy(alpha = 0.3f))
}

@Composable
fun ChangePasswordDialog(
    oldPassword: String,
    onOldPasswordChange: (String) -> Unit,
    newPassword: String,
    onNewPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onChangePassword: () -> Unit,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("修改密码", fontSize = 16.sp) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = onOldPasswordChange,
                    label = { Text("原密码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.brandTeal,
                        unfocusedBorderColor = appColors.divider
                    )
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = onNewPasswordChange,
                    label = { Text("新密码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.brandTeal,
                        unfocusedBorderColor = appColors.divider
                    )
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    label = { Text("确认新密码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.brandTeal,
                        unfocusedBorderColor = appColors.divider
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onChangePassword) {
                Text("确定")
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

fun formatTimestamp(timestamp: Long): String {
    if (timestamp == 0L) return "未记录"
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        sdf.format(java.util.Date(timestamp))
    } catch (e: Exception) {
        "未知"
    }
}
