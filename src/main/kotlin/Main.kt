import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import java.sql.DriverManager

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(ContentNegotiation) {
            gson()
        }

        routing {

            get("/users") {
                val users = getAllUsersFromDatabase()
                call.respond(users)
            }

            get("/") {
                call.respondText("Server is running!")
            }

            post("/login") {
                val request = call.receive<LoginRequest>()
                val user = authenticateUser(request.login, request.password)

                if (user != null) {
                    call.respond(user)
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Неверный логин или пароль")
                }
            }

            get("/users/{id}") {
                val id = call.parameters["id"]
                call.respond(mapOf("id" to id, "name" to "User"))
            }
        }
    }.start(wait = true)
}

data class LoginRequest(val login: String, val password: String)
data class UserResponse(val id: Int, val login: String, val name: String, val role: String)

// ⬇️ ЭТА ФУНКЦИЯ ДОЛЖНА БЫТЬ ТОЛЬКО ОДНА ⬇️
fun authenticateUser(login: String, password: String): UserResponse? {
    val url = "jdbc:postgresql://localhost:5432/mydatabase"
    val conn = DriverManager.getConnection(url, "myuser", "mypassword")

    try {
        val stmt = conn.prepareStatement("SELECT id, login, name, role FROM users WHERE login = ? AND password = ?")
        stmt.setString(1, login)
        stmt.setString(2, password)
        val rs = stmt.executeQuery()

        return if (rs.next()) {
            UserResponse(
                id = rs.getInt("id"),
                login = rs.getString("login"),
                name = rs.getString("name"),
                role = rs.getString("role")
            )
        } else null
    } finally {
        conn.close()
    }
}

fun getAllUsersFromDatabase(): List<UserResponse> {
    val url = "jdbc:postgresql://localhost:5432/mydatabase"
    val conn = DriverManager.getConnection(url, "myuser", "mypassword")
    val users = mutableListOf<UserResponse>()

    try {
        val stmt = conn.prepareStatement("SELECT id, login, name, role FROM users")
        val rs = stmt.executeQuery()

        while (rs.next()) {
            users.add(
                UserResponse(
                    id = rs.getInt("id"),
                    login = rs.getString("login"),
                    name = rs.getString("name"),
                    role = rs.getString("role")
                )
            )
        }
    } finally {
        conn.close()
    }

    return users
}