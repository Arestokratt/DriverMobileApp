package models.entities

import java.sql.Timestamp

data class Order(
    val id: Long,
    val orderNumber: String,
    val orderDate: Timestamp,

    // Данные этапа 1
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

    // Назначения
    val assignedDriverId: String?,
    val assignedDriverName: String?,

    // Статус
    val status: String,
    val notes: String?,

    val createdAt: Timestamp,
    val updatedAt: Timestamp
)

data class OrderStage(
    val id: Long,
    val orderId: Long,
    val stageNumber: Int,
    val status: String, // PENDING, IN_PROGRESS, COMPLETED
    val startedAt: Timestamp?,
    val completedAt: Timestamp?,
    val driverNotes: String?,
    val arrivalTime: Timestamp?,
    val departureTime: Timestamp?,
    val gpsLatitude: Double?,
    val gpsLongitude: Double?,
    val photoUrl: String?
)

data class StageTemplate(
    val stageNumber: Int,
    val stageName: String,
    val stageNameShort: String,
    val description: String?,
    val isVisibleToDriver: Boolean,
    val sortOrder: Int
)