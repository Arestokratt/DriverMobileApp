package routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.http.*
import models.requests.LoginRequest
import services.UserService
import repositories.CreateUserRequest
import repositories.CreateDriverRequest

fun Route.userRoutes() {
    val userService = UserService()

    // Существующие маршруты
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
        val id = call.parameters["id"]
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

    // ✅ НОВЫЙ МАРШРУТ: Создание пользователя
    post("/api/users/create") {
        try {
            val request = call.receive<CreateUserRequest>()
            println("Received create user request: $request")

            val existingUser = userService.findUserByLogin(request.login)
            if (existingUser != null) {
                call.respond(HttpStatusCode.Conflict, "Пользователь с таким логином уже существует")
                return@post
            }

            val user = userService.createUser(request)
            if (user != null) {
                call.respond(HttpStatusCode.Created, user)
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Ошибка создания пользователя")
            }
        } catch (e: Exception) {
            println("Error in create user: ${e.message}")
            e.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, "Ошибка: ${e.message}")
        }
    }

    // ✅ НОВЫЙ МАРШРУТ: Создание водителя
    post("/api/drivers/create") {
        try {
            val request = call.receive<CreateDriverRequest>()
            println("Received create driver request: $request")

            val user = userService.findUserByLogin(request.userLogin)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, "Пользователь с логином ${request.userLogin} не найден")
                return@post
            }

            val existingDriver = userService.findDriverByLogin(request.userLogin)
            if (existingDriver != null) {
                call.respond(HttpStatusCode.Conflict, "Водитель с таким логином уже существует")
                return@post
            }

            val driver = userService.createDriver(request)
            if (driver != null) {
                call.respond(HttpStatusCode.Created, driver)
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Ошибка создания водителя")
            }
        } catch (e: Exception) {
            println("Error in create driver: ${e.message}")
            e.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, "Ошибка: ${e.message}")
        }
    }
    // Получить список всех водителей с ВУ
    get("/api/drivers/list") {
        try {
            val drivers = userService.getAllDriversWithLicense()
            call.respond(HttpStatusCode.OK, drivers)
        } catch (e: Exception) {
            println("Error getting drivers list: ${e.message}")
            call.respond(HttpStatusCode.InternalServerError, "Ошибка получения списка водителей")
        }
    }
}