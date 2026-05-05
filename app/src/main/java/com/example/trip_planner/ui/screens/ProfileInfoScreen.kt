package com.example.trip_planner.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trip_planner.ui.theme.LocalAppColors
import com.example.trip_planner.viewModel.UserViewModel

/**
 * 个人信息页面
 * 用于预览和修改个人信息
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileInfoScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    userViewModel: UserViewModel = viewModel()
) {
    val appColors = LocalAppColors.current
    val context = LocalContext.current
    val currentUser by userViewModel.currentUser.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var editNickname by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editBio by remember { mutableStateOf("") }
    var editGender by remember { mutableStateOf(0) }
    var editBirthday by remember { mutableStateOf("") }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            editNickname = it.nickname
            editPhone = it.phone
            editBio = it.bio
            editGender = it.gender
            editBirthday = it.birthday
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("个人信息", fontSize = 16.sp) },
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
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(appColors.brandTeal.copy(alpha = 0.2f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        currentUser?.nickname?.take(1) ?: currentUser?.username?.take(1) ?: "U",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.brandTeal
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    currentUser?.nickname?.takeIf { it.isNotEmpty() } ?: currentUser?.username ?: "用户",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = appColors.textPrimary
                )

                if (currentUser?.email?.isNotEmpty() == true) {
                    Text(
                        currentUser?.email ?: "",
                        fontSize = 13.sp,
                        color = appColors.textSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showEditDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = appColors.brandTeal)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("编辑资料", fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            ProfileInfoSection(title = "基本信息", appColors = appColors) {
                ProfileInfoRow(
                    icon = Icons.Default.Person,
                    label = "用户名",
                    value = currentUser?.username ?: ""
                )
                ProfileInfoRow(
                    icon = Icons.Default.Email,
                    label = "邮箱",
                    value = currentUser?.email?.takeIf { it.isNotEmpty() } ?: "未绑定"
                )
                ProfileInfoRow(
                    icon = Icons.Default.Phone,
                    label = "手机号",
                    value = currentUser?.phone?.takeIf { it.isNotEmpty() } ?: "未绑定"
                )
            }

            ProfileInfoSection(title = "个人资料", appColors = appColors) {
                ProfileInfoRow(
                    icon = Icons.Default.Cake,
                    label = "生日",
                    value = currentUser?.birthday?.takeIf { it.isNotEmpty() } ?: "未设置"
                )
                ProfileInfoRow(
                    icon = Icons.Default.Wc,
                    label = "性别",
                    value = when (currentUser?.gender) {
                        1 -> "男"
                        2 -> "女"
                        else -> "未设置"
                    }
                )
                ProfileInfoRow(
                    icon = Icons.Default.Info,
                    label = "个人简介",
                    value = currentUser?.bio?.takeIf { it.isNotEmpty() } ?: "未填写"
                )
            }

            ProfileInfoSection(title = "账号信息", appColors = appColors) {
                ProfileInfoRow(
                    icon = Icons.Default.Schedule,
                    label = "注册时间",
                    value = formatTimestamp(currentUser?.createdAt ?: 0)
                )
                ProfileInfoRow(
                    icon = Icons.AutoMirrored.Filled.Login,
                    label = "最后登录",
                    value = formatTimestamp(currentUser?.lastLoginAt ?: 0)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            nickname = editNickname,
            onNicknameChange = { editNickname = it },
            phone = editPhone,
            onPhoneChange = { editPhone = it },
            bio = editBio,
            onBioChange = { editBio = it },
            gender = editGender,
            onGenderChange = { editGender = it },
            birthday = editBirthday,
            onBirthdayChange = { editBirthday = it },
            onDismiss = { showEditDialog = false },
            onSave = {
                userViewModel.updateProfile(
                    nickname = editNickname,
                    phone = editPhone,
                    bio = editBio,
                    gender = editGender,
                    birthday = editBirthday
                )
                Toast.makeText(context, "资料已更新", Toast.LENGTH_SHORT).show()
                showEditDialog = false
            },
            appColors = appColors
        )
    }
}

@Composable
fun ProfileInfoSection(
    title: String,
    appColors: com.example.trip_planner.ui.theme.AppColors,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(appColors.cardBackground)
    ) {
        Text(
            title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = appColors.textPrimary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
        )
        content()
    }
}

@Composable
fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    val appColors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(icon, contentDescription = null, tint = appColors.brandTeal, modifier = Modifier.size(18.dp))
            Text(label, fontSize = 13.sp, color = appColors.textSecondary)
        }
        Text(
            value,
            fontSize = 13.sp,
            color = appColors.textPrimary,
            modifier = Modifier.weight(2f),
            maxLines = 2
        )
    }
    HorizontalDivider(color = appColors.divider.copy(alpha = 0.3f))
}

@Composable
fun EditProfileDialog(
    nickname: String,
    onNicknameChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    bio: String,
    onBioChange: (String) -> Unit,
    gender: Int,
    onGenderChange: (Int) -> Unit,
    birthday: String,
    onBirthdayChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    appColors: com.example.trip_planner.ui.theme.AppColors
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑资料", fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = onNicknameChange,
                    label = { Text("昵称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.brandTeal,
                        unfocusedBorderColor = appColors.divider
                    )
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = { Text("手机号") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.brandTeal,
                        unfocusedBorderColor = appColors.divider
                    )
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = onBioChange,
                    label = { Text("个人简介") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.brandTeal,
                        unfocusedBorderColor = appColors.divider
                    )
                )

                OutlinedTextField(
                    value = birthday,
                    onValueChange = onBirthdayChange,
                    label = { Text("生日 (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.brandTeal,
                        unfocusedBorderColor = appColors.divider
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("性别:", modifier = Modifier.align(Alignment.CenterVertically))
                    FilterChip(
                        selected = gender == 0,
                        onClick = { onGenderChange(0) },
                        label = { Text("保密") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = appColors.brandTeal
                        )
                    )
                    FilterChip(
                        selected = gender == 1,
                        onClick = { onGenderChange(1) },
                        label = { Text("男") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = appColors.brandTeal
                        )
                    )
                    FilterChip(
                        selected = gender == 2,
                        onClick = { onGenderChange(2) },
                        label = { Text("女") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = appColors.brandTeal
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
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
