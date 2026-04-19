package config

import java.sql.Connection
import java.sql.DriverManager

object DatabaseConfig {
    private const val URL = "jdbc:postgresql://2.26.29.31:5432/mobiledriver"
    private const val USER = "admin"
    private const val PASSWORD = "38m67iee"

    fun getConnection(): Connection {
        return DriverManager.getConnection(URL, USER, PASSWORD)
    }
}