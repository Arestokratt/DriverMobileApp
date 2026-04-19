package com.example.drivermobileapp.data.repositories

import com.example.drivermobileapp.data.api.AuthApi
import com.example.drivermobileapp.data.models.ShiftStartRequest
import com.example.drivermobileapp.data.models.TransportCheckRequest
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
        return try {
            val request = ShiftStartRequest(
                userId = userId,
                driverLicense = driverLicense,
                licensePlate = licensePlate,
                startTime = System.currentTimeMillis()
            )

            val response = api.startShift(request)

            if (response.success) {
                Result.success(response.shiftId.toString())
            } else {
                Result.failure(Exception(response.message ?: "Не удалось начать смену"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Ошибка сети: проверьте подключение"))
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка сервера: ${e.message}"))
        }
    }
}