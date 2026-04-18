package services

import repositories.ShiftRepository
import models.requests.*
import models.responses.*

class ShiftService {
    private val shiftRepository = ShiftRepository()

    fun checkTransport(request: TransportCheckRequest): TransportCheckResponse {
        // Валидация входных данных
        if (request.driverLicense.isBlank() || request.licensePlate.isBlank()) {
            return TransportCheckResponse(
                isValid = false,
                message = "Заполните все поля"
            )
        }

        return shiftRepository.checkTransport(request.driverLicense, request.licensePlate)
    }

    fun startShift(request: ShiftStartRequest): ShiftStartResponse {
        // Валидация
        if (request.userId <= 0) {
            return ShiftStartResponse(-1, false, "Неверный ID пользователя")
        }
        if (request.driverLicense.isBlank() || request.licensePlate.isBlank()) {
            return ShiftStartResponse(-1, false, "Заполните все поля")
        }

        // Проверяем, нет ли активной смены
        if (shiftRepository.hasActiveShift(request.userId)) {
            return ShiftStartResponse(-1, false, "У вас уже есть активная смена")
        }

        // Проверяем транспорт
        val vehicleCheck = shiftRepository.checkTransport(request.driverLicense, request.licensePlate)
        if (!vehicleCheck.isValid) {
            return ShiftStartResponse(-1, false, vehicleCheck.message)
        }

        // Начинаем смену
        val shiftId = shiftRepository.startShift(
            request.userId,
            request.driverLicense,
            request.licensePlate,
            request.startTime
        )

        return if (shiftId > 0) {
            ShiftStartResponse(shiftId, true, "Смена успешно начата")
        } else {
            ShiftStartResponse(-1, false, "Не удалось создать смену")
        }
    }

    fun endShift(request: ShiftEndRequest): ShiftEndResponse {
        if (request.shiftId <= 0) {
            return ShiftEndResponse(false, "Неверный ID смены")
        }

        val endTime = request.endTime ?: System.currentTimeMillis()
        val success = shiftRepository.endShift(request.shiftId, endTime)

        return if (success) {
            ShiftEndResponse(true, "Смена успешно завершена")
        } else {
            ShiftEndResponse(false, "Активная смена не найдена")
        }
    }

    fun getActiveShift(userId: Int): ActiveShiftResponse? {
        if (userId <= 0) return null
        return shiftRepository.getActiveShiftByUserId(userId)
    }
}