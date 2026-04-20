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
import repositories.CreateUserRequest
import repositories.CreateDriverRequest

fun Route.userRoutes() {
    val userService = UserService()

    get("/users") {
        val users = userService.getAllUsers()
        call.respond(users)
    }

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
        val id = call.parameters["id"]
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
