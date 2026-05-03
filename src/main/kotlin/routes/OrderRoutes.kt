package routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import models.requests.CompleteStageRequest
import models.responses.ErrorResponse
import services.OrderService

fun Route.orderRoutes() {
    val orderService = OrderService()

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

        if (response.success) {
            call.respond(response)
        } else {
            call.respond(HttpStatusCode.BadRequest, response)
        }
    }

    // Получить заявки водителя (добавьте позже)
    get("/api/driver/{driverId}/orders") {
        val driverId = call.parameters["driverId"]
        if (driverId.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("INVALID_DRIVER", "Неверный ID водителя")
            )
            return@get
        }
        // TODO: реализовать
        call.respond(listOf<Any>())
    }
}