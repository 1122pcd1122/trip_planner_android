package com.example.trip_planner.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import com.example.trip_planner.data.local.entity.TripPlanEntity
import com.example.trip_planner.network.model.DayPlan
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

/**
 * 日历导出工具类
 * 将行程数据导出为日历事件
 */
object CalendarExportUtils {

    /**
     * 将行程添加到日历
     */
    fun addTripToCalendar(context: Context, plan: TripPlanEntity) {
        try {
            val gson = Gson()
            val dayPlans = if (plan.dayPlansJson.isNotBlank()) {
                gson.fromJson(plan.dayPlansJson, Array<DayPlan>::class.java).toList()
            } else {
                emptyList()
            }

            if (dayPlans.isEmpty()) {
                Toast.makeText(context, "暂无行程数据可导出", Toast.LENGTH_SHORT).show()
                return
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()

            dayPlans.forEach { day ->
                try {
                    val startDate = dateFormat.parse(day.date)
                    if (startDate != null) {
                        calendar.time = startDate
                        
                        calendar.set(Calendar.HOUR_OF_DAY, 9)
                        calendar.set(Calendar.MINUTE, 0)
                        val startMillis = calendar.timeInMillis

                        calendar.set(Calendar.HOUR_OF_DAY, 18)
                        calendar.set(Calendar.MINUTE, 0)
                        val endMillis = calendar.timeInMillis

                        val itinerarySummary = day.itinerary.take(3).joinToString(", ") { it.spot }
                        val description = buildDescription(day)

                        addCalendarEvent(
                            context = context,
                            title = "📍 ${plan.destination} - 第${day.dayNum}天",
                            description = description,
                            startTimeMillis = startMillis,
                            endTimeMillis = endMillis
                        )
                    }
                } catch (e: Exception) {
                    return@forEach
                }
            }

            Toast.makeText(context, "已添加 ${dayPlans.size} 天行程到日历", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "导出到日历失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 构建行程描述
     */
    private fun buildDescription(day: DayPlan): String {
        val builder = StringBuilder()
        builder.appendLine("天气：${day.weather}")
        builder.appendLine()
        
        builder.appendLine("行程安排：")
        day.itinerary.forEach { item ->
            builder.appendLine("• ${item.time} ${item.spot}")
        }
        builder.appendLine()

        day.meals?.let { meals ->
            meals.lunch?.let { lunch ->
                builder.appendLine("午餐：${lunch.name}")
            }
            meals.dinner?.let { dinner ->
                builder.appendLine("晚餐：${dinner.name}")
            }
            builder.appendLine()
        }

        if (day.tips.isNotBlank()) {
            builder.appendLine("提示：${day.tips}")
        }

        return builder.toString()
    }

    /**
     * 添加日历事件
     */
    private fun addCalendarEvent(
        context: Context,
        title: String,
        description: String,
        startTimeMillis: Long,
        endTimeMillis: Long
    ) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.Events.DESCRIPTION, description)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTimeMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTimeMillis)
            putExtra(CalendarContract.Events.ALL_DAY, false)
            putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "未找到日历应用", Toast.LENGTH_SHORT).show()
        }
    }
}
