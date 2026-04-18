package com.example.drivermobileapp.driver.DriverCheck

import com.example.drivermobileapp.data.api.AuthApi
import com.example.drivermobileapp.data.models.TransportCheckRequest
import com.example.drivermobileapp.data.models.ShiftStartRequest
import java.io.IOException

class ShiftRepository(private val api: AuthApi) {

    // Метод для проверки транспорта
    suspend fun checkTransport(licensePlate: String, carBrand: String): Result<Boolean> {
        return try {
            val response = api.checkTransport(
                TransportCheckRequest(licensePlate, carBrand)
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

    // Метод для начала смены
    suspend fun startShift(userId: String, driverLicense: String, licensePlate: String): Result<Int> {
        return try {
            val request = ShiftStartRequest(
                userId = userId,
                driverLicense = driverLicense,  // ← изменено
                licensePlate = licensePlate,
                startTime = System.currentTimeMillis()
            )
            val response = api.startShift(request)

            if (response.success) {
                Result.success(response.shiftId)
            } else {
                Result.failure(Exception("Не удалось начать смену"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Ошибка сети: проверьте подключение"))
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка сервера: ${e.message}"))
        }
    }
}