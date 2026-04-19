package repositories

import config.DatabaseConfig
import java.util.UUID

data class Driver(
    val id: String,
    val driverLicense: String,
    val userLogin: String
)

data class CreateDriverRequest(
    val userLogin: String,
    val driverLicense: String
)

class DriverRepository {

    fun createDriver(request: CreateDriverRequest): Driver? {
        val query = """
            INSERT INTO drivers ("driverLicense", user_login)
            VALUES (?, ?)
            RETURNING id, "driverLicense", user_login
        """

        return try {
            DatabaseConfig.getConnection().use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, request.driverLicense)
                    stmt.setString(2, request.userLogin)

                    val rs = stmt.executeQuery()

                    if (rs.next()) {
                        Driver(
                            id = rs.getString("id"),
                            driverLicense = rs.getString("driverLicense"),
                            userLogin = rs.getString("user_login")
                        )
                    } else null
                }
            }
        } catch (e: Exception) {
            println("Error in createDriver: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    fun findDriverByLogin(userLogin: String): Driver? {
        val query = """
            SELECT id, "driverLicense", user_login
            FROM drivers 
            WHERE user_login = ?
        """

        return DatabaseConfig.getConnection().use { conn ->
            conn.prepareStatement(query).use { stmt ->
                stmt.setString(1, userLogin)
                val rs = stmt.executeQuery()

                if (rs.next()) {
                    Driver(
                        id = rs.getString("id"),
                        driverLicense = rs.getString("driverLicense"),
                        userLogin = rs.getString("user_login")
                    )
                } else null
            }
        }
    }

    fun getAllDriversWithDetails(): List<Map<String, Any?>> {
        val query = """
            SELECT 
                u.login,
                u.name,
                u.is_active,
                d."driverLicense" as driver_license
            FROM users u
            LEFT JOIN drivers d ON u.login = d.user_login
            WHERE u.role = 'DRIVER'
        """

        return DatabaseConfig.getConnection().use { conn ->
            conn.prepareStatement(query).use { stmt ->
                val rs = stmt.executeQuery()
                val drivers = mutableListOf<Map<String, Any?>>()

                while (rs.next()) {
                    drivers.add(
                        mapOf(
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