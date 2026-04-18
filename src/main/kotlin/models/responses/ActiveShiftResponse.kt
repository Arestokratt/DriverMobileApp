package models.responses

data class ActiveShiftResponse(
    val shiftId: Int,
    val driverLicense: String,
    val licensePlate: String,
    val startTime: Long,
    val carBrand: String?
)