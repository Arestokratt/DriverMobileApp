package com.example.drivermobileapp.data.models

import java.io.Serializable

enum class OrderStatus {
    NEW,         // Новые
    IN_PROGRESS, // Текущие
    COMPLETED,   // Выполненные
    ARCHIVED     // Архив
}

data class Order(
    val id: String,
    val title: String,
    val description: String,
    val fromAddress: String,
    val toAddress: String,
    val cargoType: String,
    val weight: Double,
    val volume: Double,
    val status: OrderStatus,
    val createdBy: String, // ID логиста, создавшего заявку
    val assignedDriver: String? = null, // ID водителя, если назначен
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable