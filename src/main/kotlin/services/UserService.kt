package services

import repositories.DriverRepository
import repositories.Driver
import repositories.CreateUserRequest
import repositories.CreateDriverRequest
import models.responses.UserResponse
import repositories.UserRepository

class UserService {
    private val userRepository = UserRepository()
    private val driverRepository = DriverRepository()

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

    // Получить пользователя по ID
    fun getUserById(id: String): UserResponse? {
        return userRepository.getUserById(id)
    }

    // ✅ Поиск пользователя по логину
    fun findUserByLogin(login: String): UserResponse? {
        return userRepository.findUserByLogin(login)
    }

    // ✅ Создание пользователя
    fun createUser(request: CreateUserRequest): UserResponse? {
        return userRepository.createUser(request)
    }

    // ✅ Создание водителя
    fun createDriver(request: CreateDriverRequest): Driver? {
        return driverRepository.createDriver(request)
    }

    // ✅ Поиск водителя по логину
    fun findDriverByLogin(userLogin: String): Driver? {
        return driverRepository.findDriverByLogin(userLogin)
    }

    // ✅ Получить всех водителей с деталями
    fun getAllDriversWithDetails(): List<Map<String, Any?>> {
        return driverRepository.getAllDriversWithDetails()
    }

    // ✅ Получить всех пользователей с ролью DRIVER
    fun getAllDrivers(): List<UserResponse> {
        return userRepository.getAllDrivers()
    }
    fun getAllDriversWithLicense(): List<Map<String, Any?>> {
        return driverRepository.getAllDriversWithLicense()
    }
}
