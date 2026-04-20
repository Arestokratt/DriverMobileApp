package repositories

import config.DatabaseConfig
import models.responses.UserResponse


data class CreateUserRequest(
    val login: String,
    val password: String,
    val role: String,
    val fullName: String,
    val isActive: Boolean
)

class UserRepository {

    fun authenticate(login: String, password: String): UserResponse? {
        val query = """
            SELECT id, login, name, role 
            FROM users 
            WHERE login = ? AND password = ?
        """

        return DatabaseConfig.getConnection().use { conn ->
            conn.prepareStatement(query).use { stmt ->
                stmt.setString(1, login)
                stmt.setString(2, password)
                val rs = stmt.executeQuery()

                if (rs.next()) {
                    UserResponse(
                        id = rs.getString("id"),
                        login = rs.getString("login"),
                        name = rs.getString("name"),
                        role = rs.getString("role")
                    )
                } else null
            }
        }
    }

    fun getAllUsers(): List<UserResponse> {
        val query = "SELECT id, login, name, role FROM users"

        return DatabaseConfig.getConnection().use { conn ->
            conn.prepareStatement(query).use { stmt ->
                val rs = stmt.executeQuery()
                val users = mutableListOf<UserResponse>()

                while (rs.next()) {
                    users.add(
                        UserResponse(
                            id = rs.getString("id"),
                            login = rs.getString("login"),
                            name = rs.getString("name"),
                            role = rs.getString("role")
                        )
                    )
                }
                users
            }
        }
    }

    // Получить пользователя по ID
    fun getUserById(userId: String): UserResponse? {
        val query = "SELECT id, login, name, role FROM users WHERE id = ?"

        return DatabaseConfig.getConnection().use { conn ->
            conn.prepareStatement(query).use { stmt ->
                stmt.setString(1, userId)
                val rs = stmt.executeQuery()

                if (rs.next()) {
                    UserResponse(
                        id = rs.getString("id"),
                        login = rs.getString("login"),
                        name = rs.getString("name"),
                        role = rs.getString("role")
                    )
                } else null
            }
        }
    }

    // Создание пользователя
    fun createUser(request: CreateUserRequest): UserResponse? {
        val query = """
        INSERT INTO users (login, password, role, name, is_active)
        VALUES (?, ?, ?, ?, ?)
        RETURNING id, login, name, role
    """

        return try {
            DatabaseConfig.getConnection().use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, request.login)
                    stmt.setString(2, request.password)
                    stmt.setString(3, request.role)
                    stmt.setString(4, request.fullName)
                    stmt.setBoolean(5, request.isActive)

                    val rs = stmt.executeQuery()

                    if (rs.next()) {
                        UserResponse(
                            id = rs.getString("id"),
                            login = rs.getString("login"),
                            name = rs.getString("name"),
                            role = rs.getString("role")
                        )
                    } else null
                }
            }
        } catch (e: Exception) {
            println("Error in createUser: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    fun findUserByLogin(login: String): UserResponse? {
        val query = "SELECT id, login, name, role FROM users WHERE login = ?"

        return DatabaseConfig.getConnection().use { conn ->
            conn.prepareStatement(query).use { stmt ->
                stmt.setString(1, login)
                val rs = stmt.executeQuery()

                if (rs.next()) {
                    UserResponse(
                        id = rs.getString("id"),
                        login = rs.getString("login"),
                        name = rs.getString("name"),
                        role = rs.getString("role")
                    )
                } else null
            }
        }
    }

    // Получить всех водителей (пользователей с ролью DRIVER)
    fun getAllDrivers(): List<UserResponse> {
        val query = "SELECT id, login, name, role FROM users WHERE role = 'DRIVER'"

        return DatabaseConfig.getConnection().use { conn ->
            conn.prepareStatement(query).use { stmt ->
                val rs = stmt.executeQuery()
                val drivers = mutableListOf<UserResponse>()

                while (rs.next()) {
                    drivers.add(
                        UserResponse(
                            id = rs.getString("id"),
                            login = rs.getString("login"),
                            name = rs.getString("name"),
                            role = rs.getString("role")
                        )
                    )
                }
                drivers
            }
        }
    }
    fun getAllDriversWithLicense(): List<Map<String, Any?>> {
        val query = """
        SELECT 
            u.id,
            u.login,
            u.name,
            u.is_active,
            d."driverLicense" as driver_license
        FROM users u
        INNER JOIN drivers d ON u.login = d.user_login
        WHERE u.role = 'DRIVER'
        ORDER BY u.name ASC
    """

        return DatabaseConfig.getConnection().use { conn ->
            conn.prepareStatement(query).use { stmt ->
                val rs = stmt.executeQuery()
                val drivers = mutableListOf<Map<String, Any?>>()

                while (rs.next()) {
                    drivers.add(
                        mapOf(
                            "id" to rs.getString("id"),
                            "login" to rs.getString("login"),
                            "name" to rs.getString("name"),
                            "isActive" to rs.getBoolean("is_active"),
                            "driverLicense" to rs.getString("driver_license")
                        )
                    )
                }
                drivers
            }
        }
    }
}
