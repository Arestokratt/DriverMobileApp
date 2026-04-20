package services

import models.requests.ShiftEndRequest
import models.requests.ShiftStartRequest
import models.requests.TransportCheckRequest
import models.responses.ActiveShiftResponse
import models.responses.ShiftEndResponse
import models.responses.ShiftStartResponse
import models.responses.TransportCheckResponse
import repositories.ShiftRepository

class ShiftService {
    private val shiftRepository = ShiftRepository()

    fun checkTransport(request: TransportCheckRequest): TransportCheckResponse {
        val driverLicense = request.driverLicense.trim()
        val licensePlate = request.licensePlate.trim()

        if (driverLicense.isBlank() || licensePlate.isBlank()) {
            return TransportCheckResponse(
                isValid = false,
                message = "Заполните все поля"
            )
        }

        return shiftRepository.checkTransport(driverLicense, licensePlate)
    }

    fun startShift(request: ShiftStartRequest): ShiftStartResponse {
        println("🚀 SERVICE: startShift called")
        println("   Request: $request")

        val userId = request.userId  // Теперь это String (UUID)
        val driverLicense = request.driverLicense.trim()
        val licensePlate = request.licensePlate.trim()

        if (userId.isBlank()) {
            return ShiftStartResponse("-1", false, "Неверный ID пользователя")
        }

        if (driverLicense.isBlank() || licensePlate.isBlank()) {
            return ShiftStartResponse("-1", false, "Заполните все поля")
        }

        if (request.startTime <= 0L) {
            return ShiftStartResponse("-1", false, "Неверное время начала смены")
        }

        // Проверка активной смены (нужно изменить метод)
        if (shiftRepository.hasActiveShift(userId)) {
            return ShiftStartResponse("-1", false, "У вас уже есть активная смена")
        }

        val vehicleCheck = shiftRepository.checkTransport(driverLicense, licensePlate)
        if (!vehicleCheck.isValid) {
            return ShiftStartResponse("-1", false, vehicleCheck.message)
        }

        val shiftId = shiftRepository.startShift(
            userId = userId,  // Передаем UUID как строку
            driverLicense = driverLicense,
            licensePlate = licensePlate,
            startTime = request.startTime
        )

        return if (shiftId != "-1") {
            ShiftStartResponse(shiftId, true, "Смена успешно начата")
        } else {
            ShiftStartResponse("-1", false, "Не удалось создать смену")
        }
    }

    fun endShift(request: ShiftEndRequest): ShiftEndResponse {
        val shiftId = request.shiftId.toIntOrNull()
        if (shiftId == null || shiftId <= 0) {
            return ShiftEndResponse(false, "Неверный ID смены")
        }

        val endTime = request.endTime ?: System.currentTimeMillis()
        if (endTime <= 0L) {
            return ShiftEndResponse(false, "Неверное время завершения смены")
        }

        val success = shiftRepository.endShift(shiftId.toString(), endTime)

        return if (success) {
            ShiftEndResponse(true, "Смена успешно завершена")
        } else {
            ShiftEndResponse(false, "Активная смена не найдена")
        }
    }

    fun getActiveShift(userId: String): ActiveShiftResponse? {
        val parsedUserId = userId.toIntOrNull() ?: return null
        if (parsedUserId <= 0) return null

        return shiftRepository.getActiveShiftByUserId(parsedUserId.toString())
    }
}
