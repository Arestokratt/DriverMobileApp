package models.requests

data class ShiftStartRequest(
    val userId: String,
    val driverLicense: String,
    val licensePlate: String,
    val startTime: Long
)