package com.example.trip_planner.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.trip_planner.ui.components.BottomNavigationBar
import com.example.trip_planner.ui.components.BottomNavItem
import com.example.trip_planner.ui.content.TravelPlannerScreen

/**
 * 主应用界面
 * 
 * 包含底部导航栏，管理规划页面和收藏页面的切换
 */
@Composable
fun TripPlannerApp() {
    var currentRoute by remember { mutableStateOf(BottomNavItem.PLAN.route) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = { route -> currentRoute = route }
            )
        }
    ) { paddingValues ->
        when (currentRoute) {
            BottomNavItem.PLAN.route -> {
                TravelPlannerScreen(modifier = Modifier.padding(paddingValues))
            }
            BottomNavItem.FAVORITE.route -> {
                com.example.trip_planner.ui.screens.FavoriteScreen(modifier = Modifier.padding(paddingValues))
            }
        }
    }
}
