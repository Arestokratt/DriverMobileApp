package com.example.drivermobileapp.data.models

data class ShiftStartRequest(
    val userId: Int,
    val carBrand: String,
    val licensePlate: String,
    val startTime: Long
)