package com.example.trip_planner.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.size
import com.example.trip_planner.ui.theme.LocalAppColors

/**
 * 底部导航项
 */
enum class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    PLAN("规划", Icons.Default.Home, "plan"),
    FAVORITE("收藏", Icons.Default.Favorite, "favorite"),
    HISTORY("历史", Icons.Default.Refresh, "history"),
    PROFILE("我的", Icons.Default.Person, "profile")
}

/**
 * 底部导航栏（极简现代风）
 */
@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val appColors = LocalAppColors.current
    NavigationBar(
        containerColor = appColors.cardBackground,
        tonalElevation = 0.dp
    ) {
        BottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { 
                    Text(
                        item.title,
                        fontSize = 11.sp
                    ) 
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = appColors.brandTeal,
                    selectedTextColor = appColors.brandTeal,
                    unselectedIconColor = appColors.textSecondary,
                    unselectedTextColor = appColors.textSecondary,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
