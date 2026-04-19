package config

import java.sql.Connection
import java.sql.DriverManager

object DatabaseConfig {
    private val url: String
        get() = System.getenv("DB_URL") ?: "jdbc:postgresql://2.26.29.31:5432/mobiledriver"

    private val user: String
        get() = System.getenv("DB_USER") ?: "admin"

    private val password: String
        get() = System.getenv("DB_PASSWORD") ?: "38m67iee"

    fun getConnection(): Connection {
        return DriverManager.getConnection(url, user, password)
    }
}
