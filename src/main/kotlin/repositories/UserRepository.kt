package repositories

import config.DatabaseConfig
import models.responses.UserResponse
import java.sql.PreparedStatement

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

    fun getUserById(userId: Int): UserResponse? {
        val query = "SELECT id, login, name, role FROM users WHERE id = ?"

        return DatabaseConfig.getConnection().use { conn ->
            conn.prepareStatement(query).use { stmt ->
                stmt.setInt(1, userId)
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
}