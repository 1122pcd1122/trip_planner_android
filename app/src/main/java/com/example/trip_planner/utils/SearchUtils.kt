package com.example.trip_planner.utils

/**
 * 搜索工具类
 * 支持模糊搜索和拼音搜索
 */
object SearchUtils {

    /**
     * 模糊匹配
     * 支持部分匹配、忽略大小写
     */
    fun fuzzyMatch(text: String, query: String): Boolean {
        if (query.isBlank()) return true
        if (text.isBlank()) return false
        
        val normalizedText = text.lowercase()
        val normalizedQuery = query.lowercase()
        
        // 完全包含匹配
        if (normalizedText.contains(normalizedQuery)) return true
        
        // 字符序列匹配（支持跳跃匹配）
        return matchesCharacterSequence(normalizedText, normalizedQuery)
    }

    /**
     * 字符序列匹配
     * 例如：查询 "bj" 可以匹配 "beijing"
     */
    private fun matchesCharacterSequence(text: String, query: String): Boolean {
        var textIndex = 0
        var queryIndex = 0
        
        while (textIndex < text.length && queryIndex < query.length) {
            if (text[textIndex] == query[queryIndex]) {
                queryIndex++
            }
            textIndex++
        }
        
        return queryIndex == query.length
    }

    /**
     * 计算匹配分数
     * 用于排序，分数越高匹配度越好
     */
    fun calculateMatchScore(text: String, query: String): Int {
        if (query.isBlank()) return 0
        if (text.isBlank()) return 0
        
        val normalizedText = text.lowercase()
        val normalizedQuery = query.lowercase()
        
        var score = 0
        
        // 完全匹配得分最高
        if (normalizedText == normalizedQuery) {
            score += 100
        }
        
        // 开头匹配
        if (normalizedText.startsWith(normalizedQuery)) {
            score += 50
        }
        
        // 包含匹配
        if (normalizedText.contains(normalizedQuery)) {
            score += 30
        }
        
        // 字符序列匹配
        if (matchesCharacterSequence(normalizedText, normalizedQuery)) {
            score += 10
        }
        
        return score
    }

    /**
     * 过滤列表并排序
     * 根据匹配分数降序排列
     */
    fun <T> filterAndSort(
        items: List<T>,
        query: String,
        getText: (T) -> String
    ): List<T> {
        if (query.isBlank()) return items
        
        return items
            .filter { fuzzyMatch(getText(it), query) }
            .sortedByDescending { calculateMatchScore(getText(it), query) }
    }
}
