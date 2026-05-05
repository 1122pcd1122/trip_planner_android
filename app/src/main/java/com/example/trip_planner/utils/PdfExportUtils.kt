package com.example.trip_planner.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.trip_planner.data.local.entity.TripPlanEntity
import com.example.trip_planner.network.model.DayPlan
import com.example.trip_planner.network.model.PlanHotel
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream

/**
 * 行程 PDF 导出工具（极简现代风）
 * 
 * 功能：
 * 1. 将行程数据导出为 PDF 文件
 * 2. 支持分享和保存到本地
 * 3. 极简现代风格排版
 */
object PdfExportUtils {

    /** PDF 页面宽度（A4 纸） */
    private const val PAGE_WIDTH = 595
    /** PDF 页面高度（A4 纸） */
    private const val PAGE_HEIGHT = 842
    /** 页面边距 */
    private const val MARGIN = 50f
    /** 内容宽度 */
    private const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN * 2
    /** 行高 */
    private const val LINE_HEIGHT = 22f
    /** 标题字号 */
    private const val TITLE_SIZE = 24f
    /** 正文字号 */
    private const val BODY_SIZE = 11f
    /** 小标题字号 */
    private const val SUBTITLE_SIZE = 16f
    /** 辅助文字字号 */
    private const val CAPTION_SIZE = 9f
    
    /** 主题色 */
    private val BRAND_TEAL = Color.parseColor("#009688")
    /** 主文字颜色 */
    private val TEXT_PRIMARY = Color.parseColor("#1D1D1F")
    /** 次要文字颜色 */
    private val TEXT_SECONDARY = Color.parseColor("#86868B")
    /** 分割线颜色 */
    private val DIVIDER_COLOR = Color.parseColor("#E5E5E7")

    /**
     * 导出行程为 PDF
     * @param context 上下文
     * @param plan 行程实体
     * @return 导出的 PDF 文件
     */
    fun exportTripPlan(context: Context, plan: TripPlanEntity): File {
        val gson = Gson()
        val dayPlans = try {
            if (plan.dayPlansJson.isNotEmpty()) {
                gson.fromJson(plan.dayPlansJson, Array<DayPlan>::class.java).toList()
            } else emptyList()
        } catch (e: Exception) { emptyList() }

        val planHotels = try {
            if (plan.hotelJson.isNotEmpty()) {
                gson.fromJson(plan.hotelJson, Array<PlanHotel>::class.java).toList()
            } else emptyList()
        } catch (e: Exception) { emptyList() }

        val pdfDocument = PdfDocument()
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var yPosition = MARGIN
        var pageNumber = 1

        val titlePaint = Paint().apply { 
            textSize = TITLE_SIZE
            isAntiAlias = true
            color = TEXT_PRIMARY
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subtitlePaint = Paint().apply { 
            textSize = SUBTITLE_SIZE
            isAntiAlias = true
            color = BRAND_TEAL
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = Paint().apply { 
            textSize = BODY_SIZE
            isAntiAlias = true
            color = TEXT_PRIMARY
        }
        val captionPaint = Paint().apply { 
            textSize = CAPTION_SIZE
            isAntiAlias = true
            color = TEXT_SECONDARY
        }
        val dividerPaint = Paint().apply {
            color = DIVIDER_COLOR
            strokeWidth = 1f
        }

        // 页面标题
        yPosition = drawText(page.canvas, "${plan.destination} ${plan.days}日游", MARGIN, yPosition, titlePaint)
        yPosition += 8
        
        // 副标题信息
        if (plan.preferences.isNotEmpty()) {
            yPosition = drawText(page.canvas, "偏好：${plan.preferences}", MARGIN, yPosition, captionPaint)
            yPosition += 12
        }
        
        // 分割线
        yPosition = drawDivider(page.canvas, MARGIN, yPosition, CONTENT_WIDTH, dividerPaint)
        yPosition += 15

        // 出行建议
        if (plan.overallTips.isNotEmpty()) {
            val (newPage, newY, newPageNum) = checkPageBreak(pdfDocument, page, yPosition, pageNumber)
            page = newPage
            yPosition = newY
            pageNumber = newPageNum
            yPosition = drawText(page.canvas, "出行建议", MARGIN, yPosition, subtitlePaint)
            yPosition += 8
            yPosition = drawWrappedText(page.canvas, plan.overallTips, MARGIN, yPosition, CONTENT_WIDTH, bodyPaint)
            yPosition += 20
        }

        // 酒店推荐
        if (planHotels.isNotEmpty()) {
            val (newPage, newY, newPageNum) = checkPageBreak(pdfDocument, page, yPosition, pageNumber)
            page = newPage
            yPosition = newY
            pageNumber = newPageNum
            yPosition = drawText(page.canvas, "酒店推荐", MARGIN, yPosition, subtitlePaint)
            yPosition += 8
            
            planHotels.forEachIndexed { index, hotel ->
                val (np, ny, npn) = checkPageBreak(pdfDocument, page, yPosition, pageNumber)
                page = np
                yPosition = ny
                pageNumber = npn
                
                // 酒店名称
                yPosition = drawText(page.canvas, "${index + 1}. ${hotel.name}", MARGIN + 10, yPosition, bodyPaint)
                yPosition += 5
                
                // 酒店详情
                if (hotel.price.isNotEmpty()) {
                    yPosition = drawText(page.canvas, "价格：${hotel.price}", MARGIN + 20, yPosition, captionPaint)
                    yPosition += 5
                }
                if (hotel.address.isNotEmpty()) {
                    yPosition = drawText(page.canvas, "地址：${hotel.address}", MARGIN + 20, yPosition, captionPaint)
                    yPosition += 5
                }
                if (hotel.advantage.isNotEmpty()) {
                    yPosition = drawWrappedText(page.canvas, "特色：${hotel.advantage}", MARGIN + 20, yPosition, CONTENT_WIDTH - 30, captionPaint)
                    yPosition += 5
                }
                yPosition += 8
            }
            yPosition += 10
        }

        // 每日行程
        if (dayPlans.isNotEmpty()) {
            val (newPage, newY, newPageNum) = checkPageBreak(pdfDocument, page, yPosition, pageNumber)
            page = newPage
            yPosition = newY
            pageNumber = newPageNum
            yPosition = drawText(page.canvas, "每日行程", MARGIN, yPosition, subtitlePaint)
            yPosition += 8

            dayPlans.forEach { day ->
                val (np, ny, npn) = checkPageBreak(pdfDocument, page, yPosition, pageNumber)
                page = np
                yPosition = ny
                pageNumber = npn
                
                // 日期标题
                yPosition = drawText(page.canvas, "第${day.dayNum}天 · ${day.date}", MARGIN + 10, yPosition, bodyPaint)
                yPosition += 5
                
                // 天气
                if (day.weather.isNotEmpty()) {
                    yPosition = drawText(page.canvas, "天气：${day.weather}", MARGIN + 20, yPosition, captionPaint)
                    yPosition += 5
                }
                
                // 行程项目
                day.itinerary.forEach { item ->
                    val (np2, ny2, npn2) = checkPageBreak(pdfDocument, page, yPosition, pageNumber)
                    page = np2
                    yPosition = ny2
                    pageNumber = npn2
                    yPosition = drawText(page.canvas, "• ${item.time} - ${item.spot}", MARGIN + 20, yPosition, bodyPaint)
                    yPosition += 5
                }

                // 餐饮
                day.meals?.let { meals ->
                    meals.lunch?.let {
                        val (np3, ny3, npn3) = checkPageBreak(pdfDocument, page, yPosition, pageNumber)
                        page = np3
                        yPosition = ny3
                        pageNumber = npn3
                        yPosition = drawText(page.canvas, "午餐：${it.name}", MARGIN + 20, yPosition, captionPaint)
                        yPosition += 5
                    }
                    meals.dinner?.let {
                        val (np4, ny4, npn4) = checkPageBreak(pdfDocument, page, yPosition, pageNumber)
                        page = np4
                        yPosition = ny4
                        pageNumber = npn4
                        yPosition = drawText(page.canvas, "晚餐：${it.name}", MARGIN + 20, yPosition, captionPaint)
                        yPosition += 5
                    }
                }

                // 每日提示
                if (day.tips.isNotEmpty()) {
                    val (np5, ny5, npn5) = checkPageBreak(pdfDocument, page, yPosition, pageNumber)
                    page = np5
                    yPosition = ny5
                    pageNumber = npn5
                    yPosition = drawWrappedText(page.canvas, "提示：${day.tips}", MARGIN + 20, yPosition, CONTENT_WIDTH - 30, captionPaint)
                    yPosition += 5
                }
                
                yPosition += 12
            }
        }

        // 页脚
        drawFooter(page.canvas, pageNumber, dividerPaint, captionPaint)

        pdfDocument.finishPage(page)

        // 保存文件
        val outputDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "TripPlans")
        if (!outputDir.exists()) outputDir.mkdirs()
        val fileName = "${plan.destination}_${plan.days}日游_${System.currentTimeMillis()}.pdf"
        val outputFile = File(outputDir, fileName)

        FileOutputStream(outputFile).use { pdfDocument.writeTo(it) }
        pdfDocument.close()

        return outputFile
    }

    /**
     * 分享 PDF 文件
     */
    fun sharePdfFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享行程 PDF"))
    }

