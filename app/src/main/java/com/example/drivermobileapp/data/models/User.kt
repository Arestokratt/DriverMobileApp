package com.example.drivermobileapp.data.models

import java.io.Serializable

data class User(
    val id: String,
    val login: String,
    val password: String,
    val role: UserRole, // DRIVER, LOGIST, ADMIN
    val fullName: String,
    val isActive: Boolean = true
) : Serializable

enum class UserRole {
    DRIVER, LOGIST, ADMIN
}