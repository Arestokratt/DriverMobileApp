package com.example.drivermobileapp.data.repository

import com.example.drivermobileapp.data.models.Order1C

class Order1CRepository {

    // Имитация получения данных из 1С
    fun getNewOrdersFrom1C(): List<Order1C> {
        // В реальном приложении здесь будет HTTP запрос к API 1С
        return listOf(
            Order1C("1C001", "2023-0001", System.currentTimeMillis() - 86400000,
                "ООО 'Ромашка'", "ул. Ленина 10", "ул. Пушкина 25",
                "Оборудование", 150.0, 2.5),

            Order1C("1C002", "2023-0002", System.currentTimeMillis() - 172800000,
                "ИП Сидоров", "пр. Мира 15", "ул. Садовая 8",
                "Мебель", 300.0, 8.0)
        )
    }

    // Имитация поиска в 1С
    fun searchOrdersIn1C(query: String, filterType: Int): List<Order1C> {
        val allOrders = getNewOrdersFrom1C()
        return allOrders.filter { order ->
            when (filterType) {
                1 -> { // По дате
                    val date = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
                        .format(java.util.Date(order.orderDate))
                    date.contains(query, ignoreCase = true)
                }
                2 -> order.orderNumber.contains(query, ignoreCase = true)
                3 -> order.clientName.contains(query, ignoreCase = true)
                else -> order.orderNumber.contains(query, ignoreCase = true) ||
                        order.clientName.contains(query, ignoreCase = true)
            }
        }
    }
}