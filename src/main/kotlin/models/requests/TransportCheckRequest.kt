package models.requests

data class TransportCheckRequest(
    val driverLicense: String,
    val licensePlate: String
)