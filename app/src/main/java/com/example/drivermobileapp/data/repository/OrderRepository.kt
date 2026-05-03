package com.example.drivermobileapp.data.repository

import OrderDriver
import com.example.drivermobileapp.data.api.ApiStageStatus
import com.example.drivermobileapp.data.api.CompleteStageRequest
import com.example.drivermobileapp.data.api.CompleteStageResponse
import com.example.drivermobileapp.data.api.OrderStagesResponse
import com.example.drivermobileapp.data.api.RetrofitClient
import com.example.drivermobileapp.data.local.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OrderRepository(private val prefs: PreferencesManager) {

    private val orderApi = RetrofitClient.orderApi

    suspend fun loadOrderStages(orderNumber: String): OrderStagesResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val response = orderApi.getOrderStages(orderNumber)
                prefs.saveCurrentStage(orderNumber, response.currentStage)
                response.stages.forEach { (stageNumber, status) ->
                    prefs.saveStageCompleted(orderNumber, stageNumber, status.isCompleted)
                }
                response
            } catch (e: Exception) {
                val cachedStage = prefs.getCurrentStage(orderNumber)
                val stages = mutableMapOf<Int, ApiStageStatus>()  // ← Изменено
                for (i in 1..7) {
                    val isCompleted = prefs.isStageCompleted(orderNumber, i)
                    stages[i] = ApiStageStatus(isCompleted = isCompleted)  // ← Изменено
                }

                OrderStagesResponse(
                    orderNumber = orderNumber,
                    currentStage = cachedStage,
                    stages = stages,
                    lastUpdated = System.currentTimeMillis()
                )
            }
        }
    }

    suspend fun completeStage(orderNumber: String, stageNumber: Int, driverId: String): CompleteStageResponse? {
        return withContext(Dispatchers.IO) {
            try {
                // ВРЕМЕННО: имитируем успешный ответ
                prefs.saveStageCompleted(orderNumber, stageNumber, true)
                prefs.saveCurrentStage(orderNumber, stageNumber + 1)

                CompleteStageResponse(
                    success = true,
                    nextStage = stageNumber + 1,
                    message = "Этап завершен"
                )

                /* Оригинальный код (закомментирован)
                val request = CompleteStageRequest(driverId = driverId)
                val response = orderApi.completeStage(orderNumber, stageNumber, request)
                if (response.success) {
                    prefs.saveStageCompleted(orderNumber, stageNumber, true)
                    prefs.saveCurrentStage(orderNumber, response.nextStage)
                }
                response
                */
            } catch (e: Exception) {
                null
            }
        }
    }
    // Временные методы для работы без сервера
    fun acceptOrder(orderId: String, driverId: String): Boolean {
        // TODO: Реализовать API вызов
        return true
    }

    fun rejectOrder(orderId: String): Boolean {
        // TODO: Реализовать API вызов
        return true
    }

    // ========== ТЕСТОВЫЕ ДАННЫЕ (ВРЕМЕННО) ==========
    fun getIncomingOrders(): List<OrderDriver> {
        return listOf(
            OrderDriver(
                id = "1",
                number = "ORD-001",
                status = OrderDriver.OrderStatus.PENDING,
                driverId = null,
                customerName = "ООО Ромашка",
                address = "г. Москва, ул. Ленина, 10",
                createdAt = System.currentTimeMillis(),
                plannedDeliveryTime = null,
                terminalPickupAddress = "г. Москва, ул. Терминальная, д. 1",
                containerType = "20ft",
                containerCount = 2,
                containerDeliveryTime = System.currentTimeMillis(),
                loadingAddress = "г. Москва, ул. Заводская, д. 15",
                cargoName = "Строительные материалы",
                cargoWeight = 2500.5,
                loadingContact = "Иванов Иван",
                departureStation = "Москва-Товарная",
                departureContact = "Петров Петр",
                destinationStation = "Санкт-Петербург-Сортировочный",
                destinationContact = "Сидоров Алексей",
                unloadingAddress = "г. Санкт-Петербург, ул. Складская, д. 10",
                unloadingContact = "Кузнецов Николай",
                terminalReturnAddress = "г. Санкт-Петербург, ул. Терминальная, д. 5"
            )
        )
    }

    fun getMyOrders(driverId: String): List<OrderDriver> {
        return listOf(
            OrderDriver(
                id = "2",
                number = "ORD-002",
                status = OrderDriver.OrderStatus.ACCEPTED,
                driverId = driverId,
                customerName = "ООО Лютик",
                address = "г. Санкт-Петербург, ул. Пушкина, 5",
                createdAt = System.currentTimeMillis(),
                plannedDeliveryTime = null,
                terminalPickupAddress = "г. Москва, ул. Терминальная, д. 2",
                containerType = "40ft",
                containerCount = 1,
                containerDeliveryTime = System.currentTimeMillis(),
                loadingAddress = "г. Москва, ул. Заводская, д. 20",
                cargoName = "Оборудование",
                cargoWeight = 5000.0,
                loadingContact = "Сергеев Сергей",
                departureStation = "Москва-Пассажирская",
                departureContact = "Алексеев Алексей",
                destinationStation = "Нижний Новгород-Сортировочный",
                destinationContact = "Николаев Николай",
                unloadingAddress = "г. Нижний Новгород, ул. Промышленная, д. 5",
                unloadingContact = "Дмитриев Дмитрий",
                terminalReturnAddress = "г. Нижний Новгород, ул. Терминальная, д. 3"
            )
        )
    }

    fun addTestOrders() {
        // Только для тестирования
    }
}