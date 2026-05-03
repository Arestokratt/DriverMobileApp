package models.responses

// Новый класс для списка заявок
data class OrderListResponse(
    val id: String,
    val orderNumber: String,
    val status: String,
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

// ========== ДОБАВЬ ЭТИ КЛАССЫ ==========

data class OrderStagesResponse(
    val orderNumber: String,
    val currentStage: Int,
    val stages: Map<Int, StageStatusResponse>,
    val lastUpdated: Long
)

data class StageStatusResponse(
    val isCompleted: Boolean,
    val completedAt: Long? = null
)

data class CompleteStageResponse(
    val success: Boolean,
    val nextStage: Int,
    val currentStage: Int,
    val allStagesCompleted: Boolean,
    val message: String?
)