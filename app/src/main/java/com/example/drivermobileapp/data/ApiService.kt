package com.example.drivermobileapp.data.api

import com.example.drivermobileapp.data.models.ShiftStartRequest
import com.example.drivermobileapp.data.models.ShiftStartResponse
import com.example.drivermobileapp.data.models.TransportCheckRequest
import com.example.drivermobileapp.data.models.TransportCheckResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): UserResponse

    @GET("/users")
    suspend fun getAllUsers(): List<UserResponse>

    @POST("/api/shift/check-transport")
    suspend fun checkTransport(@Body request: TransportCheckRequest): TransportCheckResponse

    @POST("/api/shift/start")
    suspend fun startShift(@Body request: ShiftStartRequest): ShiftStartResponse
}

object RetrofitClient {
    // Для эмулятора используйте 10.0.2.2
    // Для реального устройства - IP вашего компьютера (например, 192.168.1.100)
    private const val BASE_URL = "http://localhost:8080/"

    val instance: AuthApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(AuthApi::class.java)
    }
}