package models.responses

data class UserResponse(
    val id: Int,
    val login: String,
    val name: String,
    val role: String
)