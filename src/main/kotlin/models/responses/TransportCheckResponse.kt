package models.responses

data class TransportCheckResponse(
    val isValid: Boolean,
    val message: String,
    val vehicleId: Int? = null,
    val carBrand: String? = null
)