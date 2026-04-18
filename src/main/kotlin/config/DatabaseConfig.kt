package config

import java.sql.Connection
import java.sql.DriverManager

object DatabaseConfig {
    private const val URL = "jdbc:postgresql://localhost:5432/mydatabase"
    private const val USER = "myuser"
    private const val PASSWORD = "mypassword"

    fun getConnection(): Connection {
        return DriverManager.getConnection(URL, USER, PASSWORD)
    }
}