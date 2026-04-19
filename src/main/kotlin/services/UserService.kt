package services

import repositories.UserRepository
import repositories.DriverRepository
import repositories.Driver
import repositories.CreateUserRequest
import repositories.CreateDriverRequest
import models.responses.UserResponse

class UserService {
    private val userRepository = UserRepository()
    private val driverRepository = DriverRepository()

    // Аутентификация
    fun authenticate(login: String, password: String): UserResponse? {
        return userRepository.authenticate(login, password)
    }

    // Получить всех пользователей
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