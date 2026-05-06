package com.example.trip_planner.data.repository

import com.example.trip_planner.data.local.dao.PreferenceTagDao
import com.example.trip_planner.data.local.entity.PreferenceTagEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PreferenceTagRepository(private val tagDao: PreferenceTagDao) {

    private val defaultTags = listOf(
        PreferenceTagEntity("1", "不吃辣", "🌶️", "辣 不吃辣 清淡 不辣", "饮食"),
        PreferenceTagEntity("2", "素食", "🥬", "素食 素 蔬菜 健康", "饮食"),
        PreferenceTagEntity("3", "经济型", "💰", "经济 便宜 省钱 性价比", "预算"),
        PreferenceTagEntity("4", "豪华型", "💎", "豪华 高端 五星 奢华", "预算"),
        PreferenceTagEntity("5", "亲子", "👨‍👩‍👧‍👦", "亲子 儿童 小孩 家庭", "人群"),
        PreferenceTagEntity("6", "情侣", "💑", "情侣 浪漫 约会 二人", "人群"),
        PreferenceTagEntity("7", "自然风光", "🏔️", "自然 风景 山水 户外", "兴趣"),
        PreferenceTagEntity("8", "人文历史", "🏛️", "人文 历史 文化 古迹", "兴趣"),
        PreferenceTagEntity("9", "购物", "🛍️", "购物 商场 逛街 买", "兴趣"),
        PreferenceTagEntity("10", "拍照打卡", "📸", "拍照 打卡 网红 出片", "兴趣"),
        PreferenceTagEntity("11", "美食", "🍜", "美食 小吃 吃 餐厅", "兴趣"),
        PreferenceTagEntity("12", "休闲", "☕", "休闲 放松 慢节奏 舒适", "兴趣"),
        PreferenceTagEntity("13", "摄影", "📷", "摄影 拍照 镜头 画面", "兴趣"),
        PreferenceTagEntity("14", "户外", "🏕️", "户外 露营 徒步 探险", "兴趣"),
        PreferenceTagEntity("15", "文化", "📚", "文化 知识 学习 体验", "兴趣")
    )

    fun getAllTags(): Flow<List<PreferenceTagEntity>> = tagDao.getAllTags()

    fun searchTags(keyword: String): Flow<List<PreferenceTagEntity>> {
        return if (keyword.isBlank()) {
            tagDao.getAllTags()
        } else {
            tagDao.searchTags(keyword.trim())
        }
    }

    fun getTagsByCategory(category: String): Flow<List<PreferenceTagEntity>> =
        tagDao.getTagsByCategory(category)

    suspend fun initDefaultTags() {
        if (tagDao.getTagCount() == 0) {
            tagDao.insertTags(defaultTags)
        }
    }

    suspend fun resetDefaultTags() {
        tagDao.clearAllTags()
        tagDao.insertTags(defaultTags)
    }

    suspend fun saveUserTag(label: String) {
        if (label.isBlank()) return
        val existing = tagDao.getTagByLabel(label)
        if (existing != null) return
        val newTag = PreferenceTagEntity(
            id = "user_${System.currentTimeMillis()}",
            label = label,
            icon = "✨",
            keywords = label,
            category = "自定义"
        )
        tagDao.insertSingleTag(newTag)
    }
}
