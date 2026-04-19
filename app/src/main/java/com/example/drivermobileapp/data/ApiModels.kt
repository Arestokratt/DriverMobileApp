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