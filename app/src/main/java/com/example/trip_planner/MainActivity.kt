package com.example.trip_planner

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.trip_planner.ui.TripPlannerApp
import com.example.trip_planner.ui.screens.OnboardingScreen
import com.example.trip_planner.ui.theme.TripPlannerTheme

/**
 * 主活动入口
 * 
 * 应用启动时首先执行的活动，负责设置 Compose 内容视图
 */
class MainActivity : ComponentActivity() {
    
    private val TAG = "MainActivity"
    
    companion object {
        private const val PREFS_NAME = "app_prefs"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        
        fun isOnboardingCompleted(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        }
        
        fun setOnboardingCompleted(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "📱 应用启动 onCreate")
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            Log.i(TAG, "📱 开始设置 Compose 内容")
            TripPlannerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainContent()
                }
            }
        }
    }
}

@Composable
fun MainContent() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showOnboarding by remember { mutableStateOf(!MainActivity.isOnboardingCompleted(context)) }
    
    if (showOnboarding) {
        OnboardingScreen(
            onFinish = {
                MainActivity.setOnboardingCompleted(context)
                showOnboarding = false
            }
        )
    } else {
        TripPlannerApp()
    }
}
