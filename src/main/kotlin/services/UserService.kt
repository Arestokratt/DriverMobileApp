package services

import repositories.UserRepository
import models.responses.UserResponse

class UserService {
    private val userRepository = UserRepository()

    fun authenticate(login: String, password: String): UserResponse? {
        if (login.isBlank() || password.isBlank()) {
            return null
        }
        return userRepository.authenticate(login, password)
    }

    fun getAllUsers(): List<UserResponse> {
        return userRepository.getAllUsers()
    }

    fun getUserById(userId: Int): UserResponse? {
        return userRepository.getUserById(userId)
    }
}