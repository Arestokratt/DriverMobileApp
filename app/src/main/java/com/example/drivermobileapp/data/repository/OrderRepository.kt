package com.example.drivermobileapp.data.repositories

import OrderDriver
//import com.example.drivermobileapp.data.models.Order
//import com.example.drivermobileapp.data.models.OrderDriver

class OrderRepository {

    // TODO: Заменить на реальную работу с API/БД
    private val orders = mutableListOf<OrderDriver>()

    fun getIncomingOrders(): List<OrderDriver> {
        return orders.filter { it.status == OrderDriver.OrderStatus.NEW }
    }

    fun getMyOrders(driverId: String): List<OrderDriver> {
        return orders.filter { it.driverId == driverId && it.status == OrderDriver.OrderStatus.ACCEPTED }
    }

    fun acceptOrder(orderId: String, driverId: String): Boolean {
        val order = orders.find { it.id == orderId }
        return if (order != null) {
            // В реальном приложении здесь будет вызов API
            orders.remove(order)
            orders.add(order.copy(
                status = OrderDriver.OrderStatus.ACCEPTED,
                driverId = driverId
            ))
            true
        } else {
            false
        }
    }

    fun rejectOrder(orderId: String): Boolean {
        val order = orders.find { it.id == orderId }
        return if (order != null) {
            // В реальном приложении здесь будет вызов API
            orders.remove(order)
            true
        } else {
            false
        }
    }

    // Метод для добавления тестовых данных
    fun addTestOrders() {
        if (orders.isEmpty()) {
            orders.addAll(listOf(
                OrderDriver(
                    id = "1",
                    number = "1001",
                    status = OrderDriver.OrderStatus.NEW,
                    driverId = null,
                    customerName = "Петр Сидоров",
                    address = "ул. Ленина, д. 15",
                    createdAt = System.currentTimeMillis(),
                    plannedDeliveryTime = System.currentTimeMillis() + 3600000,
                    terminalPickupAddress = "ул. Заводская, 10",
                    containerType = "20-футовый",
                    containerCount = 2,
                    loadingAddress = "ул. Промышленная, 25",
                    cargoName = "Электроника",
                    cargoWeight = 1500.0,
                    loadingContact = "Иван +7-999-123-45-67",
                    departureStation = "Станция Москва-Товарная", // ← ДОБАВЛЕНО
                    departureContact = "Диспетчер +7-999-765-43-21",
                    destinationStation = "Станция СПб-Финляндский", // ← ДОБАВЛЕНО
                    destinationContact = "Диспетчер +7-812-123-45-67",
                    unloadingAddress = "ул. Невская, 50",
                    unloadingContact = "Мария +7-812-987-65-43",
                    terminalReturnAddress = "ул. Портовая, 5",
                    containerDeliveryTime = System.currentTimeMillis() + 86400000, // Завтра
                ),
                OrderDriver(
                    id = "2",
                    number = "1002",
                    status = OrderDriver.OrderStatus.NEW,
                    driverId = null,
                    customerName = "Мария Иванова",
                    address = "пр. Мира, д. 42",
                    createdAt = System.currentTimeMillis(),
                    plannedDeliveryTime = System.currentTimeMillis() + 7200000,
                    terminalPickupAddress = null,
                    containerType = "40-футовый",
                    containerCount = 1,
                    loadingAddress = "ул. Складская, 15",
                    cargoName = "Одежда",
                    cargoWeight = 800.0,
                    loadingContact = "Сергей +7-999-111-22-33",
                    departureStation = "Станция Москва-Павелецкая", // ← ДОБАВЛЕНО
                    departureContact = null,
                    destinationStation = null, // Только этапы 1-4
                    destinationContact = null,
                    unloadingAddress = null,
                    unloadingContact = null,
                    terminalReturnAddress = null,
                    containerDeliveryTime = System.currentTimeMillis() + 86400000, // Завтра
                ),
                OrderDriver(
                    id = "3",
                    number = "1003",
                    status = OrderDriver.OrderStatus.ACCEPTED,
                    driverId = "driver1",
                    customerName = "Алексей Петров",
                    address = "ул. Садовая, д. 7",
                    createdAt = System.currentTimeMillis() - 86400000,
                    plannedDeliveryTime = System.currentTimeMillis() + 1800000,
                    terminalPickupAddress = null,
                    containerType = "20-футовый рефрижератор",
                    containerCount = 1,
                    loadingAddress = null,
                    cargoName = "Продукты питания",
                    cargoWeight = 1200.0,
                    loadingContact = null,
                    departureStation = null,
                    departureContact = null,
                    destinationStation = "Станция СПб-Ладожский", // ← ДОБАВЛЕНО
                    destinationContact = "Диспетчер +7-812-555-44-33",
                    unloadingAddress = null,
                    unloadingContact = null,
                    terminalReturnAddress = null,
                    containerDeliveryTime = System.currentTimeMillis() + 86400000, // Завтра
                )
            ))
        }
    }
}