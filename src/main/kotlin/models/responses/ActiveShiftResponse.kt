package models.responses

data class ActiveShiftResponse(
    val shiftId: String,
    val driverLicense: String,
    val licensePlate: String,
    val startTime: Long,
    val carBrand: String?
)