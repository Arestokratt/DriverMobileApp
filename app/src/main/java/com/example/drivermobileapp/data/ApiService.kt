package com.example.drivermobileapp.data.api

import com.example.drivermobileapp.data.models.Driver
import com.example.drivermobileapp.data.models.ShiftStartRequest
import com.example.drivermobileapp.data.models.ShiftStartResponse
import com.example.drivermobileapp.data.models.TransportCheckRequest
import com.example.drivermobileapp.data.models.TransportCheckResponse

import com.example.drivermobileapp.data.models.User
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// Добавь эти data classes прямо здесь или импортируй из другого файла

data class CreateUserRequest(
    val login: String,
    val password: String,
    val role: String,
    val fullName: String,
    val isActive: Boolean
)

data class CreateDriverRequest(
    val userLogin: String,
    val driverLicense: String
)

//  ИНТЕРФЕЙС ДЛЯ АВТОРИЗАЦИИ И ДЛЯ ПРОВЕРКИ ТРАНСПОРТА (В БУДУЩЕМ ОТДЕЛИТЬ)
interface AuthApi {
    //    Авторизация
    @POST("login")  // Убрал / в начале
    suspend fun login(@Body request: LoginRequest): UserResponse

    @GET("users")  // Убрал / в начале
    suspend fun getAllUsers(): List<UserResponse>

    //    Водители
    @GET("api/drivers/list")
    suspend fun getDriversList(): List<DriverResponse>

    @POST("api/shift/check-transport")
    suspend fun checkTransport(@Body request: TransportCheckRequest): TransportCheckResponse

    @POST("api/shift/start")
    suspend fun startShift(@Body request: ShiftStartRequest): ShiftStartResponse

    @POST("api/users/create")
    suspend fun createUser(@Body request: CreateUserRequest): User

    @POST("api/drivers/create")
    suspend fun createDriver(@Body request: CreateDriverRequest): Driver
}

//  ИНТЕРФЕЙС ДЛЯ ЗАКАЗОВ И ЭТАПОВ
interface OrderApi {
    @GET("api/orders/{orderNumber}/stages")
    suspend fun getOrderStages(
        @Path("orderNumber") orderNumber: String
    ): OrderStagesResponse  // ← Из ApiModels

    @POST("api/orders/{orderNumber}/stages/{stageNumber}/complete")
    suspend fun completeStage(
        @Path("orderNumber") orderNumber: String,
        @Path("stageNumber") stageNumber: Int,
        @Body request: CompleteStageRequest  // ← Из ApiModels
    ): CompleteStageResponse  // ← Из ApiModels
}

object RetrofitClient {
    // Для эмулятора Android Studio используй 10.0.2.2
    // Для реального устройства - IP твоего компьютера
    private const val BASE_URL = "http://10.0.2.2:8080/"  // ← Изменил localhost на 10.0.2.2

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy {
        instance.create(AuthApi::class.java)
    }

    val orderApi: OrderApi by lazy {
        instance.create(OrderApi::class.java)
    }
}