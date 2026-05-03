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