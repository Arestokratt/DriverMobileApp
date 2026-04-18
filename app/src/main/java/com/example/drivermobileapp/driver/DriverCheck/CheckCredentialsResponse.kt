package com.example.drivermobileapp.driver.DriverCheck

data class CheckCredentialsResponse(
    val success: Boolean,
    val message: String?,
    val data: CheckData?
)

data class CheckData(
    val isCarValid: Boolean,
    val isLicenseValid: Boolean,
    val carName: String?,
    val driverName: String?
)