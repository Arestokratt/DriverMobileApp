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
import java.sql.SQLException

fun main() {
    println("🚀 Starting Ktor server on port 8080...")

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(ContentNegotiation) {
            gson()
        }

        routing {
            get("/") {
                call.respondText("Server is running!")
            }

            get("/health") {
                call.respond(mapOf("status" to "ok", "timestamp" to System.currentTimeMillis()))
            }

            post("/login") {
                try {
                    val request = call.receive<LoginRequest>()
                    println("=== LOGIN REQUEST ===")
                    println("Login: ${request.login}")
                    println("===================")

                    val user = authenticateUser(request.login, request.password)

                    if (user != null) {
                        println("✅ User found: ${user.name}")
                        call.respond(user)
                    } else {
                        println("❌ User NOT found for login: ${request.login}")
                        call.respond(HttpStatusCode.Unauthorized, mapOf(
                            "error" to "Неверный логин или пароль"
                        ))
                    }
                } catch (e: Exception) {
                    println("🔥 ERROR: ${e.message}")
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, mapOf(
                        "error" to "Внутренняя ошибка сервера: ${e.message}"
                    ))
                }
            }

            get("/drivers") {
                try {
                    val drivers = getAllDrivers()
                    call.respond(drivers)
                } catch (e: Exception) {
                    println("Error in /drivers: ${e.message}")
                    call.respond(HttpStatusCode.InternalServerError, mapOf(
                        "error" to e.message
                    ))
                }
            }

            get("/drivers/{id}") {
                try {
                    val id = call.parameters["id"]
                    val driver = getDriverById(id)
                    if (driver != null) {
                        call.respond(driver)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Driver not found"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }

            get("/orders/driver/{driverId}") {
                try {
                    val driverId = call.parameters["driverId"]
                    val orders = getOrdersByDriver(driverId)
                    call.respond(orders)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }

            get("/orders/active") {
                try {
                    val orders = getActiveOrders()
                    call.respond(orders)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }

            get("/orders/{orderId}/stages") {
                try {
                    val orderId = call.parameters["orderId"]
                    val stages = getOrderStages(orderId)
                    call.respond(stages)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }

            // Тестовый endpoint для проверки БД
            get("/test-db") {
                try {
                    val conn = getDatabaseConnection()
                    val stmt = conn.createStatement()
                    val rs = stmt.executeQuery("SELECT COUNT(*) FROM users")
                    rs.next()
                    val userCount = rs.getInt(1)
                    conn.close()

                    call.respond(mapOf(
                        "status" to "ok",
                        "users_count" to userCount,
                        "message" to "Database connection successful"
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf(
                        "error" to e.message
                    ))
                }
            }
        }
    }.start(wait = true)
}

// Data classes
data class LoginRequest(val login: String, val password: String)

data class UserResponse(
    val id: String,
    val login: String,
    val name: String,
    val role: String
)

data class DriverResponse(
    val id: String,
    val firstName: String,
    val lastName: String,
    val middleName: String,
    val phoneNumber: String,
    val passportData: String,
    val driverLicenseNumber: String,
    val rating: Double,
    val isActive: Boolean
) {
    val fullName: String get() = "$lastName $firstName $middleName".trim()
}

data class OrderResponse(
    val id: String,
    val orderNumber: String,
    val clientName: String,
    val fromAddress: String,
    val toAddress: String,
    val cargoType: String,
    val weight: Double,
    val status: String,
    val priority: String,
    val assignedDriverId: String?
)

// ЕДИНАЯ функция для получения соединения с БД
fun getDatabaseConnection(): java.sql.Connection {
    val dbHost = "localhost"
    val dbPort = "5432"
    val dbName = "mydatabase"
    val dbUser = "myuser"
    val dbPassword = "mypassword"

    val url = "jdbc:postgresql://$dbHost:$dbPort/$dbName"
    println("🔗 Connecting to: $url")

    return DriverManager.getConnection(url, dbUser, dbPassword)
}

// Аутентификация пользователя
fun authenticateUser(login: String, password: String): UserResponse? {
    var conn: java.sql.Connection? = null
    var stmt: java.sql.PreparedStatement? = null
    var rs: java.sql.ResultSet? = null

    return try {
        conn = getDatabaseConnection()
        println("✅ Connected to database")

        stmt = conn.prepareStatement("""
            SELECT id, login, full_name, role 
            FROM users 
            WHERE login = ? AND is_active = true
        """)
        stmt.setString(1, login)
        rs = stmt.executeQuery()

        if (rs.next()) {
            UserResponse(
                id = rs.getString("id"),
                login = rs.getString("login"),
                name = rs.getString("full_name"),
                role = rs.getString("role").lowercase()
            )
        } else {
            println("❌ User not found: $login")
            null
        }
    } catch (e: SQLException) {
        println("❌ Database error: ${e.message}")
        e.printStackTrace()
        null
    } finally {
        rs?.close()
        stmt?.close()
        conn?.close()
    }
}

// Получить всех водителей
fun getAllDrivers(): List<DriverResponse> {
    var conn: java.sql.Connection? = null
    var stmt: java.sql.Statement? = null
    var rs: java.sql.ResultSet? = null

    return try {
        conn = getDatabaseConnection()
        stmt = conn.createStatement()
        rs = stmt.executeQuery("""
            SELECT id, first_name, last_name, middle_name, 
                   phone_number, passport_data, driver_license_number, 
                   rating, is_active
            FROM drivers
            WHERE is_active = true
            ORDER BY rating DESC
        """)

        val drivers = mutableListOf<DriverResponse>()
        while (rs.next()) {
            drivers.add(
                DriverResponse(
                    id = rs.getString("id"),
                    firstName = rs.getString("first_name"),
                    lastName = rs.getString("last_name"),
                    middleName = rs.getString("middle_name") ?: "",
                    phoneNumber = rs.getString("phone_number"),
                    passportData = rs.getString("passport_data") ?: "",
                    driverLicenseNumber = rs.getString("driver_license_number"),
                    rating = rs.getDouble("rating"),
                    isActive = rs.getBoolean("is_active")
                )
            )
        }
        drivers
    } catch (e: SQLException) {
        println("❌ Error getting drivers: ${e.message}")
        e.printStackTrace()
        emptyList()
    } finally {
        rs?.close()
        stmt?.close()
        conn?.close()
    }
}

// Получить водителя по ID
fun getDriverById(id: String?): DriverResponse? {
    if (id == null) return null

    var conn: java.sql.Connection? = null
    var stmt: java.sql.PreparedStatement? = null
    var rs: java.sql.ResultSet? = null

    return try {
        conn = getDatabaseConnection()
        stmt = conn.prepareStatement("""
            SELECT id, first_name, last_name, middle_name, 
                   phone_number, passport_data, driver_license_number, 
                   rating, is_active
            FROM drivers
            WHERE id = ?::uuid AND is_active = true
        """)
        stmt.setString(1, id)
        rs = stmt.executeQuery()

        if (rs.next()) {
            DriverResponse(
                id = rs.getString("id"),
                firstName = rs.getString("first_name"),
                lastName = rs.getString("last_name"),
                middleName = rs.getString("middle_name") ?: "",
                phoneNumber = rs.getString("phone_number"),
                passportData = rs.getString("passport_data") ?: "",
                driverLicenseNumber = rs.getString("driver_license_number"),
                rating = rs.getDouble("rating"),
                isActive = rs.getBoolean("is_active")
            )
        } else null
    } catch (e: SQLException) {
        println("❌ Error getting driver by id: ${e.message}")
        e.printStackTrace()
        null
    } finally {
        rs?.close()
        stmt?.close()
        conn?.close()
    }
}

// Получить заказы водителя
fun getOrdersByDriver(driverId: String?): List<OrderResponse> {
    if (driverId == null) return emptyList()

    var conn: java.sql.Connection? = null
    var stmt: java.sql.PreparedStatement? = null
    var rs: java.sql.ResultSet? = null

    return try {
        conn = getDatabaseConnection()
        stmt = conn.prepareStatement("""
            SELECT id, order_number, client_name, from_address, to_address,
                   cargo_type, weight, status, priority, assigned_driver_id
            FROM orders
            WHERE assigned_driver_id = ?::uuid
            ORDER BY order_date DESC
        """)
        stmt.setString(1, driverId)
        rs = stmt.executeQuery()

        val orders = mutableListOf<OrderResponse>()
        while (rs.next()) {
            orders.add(
                OrderResponse(
                    id = rs.getString("id"),
                    orderNumber = rs.getString("order_number"),
                    clientName = rs.getString("client_name"),
                    fromAddress = rs.getString("from_address"),
                    toAddress = rs.getString("to_address"),
                    cargoType = rs.getString("cargo_type") ?: "",
                    weight = rs.getDouble("weight"),
                    status = rs.getString("status"),
                    priority = rs.getString("priority"),
                    assignedDriverId = rs.getString("assigned_driver_id")
                )
            )
        }
        orders
    } catch (e: SQLException) {
        println("❌ Error getting orders by driver: ${e.message}")
        e.printStackTrace()
        emptyList()
    } finally {
        rs?.close()
        stmt?.close()
        conn?.close()
    }
}

// Получить активные заказы
fun getActiveOrders(): List<OrderResponse> {
    var conn: java.sql.Connection? = null
    var stmt: java.sql.Statement? = null
    var rs: java.sql.ResultSet? = null

    return try {
        conn = getDatabaseConnection()
        stmt = conn.createStatement()
        rs = stmt.executeQuery("""
            SELECT id, order_number, client_name, from_address, to_address,
                   cargo_type, weight, status, priority, assigned_driver_id
            FROM orders
            WHERE status IN ('NEW', 'ACCEPTED', 'IN_PROGRESS')
            ORDER BY 
                CASE priority 
                    WHEN 'HIGH' THEN 1
                    WHEN 'URGENT' THEN 2
                    ELSE 3
                END,
                order_date DESC
        """)

        val orders = mutableListOf<OrderResponse>()
        while (rs.next()) {
            orders.add(
                OrderResponse(
                    id = rs.getString("id"),
                    orderNumber = rs.getString("order_number"),
                    clientName = rs.getString("client_name"),
                    fromAddress = rs.getString("from_address"),
                    toAddress = rs.getString("to_address"),
                    cargoType = rs.getString("cargo_type") ?: "",
                    weight = rs.getDouble("weight"),
                    status = rs.getString("status"),
                    priority = rs.getString("priority"),
                    assignedDriverId = rs.getString("assigned_driver_id")
                )
            )
        }
        orders
    } catch (e: SQLException) {
        println("❌ Error getting active orders: ${e.message}")
        e.printStackTrace()
        emptyList()
    } finally {
        rs?.close()
        stmt?.close()
        conn?.close()
    }
}

// Получить этапы заказа
fun getOrderStages(orderId: String?): List<Map<String, Any>> {
    if (orderId == null) return emptyList()

    var conn: java.sql.Connection? = null
    var stmt: java.sql.PreparedStatement? = null
    var rs: java.sql.ResultSet? = null

    return try {
        conn = getDatabaseConnection()
        stmt = conn.prepareStatement("""
            SELECT stage_number, stage_name, is_completed, arrived_time, 
                   departed_time, driver_notes
            FROM order_stages
            WHERE order_id = ?::uuid
            ORDER BY stage_number
        """)
        stmt.setString(1, orderId)
        rs = stmt.executeQuery()

        val stages = mutableListOf<Map<String, Any>>()
        while (rs.next()) {
            stages.add(
                mapOf(
                    "stageNumber" to rs.getInt("stage_number"),
                    "stageName" to rs.getString("stage_name"),
                    "isCompleted" to rs.getBoolean("is_completed"),
                    "arrivedTime" to (rs.getTimestamp("arrived_time")?.time ?: 0L),
                    "departedTime" to (rs.getTimestamp("departed_time")?.time ?: 0L),
                    "driverNotes" to (rs.getString("driver_notes") ?: "")
                )
            )
        }
        stages
    } catch (e: SQLException) {
        println("❌ Error getting order stages: ${e.message}")
        e.printStackTrace()
        emptyList()
    } finally {
        rs?.close()
        stmt?.close()
        conn?.close()
    }
}