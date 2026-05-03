package com.example.drivermobileapp.data.api

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("login")
    val login: String,
    @SerializedName("password")
    val password: String
)

data class UserResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("login")
    val login: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("role")
    val role: String
)
data class DriverResponse(
    val id: String,
    val login: String,
    val name: String,
    val isActive: Boolean,
    val driverLicense: String
)

//  DATA CLASSES ДЛЯ ЭТАПОВ
data class ApiStageStatus(
    @SerializedName("isCompleted")
    val isCompleted: Boolean,

    @SerializedName("completedAt")
    val completedAt: Long? = null
)

data class OrderStagesResponse(
    @SerializedName("orderNumber")
    val orderNumber: String,

    @SerializedName("currentStage")
    val currentStage: Int,

    @SerializedName("stages")
    val stages: Map<Int, ApiStageStatus>,

    @SerializedName("lastUpdated")
    val lastUpdated: Long
)

data class CompleteStageRequest(
    @SerializedName("driverId")
    val driverId: String,

    @SerializedName("driverNotes")  // ← Добавь это поле
    val driverNotes: String? = null,

    @SerializedName("completedAt")
    val completedAt: Long = System.currentTimeMillis()
)

data class CompleteStageResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("nextStage")
    val nextStage: Int,

    @SerializedName("message")
    val message: String? = null
)

data class AcceptOrderRequest(
    val driverId: String
)

data class AcceptOrderResponse(
    val success: Boolean
)

data class RejectOrderResponse(
    val success: Boolean
)

// Модель заказа от сервера
data class OrderResponse(
    val id: Long,
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