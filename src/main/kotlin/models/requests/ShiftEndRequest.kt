package models.requests

data class ShiftEndRequest(
    val shiftId: String,
    val endTime: Long? = null
)