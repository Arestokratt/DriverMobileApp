package com.example.drivermobileapp.data.models

import java.io.Serializable

data class Driver(
    val id: String,
    val firstName: String,
    val lastName: String,
    val middleName: String,
    val phoneNumber: String,
    val passportData: String,
    val driverLicenseNumber: String,
    val rating: Double,
    val photoUrl: String = "",
    val isActive: Boolean = true,
    val registrationDate: Long = System.currentTimeMillis()
) : Serializable {
    val fullName: String
        get() = "$lastName $firstName $middleName"
}

data class Vehicle(
    val id: String,
    val driverId: String,
    val brand: String,
    val model: String,
    val licensePlate: String,
    val loadCapacity: Double, // грузоподъемность в кг
    val volume: Double, // объем в м³
    val vehicleType: String
) : Serializable

data class DriverShift(
    val id: String,
    val driverId: String,
    val startTime: Long,
    val endTime: Long,
    val vehicleId: String,
    val status: ShiftStatus = ShiftStatus.COMPLETED
) : Serializable

enum class ShiftStatus {
    ACTIVE, COMPLETED, CANCELLED
}