package com.example.trip_planner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trip_planner.ui.theme.LocalAppColors

/**
 * 隐私政策页面
 * 上架应用市场必须提供
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    val appColors = LocalAppColors.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("隐私政策") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("旅行规划助手隐私政策", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("更新日期：2024年5月1日", color = appColors.textSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("一、我们如何收集和使用您的个人信息", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "为了向您提供旅行规划服务，我们会收集以下信息：\n" +
                "1. 您输入的目的地、旅行天数、偏好等信息\n" +
                "2. 设备信息（用于适配不同设备）\n" +
                "3. 日志信息（用于问题排查和优化服务）",
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("二、我们如何存储您的信息", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "您的行程数据、收藏记录等会存储在您的设备本地数据库中。\n" +
                "我们不会将您的个人数据上传到我们的服务器。",
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("三、第三方服务", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "本应用使用以下第三方服务：\n" +
                "1. 高德地图SDK：用于地图展示和定位服务\n" +
                "2. 网络请求：用于获取天气、酒店、景点、餐厅等数据\n" +
                "第三方服务有其独立的隐私政策，建议您阅读了解。",
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("四、您的权利", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "您有权：\n" +
                "1. 查看、修改、删除您的本地数据\n" +
                "2. 随时卸载本应用\n" +
                "3. 联系我们提出隐私相关疑问",
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("五、联系我们", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "如您对本隐私政策有任何疑问，请通过以下方式联系我们：\n" +
                "邮箱：support@tripplanner.com",
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
