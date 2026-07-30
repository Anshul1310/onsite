package com.example.myapplication.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("mark")
    suspend fun markAttendance(
        @Header("Authorization") authHeader: String,
        @Body request: MarkRequest
    ): Response<MarkResponse>

    @GET("attendance/{date}")
    suspend fun getAttendance(
        @Path("date") date: String
    ): AttendanceResponse

    @GET("attendance/count/{rollNo}")
    suspend fun getAttendanceCount(
        @Path("rollNo") rollNo: String
    ): AttendanceCountResponse

    @POST("generate")
    suspend fun generateQr(
        @Body request: GenerateRequest = GenerateRequest()
    ): Response<GenerateResponse>
}
