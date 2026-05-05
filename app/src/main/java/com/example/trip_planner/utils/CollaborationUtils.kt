package com.example.trip_planner.utils

import android.content.Context
import android.content.Intent
import com.example.trip_planner.data.local.entity.TripPlanEntity
import com.google.gson.Gson
import java.util.*

/**
 * 协作工具类
 * 支持行程共享和协同编辑
 */
object CollaborationUtils {

    /**
     * 生成分享码
     * 将行程数据编码为可分享的字符串
     */
    fun generateShareCode(tripPlan: TripPlanEntity): String {
        val shareData = mapOf(
            "destination" to tripPlan.destination,
            "days" to tripPlan.days,
            "preferences" to tripPlan.preferences,
            "hotelJson" to tripPlan.hotelJson,
            "dayPlansJson" to tripPlan.dayPlansJson,
            "overallTips" to tripPlan.overallTips,
            "version" to 1
        )
        val json = Gson().toJson(shareData)
        return Base64.getEncoder().encodeToString(json.toByteArray())
    }

    /**
     * 解析分享码
     * 从分享码中恢复行程数据
     */
    fun parseShareCode(shareCode: String): Map<String, Any>? {
        return try {
            val json = String(Base64.getDecoder().decode(shareCode))
            val type = object : com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 分享行程
     * 通过系统分享功能发送分享码
     */
    fun shareTrip(context: Context, tripPlan: TripPlanEntity) {
        val shareCode = generateShareCode(tripPlan)
        val shareText = """
            🗺️ 旅行行程分享
            
            目的地：${tripPlan.destination}
            天数：${tripPlan.days}天
            
            分享码：
            $shareCode
            
            在 Trip Planner 应用中导入此行程
        """.trimIndent()

        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(intent, "分享行程"))
    }

    /**
     * 导入行程
     * 从分享码创建新行程
     */
    fun importTripFromCode(shareCode: String): TripPlanEntity? {
        val data = parseShareCode(shareCode) ?: return null

        return TripPlanEntity(
            destination = data["destination"] as? String ?: "",
            days = (data["days"] as? Double)?.toInt() ?: 1,
            preferences = data["preferences"] as? String ?: "",
            hotelJson = data["hotelJson"] as? String ?: "",
            dayPlansJson = data["dayPlansJson"] as? String ?: "",
            overallTips = data["overallTips"] as? String ?: "",
            timestamp = System.currentTimeMillis()
        )
    }
}
