package com.example.drivermobileapp.data.models

data class ShiftStartRequest(
    val userId: String,
    val driverLicense: String,   // ← ВУ
    val licensePlate: String,    // ← Гос. номер
    val startTime: Long
)