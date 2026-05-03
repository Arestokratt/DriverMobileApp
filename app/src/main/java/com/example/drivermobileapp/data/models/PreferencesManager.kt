package com.example.drivermobileapp.data.local

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("order_cache", Context.MODE_PRIVATE)

    // Сохраняем текущий этап
    fun saveCurrentStage(orderNumber: String, stage: Int) {
        prefs.edit().putInt("current_stage_$orderNumber", stage).apply()
    }

    fun getCurrentStage(orderNumber: String): Int {
        return prefs.getInt("current_stage_$orderNumber", 1)
    }

    // Сохраняем статус конкретного этапа
    fun saveStageCompleted(orderNumber: String, stageNumber: Int, isCompleted: Boolean) {
        prefs.edit().putBoolean("stage_${orderNumber}_${stageNumber}", isCompleted).apply()
    }

    fun isStageCompleted(orderNumber: String, stageNumber: Int): Boolean {
        return prefs.getBoolean("stage_${orderNumber}_${stageNumber}", false)
    }

    fun saveCurrentDriverId(driverId: String) {
        println("DEBUG PREFS: saveCurrentDriverId = '$driverId'")
        prefs.edit().putString("current_driver_id", driverId).apply()
    }

    fun getCurrentDriverId(): String {
        val id = prefs.getString("current_driver_id", "") ?: ""
        println("DEBUG PREFS: getCurrentDriverId = '$id'")
        return id
    }

    // Очистить кэш заказа
    fun clearOrderCache(orderNumber: String) {
        prefs.edit().remove("current_stage_$orderNumber").apply()
        for (i in 1..7) {
            prefs.edit().remove("stage_${orderNumber}_$i").apply()
        }
    }
}