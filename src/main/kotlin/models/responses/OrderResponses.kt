package models.responses

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

data class OrderResponse(
    val success: Boolean,
    val order: Any?,
    val message: String?
)