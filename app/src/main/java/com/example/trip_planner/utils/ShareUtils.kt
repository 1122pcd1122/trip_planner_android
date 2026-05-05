package com.example.trip_planner.utils

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.trip_planner.data.local.entity.TripPlanEntity
import com.example.trip_planner.network.model.DayPlan
import com.example.trip_planner.network.model.PlanHotel
import com.google.gson.Gson

/**
 * 行程分享工具类
 * 将行程数据格式化为可分享的文本内容
 */
object ShareUtils {

    /**
     * 将行程数据格式化为分享文本（简洁版）
     */
    fun formatTripPlanForShare(plan: TripPlanEntity): String {
        val builder = StringBuilder()
        builder.appendLine("✈️ 我的旅行计划")
        builder.appendLine("━━━━━━━━━━━━━━━━")
        builder.appendLine()
        builder.appendLine("📍 目的地：${plan.destination}")
        builder.appendLine("📅 天数：${plan.days}天")
        if (plan.preferences.isNotBlank()) {
            builder.appendLine("💡 偏好：${plan.preferences}")
        }
        builder.appendLine()

        if (plan.overallTips.isNotBlank()) {
            builder.appendLine("📝 出行建议")
            builder.appendLine(plan.overallTips)
            builder.appendLine()
        }

        builder.appendLine("━━━━━━━━━━━━━━━━")
        builder.appendLine("由「旅行规划助手」生成")

        return builder.toString()
    }

    /**
     * 将行程数据格式化为详细分享文本（包含每日行程和酒店）
     */
    fun formatDetailedTripPlanForShare(plan: TripPlanEntity): String {
        val gson = Gson()
        val builder = StringBuilder()
        
        builder.appendLine("✈️ 我的旅行计划")
        builder.appendLine("━━━━━━━━━━━━━━━━")
        builder.appendLine()
        builder.appendLine("📍 目的地：${plan.destination}")
        builder.appendLine("📅 天数：${plan.days}天")
        if (plan.preferences.isNotBlank()) {
            builder.appendLine("💡 偏好：${plan.preferences}")
        }
        builder.appendLine()

        try {
            val dayPlans = if (plan.dayPlansJson.isNotBlank()) {
                gson.fromJson(plan.dayPlansJson, Array<DayPlan>::class.java).toList()
            } else {
                emptyList()
            }

            val planHotels = if (plan.hotelJson.isNotBlank()) {
                gson.fromJson(plan.hotelJson, Array<PlanHotel>::class.java).toList()
            } else {
                emptyList()
            }

            if (planHotels.isNotEmpty()) {
                builder.appendLine("🏨 推荐酒店")
                planHotels.forEach { hotel ->
                    builder.appendLine("• ${hotel.name} - ${hotel.address}")
                }
                builder.appendLine()
            }

            if (dayPlans.isNotEmpty()) {
                dayPlans.forEach { day ->
                    builder.appendLine("📅 第${day.dayNum}天 - ${day.date}")
                    builder.appendLine("天气：${day.weather}")
                    builder.appendLine()
                    
                    day.itinerary.forEach { item ->
                        builder.appendLine("  ${item.time} ${item.spot}")
                    }
                    builder.appendLine()

                    day.meals?.let { meals ->
                        meals.lunch?.let { lunch ->
                            builder.appendLine("  🍽️ 午餐：${lunch.name}")
                        }
                        meals.dinner?.let { dinner ->
                            builder.appendLine("  🍽️ 晚餐：${dinner.name}")
                        }
                        builder.appendLine()
                    }

                    if (day.tips.isNotBlank()) {
                        builder.appendLine("  💡 ${day.tips}")
                        builder.appendLine()
                    }

                    builder.appendLine("────────────────")
                    builder.appendLine()
                }
            }
        } catch (e: Exception) {
            builder.appendLine("（详细行程解析失败）")
            builder.appendLine()
        }

        if (plan.overallTips.isNotBlank()) {
            builder.appendLine("📝 出行建议")
            builder.appendLine(plan.overallTips)
            builder.appendLine()
        }

        builder.appendLine("━━━━━━━━━━━━━━━━")
        builder.appendLine("由「旅行规划助手」生成")

        return builder.toString()
    }

    /**
     * 分享行程文本（简洁版）
     */
    fun shareTripPlan(context: Context, plan: TripPlanEntity) {
        val shareText = formatTripPlanForShare(plan)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "${plan.destination}旅行计划")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(shareIntent, "分享行程"))
    }

    /**
     * 分享详细行程文本
     */
    fun shareDetailedTripPlan(context: Context, plan: TripPlanEntity) {
        val shareText = formatDetailedTripPlanForShare(plan)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "${plan.destination}旅行计划（详细版）")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(shareIntent, "分享详细行程"))
    }

    /**
     * 复制行程到剪贴板
     */
    fun copyTripPlan(context: Context, plan: TripPlanEntity) {
        val shareText = formatTripPlanForShare(plan)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = ClipData.newPlainText("旅行计划", shareText)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
    }

    /**
     * 复制详细行程到剪贴板
     */
    fun copyDetailedTripPlan(context: Context, plan: TripPlanEntity) {
        val shareText = formatDetailedTripPlanForShare(plan)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = ClipData.newPlainText("详细旅行计划", shareText)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "已复制详细行程到剪贴板", Toast.LENGTH_SHORT).show()
    }
}
