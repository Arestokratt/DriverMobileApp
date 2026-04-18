package com.example.drivermobileapp.data.models

object OrderPriorityStore {
    private val priorities = mutableMapOf<String, String>()

    fun getPriority(orderId: String): String? = priorities[orderId]

    fun setPriority(orderId: String, priority: String) {
        priorities[orderId] = priority
    }
}
