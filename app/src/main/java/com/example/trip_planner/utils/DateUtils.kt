package com.example.trip_planner.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object DateUtils {
    
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    fun formatDate(date: LocalDate): String {
        return date.format(DATE_FORMATTER)
    }
    
    fun parseDate(dateStr: String): LocalDate? {
        return try {
            LocalDate.parse(dateStr, DATE_FORMATTER)
        } catch (e: DateTimeParseException) {
            null
        }
    }
    
    fun calculateDays(startDate: String, endDate: String): String {
        if (startDate.isEmpty() || endDate.isEmpty()) {
            return "3"
        }
        
        return try {
            val startDt = LocalDate.parse(startDate, DATE_FORMATTER)
            val endDt = LocalDate.parse(endDate, DATE_FORMATTER)
            java.time.temporal.ChronoUnit.DAYS.between(startDt, endDt).plus(1).toString()
        } catch (e: DateTimeParseException) {
            "3"
        }
    }
    
    fun calculateEndDate(startDate: String, days: Int): String? {
        return try {
            val startDt = LocalDate.parse(startDate, DATE_FORMATTER)
            val endDt = startDt.plusDays((days - 1).toLong())
            formatDate(endDt)
        } catch (e: DateTimeParseException) {
            null
        }
    }
    
    fun isValidDate(dateStr: String): Boolean {
        return try {
            LocalDate.parse(dateStr, DATE_FORMATTER)
            true
        } catch (e: DateTimeParseException) {
            false
        }
    }
    
    fun isStartDateBeforeEndDate(startDate: String, endDate: String): Boolean {
        return try {
            val startDt = LocalDate.parse(startDate, DATE_FORMATTER)
            val endDt = LocalDate.parse(endDate, DATE_FORMATTER)
            startDt.isBefore(endDt) || startDt.isEqual(endDt)
        } catch (e: DateTimeParseException) {
            false
        }
    }
    
    fun getToday(): String {
        return formatDate(LocalDate.now())
    }
    
    fun getDateAfterDays(days: Int): String {
        return formatDate(LocalDate.now().plusDays(days.toLong()))
    }
    
    fun getDefaultDateRange(): Pair<String, String> {
        val today = LocalDate.now()
        val endDate = today.plusDays(2)
        return formatDate(today) to formatDate(endDate)
    }
}