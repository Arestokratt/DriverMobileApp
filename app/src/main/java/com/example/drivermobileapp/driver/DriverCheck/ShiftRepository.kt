package com.example.drivermobileapp.data.repositories

import com.example.drivermobileapp.data.api.AuthApi
import com.example.drivermobileapp.data.api.RetrofitClient.authApi
import com.example.drivermobileapp.data.models.ShiftStartRequest
import com.example.drivermobileapp.data.models.TransportCheckRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class ShiftRepository(private val api: AuthApi) {

    suspend fun checkTransport(driverLicense: String, licensePlate: String): Result<Boolean> {
        return try {
            val response = api.checkTransport(
                TransportCheckRequest(
                    driverLicense = driverLicense,
                    licensePlate = licensePlate
                )
            )

            if (response.isValid) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.message ?: "Транспорт не найден в системе"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Ошибка сети: проверьте подключение"))
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка сервера: ${e.message}"))
        }
    }

    suspend fun startShift(userId: String, driverLicense: String, licensePlate: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()
                val request = ShiftStartRequest(userId, driverLicense, licensePlate, startTime)
                val response = authApi.startShift(request)

                if (response.success) {
                    Result.success(response.shiftId)
                } else {
                    Result.failure(Exception(response.message ?: "Ошибка начала смены"))
                }
            } catch (e: Exception) {
                println("DEBUG: startShift error = ${e.message}")
                Result.failure(e)
            }
        }
    }
}