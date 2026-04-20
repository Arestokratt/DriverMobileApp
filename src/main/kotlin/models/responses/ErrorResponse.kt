package models.responses

data class ErrorResponse(
    val error: String,
    val message: String,
    val details: String? = null
)
