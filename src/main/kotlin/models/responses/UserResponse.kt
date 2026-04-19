package models.responses

data class UserResponse(
    val id: String,
    val login: String,
    val name: String,
    val role: String
)