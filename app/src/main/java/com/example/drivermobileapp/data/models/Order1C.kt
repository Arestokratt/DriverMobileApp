package com.example.drivermobileapp.data.models

import java.io.Serializable

data class Order1C(
    val id: String,
    val orderNumber: String,
    val orderDate: Long,
    val clientName: String,
    val fromAddress: String,
    val toAddress: String,
    val cargoType: String,
    val weight: Double,
    val volume: Double,
    var status: String = "NEW",
    val priority: String = "NORMAL",

    // Изменяем на var для полей, которые могут обновляться
    var containerType: String = "",
    var containerCount: Int = 0,
    var containerDeliveryDateTime: Long = 0L,
    var containerDeliveryAddress: String = "",
    var loadingContactPerson: String = "",
    var clientLegalName: String = "",
    var clientPostalAddress: String = "",
    var cargoName: String = "",
    var cargoPieces: Int = 0,
    var departureStation: String = "",
    var destinationStation: String = "",
    var consigneeName: String = "",
    var consigneePostalAddress: String = "",
    var unloadingContactPerson: String = "",
    var notes: String = "",
    var emptyContainerTerminal: String = "",
    var assignedDriverId: String? = null,
    var orderStage: OrderStage = OrderStage.APPLICATION,

    // Этапы
    var stage1Completed: Boolean = false,
    var stage2Completed: Boolean = false,
    var stage3Completed: Boolean = false,
    var stage4Completed: Boolean = false,
    var stage5Completed: Boolean = false,
    var stage6Completed: Boolean = false,
    var stage7Completed: Boolean = false,

    // Дополнительные поля для этапов
    var terminalInfo: String = "",
    var warehouseInfo: String = "",
    var departureStationInfo: String = "",
    var destinationStationInfo: String = "",
    var cargoIssueInfo: String = "",
    var containerReturnInfo: String = "",

    // Этапы
    var terminalStage: TerminalStage = TerminalStage(),
    var terminalPhotos: List<String> = emptyList(),
    var terminalDocuments: List<String> = emptyList(), // ДОБАВЛЯЕМ ЭТО ПОЛЕ!
    var warehouseStage: WarehouseStage = WarehouseStage(),
    var warehousePhotos: List<String> = emptyList(),
    var warehouseDocuments: List<String> = emptyList(),
    var departureStationStage: StationStage = StationStage(),
    var departureStationDocuments: List<String> = emptyList(),
    var destinationStationStage: StationStage = StationStage(), // ДОБАВЛЯЕМ ЭТО ПОЛЕ!
    var destinationStationDocuments: List<String> = emptyList(), // ДОБАВЛЯЕМ ЭТО ПОЛЕ


    // НОВЫЕ ПОЛЯ ДЛЯ ЭТАПА 6 - ВЫДАЧА ГРУЗА
    var cargoIssueStage: CargoIssueStage = CargoIssueStage(),
    var cargoIssuePhotos: List<String> = emptyList(),
    var cargoIssueDocuments: List<String> = emptyList(),

    // НОВЫЕ ПОЛЯ ДЛЯ ЭТАПА 7 - ТЕРМИНАЛ СДАЧИ
    var containerReturnStage: ContainerReturnStage = ContainerReturnStage(),
    var containerReturnDocuments: List<String> = emptyList(),

) : Serializable

enum class OrderStage {
    APPLICATION,    // Этап 1: Заявка
    LOADING,        // Этап 2: Погрузка
    IN_TRANSIT,     // Этап 3: В пути
    UNLOADING,      // Этап 4: Выгрузка
    COMPLETED       // Этап 5: Завершено
}

// Модель для этапа терминала
data class TerminalStage(
    val acceptedTime: Long = 0L,        // Время принятия заявки водителем
    val arrivedTime: Long = 0L,         // Время прибытия на терминал
    val departedTime: Long = 0L,        // Время выезда с терминала
    val terminalName: String = "",      // Название терминала
    val containerNumber: String = "",   // Номер контейнера
    val driverNotes: String = ""        // Заметки водителя
) : Serializable

// Модель для этапа склада
data class WarehouseStage(
    val arrivedTime: Long = 0L,         // Время прибытия на склад
    val departedTime: Long = 0L,        // Время выезда со склада
    val warehouseName: String = "",     // Название склада
    val loadingTime: String = "",       // Время погрузки
    val cargoCondition: String = "",    // Состояние груза
    val driverNotes: String = ""        // Заметки водителя
) : Serializable

/// Модель для этапа станции (общая для отправления и назначения)
data class StationStage(
    var arrivedTime: Long = 0L,
    var departedTime: Long = 0L,
    var stationName: String = "",
    var trainNumber: String = "",
    var departureTime: Long = 0L,
    var driverNotes: String = ""
) : Serializable

// Модель для этапа 6 - Выдача груза
// Модель для этапа 6 - Выдача груза (упрощенная)
data class CargoIssueStage(
    var arrivedTime: Long = 0L,         // Время прибытия на склад грузополучателя
    var departedTime: Long = 0L         // Время выезда со склада грузополучателя
) : Serializable

// Модель для этапа 7 - Терминал сдачи порожнего контейнера
data class ContainerReturnStage(
    var arrivedTime: Long = 0L,         // Время прибытия на терминал сдачи
    var departedTime: Long = 0L,        // Время выезда с терминала сдачи
    var terminalName: String = "",      // Название терминала сдачи
    var containerCondition: String = "", // Состояние контейнера
    var driverNotes: String = ""        // Заметки водителя
) : Serializable