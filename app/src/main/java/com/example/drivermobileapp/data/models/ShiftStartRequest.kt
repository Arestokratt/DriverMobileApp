package com.example.drivermobileapp.data.models

import com.google.gson.annotations.SerializedName

data class ShiftStartRequest(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("driverLicense")
    val driverLicense: String,
    @SerializedName("licensePlate")
    val licensePlate: String,
    @SerializedName("startTime")
    val startTime: Long
)