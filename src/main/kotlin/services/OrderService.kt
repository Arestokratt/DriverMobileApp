package services

import models.requests.CompleteStageRequest
import models.responses.CompleteStageResponse
import models.responses.OrderStagesResponse
import models.responses.StageStatusResponse
import repositories.OrderRepository

class OrderService {
    private val orderRepository = OrderRepository()

    fun getOrderStages(orderNumber: String): OrderStagesResponse? {
        val order = orderRepository.getOrderByNumber(orderNumber) ?: return null
        val stages = orderRepository.getOrderStages(orderNumber)
        val currentStage = orderRepository.getCurrentActiveStage(orderNumber)

        val stagesMap = mutableMapOf<Int, StageStatusResponse>()
        stages.forEach { stage ->
            stagesMap[stage.stageNumber] = StageStatusResponse(
                isCompleted = stage.status == "COMPLETED",
                completedAt = stage.completedAt?.time
            )
        }

        return OrderStagesResponse(
            orderNumber = orderNumber,
            currentStage = currentStage,
            stages = stagesMap,
            lastUpdated = System.currentTimeMillis()
        )
    }

    fun completeStage(orderNumber: String, stageNumber: Int, request: CompleteStageRequest): CompleteStageResponse {
        val currentStage = orderRepository.getCurrentActiveStage(orderNumber)

        // Проверяем, что завершаемый этап - текущий
        if (stageNumber != currentStage) {
            return CompleteStageResponse(
                success = false,
                nextStage = currentStage,
                currentStage = currentStage,
                allStagesCompleted = false,
                message = "Сначала завершите текущий этап"
            )
        }

        val success = orderRepository.completeStage(
            orderNumber = orderNumber,
            stageNumber = stageNumber,
            driverId = request.driverId,
            driverNotes = request.driverNotes,
            arrivalTime = request.arrivalTime,
            departureTime = request.departureTime,
            gpsLatitude = request.gpsLatitude,
            gpsLongitude = request.gpsLongitude,
            photoUrl = request.photoUrl
        )

        val nextStage = stageNumber + 1
        val allStagesCompleted = nextStage > 7

        return if (success) {
            CompleteStageResponse(
                success = true,
                nextStage = nextStage,
                currentStage = if (allStagesCompleted) stageNumber else nextStage,
                allStagesCompleted = allStagesCompleted,
                message = "Этап $stageNumber успешно завершен"
            )
        } else {
            CompleteStageResponse(
                success = false,
                nextStage = stageNumber,
                currentStage = currentStage,
                allStagesCompleted = false,
                message = "Ошибка при завершении этапа"
            )
        }
    }
}