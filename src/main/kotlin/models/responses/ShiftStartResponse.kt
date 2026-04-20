package models.responses

data class ShiftStartResponse(
    val shiftId: String,
    val success: Boolean,
    val message: String
)