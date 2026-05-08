package com.example.trip_planner.network

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object NetworkClient {

    private const val BASE_URL = "http://10.0.2.2:8000"
    private const val CONNECT_TIMEOUT_SECONDS = 30L
    private const val READ_TIMEOUT_SECONDS = 120L
    private const val WRITE_TIMEOUT_SECONDS = 30L
    private const val MAX_RETRY_COUNT = 3
    private const val RETRY_DELAY_MILLIS = 1000L

    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val retryInterceptor by lazy {
        okhttp3.Interceptor { chain ->
            val request = chain.request()
            var response: okhttp3.Response? = null
            var exception: IOException? = null

            for (attempt in 1..MAX_RETRY_COUNT) {
                try {
                    response?.close()
                    response = chain.proceed(request)

                    val currentResponse = response
                    if (currentResponse != null && currentResponse.isSuccessful) {
                        return@Interceptor currentResponse
                    }

                    if (currentResponse != null) {
                        val code = currentResponse.code
                        if (code >= 500 && code < 600 && attempt < MAX_RETRY_COUNT) {
                            Thread.sleep(RETRY_DELAY_MILLIS * attempt)
                            continue
                        }
                        return@Interceptor currentResponse
                    }
                } catch (e: IOException) {
                    exception = e
                    if (attempt < MAX_RETRY_COUNT) {
                        Thread.sleep(RETRY_DELAY_MILLIS * attempt)
                    }
                }
            }

            throw exception ?: IOException("请求失败")
        }
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(retryInterceptor)
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
    }

    val tripApiService: TripApiService by lazy { retrofit.create(TripApiService::class.java) }
    val authApiService: AuthApiService by lazy { retrofit.create(AuthApiService::class.java) }
}