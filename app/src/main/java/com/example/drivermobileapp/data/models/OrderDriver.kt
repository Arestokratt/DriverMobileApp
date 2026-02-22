import java.io.Serializable

data class OrderDriver(
    val id: String,
    val number: String,
    val status: OrderStatus,
    val driverId: String?,

    // Основная информация
    val customerName: String,
    val address: String,
    val createdAt: Long,
    val plannedDeliveryTime: Long?,

    // Детальная информация по ТЗ - все nullable с null по умолчанию
    val terminalPickupAddress: String? = null,
    val containerType: String? = null,
    val containerCount: Int? = null,
    val containerDeliveryTime: Long? = null, // ← ДОБАВЛЯЕМ новое поле
    val loadingAddress: String? = null,
    val cargoName: String? = null,
    val cargoWeight: Double? = null,
    val loadingContact: String? = null,
    val departureStation: String? = null,
    val departureContact: String? = null,
    val destinationStation: String? = null,
    val destinationContact: String? = null,
    val unloadingAddress: String? = null, // ← переименовываем для соответствия ТЗ
    val unloadingContact: String? = null,
    val terminalReturnAddress: String? = null,

    // Этап 1 и далее


    val stages: OrderStages = OrderStages()
) : Serializable {

    enum class OrderStatus {
        NEW, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    data class OrderStages(
        val stage1: Stage = Stage(), // Заявка
        val stage2: Stage = Stage(), // Терминал вывоза
        val stage3: Stage = Stage(), // Склад
        val stage4: Stage = Stage(), // Станция отправления
        val stage5: Stage = Stage(), // Станция назначения
        val stage6: Stage = Stage(), // Выдача груза
        val stage7: Stage = Stage()  // Терминал сдачи
    ) : Serializable

    data class Stage(
        val isCompleted: Boolean = false,
        val completionTime: Long? = null,
        val notes: String? = null,
        val photos: List<String> = emptyList(),
        val documents: List<String> = emptyList(),

        // Данные для Этапа №2
        val arrivalTime: Long? = null,    // Время прибытия
        val departureTime: Long? = null,  // Время выезда
        val containerPhotos: List<String> = emptyList(), // Фото контейнера
        val terminalDocuments: List<String> = emptyList(), // Документы терминала

        // Данные для Этапа №3 (Склад)
        val warehouseArrivalTime: Long? = null,    // Время прибытия на склад
        val warehouseDepartureTime: Long? = null,  // Время выезда со склада
        val loadingPhotos: List<String> = emptyList(), // Фото погрузки
        val warehouseDocuments: List<String> = emptyList(), // Документы склада

        // Данные для Этапа №4 (Станция отправления)
        val departureStationArrivalTime: Long? = null,    // Время прибытия на станцию
        val departureStationDepartureTime: Long? = null,  // Время выезда со станции
        val departureStationDocuments: List<String> = emptyList(), // Документы станции

        // Данные для Этапа №5 (Станция назначения)
        val destinationStationArrivalTime: Long? = null,    // Время прибытия на станцию назначения
        val destinationStationDepartureTime: Long? = null,  // Время выезда со станции назначения
        val destinationContainerPhotos: List<String> = emptyList(), // Фото контейнера на станции назначения
        val destinationStationDocuments: List<String> = emptyList(), // Документы станции назначения

        // Данные для Этапа №6 (Выдача груза)
        val unloadingArrivalTime: Long? = null,    // Время прибытия на склад выгрузки
        val unloadingDepartureTime: Long? = null,  // Время выезда со склада выгрузки
        val unloadingPhotos: List<String> = emptyList(), // Фото выгрузки
        val unloadingDocuments: List<String> = emptyList(), // Документы выгрузки

        // Данные для Этапа №7 (Терминал сдачи)
        val returnTerminalArrivalTime: Long? = null,    // Время прибытия на терминал сдачи
        val returnTerminalDepartureTime: Long? = null,  // Время выезда с терминала сдачи
        val returnTerminalDocuments: List<String> = emptyList() // Документы терминала сдачи
    ) : Serializable

    // Методы для проверки видимости этапов
    fun shouldShowStages1to4(): Boolean {
        return !departureStation.isNullOrEmpty()
    }

    fun shouldShowStages5to7(): Boolean {
        return !destinationStation.isNullOrEmpty()
    }
}