package com.example.trip_planner.ui

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trip_planner.ui.components.BottomNavigationBar
import com.example.trip_planner.ui.components.BottomNavItem
import com.example.trip_planner.ui.screens.AuthScreen
import com.example.trip_planner.ui.screens.BudgetScreen
import com.example.trip_planner.ui.screens.DetailScreen
import com.example.trip_planner.ui.screens.DetailType
import com.example.trip_planner.ui.screens.FavoriteScreen
import com.example.trip_planner.ui.screens.HistoryDetailScreen
import com.example.trip_planner.ui.screens.HistoryScreen
import com.example.trip_planner.ui.screens.PackingListScreen
import com.example.trip_planner.ui.screens.PrivacyPolicyScreen
import com.example.trip_planner.ui.screens.ProfileScreen
import com.example.trip_planner.ui.screens.ProfileInfoScreen
import com.example.trip_planner.ui.screens.SettingsScreen
import com.example.trip_planner.ui.screens.TravelPlannerScreen
import com.example.trip_planner.ui.screens.TripNotesScreen
import com.example.trip_planner.ui.screens.DataBackupScreen
import com.example.trip_planner.data.local.entity.TripPlanEntity
import com.example.trip_planner.utils.ThemeManager
import com.example.trip_planner.viewModel.UserViewModel

/**
 * 主应用界面
 *
 * 包含底部导航栏，管理规划页面、收藏页面、历史记录和个人中心的切换
 * 同时处理详情页、隐私政策页和历史记录详情页的导航
 */
@Composable
fun TripPlannerApp() {
    val context = LocalContext.current
    val isInspectionMode = LocalInspectionMode.current
    var isDarkTheme by remember { mutableStateOf(!isInspectionMode && ThemeManager.isDarkMode(context)) }
    var currentRoute by remember { mutableStateOf(BottomNavItem.PLAN.route) }
    var currentDetailType by remember { mutableStateOf<DetailType?>(null) }
    var historyPlanToLoad by remember { mutableStateOf<Long?>(null) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var historyDetailPlan by remember { mutableStateOf<TripPlanEntity?>(null) }
    var showBudgetScreen by remember { mutableStateOf(false) }
    var showNotesScreen by remember { mutableStateOf(false) }
    var showPackingScreen by remember { mutableStateOf(false) }
    var showAuthScreen by remember { mutableStateOf(false) }
    var showSettingsScreen by remember { mutableStateOf(false) }
    var showDataBackupScreen by remember { mutableStateOf(false) }
    var showProfileInfoScreen by remember { mutableStateOf(false) }

    val userViewModel: UserViewModel = viewModel()
    val currentUserId by userViewModel.currentUserId.collectAsState()

    fun toggleTheme() {
        if (!isInspectionMode) {
            val activity = context as? Activity
            activity?.let {
                ThemeManager.toggleDarkMode(it)
                isDarkTheme = !isDarkTheme
            }
        } else {
            isDarkTheme = !isDarkTheme
        }
    }

    if (showAuthScreen) {
        AuthScreen(
            onLoginSuccess = { showAuthScreen = false }
        )
    } else if (historyDetailPlan != null) {
        HistoryDetailScreen(
            plan = historyDetailPlan!!,
            onBack = { historyDetailPlan = null }
        )
    } else if (showBudgetScreen) {
        BudgetScreen(
            tripId = "",
            userId = currentUserId,
            onBack = { showBudgetScreen = false }
        )
    } else if (showNotesScreen) {
        TripNotesScreen(
            tripId = "",
            userId = currentUserId,
            onBack = { showNotesScreen = false }
        )
    } else if (showPackingScreen) {
        PackingListScreen(
            tripId = "",
            userId = currentUserId,
            onBack = { showPackingScreen = false }
        )
    } else if (showProfileInfoScreen) {
        ProfileInfoScreen(
            onBack = { showProfileInfoScreen = false }
        )
    } else if (showPrivacyPolicy) {
        PrivacyPolicyScreen(onBack = { showPrivacyPolicy = false })
    } else if (showDataBackupScreen) {
        DataBackupScreen(
            onBack = {
                showDataBackupScreen = false
            }
        )
    } else if (showSettingsScreen) {
        SettingsScreen(
            onBack = { showSettingsScreen = false },
            onPrivacyPolicyClick = { showPrivacyPolicy = true },
            onDataBackupClick = { showDataBackupScreen = true }
        )
    } else if (currentDetailType != null) {
        DetailScreen(
            detailType = currentDetailType!!,
            onBack = { currentDetailType = null }
        )
    } else {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route -> currentRoute = route }
                )
            }
        ) { paddingValues ->
            AnimatedContent(
                targetState = currentRoute,
                transitionSpec = {
                    slideInHorizontally(animationSpec = tween(300)) { it } togetherWith
                    slideOutHorizontally(animationSpec = tween(300)) { -it }
                },
                label = "PageTransition",
                modifier = Modifier.padding(paddingValues)
            ) { route ->
                when (route) {
                    BottomNavItem.PLAN.route -> {
                        TravelPlannerScreen(
                            onNavigateToDetail = { detailType -> currentDetailType = detailType },
                            historyPlanId = historyPlanToLoad,
                            onHistoryPlanLoaded = { historyPlanToLoad = null }
                        )
                    }
                    BottomNavItem.FAVORITE.route -> {
                        FavoriteScreen(
                            onNavigateToDetail = { detailType -> currentDetailType = detailType }
                        )
                    }
                    BottomNavItem.HISTORY.route -> {
                        HistoryScreen(
                            onPlanClick = { plan ->
                                historyDetailPlan = plan
                            }
                        )
                    }
                    BottomNavItem.PROFILE.route -> {
                        ProfileScreen(
                            onBack = { showAuthScreen = true },
                            onLogout = { showAuthScreen = true },
                            onNavigateToBudget = { showBudgetScreen = true },
                            onNavigateToNotes = { showNotesScreen = true },
                            onNavigateToPacking = { showPackingScreen = true },
                            onNavigateToProfileInfo = { showProfileInfoScreen = true },
                            onNavigateToSettings = { showSettingsScreen = true },
                            onToggleTheme = { toggleTheme() },
                            isDarkTheme = isDarkTheme
                        )
                    }
                }
            }
        }
    }
}
