package com.example.trip_planner.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

/**
 * 主题管理器
 * 
 * 功能：
 * 1. 管理深色模式切换
 * 2. 持久化用户主题偏好
 */
object ThemeManager {

    /** 主题偏好 Key */
    private const val PREF_DARK_MODE = "dark_mode"
    /** SharedPreferences 文件名 */
    private const val PREF_NAME = "theme_prefs"

    /**
     * 获取当前是否为深色模式
     */
    fun isDarkMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(PREF_DARK_MODE, false)
    }

    /**
     * 切换深色模式并重建 Activity
     */
    fun toggleDarkMode(activity: Activity) {
        val isDark = isDarkMode(activity)
        val prefs = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(PREF_DARK_MODE, !isDark) }
        AppCompatDelegate.setDefaultNightMode(
            if (!isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        activity.recreate()
    }

    /**
     * 初始化主题（应用启动时调用）
     */
    fun initTheme(context: Context) {
        val isDark = isDarkMode(context)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
