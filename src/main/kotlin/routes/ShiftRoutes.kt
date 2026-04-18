package routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.http.*
import models.requests.*
import services.ShiftService

fun Route.shiftRoutes() {
    val shiftService = ShiftService()

    // Проверка транспорта
    post("/api/shift/check-transport") {
        val request = call.receive<TransportCheckRequest>()
        val result = shiftService.checkTransport(request)
        call.respond(result)
    }

    // Начало смены
    post("/api/shift/start") {
        val request = call.receive<ShiftStartRequest>()
        val result = shiftService.startShift(request)

        if (result.success) {
            call.respond(result)
        } else {
            call.respond(HttpStatusCode.BadRequest, result)
        }
    }

    // Завершение смены
    post("/api/shift/end") {
        val request = call.receive<ShiftEndRequest>()
        val result = shiftService.endShift(request)

        if (result.success) {
            call.respond(result)
        } else {
            call.respond(HttpStatusCode.BadRequest, result)
        }
    }

    // Получить активную смену
    get("/api/shift/active/{userId}") {
        val userId = call.parameters["userId"]?.toIntOrNull()
        if (userId != null) {
            val shift = shiftService.getActiveShift(userId)
            if (shift != null) {
                call.respond(shift)
            } else {
                call.respond(HttpStatusCode.NotFound, "Активная смена не найдена")
            }
        } else {
            call.respond(HttpStatusCode.BadRequest, "Неверный ID пользователя")
        }
    }
}