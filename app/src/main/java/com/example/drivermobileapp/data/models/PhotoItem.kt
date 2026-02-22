package com.example.drivermobileapp.data.models

import java.io.Serializable

data class PhotoItem(
    val id: String,
    val fileName: String,
    val description: String,
    val timestamp: Long,
    val photoUrl: String = "" // URL или путь к фото
) : Serializable