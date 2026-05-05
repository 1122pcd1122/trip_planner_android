package com.example.trip_planner.network

import com.example.trip_planner.network.model.AgentResult
import com.example.trip_planner.network.model.ApiResponse
import com.example.trip_planner.network.model.DetailRequest
import com.example.trip_planner.network.model.TripPlanRequest
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Trip API 服务接口
 * 
 * 定义所有后端 API 调用的接口方法
 * 使用 Retrofit 的 suspend 函数实现异步请求
 * 
 * 接口列表：
 * - generateTripPlan: 生成旅行计划（SSE 流式返回）
 * - getWeather: 获取天气信息
 * - getAttractions: 获取景点推荐
 * - getHotels: 获取酒店推荐
 * - getRestaurants: 获取餐饮推荐
 */
interface TripApiService {

    /**
     * 获取天气信息
     * 
     * @param request 包含目的地的请求体
     * @return ApiResponse 服务器响应
     */
    @POST("/api/weather")
    suspend fun getWeather(@Body request: TripPlanRequest): ApiResponse

    /**
     * 获取景点推荐
     * 
     * @param request 包含目的地的请求体
     * @return ApiResponse 服务器响应
     */
    @POST("/api/attraction")
    suspend fun getAttractions(@Body request: TripPlanRequest): ApiResponse

    /**
     * 获取酒店推荐
     * 
     * @param request 包含目的地的请求体
     * @return ApiResponse 服务器响应
     */
    @POST("/api/hotel")
    suspend fun getHotels(@Body request: TripPlanRequest): ApiResponse

    /**
     * 获取餐饮推荐
     * 
     * @param request 包含目的地的请求体
     * @return ApiResponse 服务器响应
     */
    @POST("/api/restaurant")
    suspend fun getRestaurants(@Body request: TripPlanRequest): ApiResponse

    /**
     * 一键生成完整旅行规划
     * 
     * ALL 模式下的统一接口，一次请求获取所有数据
     * 包含天气、景点、酒店、餐厅和行程汇总
     * 
     * @param request 包含目的地、天数、偏好的请求体
     * @return ApiResponse 服务器响应
     */
    @POST("/api/plan")
    suspend fun generateAllInOne(@Body request: TripPlanRequest): ApiResponse

    /**
     * 获取酒店详情
     * 
     * @param request 包含酒店名称、经纬度的请求体
     * @return ApiResponse 服务器响应，包含酒店详细信息 JSON
     */
    @POST("/api/hotel/detail")
    suspend fun getHotelDetail(@Body request: DetailRequest): ApiResponse

    /**
     * 获取景点详情
     * 
     * @param request 包含景点名称、经纬度的请求体
     * @return ApiResponse 服务器响应，包含景点详细信息 JSON
     */
    @POST("/api/attraction/detail")
    suspend fun getAttractionDetail(@Body request: DetailRequest): ApiResponse

    /**
     * 获取餐厅详情
     * 
     * @param request 包含餐厅名称、经纬度的请求体
     * @return ApiResponse 服务器响应，包含餐厅详细信息 JSON
     */
    @POST("/api/restaurant/detail")
    suspend fun getRestaurantDetail(@Body request: DetailRequest): ApiResponse
}