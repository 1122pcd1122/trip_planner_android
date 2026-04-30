package com.example.trip_planner.ui.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trip_planner.network.model.DayPlan
import com.example.trip_planner.network.model.PlanHotel
import com.example.trip_planner.network.model.WeatherResponse

/**
 * 统一顶部导航栏
 * 
 * 用于各个 Agent 详情页的顶部导航，包含返回按钮和标题
 *
 * @param title 页面标题
 * @param onBackClick 返回按钮点击回调
 */
@Composable
fun UnifiedTopBar(title: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = BrandTeal,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

/**
 * 天气信息展示界面
 * 
 * 展示目标城市的多天天气信息
 * 包括温度、天气状况和出行建议
 * 使用渐变背景卡片设计
 *
 * @param weatherInfo 天气数据列表，为空时显示加载提示
 * @param onBackClick 返回按钮点击回调
 */
@Composable
fun WeatherUI(weatherInfo: List<WeatherResponse>, onBackClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        UnifiedTopBar(title = "天气预报", onBackClick = onBackClick)

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(weatherInfo.size) { index ->
                val weather = weatherInfo[index]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF80D0C7), Color(0xFF13547A))))
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            text = weather.date,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = weather.temperature,
                            color = Color.White,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = weather.weather,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            if (weatherInfo.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "出行建议",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = BrandTeal
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            weatherInfo.forEach { weather ->
                                Text(
                                    text = "${weather.date}: ${weather.tips}",
                                    color = TextPrimary,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
            
            if (weatherInfo.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SoftBackground),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "正在加载天气数据...",
                            color = TextSecondary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * POI 列表展示界面
 * 
 * 通用组件，用于展示酒店或餐厅列表
 * 根据 POI 数据自动判断类型（酒店/餐厅）
 * 显示名称、评分、价格、地址等信息
 *
 * @param pois POI 数据列表
 * @param onBackClick 返回按钮点击回调
 */
@Composable
fun PoiListUI(pois: List<PoiModel>, onBackClick: () -> Unit,agentType: AgentType) {
    val title = when (agentType) {
        AgentType.RESTAURANT -> "美食推荐"
        AgentType.HOTEL -> "酒店推荐"
        else -> "推荐地点"
    }

    Column(modifier = Modifier.fillMaxSize()) {
        UnifiedTopBar(title = title, onBackClick = onBackClick)

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (pois.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SoftBackground),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "正在加载数据...",
                            color = TextSecondary,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                }
            }
            
            items(pois) { poi ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = poi.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = poi.price,
                                fontWeight = FontWeight.Bold,
                                color = BrandTeal
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (poi.rating.isNotBlank()) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "评分",
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = " ${poi.rating} 分",
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                            if (poi.distance.isNotBlank()) {
                                Text(text = poi.distance, color = TextSecondary, fontSize = 14.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = poi.desc,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 汇总展示界面
 * 
 * 展示 ALL 模式下的行程规划数据
 * 包括每日行程、酒店推荐和整体出行建议
 *
 * @param dayPlans 每日行程列表
 * @param planHotels 酒店推荐列表
 * @param overallTips 整体出行建议
 * @param onBackClick 返回按钮点击回调
 */
@Composable
fun SummaryUI(
    dayPlans: List<DayPlan>,
    planHotels: List<PlanHotel>,
    overallTips: String,
    onBackClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        UnifiedTopBar(title = "行程总览", onBackClick = onBackClick)

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 整体出行建议
            if (overallTips.isNotBlank()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9F8)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "💡 出行建议",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = BrandTeal
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = overallTips,
                                fontSize = 14.sp,
                                color = TextPrimary,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            // 每日行程详情
            dayPlans.forEach { dayPlan ->
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // 日期和天气标题
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Day ${dayPlan.dayNum} · ${dayPlan.date}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = BrandTeal
                                )
                                Text(
                                    text = dayPlan.weather,
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // 行程安排
                            dayPlan.itinerary.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = item.time,
                                        fontSize = 12.sp,
                                        color = BrandTeal,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.width(50.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.spot,
                                            fontSize = 14.sp,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = item.address,
                                            fontSize = 12.sp,
                                            color = TextSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                            
                            // 餐饮安排
                            dayPlan.meals?.let { meals ->
                                if (meals.lunch != null || meals.dinner != null) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "🍽️ 餐饮安排",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    meals.lunch?.let { lunch ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "午餐",
                                                fontSize = 12.sp,
                                                color = BrandTeal,
                                                modifier = Modifier.width(40.dp)
                                            )
                                            Text(
                                                text = "${lunch.name} - ${lunch.dish}",
                                                fontSize = 12.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                    meals.dinner?.let { dinner ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "晚餐",
                                                fontSize = 12.sp,
                                                color = BrandTeal,
                                                modifier = Modifier.width(40.dp)
                                            )
                                            Text(
                                                text = "${dinner.name} - ${dinner.dish}",
                                                fontSize = 12.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // 每日出行建议
                            if (dayPlan.tips.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "💡 出行贴士",
                                    fontSize = 12.sp,
                                    color = BrandTeal,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = dayPlan.tips,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            // 酒店推荐
            if (planHotels.isNotEmpty()) {
                item {
                    Text(
                        text = "🏨 酒店推荐",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(planHotels) { hotel ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = hotel.name,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = hotel.price,
                                    fontSize = 12.sp,
                                    color = BrandTeal,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = hotel.address,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            if (hotel.advantage.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "✨ ${hotel.advantage}",
                                    fontSize = 12.sp,
                                    color = Color(0xFFFF9800)
                                )
                            }
                        }
                    }
                }
            }

            // 空数据提示
            if (dayPlans.isEmpty() && planHotels.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SoftBackground),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "点击「汇总」获取完整旅行信息",
                            color = TextSecondary,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                }
            }
        }
    }
}
