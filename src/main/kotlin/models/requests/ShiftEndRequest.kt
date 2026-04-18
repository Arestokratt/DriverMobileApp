package models.requests

data class ShiftEndRequest(
    val shiftId: Int,
    val endTime: Long? = null
)