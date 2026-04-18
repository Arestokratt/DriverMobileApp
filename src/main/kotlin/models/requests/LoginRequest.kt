package models.requests

data class LoginRequest(
    val login: String,
    val password: String
)