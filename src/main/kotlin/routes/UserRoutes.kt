package routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import models.requests.LoginRequest
import models.responses.ErrorResponse
import services.UserService

fun Route.userRoutes() {
    val userService = UserService()

    get("/") {
        call.respondText("Server is running!")
    }

    get("/health") {
        call.respond(HttpStatusCode.OK, mapOf("status" to "ok"))
    }

    get("/users") {
        call.respond(userService.getAllUsers())
    }

    get("/api/users") {
        call.respond(userService.getAllUsers())
    }

    post("/login") {
        val request = call.receive<LoginRequest>()
        val user = userService.authenticate(request.login, request.password)

        if (user != null) {
            call.respond(user)
        } else {
            call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse("AUTH_FAILED", "Неверный логин или пароль")
            )
        }
    }

    post("/api/login") {
        val request = call.receive<LoginRequest>()
        val user = userService.authenticate(request.login, request.password)

        if (user != null) {
            call.respond(user)
        } else {
            call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse("AUTH_FAILED", "Неверный логин или пароль")
            )
        }
    }

    get("/users/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
        if (id == null) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("INVALID_USER_ID", "Неверный ID пользователя")
            )
            return@get
        }

        val user = userService.getUserById(id)
        if (user != null) {
            call.respond(user)
        } else {
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse("USER_NOT_FOUND", "Пользователь не найден")
            )
        }
    }

    get("/api/users/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
        if (id == null) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("INVALID_USER_ID", "Неверный ID пользователя")
            )
            return@get
        }

        val user = userService.getUserById(id)
        if (user != null) {
            call.respond(user)
        } else {
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse("USER_NOT_FOUND", "Пользователь не найден")
            )
        }
    }
}
