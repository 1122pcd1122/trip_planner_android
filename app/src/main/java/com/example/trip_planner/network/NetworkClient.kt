package com.example.trip_planner.network


import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 网络客户端配置对象
 * 
 * 负责创建和配置 Retrofit 实例，统一管理网络请求配置
 * 
 * 主要功能：
 * - 配置 OkHttpClient（连接超时、读取超时、日志拦截器）
 * - 配置 Retrofit（基础URL、Gson转换器）
 * - 提供 TripApiService 单例供 Repository 使用
 * 
 * 使用方式：直接通过 NetworkClient.tripApiService 获取 API 服务实例
 */
object NetworkClient {

    /** API 基础URL - 指向本地模拟器的后端服务器 */
    private const val BASE_URL = "http://10.0.2.2:8000"

    /**
     * HTTP 日志拦截器
     * 
     * 用于打印网络请求/响应的详细信息，方便调试
     * Level.BODY 会打印完整的请求头、响应头和请求体
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * OkHttp 客户端配置
     * 
     * 配置说明：
     * - connectTimeout: 30秒，连接服务器的最大等待时间
     * - readTimeout: 30秒，读取响应的最大等待时间
     * - writeTimeout: 30秒，发送请求的最大等待时间
     * - addInterceptor: 添加日志拦截器用于调试
     */
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(300, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .writeTimeout(300, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    /**
     * Retrofit 实例配置
     * 
     * - baseUrl: API 基础地址
     * - client: 使用配置好的 OkHttpClient
     * - addConverterFactory: 使用 Gson 进行 JSON 序列化/反序列化
     */
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(Gson()))
        .build()

    /**
     * TripApiService 实例
     * 
     * 通过 Retrofit 创建 API 服务接口的实现类
     * 供 Repository 层调用具体的 API 方法
     */
    val tripApiService: TripApiService = retrofit.create(TripApiService::class.java)
}
