package routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import models.requests.ShiftEndRequest
import models.requests.ShiftStartRequest
import models.requests.TransportCheckRequest
import models.responses.ErrorResponse
import services.ShiftService

fun Route.shiftRoutes() {
    val shiftService = ShiftService()

    post("/api/shift/check-transport") {
        val request = call.receive<TransportCheckRequest>()
        call.respond(shiftService.checkTransport(request))
    }

    post("/api/shift/start") {
        println("📍 ROUTE: /api/shift/start CALLED")
        val request = call.receive<ShiftStartRequest>()
        println("📍 ROUTE: Received request: $request")
        val result = shiftService.startShift(request)
        println("📍 ROUTE: Result: $result")

        if (result.success) {
            call.respond(result)
        } else {
            call.respond(HttpStatusCode.BadRequest, result)
        }
    }

    post("/api/shift/end") {
        val request = call.receive<ShiftEndRequest>()
        val result = shiftService.endShift(request)

        if (result.success) {
            call.respond(result)
        } else {
            call.respond(HttpStatusCode.BadRequest, result)
        }
    }

    get("/api/shift/active/{userId}") {
        val userId = call.parameters["userId"]?.toIntOrNull()
        if (userId == null) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("INVALID_USER_ID", "Неверный ID пользователя")
            )
            return@get
        }

        val shift = shiftService.getActiveShift(userId.toString())
        if (shift != null) {
            call.respond(shift)
        } else {
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse("ACTIVE_SHIFT_NOT_FOUND", "Активная смена не найдена")
            )
        }
    }
}
