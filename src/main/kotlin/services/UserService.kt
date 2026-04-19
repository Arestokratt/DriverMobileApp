package services

import models.responses.UserResponse
import repositories.UserRepository

class UserService {
    private val userRepository = UserRepository()

    fun authenticate(login: String, password: String): UserResponse? {
        val normalizedLogin = login.trim()
        val normalizedPassword = password.trim()

        if (normalizedLogin.isBlank() || normalizedPassword.isBlank()) {
            return null
        }

        return userRepository.authenticate(normalizedLogin, normalizedPassword)
    }

    fun getAllUsers(): List<UserResponse> {
        return userRepository.getAllUsers()
    }

    fun getUserById(userId: Int): UserResponse? {
        return userRepository.getUserById(userId)
    }
}
