package com.example.trip_planner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trip_planner.ui.theme.AppColors
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

/**
 * 旅行倒计时组件（极简现代风）
 */
@Composable
fun TripCountdown(
    startDate: String,
    appColors: AppColors,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
    }

    val targetTime = parseDateToMillis(startDate)
    val diff = targetTime - currentTime

    val infiniteTransition = rememberInfiniteTransition(label = "countdown")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(appColors.cardBackground)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "距离出发",
            fontSize = 11.sp,
            color = appColors.textSecondary,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (diff > 0) {
            val days = TimeUnit.MILLISECONDS.toDays(diff)
            val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CountdownUnit("$days", "天", appColors, scale)
                CountdownUnit("$hours", "时", appColors, scale)
                CountdownUnit("$minutes", "分", appColors, scale)
                CountdownUnit("$seconds", "秒", appColors, scale)
            }
        } else {
            Text(
                "🎉 旅途已开始",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = appColors.brandTeal
            )
        }
    }
}

@Composable
fun CountdownUnit(
    value: String,
    label: String,
    appColors: AppColors,
    scale: Float
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(scale)
    ) {
        Text(
            value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.brandTeal
        )
        Text(
            label,
            fontSize = 9.sp,
            color = appColors.textSecondary,
            letterSpacing = 0.5.sp
        )
    }
}

private fun parseDateToMillis(dateStr: String): Long {
    return try {
        val parts = dateStr.split("-")
        if (parts.size == 3) {
            val year = parts[0].toInt()
            val month = parts[1].toInt() - 1
            val day = parts[2].toInt()
            val calendar = java.util.Calendar.getInstance()
            calendar.set(year, month, day, 0, 0, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        } else {
            System.currentTimeMillis()
        }
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}