    private fun drawText(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint): Float {
        canvas.drawText(text, x, y, paint)
        return y + LINE_HEIGHT
    }

    private fun drawWrappedText(canvas: Canvas, text: String, x: Float, y: Float, maxWidth: Float, paint: Paint): Float {
        val words = text.split(" ")
        var currentY = y
        var line = ""
        words.forEach { word ->
            val testLine = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(testLine) > maxWidth && line.isNotEmpty()) {
                canvas.drawText(line, x, currentY, paint)
                currentY += LINE_HEIGHT
                line = word
            } else {
                line = testLine
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line, x, currentY, paint)
            currentY += LINE_HEIGHT
        }
        return currentY
    }

    private fun drawDivider(canvas: Canvas, x: Float, y: Float, width: Float, paint: Paint): Float {
        canvas.drawLine(x, y, x + width, y, paint)
        return y
    }

    private fun drawFooter(canvas: Canvas, pageNumber: Int, dividerPaint: Paint, captionPaint: Paint) {
        val footerY = PAGE_HEIGHT - 30f
        drawDivider(canvas, MARGIN, footerY - 10, CONTENT_WIDTH, dividerPaint)
        canvas.drawText("第 $pageNumber 页", PAGE_WIDTH - MARGIN - 60, footerY, captionPaint)
    }

    private fun checkPageBreak(pdfDocument: PdfDocument, page: PdfDocument.Page, yPosition: Float, currentPageNumber: Int): Triple<PdfDocument.Page, Float, Int> {
        if (yPosition > PAGE_HEIGHT - MARGIN - 80f) {
            pdfDocument.finishPage(page)
            val newPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNumber + 1).create()
            val newPage = pdfDocument.startPage(newPageInfo)
            return Triple(newPage, MARGIN, currentPageNumber + 1)
        }
        return Triple(page, yPosition, currentPageNumber)
    }
}
