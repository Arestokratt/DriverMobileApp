package com.example.drivermobileapp.data.models

import com.google.gson.annotations.SerializedName

data class ShiftStartResponse(
    @SerializedName("shiftId")
    val shiftId: String,
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String
)