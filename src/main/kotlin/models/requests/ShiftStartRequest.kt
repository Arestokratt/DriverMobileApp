package models.requests

data class ShiftStartRequest(
    val userId: Int,
    val driverLicense: String,
    val licensePlate: String,
    val startTime: Long
)