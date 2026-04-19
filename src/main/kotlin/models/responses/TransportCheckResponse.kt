package models.responses

data class TransportCheckResponse(
    val isValid: Boolean,
    val message: String,
    val vehicleId: String? = null,
    val carBrand: String? = null
)