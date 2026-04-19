import io.ktor.http.HttpStatusCode
import io.ktor.serialization.gson.gson
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import java.sql.SQLException
import models.responses.ErrorResponse
import routes.shiftRoutes
import routes.userRoutes

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(ContentNegotiation) {
            gson()
        }

        install(StatusPages) {
            exception<BadRequestException> { call, cause ->
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        error = "INVALID_REQUEST",
                        message = cause.message ?: "Некорректное тело запроса"
                    )
                )
            }

            exception<NumberFormatException> { call, _ ->
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        error = "INVALID_REQUEST",
                        message = "Одно из числовых полей заполнено неверно"
                    )
                )
            }

            exception<SQLException> { call, cause ->
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(
                        error = "DATABASE_ERROR",
                        message = "Ошибка при обращении к базе данных",
                        details = cause.message
                    )
                )
            }

            exception<Throwable> { call, cause ->
                println("=".repeat(80))
                println("🚨 UNHANDLED EXCEPTION 🚨")
                println("=".repeat(80))
                println("Message: ${cause.message}")
                println("Type: ${cause.javaClass.simpleName}")
                println("\nFull stack trace:")
                cause.printStackTrace(System.out)  // Это напечатает полную ошибку
                println("=".repeat(80))

                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(
                        error = "INTERNAL_SERVER_ERROR",
                        message = "Внутренняя ошибка сервера",
                        details = cause.message  // Временно для отладки
                    )
                )
            }
        }

        routing {
            userRoutes()
            shiftRoutes()
        }
    }.start(wait = true)
}
