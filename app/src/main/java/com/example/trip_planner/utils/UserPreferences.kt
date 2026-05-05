package com.example.trip_planner.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * 用户偏好设置管理
 * 用于保存用户登录状态等信息
 */
object UserPreferences {

    private const val PREF_NAME = "user_preferences"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"
    private const val KEY_NICKNAME = "nickname"
    private const val KEY_EMAIL = "email"
    private const val KEY_TOKEN = "token"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveLoginState(context: Context, userId: Long, username: String = "", nickname: String = "", email: String = "", token: String = "") {
        getPreferences(context).edit().apply {
            putLong(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_NICKNAME, nickname)
            putString(KEY_EMAIL, email)
            putString(KEY_TOKEN, token)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun clearLoginState(context: Context) {
        getPreferences(context).edit().apply {
            remove(KEY_USER_ID)
            remove(KEY_USERNAME)
            remove(KEY_NICKNAME)
            remove(KEY_EMAIL)
            remove(KEY_TOKEN)
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
    }

    fun getLoggedInUserId(context: Context): Long {
        return getPreferences(context).getLong(KEY_USER_ID, 0L)
    }

    fun getUsername(context: Context): String {
        return getPreferences(context).getString(KEY_USERNAME, "") ?: ""
    }

    fun getNickname(context: Context): String {
        return getPreferences(context).getString(KEY_NICKNAME, "") ?: ""
    }

    fun getEmail(context: Context): String {
        return getPreferences(context).getString(KEY_EMAIL, "") ?: ""
    }

    fun getToken(context: Context): String {
        return getPreferences(context).getString(KEY_TOKEN, "") ?: ""
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }
}
