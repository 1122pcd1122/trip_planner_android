package com.example.trip_planner.utils

/**
 * 统一错误信息处理工具
 * 将技术性错误转换为用户友好的提示
 */
object ErrorUtils {

    /**
     * 将错误信息转换为用户友好的提示
     */
    fun getFriendlyErrorMessage(error: String?): String {
        if (error.isNullOrBlank()) {
            return "未知错误，请稍后再试"
        }

        return when {
            error.contains("网络请求失败") || error.contains("UnknownHostException") || error.contains("SocketTimeoutException") -> {
                "网络连接失败，请检查网络设置后重试"
            }
            error.contains("数据解析失败") -> {
                "数据加载异常，请稍后再试"
            }
            error.contains("401") || error.contains("unauthorized") -> {
                "服务验证失败，请稍后再试"
            }
            error.contains("404") || error.contains("not found") -> {
                "服务暂时不可用，请稍后再试"
            }
            error.contains("500") -> {
                "服务器繁忙，请稍后再试"
            }
            else -> error
        }
    }

    /**
     * 获取加载失败的提示
     */
    fun getLoadFailedMessage(type: String): String {
        return when (type) {
            "weather" -> "天气数据加载失败"
            "hotel" -> "酒店推荐加载失败"
            "attraction" -> "景点推荐加载失败"
            "restaurant" -> "餐厅推荐加载失败"
            "all" -> "行程规划加载失败"
            else -> "数据加载失败"
        }
    }
}
