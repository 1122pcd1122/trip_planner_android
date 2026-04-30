package com.example.trip_planner.network.model

/**
 * API 统一响应格式
 * 后端所有接口都返回这种格式的 JSON
 *
 * @property status 状态: "success" 成功, "error" 失败
 * @property message 返回的具体数据（通常是 JSON 字符串）
 * @property code HTTP 状态码
 */
data class ApiResponse(
    val status: String,
    val message: String,
    val code: String
)

/**
 * 旅行计划请求体
 * 用于 generateTripPlan 接口
 *
 * @property destination 目的地
 * @property days 旅行天数
 * @property preferences 用户偏好
 */
data class TripPlanRequest(
    val destination: String,
    val days: String,
    val preferences: String
)

/**
 * 天气信息数据模型
 *
 * @property cityName 城市名称
 * @property latitude 纬度
 * @property longitude 经度
 * @property date 日期
 * @property weather 天气状态
 * @property temperature 温度
 * @property tips 出行建议
 */
data class WeatherResponse(
    val cityName: String,
    val latitude: String,
    val longitude: String,
    val date: String,
    val weather: String,
    val temperature: String,
    val tips: String
)

/**
 * 天气列表响应模型（多天）
 *
 * @property weatherList 天气信息列表
 */
data class WeatherListResponse(
    val weatherList: List<WeatherResponse>
)

/**
 * 景点信息数据模型
 *
 * @property name 景点名称
 * @property latitude 纬度
 * @property longitude 经度
 * @property address 地址
 * @property score 评分
 * @property intro 简介
 */
data class SpotInfo(
    val name: String,
    val latitude: String,
    val longitude: String,
    val address: String,
    val score: String,
    val intro: String
)

/**
 * 酒店信息数据模型
 *
 * @property name 酒店名称
 * @property latitude 纬度
 * @property longitude 经度
 * @property address 地址
 * @property priceRange 价格区间
 * @property feature 特色
 */
data class HotelInfoDto(
    val name: String,
    val latitude: String,
    val longitude: String,
    val address: String,
    val priceRange: String,
    val feature: String
)

/**
 * 餐厅信息数据模型
 *
 * @property name 餐厅名称
 * @property latitude 纬度
 * @property longitude 经度
 * @property address 地址
 * @property featureDish 招牌菜
 * @property score 评分
 */
data class RestaurantInfoDto(
    val name: String,
    val latitude: String,
    val longitude: String,
    val address: String,
    val featureDish: String,
    val score: String
)

/**
 * 景点列表响应
 * @property spotList 景点列表
 */
data class AttractionResponse(val spotList: List<SpotInfo>)

/**
 * 酒店列表响应
 * @property hotelList 酒店列表
 */
data class HotelResponse(val hotelList: List<HotelInfoDto>)

/**
 * 餐厅列表响应
 * @property foodList 餐厅列表
 */
data class RestaurantResponse(val foodList: List<RestaurantInfoDto>)


/**
 * 行程项（每个时间点的安排）
 * @property time 时间
 * @property spot 地点名称
 * @property address 地址
 * @property latitude 纬度
 * @property longitude 经度
 */
data class ItineraryItem(
    val time: String,
    val spot: String,
    val address: String,
    val latitude: String,
    val longitude: String
)

/**
 * 餐饮信息
 * @property name 餐厅名称
 * @property address 地址
 * @property dish 招牌菜
 */
data class MealInfo(
    val name: String,
    val address: String,
    val dish: String
)

/**
 * 每日餐饮安排
 * @property lunch 午餐
 * @property dinner 晚餐
 */
data class DayMeals(
    val lunch: MealInfo?,
    val dinner: MealInfo?
)

/**
 * 每日行程安排
 * @property dayNum 天数
 * @property date 日期
 * @property weather 天气
 * @property itinerary 行程列表
 * @property meals 餐饮安排
 * @property tips 出行建议
 */
data class DayPlan(
    val dayNum: Int,
    val date: String,
    val weather: String,
    val itinerary: List<ItineraryItem>,
    val meals: DayMeals?,
    val tips: String
)

/**
 * 酒店信息（行程规划返回格式）
 * @property name 酒店名称
 * @property address 地址
 * @property price 价格
 * @property advantage 优势
 * @property latitude 纬度
 * @property longitude 经度
 */
data class PlanHotel(
    val name: String,
    val address: String,
    val price: String,
    val advantage: String,
    val latitude: String,
    val longitude: String
)

/**
 * 统一行程规划响应（新接口返回格式）
 *
 * @property days 每日行程列表
 * @property hotel 酒店推荐列表
 * @property overallTips 整体出行建议
 */
data class TripPlanResponse(
    val days: List<DayPlan>,
    val hotel: List<PlanHotel>,
    val overallTips: String
)

/**
 * Agent 请求结果
 * 用于非流式请求的结果封装
 *
 * - Success: 请求成功，包含数据
 * - Error: 请求失败，包含错误信息
 * - Loading: 加载中状态
 */
sealed class AgentResult<out T> {
    data class Success<T>(val data: T) : AgentResult<T>()
    data class Error(val message: String) : AgentResult<Nothing>()
    object Loading : AgentResult<Nothing>()
}
