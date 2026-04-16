package com.example.drivermobileapp.data.models

import java.util.Locale

object OrderPriority {
    const val NORMAL = "NORMAL"
    const val URGENT = "URGENT"
    const val HIGH = "HIGH"

    fun spinnerItems(): Array<String> = arrayOf("Обычный", "Срочный", "Наивысший")

    fun fromSpinnerPosition(position: Int): String {
        return when (position) {
            1 -> URGENT
            2 -> HIGH
            else -> NORMAL
        }
    }

    fun toSpinnerPosition(priority: String): Int {
        return when (normalize(priority)) {
            URGENT -> 1
            HIGH -> 2
            else -> 0
        }
    }

    fun label(priority: String): String {
        return when (normalize(priority)) {
            URGENT -> "Срочный"
            HIGH -> "Наивысший"
            else -> "Обычный"
        }
    }

    fun marker(priority: String): String {
        return when (normalize(priority)) {
            URGENT -> "[СРОЧНО]"
            HIGH -> "[ПРИОРИТЕТ]"
            else -> ""
        }
    }

    fun rank(priority: String): Int {
        return when (normalize(priority)) {
            HIGH -> 2
            URGENT -> 1
            else -> 0
        }
    }

    private fun normalize(priority: String): String {
        return priority.uppercase(Locale.ROOT)
    }
}
