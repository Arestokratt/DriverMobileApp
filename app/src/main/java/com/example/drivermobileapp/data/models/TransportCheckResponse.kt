package com.example.drivermobileapp.data.models

data class TransportCheckResponse(
    val isValid: Boolean,
    val message: String? = null
)