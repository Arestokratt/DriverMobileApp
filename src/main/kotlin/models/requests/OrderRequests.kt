package models.requests

data class CompleteStageRequest(
    val driverId: String,
    val driverNotes: String? = null,
    val arrivalTime: Long? = null,
    val departureTime: Long? = null,
    val gpsLatitude: Double? = null,
    val gpsLongitude: Double? = null,
    val photoUrl: String? = null
)

data class CreateOrderRequest(
    val orderNumber: String,
    val emptyContainerTerminalAddress: String,
    val containerType: String,
    val containerCount: Int,
    val containerDeliveryDateTime: Long,
    val containerLoadingAddress: String,
    val cargoName: String,
    val cargoWeight: Double,
    val loadingContactPerson: String,
    val loadingContactPhone: String?,
    val departureStationName: String,
    val departureStationContact: String,
    val departureStationPhone: String?,
    val destinationStationName: String,
    val destinationStationContact: String,
    val destinationStationPhone: String?,
    val unloadingAddress: String,
    val unloadingContactPerson: String,
    val unloadingContactPhone: String?,
    val returnTerminalAddress: String,
    val assignedDriverId: String?,
    val assignedDriverName: String?,
    val notes: String?
)