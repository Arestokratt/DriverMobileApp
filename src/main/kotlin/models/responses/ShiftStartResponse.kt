package models.responses

data class ShiftStartResponse(
    val shiftId: Int,
    val success: Boolean,
    val message: String
)