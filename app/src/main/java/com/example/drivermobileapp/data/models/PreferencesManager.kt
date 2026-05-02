package com.example.drivermobileapp.data.local

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("order_progress", Context.MODE_PRIVATE)

    fun saveCurrentStage(orderNumber: String, stage: Int) {
        prefs.edit().putInt("stage_$orderNumber", stage).apply()
    }

    fun getCurrentStage(orderNumber: String): Int {
        return prefs.getInt("stage_$orderNumber", 1) // По умолчанию 1
    }

    fun clearOrderProgress(orderNumber: String) {
        prefs.edit().remove("stage_$orderNumber").apply()
    }
}