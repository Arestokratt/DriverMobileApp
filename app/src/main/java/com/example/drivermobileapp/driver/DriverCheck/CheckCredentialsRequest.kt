package com.example.drivermobileapp.driver.DriverCheck

data class CheckCredentialsRequest(
    val carBrand: String,
    val driverLicense: String
)