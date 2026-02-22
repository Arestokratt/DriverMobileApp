package com.example.drivermobileapp.data.models

import java.io.Serializable

data class DocumentItem(
    val id: String,
    val fileName: String,
    val documentType: String,
    val description: String,
    val timestamp: Long,
    val fileSize: String = "",
    val documentUrl: String = "" // URL или путь к документу
) : Serializable