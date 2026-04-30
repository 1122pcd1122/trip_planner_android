package com.example.trip_planner

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import com.example.trip_planner.ui.TripPlannerApp

/**
 * 主活动入口
 * 
 * 应用启动时首先执行的活动，负责设置 Compose 内容视图
 */
class MainActivity : ComponentActivity() {
    
    private val TAG = "MainActivity"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "📱 应用启动 onCreate")
        super.onCreate(savedInstanceState)

        setContent {
            Log.i(TAG, "📱 开始设置 Compose 内容")
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TripPlannerApp()
                }
            }
        }
    }
}
