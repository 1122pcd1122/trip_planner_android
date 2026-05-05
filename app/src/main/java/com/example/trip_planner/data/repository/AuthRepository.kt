package com.example.trip_planner.data.repository

import android.util.Log
import com.example.trip_planner.network.AuthApiService
import com.example.trip_planner.network.AuthRequest
import com.example.trip_planner.network.AuthResponse
import com.example.trip_planner.network.CloudTripDetail
import com.example.trip_planner.network.CloudTripInfo
import com.example.trip_planner.network.GetTripRequest
import com.example.trip_planner.network.LoginResult
import com.example.trip_planner.network.NetworkClient
import com.example.trip_planner.network.SaveTripRequest
import com.example.trip_planner.network.TokenRequest
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository {

    private val TAG = "AuthRepository"
    private val apiService = NetworkClient.authApiService
    private val gson = Gson()

    suspend fun register(username: String, password: String, email: String): Result<LoginResult> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.register(AuthRequest(username, password, email))
            if (response.status == "success") {
                val json = gson.fromJson(response.message, JsonObject::class.java)
                val result = LoginResult(
                    userId = json.get("user_id").asLong,
                    username = json.get("username").asString,
                    nickname = json.get("username").asString,
                    email = json.get("email").asString,
                    token = json.get("token").asString
                )
                Result.success(result)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "注册失败: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun login(username: String, password: String): Result<LoginResult> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(AuthRequest(username, password))
            if (response.status == "success") {
                val json = gson.fromJson(response.message, JsonObject::class.java)
                val result = LoginResult(
                    userId = json.get("user_id").asLong,
                    username = json.get("username").asString,
                    nickname = json.get("nickname").asString,
                    email = json.get("email").asString,
                    token = json.get("token").asString
                )
                Result.success(result)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "登录失败: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun verifyToken(token: String): Result<LoginResult> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.verifyToken(TokenRequest(token))
            if (response.status == "success") {
                val json = gson.fromJson(response.message, JsonObject::class.java)
                val result = LoginResult(
                    userId = json.get("user_id").asLong,
                    username = json.get("username").asString,
                    nickname = json.get("nickname").asString,
                    email = json.get("email").asString,
                    token = token
                )
                Result.success(result)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Token 验证失败: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun saveTripToCloud(
        token: String,
        tripId: String,
        destination: String,
        days: Int,
        startDate: String,
        endDate: String,
        preferences: String,
        tripData: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.saveTrip(
                SaveTripRequest(
                    token = token,
                    trip_id = tripId,
                    destination = destination,
                    days = days,
                    start_date = startDate,
                    end_date = endDate,
                    preferences = preferences,
                    trip_data = tripData
                )
            )
            if (response.status == "success") {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "保存行程失败: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getCloudTripList(token: String): Result<List<CloudTripInfo>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTripList(TokenRequest(token))
            if (response.status == "success") {
                val tripList = gson.fromJson(response.message, Array<CloudTripInfoDto>::class.java)
                val result = tripList.map { dto ->
                    CloudTripInfo(
                        tripId = dto.tripId,
                        destination = dto.destination,
                        days = dto.days,
                        startDate = dto.startDate,
                        endDate = dto.endDate,
                        preferences = dto.preferences,
                        updatedAt = dto.updatedAt
                    )
                }
                Result.success(result)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取行程列表失败: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getCloudTrip(token: String, tripId: String): Result<CloudTripDetail> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTrip(GetTripRequest(token, tripId))
            if (response.status == "success") {
                val detail = gson.fromJson(response.message, CloudTripDetailDto::class.java)
                val result = CloudTripDetail(
                    tripId = detail.tripId,
                    destination = detail.destination,
                    days = detail.days,
                    startDate = detail.startDate,
                    endDate = detail.endDate,
                    preferences = detail.preferences,
                    tripData = detail.tripData,
                    updatedAt = detail.updatedAt
                )
                Result.success(result)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取行程失败: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteCloudTrip(token: String, tripId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteTrip(GetTripRequest(token, tripId))
            if (response.status == "success") {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "删除行程失败: ${e.message}")
            Result.failure(e)
        }
    }
}

data class CloudTripInfoDto(
    val trip_id: String,
    val destination: String,
    val days: Int,
    val start_date: String,
    val end_date: String,
    val preferences: String,
    val updated_at: String
) {
    val tripId get() = trip_id
    val startDate get() = start_date
    val endDate get() = end_date
    val updatedAt get() = updated_at
}

data class CloudTripDetailDto(
    val trip_id: String,
    val destination: String,
    val days: Int,
    val start_date: String,
    val end_date: String,
    val preferences: String,
    val trip_data: String,
    val updated_at: String
) {
    val tripId get() = trip_id
    val startDate get() = start_date
    val endDate get() = end_date
    val tripData get() = trip_data
    val updatedAt get() = updated_at
}
