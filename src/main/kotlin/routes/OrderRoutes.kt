package routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import models.requests.CompleteStageRequest
import models.requests.AcceptOrderRequest
import models.responses.ErrorResponse
import models.responses.OrderListResponse
import repositories.OrderRepository
import models.responses.CompleteStageResponse
import services.OrderService

fun Route.orderRoutes() {
    val orderService = OrderService()
    val orderRepository = OrderRepository()

    // ========== ЭТАПЫ ==========

    // Получить статусы этапов заявки
    get("/api/orders/{orderNumber}/stages") {
        val orderNumber = call.parameters["orderNumber"]
        if (orderNumber.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("INVALID_ORDER", "Неверный номер заказа")
            )
            return@get
        }

        val response = orderService.getOrderStages(orderNumber)
        if (response != null) {
            call.respond(response)
        } else {
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse("ORDER_NOT_FOUND", "Заказ не найден")
            )
        }
    }

    // Завершить этап
    post("/api/orders/{orderNumber}/stages/{stageNumber}/complete") {
        val orderNumber = call.parameters["orderNumber"]
        val stageNumber = call.parameters["stageNumber"]?.toIntOrNull()

        if (orderNumber.isNullOrBlank() || stageNumber == null) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("INVALID_PARAMS", "Неверные параметры")
            )
            return@post
        }

        val request = call.receive<CompleteStageRequest>()
        val response = orderService.completeStage(orderNumber, stageNumber, request)

        // Явно указываем тип
        call.respond(response as CompleteStageResponse)
    }

    // ========== ЗАЯВКИ ДЛЯ ВОДИТЕЛЯ ==========

    // Получить входящие заявки (статус PENDING)
    get("/api/driver/{driverId}/incoming-orders") {
        val driverId = call.parameters["driverId"]
        println("DEBUG ROUTE: incoming-orders called with driverId = '$driverId'")

        if (driverId.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("INVALID_DRIVER", "Неверный ID водителя")
            )
            return@get
        }

        val orders = orderRepository.getIncomingOrders(driverId)
        println("DEBUG ROUTE: found ${orders.size} orders")

        val response = orders.map { order ->
            println("DEBUG ROUTE: Converting order ${order.orderNumber}")
            OrderListResponse(
                id = order.id.toString(),
                orderNumber = order.orderNumber,
                status = order.status,
                emptyContainerTerminalAddress = order.emptyContainerTerminalAddress,
                containerType = order.containerType,
                containerCount = order.containerCount,
                containerDeliveryDateTime = order.containerDeliveryDateTime,  // ← Long
                containerLoadingAddress = order.containerLoadingAddress,
                cargoName = order.cargoName,
                cargoWeight = order.cargoWeight,
                loadingContactPerson = order.loadingContactPerson,
                loadingContactPhone = order.loadingContactPhone,
                departureStationName = order.departureStationName,
                departureStationContact = order.departureStationContact,
                departureStationPhone = order.departureStationPhone,
                destinationStationName = order.destinationStationName,
                destinationStationContact = order.destinationStationContact,
                destinationStationPhone = order.destinationStationPhone,
                unloadingAddress = order.unloadingAddress,
                unloadingContactPerson = order.unloadingContactPerson,
                unloadingContactPhone = order.unloadingContactPhone,
                returnTerminalAddress = order.returnTerminalAddress,
                assignedDriverId = order.assignedDriverId,
                assignedDriverName = order.assignedDriverName,
                notes = order.notes
            )
        }
        println("DEBUG ROUTE: Sending response with ${response.size} orders")
        call.respond(response)
    }

    // Получить мои заявки (статус ACCEPTED или IN_PROGRESS)
    get("/api/driver/{driverId}/my-orders") {
        val driverId = call.parameters["driverId"]
        println("DEBUG ROUTE: my-orders called with driverId = '$driverId'")

        if (driverId.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("INVALID_DRIVER", "Неверный ID водителя")
            )
            return@get
        }

        val orders = orderRepository.getMyOrders(driverId)
        println("DEBUG ROUTE: found ${orders.size} orders")

        val response = orders.map { order ->
            OrderListResponse(
                id = order.id.toString(),
                orderNumber = order.orderNumber,
                status = order.status,
                emptyContainerTerminalAddress = order.emptyContainerTerminalAddress,
                containerType = order.containerType,
                containerCount = order.containerCount,
                containerDeliveryDateTime = order.containerDeliveryDateTime,
                containerLoadingAddress = order.containerLoadingAddress,
                cargoName = order.cargoName,
                cargoWeight = order.cargoWeight,
                loadingContactPerson = order.loadingContactPerson,
                loadingContactPhone = order.loadingContactPhone,
                departureStationName = order.departureStationName,
                departureStationContact = order.departureStationContact,
                departureStationPhone = order.departureStationPhone,
                destinationStationName = order.destinationStationName,
                destinationStationContact = order.destinationStationContact,
                destinationStationPhone = order.destinationStationPhone,
                unloadingAddress = order.unloadingAddress,
                unloadingContactPerson = order.unloadingContactPerson,
                unloadingContactPhone = order.unloadingContactPhone,
                returnTerminalAddress = order.returnTerminalAddress,
                assignedDriverId = order.assignedDriverId,
                assignedDriverName = order.assignedDriverName,
                notes = order.notes
            )
        }
        call.respond(response)
    }

    // ========== ДЕЙСТВИЯ С ЗАЯВКОЙ ==========

    // Принять заявку
    post("/api/orders/{orderNumber}/accept") {
        val orderNumber = call.parameters["orderNumber"]
        println("DEBUG ROUTE: accept order called with orderNumber = '$orderNumber'")

        if (orderNumber.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("INVALID_ORDER", "Неверный номер заказа")
            )
            return@post
        }

        val request = call.receive<AcceptOrderRequest>()
        println("DEBUG ROUTE: driverId from request = '${request.driverId}'")

        val success = orderRepository.acceptOrder(orderNumber, request.driverId)
        println("DEBUG ROUTE: accept order success = $success")

        call.respond(mapOf("success" to success))
    }

    // Отклонить заявку
    post("/api/orders/{orderNumber}/reject") {
        val orderNumber = call.parameters["orderNumber"]
        println("DEBUG ROUTE: reject order called with orderNumber = '$orderNumber'")

        if (orderNumber.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("INVALID_ORDER", "Неверный номер заказа")
            )
            return@post
        }

        val success = orderRepository.rejectOrder(orderNumber)
        call.respond(mapOf("success" to success))
    }
}