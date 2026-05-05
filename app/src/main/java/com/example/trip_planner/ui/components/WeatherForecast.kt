package com.example.trip_planner.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trip_planner.data.local.entity.WeatherCacheEntity
import com.example.trip_planner.ui.theme.AppColors
import com.example.trip_planner.utils.WeatherUtils

/**
 * 天气展示组件（极简现代风）
 * 显示行程期间的天气预报
 */
@Composable
fun WeatherForecast(
    weatherList: List<WeatherCacheEntity>,
    isLoading: Boolean = false,
    error: String? = null,
    onRetry: () -> Unit = {},
    appColors: AppColors,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Text(
            "天气预报",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = appColors.textSecondary,
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(appColors.cardBackground),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = appColors.brandTeal
                    )
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(appColors.cardBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            error,
                            fontSize = 12.sp,
                            color = appColors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onRetry) {
                            Text("重试", color = appColors.brandTeal, fontSize = 12.sp)
                        }
                    }
                }
            }
            weatherList.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(appColors.cardBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "暂无天气数据",
                        fontSize = 12.sp,
                        color = appColors.textSecondary
                    )
                }
            }
            else -> {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(weatherList) { weather ->
                        WeatherDayCard(
                            weather = weather,
                            appColors = appColors
                        )
                    }
                }
            }
        }
    }
}

/**
 * 单日天气卡片（极简现代风）
 */
@Composable
fun WeatherDayCard(
    weather: WeatherCacheEntity,
    appColors: AppColors
) {
    @Suppress("UNUSED_VARIABLE") val tempGradient = getTempGradient(weather.temperature)
    val dateDisplay = WeatherUtils.formatDate(weather.date)

    Column(
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(appColors.cardBackground)
            .padding(vertical = 14.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            dateDisplay,
            fontSize = 10.sp,
            color = appColors.textSecondary,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )

        Text(
            WeatherUtils.getWeatherIcon(weather.weather),
            fontSize = 28.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            weather.temperature,
            fontSize = 14.sp,
            color = appColors.textPrimary,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            weather.weather,
            fontSize = 10.sp,
            color = appColors.textSecondary,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        if (weather.tips.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                weather.tips,
                fontSize = 9.sp,
                color = appColors.brandTeal,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 根据温度返回渐变色
 */
private fun getTempGradient(temperature: String): Brush {
    val temp = temperature.replace(Regex("[^0-9-]"), "").toIntOrNull() ?: 20
    return when {
        temp < 0 -> Brush.verticalGradient(listOf(Color(0xFF4A90D9), Color(0xFF74B9FF)))
        temp < 10 -> Brush.verticalGradient(listOf(Color(0xFF74B9FF), Color(0xFF81ECEC)))
        temp < 20 -> Brush.verticalGradient(listOf(Color(0xFF81ECEC), Color(0xFFFFEAA7)))
        temp < 30 -> Brush.verticalGradient(listOf(Color(0xFFFFEAA7), Color(0xFFFDCB6E)))
        else -> Brush.verticalGradient(listOf(Color(0xFFFDCB6E), Color(0xFFE17055)))
    }
}
