    package com.example.trip_planner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.trip_planner.utils.ThemeManager

/**
 * 品牌色（降低饱和度，更柔和）
 */
val BrandTeal = Color(0xFF4A9E8E)

/**
 * 应用扩展颜色（极简现代风）
 */
data class AppColors(
    val softBackground: Color,
    val cardBackground: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val brandTeal: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val weatherGradientStart: Color,
    val weatherGradientEnd: Color,
    val divider: Color
)

/** 浅色模式扩展颜色（温暖柔和，适合旅行应用） */
val LightAppColors = AppColors(
    softBackground = Color(0xFFF5F5F7),
    cardBackground = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF1D1D1F),
    textSecondary = Color(0xFF86868B),
    brandTeal = Color(0xFF4A9E8E),
    success = Color(0xFF34C759),
    warning = Color(0xFFFF9500),
    error = Color(0xFFFF3B30),
    weatherGradientStart = Color(0xFF5AC8FA),
    weatherGradientEnd = Color(0xFF4A9E8E),
    divider = Color(0xFFE5E5EA)
)

/** 深色模式扩展颜色（柔和深灰蓝，适合旅行应用） */
val DarkAppColors = AppColors(
    softBackground = Color(0xFF121212),
    cardBackground = Color(0xFF1E1E1E),
    textPrimary = Color(0xFFE8E8E8),
    textSecondary = Color(0xFF9E9E9E),
    brandTeal = Color(0xFF5AC8B8),
    success = Color(0xFF30D158),
    warning = Color(0xFFFFD60A),
    error = Color(0xFFFF453A),
    weatherGradientStart = Color(0xFF3A8FB7),
    weatherGradientEnd = Color(0xFF5AC8B8),
    divider = Color(0xFF2C2C2E)
)

/** 本地 Composition 用于提供应用扩展颜色 */
val LocalAppColors = staticCompositionLocalOf { LightAppColors }

/**
 * 浅色模式配色（温暖柔和，适合旅行应用）
 */
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4A9E8E),
    secondary = Color(0xFF5C7A75),
    tertiary = Color(0xFF86868B),
    background = Color(0xFFF5F5F7),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color(0xFF1D1D1F),
    onBackground = Color(0xFF1D1D1F),
    onSurface = Color(0xFF1D1D1F),
    error = Color(0xFFFF3B30)
)

/**
 * 深色模式配色（柔和深灰，适合旅行应用）
 */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF5AC8B8),
    secondary = Color(0xFF7A9490),
    tertiary = Color(0xFF9E9E9E),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color(0xFF000000),
    onSecondary = Color.White,
    onTertiary = Color(0xFFE8E8E8),
    onBackground = Color(0xFFE8E8E8),
    onSurface = Color(0xFFE8E8E8),
    error = Color(0xFFFF453A)
)

/**
 * 应用主题
 * 
 * 根据用户偏好自动切换明暗主题
 */
@Composable
fun TripPlannerTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isDarkMode by remember { mutableStateOf(ThemeManager.isDarkMode(context)) }
    
    val colorScheme = if (isDarkMode) DarkColorScheme else LightColorScheme
    val appColors = if (isDarkMode) DarkAppColors else LightAppColors
    
    androidx.compose.runtime.CompositionLocalProvider(
        LocalAppColors provides appColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

/**
 * 获取当前应用扩展颜色
 */
val MaterialTheme.appColors: AppColors
    @Composable
    @Stable
    get() = LocalAppColors.current
