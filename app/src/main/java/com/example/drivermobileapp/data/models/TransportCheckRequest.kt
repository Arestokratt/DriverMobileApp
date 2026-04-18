package com.example.drivermobileapp.data.models

data class TransportCheckRequest(
    val driverLicense: String,  // ← ВУ
    val licensePlate: String     // ← Гос. номер
)