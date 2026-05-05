package com.example.trip_planner.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.*

/**
 * 语言枚举
 */
enum class AppLanguage(val code: String, val displayName: String) {
    CHINESE("zh", "中文"),
    ENGLISH("en", "English"),
    JAPANESE("ja", "日本語"),
    KOREAN("ko", "한국어")
}

/**
 * 国际化工具类
 * 支持多语言切换
 */
object I18nUtils {

    private const val PREFS_NAME = "app_settings"
    private const val KEY_LANGUAGE = "language"

    /**
     * 获取当前语言设置
     */
    fun getCurrentLanguage(context: Context): AppLanguage {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val code = prefs.getString(KEY_LANGUAGE, "zh") ?: "zh"
        return AppLanguage.entries.find { it.code == code } ?: AppLanguage.CHINESE
    }

    /**
     * 设置语言
     */
    fun setLanguage(context: Context, language: AppLanguage) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
    }

    /**
     * 应用语言配置到 Context
     */
    fun applyLanguage(context: Context): Context {
        val language = getCurrentLanguage(context)
        val locale = Locale(language.code)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }

    /**
     * 获取翻译字符串
     */
    fun getString(context: Context, key: String): String {
        val language = getCurrentLanguage(context)
        val translations = getTranslations(language)
        return translations[key] ?: key
    }

    /**
     * 获取指定语言的翻译表
     */
    private fun getTranslations(language: AppLanguage): Map<String, String> {
        return when (language) {
            AppLanguage.CHINESE -> chineseTranslations
            AppLanguage.ENGLISH -> englishTranslations
            AppLanguage.JAPANESE -> japaneseTranslations
            AppLanguage.KOREAN -> koreanTranslations
        }
    }

    private val chineseTranslations = mapOf(
        "app_name" to "旅行规划",
        "home" to "首页",
        "history" to "历史",
        "favorites" to "收藏",
        "settings" to "设置",
        "search" to "搜索",
        "destination" to "目的地",
        "days" to "天数",
        "preferences" to "偏好",
        "generate" to "生成行程",
        "weather" to "天气",
        "hotel" to "酒店",
        "attraction" to "景点",
        "restaurant" to "餐厅",
        "expense" to "费用",
        "share" to "分享",
        "import" to "导入",
        "delete" to "删除",
        "edit" to "编辑",
        "save" to "保存",
        "cancel" to "取消",
        "undo" to "撤销",
        "add_expense" to "添加费用",
        "total_expense" to "总花费",
        "no_data" to "暂无数据",
        "loading" to "加载中...",
        "error" to "出错了",
        "retry" to "重试",
        "language" to "语言",
        "dark_mode" to "深色模式",
        "clear_cache" to "清除缓存",
        "about" to "关于"
    )

    private val englishTranslations = mapOf(
        "app_name" to "Trip Planner",
        "home" to "Home",
        "history" to "History",
        "favorites" to "Favorites",
        "settings" to "Settings",
        "search" to "Search",
        "destination" to "Destination",
        "days" to "Days",
        "preferences" to "Preferences",
        "generate" to "Generate Trip",
        "weather" to "Weather",
        "hotel" to "Hotel",
        "attraction" to "Attraction",
        "restaurant" to "Restaurant",
        "expense" to "Expense",
        "share" to "Share",
        "import" to "Import",
        "delete" to "Delete",
        "edit" to "Edit",
        "save" to "Save",
        "cancel" to "Cancel",
        "undo" to "Undo",
        "add_expense" to "Add Expense",
        "total_expense" to "Total Expense",
        "no_data" to "No Data",
        "loading" to "Loading...",
        "error" to "Error",
        "retry" to "Retry",
        "language" to "Language",
        "dark_mode" to "Dark Mode",
        "clear_cache" to "Clear Cache",
        "about" to "About"
    )

    private val japaneseTranslations = mapOf(
        "app_name" to "旅行プランナー",
        "home" to "ホーム",
        "history" to "履歴",
        "favorites" to "お気に入り",
        "settings" to "設定",
        "search" to "検索",
        "destination" to "目的地",
        "days" to "日間",
        "preferences" to "好み",
        "generate" to "旅程生成",
        "weather" to "天気",
        "hotel" to "ホテル",
        "attraction" to "観光スポット",
        "restaurant" to "レストラン",
        "expense" to "費用",
        "share" to "共有",
        "import" to "インポート",
        "delete" to "削除",
        "edit" to "編集",
        "save" to "保存",
        "cancel" to "キャンセル",
        "undo" to "元に戻す",
        "add_expense" to "費用追加",
        "total_expense" to "合計費用",
        "no_data" to "データなし",
        "loading" to "読み込み中...",
        "error" to "エラー",
        "retry" to "再試行",
        "language" to "言語",
        "dark_mode" to "ダークモード",
        "clear_cache" to "キャッシュクリア",
        "about" to "について"
    )

    private val koreanTranslations = mapOf(
        "app_name" to "여행 플래너",
        "home" to "홈",
        "history" to "기록",
        "favorites" to "즐겨찾기",
        "settings" to "설정",
        "search" to "검색",
        "destination" to "목적지",
        "days" to "일수",
        "preferences" to "선호",
        "generate" to "일정 생성",
        "weather" to "날씨",
        "hotel" to "호텔",
        "attraction" to "관광지",
        "restaurant" to "레스토랑",
        "expense" to "비용",
        "share" to "공유",
        "import" to "가져오기",
        "delete" to "삭제",
        "edit" to "편집",
        "save" to "저장",
        "cancel" to "취소",
        "undo" to "실행 취소",
        "add_expense" to "비용 추가",
        "total_expense" to "총 비용",
        "no_data" to "데이터 없음",
        "loading" to "로딩 중...",
        "error" to "오류",
        "retry" to "다시 시도",
        "language" to "언어",
        "dark_mode" to "다크 모드",
        "clear_cache" to "캐시 지우기",
        "about" to "정보"
    )
}
