package com.example.drivermobileapp.data.repository

import OrderDriver
import com.example.drivermobileapp.data.api.RetrofitClient
import com.example.drivermobileapp.data.api.ApiStageStatus
import com.example.drivermobileapp.data.api.OrderStagesResponse
import com.example.drivermobileapp.data.api.CompleteStageRequest
import com.example.drivermobileapp.data.api.CompleteStageResponse
import com.example.drivermobileapp.data.api.AcceptOrderRequest
import com.example.drivermobileapp.data.api.OrderResponse
import com.example.drivermobileapp.data.local.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OrderRepository(private val prefs: PreferencesManager) {

    private val orderApi = RetrofitClient.orderApi

    // ========== РАБОТА С ЭТАПАМИ (API) ==========

    suspend fun getOrderStages(orderNumber: String): OrderStagesResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val response = orderApi.getOrderStages(orderNumber)
                prefs.saveCurrentStage(orderNumber, response.currentStage)
                response.stages.forEach { (stageNumber, status) ->
                    prefs.saveStageCompleted(orderNumber, stageNumber, status.isCompleted)
                }
                response
            } catch (e: Exception) {
                // Если сервер не отвечает, берем из кэша
                val cachedStage = prefs.getCurrentStage(orderNumber)
                val stages = mutableMapOf<Int, ApiStageStatus>()
                for (i in 1..7) {
                    val isCompleted = prefs.isStageCompleted(orderNumber, i)
                    stages[i] = ApiStageStatus(isCompleted = isCompleted)
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
                val request = CompleteStageRequest(driverId = driverId)
                val response = orderApi.completeStage(orderNumber, stageNumber, request)

                if (response.success) {
                    prefs.saveStageCompleted(orderNumber, stageNumber, true)
                    prefs.saveCurrentStage(orderNumber, response.nextStage)
                }
                response
            } catch (e: Exception) {
                null
            }
        }
    }

    // ========== РАБОТА СО СПИСКАМИ ЗАЯВОК (API) ==========

    suspend fun getIncomingOrders(driverId: String): List<OrderDriver> {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG CLIENT: Calling API with URL: http://10.0.2.2:8080/api/driver/$driverId/incoming-orders")
                val response = orderApi.getIncomingOrders(driverId)
                println("DEBUG CLIENT: API call successful, response size = ${response.size}")
                response.map { convertToOrderDriver(it) }
            } catch (e: Exception) {
                println("DEBUG CLIENT: API call FAILED: ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        }
    }

    suspend fun getMyOrders(driverId: String): List<OrderDriver> {
        return withContext(Dispatchers.IO) {
            try {
                val response = orderApi.getMyOrders(driverId)
                response.map { convertToOrderDriver(it) }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun acceptOrder(orderNumber: String, driverId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG CLIENT: acceptOrder called with driverId = '$driverId'")
                val response = orderApi.acceptOrder(orderNumber, AcceptOrderRequest(driverId))
                println("DEBUG CLIENT: acceptOrder response success = ${response.success}")
                response.success
            } catch (e: Exception) {
                println("DEBUG CLIENT: acceptOrder error = ${e.message}")
                false
            }
        }
    }

    suspend fun rejectOrder(orderNumber: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = orderApi.rejectOrder(orderNumber)
                response.success
            } catch (e: Exception) {
                false
            }
        }
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ==========

    private fun convertToOrderDriver(response: OrderResponse): OrderDriver {
        return OrderDriver(
            id = response.id.toString(),
            number = response.orderNumber,
            status = convertStatus(response.status),
            driverId = response.assignedDriverId,
            customerName = response.loadingContactPerson,
            address = response.containerLoadingAddress,
            createdAt = System.currentTimeMillis(),
            plannedDeliveryTime = null,
            terminalPickupAddress = response.emptyContainerTerminalAddress,
            containerType = response.containerType,
            containerCount = response.containerCount,
            containerDeliveryTime = response.containerDeliveryDateTime,
            loadingAddress = response.containerLoadingAddress,
            cargoName = response.cargoName,
            cargoWeight = response.cargoWeight,
            loadingContact = response.loadingContactPerson,
            departureStation = response.departureStationName,
            departureContact = response.departureStationContact,
            destinationStation = response.destinationStationName,
            destinationContact = response.destinationStationContact,
            unloadingAddress = response.unloadingAddress,
            unloadingContact = response.unloadingContactPerson,
            terminalReturnAddress = response.returnTerminalAddress
        )
    }

    private fun convertStatus(status: String): OrderDriver.OrderStatus {
        return when (status.uppercase()) {
            "PENDING" -> OrderDriver.OrderStatus.PENDING
            "NEW" -> OrderDriver.OrderStatus.NEW
            "ACCEPTED" -> OrderDriver.OrderStatus.ACCEPTED
            "IN_PROGRESS" -> OrderDriver.OrderStatus.IN_PROGRESS
            "COMPLETED" -> OrderDriver.OrderStatus.COMPLETED
            "CANCELLED" -> OrderDriver.OrderStatus.CANCELLED
            "REJECTED" -> OrderDriver.OrderStatus.CANCELLED
            else -> OrderDriver.OrderStatus.PENDING
        }
    }
}