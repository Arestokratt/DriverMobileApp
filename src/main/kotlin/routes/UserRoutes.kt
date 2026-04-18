package routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.http.*
import models.requests.LoginRequest
import services.UserService

fun Route.userRoutes() {
    val userService = UserService()

    get("/users") {
        val users = userService.getAllUsers()
        call.respond(users)
    }

    get("/") {
        call.respondText("Server is running!")
    }

    post("/login") {
        val request = call.receive<LoginRequest>()
        val user = userService.authenticate(request.login, request.password)

        if (user != null) {
            call.respond(user)
        } else {
            call.respond(HttpStatusCode.Unauthorized, "Неверный логин или пароль")
        }
    }

    get("/users/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
        if (id != null) {
            val user = userService.getUserById(id)
            if (user != null) {
                call.respond(user)
            } else {
                call.respond(HttpStatusCode.NotFound, "Пользователь не найден")
            }
        } else {
            call.respond(HttpStatusCode.BadRequest, "Неверный ID")
        }
    }
}