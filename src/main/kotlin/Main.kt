import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import io.ktor.server.routing.routing
import routes.userRoutes
import routes.shiftRoutes

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(ContentNegotiation) {
            gson()
        }

        routing {
            userRoutes()    // Все маршруты для пользователей
            shiftRoutes()   // Все маршруты для смен
        }
    }.start(wait = true)
}