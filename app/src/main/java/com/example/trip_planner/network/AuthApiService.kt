package com.example.trip_planner.network

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("/api/auth/register")
    suspend fun register(@Body request: AuthRequest): AuthResponse

    @POST("/api/auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @POST("/api/auth/verify")
    suspend fun verifyToken(@Body request: TokenRequest): AuthResponse

    @POST("/api/trip/save")
    suspend fun saveTrip(@Body request: SaveTripRequest): AuthResponse

    @POST("/api/trip/list")
    suspend fun getTripList(@Body request: TokenRequest): AuthResponse

    @POST("/api/trip/get")
    suspend fun getTrip(@Body request: GetTripRequest): AuthResponse

    @POST("/api/trip/delete")
    suspend fun deleteTrip(@Body request: GetTripRequest): AuthResponse
}

data class AuthRequest(
    val username: String,
    val password: String,
    val email: String = ""
)

data class TokenRequest(
    val token: String
)

data class SaveTripRequest(
    val token: String,
    val trip_id: String,
    val destination: String,
    val days: Int,
    val start_date: String = "",
    val end_date: String = "",
    val preferences: String = "",
    val trip_data: String
)

data class GetTripRequest(
    val token: String,
    val trip_id: String
)

data class AuthResponse(
    val status: String,
    val message: String,
    val code: String
)

data class LoginResult(
    val userId: Long,
    val username: String,
    val nickname: String,
    val email: String,
    val token: String
)

data class CloudTripInfo(
    val tripId: String,
    val destination: String,
    val days: Int,
    val startDate: String,
    val endDate: String,
    val preferences: String,
    val updatedAt: String
)

data class CloudTripDetail(
    val tripId: String,
    val destination: String,
    val days: Int,
    val startDate: String,
    val endDate: String,
    val preferences: String,
    val tripData: String,
    val updatedAt: String
)
