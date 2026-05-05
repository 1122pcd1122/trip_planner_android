package com.example.trip_planner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trip_planner.ui.theme.LocalAppColors
import com.example.trip_planner.viewModel.UserViewModel

/**
 * 登录/注册页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    userViewModel: UserViewModel = viewModel()
) {
    val appColors = LocalAppColors.current
    var isLoginMode by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isLoginMode) "登录" else "注册", fontSize = 16.sp) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                if (isLoginMode) "欢迎回来" else "创建账号",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("用户名") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = appColors.brandTeal,
                    unfocusedBorderColor = appColors.divider
                )
            )

            if (!isLoginMode) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("邮箱（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.brandTeal,
                        unfocusedBorderColor = appColors.divider
                    )
                )
            }

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密码") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showPassword) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = appColors.brandTeal,
                    unfocusedBorderColor = appColors.divider
                )
            )

            if (!isLoginMode) {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("确认密码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.brandTeal,
                        unfocusedBorderColor = appColors.divider
                    )
                )
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    errorMessage,
                    color = appColors.error,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Button(
                onClick = {
                    errorMessage = ""
                    if (username.isEmpty() || password.isEmpty()) {
                        errorMessage = "用户名和密码不能为空"
                        return@Button
                    }
                    if (!isLoginMode && password != confirmPassword) {
                        errorMessage = "两次密码输入不一致"
                        return@Button
                    }
                    if (!isLoginMode) {
                        userViewModel.register(username, email, password,
                            onSuccess = {
                                userViewModel.login(username, password,
                                    onSuccess = onLoginSuccess,
                                    onError = { errorMessage = it }
                                )
                            },
                            onError = { errorMessage = it }
                        )
                    } else {
                        userViewModel.login(username, password,
                            onSuccess = onLoginSuccess,
                            onError = { errorMessage = it }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = appColors.brandTeal)
            ) {
                Text(if (isLoginMode) "登录" else "注册", fontSize = 14.sp)
            }

            TextButton(
                onClick = {
                    isLoginMode = !isLoginMode
                    errorMessage = ""
                    password = ""
                    confirmPassword = ""
                }
            ) {
                Text(
                    if (isLoginMode) "没有账号？立即注册" else "已有账号？立即登录",
                    color = appColors.brandTeal,
                    fontSize = 12.sp
                )
            }
        }
    }
}
