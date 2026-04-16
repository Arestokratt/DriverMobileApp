package com.example.drivermobileapp.logist

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.models.Order1C
import com.example.drivermobileapp.data.models.OrderPriority
import com.example.drivermobileapp.data.models.User
import java.text.SimpleDateFormat
import java.util.*
import android.os.Handler
import com.example.drivermobileapp.data.models.CargoIssueStage
import com.example.drivermobileapp.data.models.OrderPriorityStore
import com.example.drivermobileapp.data.models.StationStage
import com.example.drivermobileapp.data.models.TerminalStage
import com.example.drivermobileapp.data.models.WarehouseStage

class CurrentOrdersActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var etSearch: EditText
    private lateinit var etAdvancedSearch: EditText
    private lateinit var btnSearch: Button
    private lateinit var btnAdvancedSearch: Button
    private lateinit var advancedSearchLayout: LinearLayout
    private lateinit var spinnerPageSize: Spinner
    private lateinit var spinnerSort: Spinner
    private lateinit var ordersListView: ListView
    private lateinit var tvEmpty: TextView
    private lateinit var progressBar: ProgressBar

    private var currentUser: User? = null
    private val allOrders = mutableListOf<Order1C>()
    private var filteredOrders = mutableListOf<Order1C>()

    private var isAdvancedSearchVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_orders)

        currentUser = intent.getSerializableExtra("USER_DATA") as? User
        initViews()
        setupSpinners()
        setupClickListeners()
        loadCurrentOrders()
    }

    override fun onResume() {
        super.onResume()
        applyStoredPriorities()
        applyFilters(
            if (isAdvancedSearchVisible) etAdvancedSearch.text.toString().trim() else etSearch.text.toString().trim(),
            if (isAdvancedSearchVisible) spinnerPageSize.selectedItem.toString().toInt() else 10,
            if (isAdvancedSearchVisible) spinnerSort.selectedItemPosition else 0
        )
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        etSearch = findViewById(R.id.etSearch)
        etAdvancedSearch = findViewById(R.id.etAdvancedSearch)
        btnSearch = findViewById(R.id.btnSearch)
        btnAdvancedSearch = findViewById(R.id.btnAdvancedSearch)
        advancedSearchLayout = findViewById(R.id.advancedSearchLayout)
        spinnerPageSize = findViewById(R.id.spinnerPageSize)
        spinnerSort = findViewById(R.id.spinnerSort)
        ordersListView = findViewById(R.id.ordersListView)
        tvEmpty = findViewById(R.id.tvEmpty)
        progressBar = findViewById(R.id.progressBar)

        // Настройка EmptyView
        ordersListView.emptyView = tvEmpty

        // Устанавливаем подсказки для полей поиска
        etSearch.hint = "Введите номер заявки, наименование грузоотправителя, дату подачи заявки"
        etAdvancedSearch.hint = "Введите номер заявки, наименование грузоотправителя, дата подачи заявки"
    }

    private fun setupSpinners() {
        // Настройка спиннера для количества элементов
        val pageSizes = arrayOf("10", "50", "100")
        val pageSizeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, pageSizes)
        pageSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPageSize.adapter = pageSizeAdapter
        spinnerPageSize.setSelection(0) // По умолчанию 10

        // Настройка спиннера для сортировки
        val sortOptions = arrayOf("по дате добавления", "по номеру заявки", "по грузоотправителю")
        val sortAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSort.adapter = sortAdapter
        spinnerSort.setSelection(0) // По умолчанию по дате добавления
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        // Кнопка "Найти" (основной поиск)
        btnSearch.setOnClickListener {
            performSearch(false)
        }

        // Кнопка "Расширенный поиск"
        btnAdvancedSearch.setOnClickListener {
            toggleAdvancedSearch()
        }

        // Поиск при нажатии Enter в основном поле
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                performSearch(false)
                true
            } else {
                false
            }
        }

        // Поиск при нажатии Enter в расширенном поле
        etAdvancedSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                performSearch(true)
                true
            } else {
                false
            }
        }

        // Обработчик клика по заявке в списке
        ordersListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            if (position < filteredOrders.size) {
                val order = filteredOrders[position]
                val intent = Intent(this, OrderDetailActivity::class.java)
                intent.putExtra("ORDER_DATA", order)
                intent.putExtra("USER_DATA", currentUser)
                startActivity(intent)
            }
        }

        // Автопоиск при изменении параметров в расширенном поиске
        spinnerPageSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isAdvancedSearchVisible) {
                    performSearch(true)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isAdvancedSearchVisible) {
                    performSearch(true)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun toggleAdvancedSearch() {
        if (isAdvancedSearchVisible) {
            advancedSearchLayout.visibility = View.GONE
            btnAdvancedSearch.text = "Перейти к расширенному поиску"
        } else {
            advancedSearchLayout.visibility = View.VISIBLE
            btnAdvancedSearch.text = "Скрыть расширенный поиск"
            // Копируем текст из основного поиска
            if (!etSearch.text.isNullOrEmpty()) {
                etAdvancedSearch.setText(etSearch.text)
            }
        }
        isAdvancedSearchVisible = !isAdvancedSearchVisible
    }

    private fun performSearch(isAdvanced: Boolean) {
        val searchQuery = if (isAdvanced) {
            etAdvancedSearch.text.toString().trim()
        } else {
            etSearch.text.toString().trim()
        }

        val pageSize = if (isAdvanced) {
            spinnerPageSize.selectedItem.toString().toInt()
        } else {
            10
        }

        val sortType = if (isAdvanced) {
            spinnerSort.selectedItemPosition
        } else {
            0 // по дате добавления по умолчанию
        }

        hideKeyboard()
        applyFilters(searchQuery, pageSize, sortType)
    }

    private fun applyFilters(searchQuery: String = "", pageSize: Int = 10, sortType: Int = 0) {
        filteredOrders.clear()

        if (searchQuery.isEmpty()) {
            filteredOrders.addAll(allOrders)
        } else {
            // Поиск по номеру заявки, грузоотправителю или дате
            filteredOrders.addAll(allOrders.filter { order ->
                order.orderNumber.contains(searchQuery, ignoreCase = true) ||
                        order.clientName.contains(searchQuery, ignoreCase = true) ||
                        formatDate(order.orderDate).contains(searchQuery, ignoreCase = true)
            })
        }

        // Применяем сортировку
        when (sortType) {
            0 -> filteredOrders.sortByDescending { it.orderDate } // по дате добавления (новые сначала)
            1 -> filteredOrders.sortBy { it.orderNumber } // по номеру заявки
            2 -> filteredOrders.sortBy { it.clientName } // по грузоотправителю
        }

        // Ограничение количества результатов
        when (sortType) {
            0 -> filteredOrders.sortWith(
                compareByDescending<Order1C> { OrderPriority.rank(it.priority) }
                    .thenByDescending { it.orderDate }
            )
            1 -> filteredOrders.sortWith(
                compareByDescending<Order1C> { OrderPriority.rank(it.priority) }
                    .thenBy { it.orderNumber }
            )
            2 -> filteredOrders.sortWith(
                compareByDescending<Order1C> { OrderPriority.rank(it.priority) }
                    .thenBy { it.clientName }
            )
        }

        if (filteredOrders.size > pageSize) {
            val limitedList = filteredOrders.subList(0, pageSize)
            filteredOrders.clear()
            filteredOrders.addAll(limitedList)
        }

        updateOrdersList()
    }

    private fun updateOrdersList() {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        val orderStrings = filteredOrders.map { order ->
            val dateString = dateFormat.format(Date(order.orderDate))
            "📋 ${order.orderNumber} • $dateString\n" +
                    "🏢 ${order.clientName}\n" +
                    "📦 ${order.cargoType}, ${order.weight} брутто(нетто)\n" +
                    "👤 ${getDriverName(order.assignedDriverId)} • ${getOrderStage(order)}"
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, orderStrings)
        ordersListView.adapter = adapter

        // Обновляем текст пустого списка
        tvEmpty.text = if (etSearch.text.isNotEmpty() || etAdvancedSearch.text.isNotEmpty()) {
            "Текущие заявки по вашему запросу не найдены"
        } else {
            "Текущих заявок нет"
        }
    }

    private fun formatDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

    private fun getDriverName(driverId: String?): String {
        return when (driverId) {
            "driver1" -> "Петров П.П."
            "driver2" -> "Иванов И.И."
            "driver3" -> "Сидоров А.А."
            "driver4" -> "Козлов Д.С."
            "driver5" -> "Семенов А.П."
            "driver7" -> "Смирнов А.В."
            else -> "Не назначен"
        }
    }

    private fun getOrderStage(order: Order1C): String {
        return when (order.status) {
            "IN_PROGRESS" -> "В процессе"
            "LOADING" -> "Погрузка"
            "IN_TRANSIT" -> "В пути"
            "UNLOADING" -> "Выгрузка"
            "COMPLETED" -> "Завершена"
            else -> "Новая"
        }
    }

    private fun showOrderDetails(order: Order1C) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val dateString = dateFormat.format(Date(order.orderDate))

        val message = """
        Номер заявки: ${order.orderNumber}
        Дата: $dateString
        Грузоотправитель: ${order.clientName}
        Статус: ${getOrderStage(order)}
        Водитель: ${getDriverName(order.assignedDriverId)}
        Адрес отправления: ${order.fromAddress}
        Адрес назначения: ${order.toAddress}
        Тип груза: ${order.cargoType}
        Вес: ${order.weight} кг
    """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Текущая заявка")
            .setMessage(message)
            .setPositiveButton("Этапы перевозки") { dialog, _ ->
                val intent = Intent(this, OrderStagesActivity::class.java)
                intent.putExtra("ORDER_DATA", order)
                intent.putExtra("USER_DATA", currentUser)
                startActivity(intent)
                dialog.dismiss()
            }
            .setNeutralButton("Подробнее") { dialog, _ ->
                val intent = Intent(this, OrderDetailActivity::class.java)
                intent.putExtra("ORDER_DATA", order)
                intent.putExtra("USER_DATA", currentUser)
                startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton("Закрыть", null)
            .show()
    }

    private fun loadCurrentOrders() {
        // Имитация загрузки текущих заявок
        showLoading(true)

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val currentTime = System.currentTimeMillis()
            val dayInMillis = 24 * 60 * 60 * 1000L

            // Заявки с разными заполненными полями для демонстрации этапов
            allOrders.addAll(listOf(
                // ДОБАВИТЬ В СПИСОК allOrders - специально для тестирования 5 этапа
                Order1C(
                    id = "1C008",
                    orderNumber = "2024-0001",
                    orderDate = currentTime - 86400000, // 1 день назад
                    clientName = "ООО 'ТехноИмпорт'",
                    fromAddress = "ул. Промышленная 25",
                    toAddress = "ул. Логистическая 8",
                    cargoType = "Электроника",
                    weight = 500.0,
                    volume = 18.0,
                    status = "IN_PROGRESS",
                    assignedDriverId = "driver5",

                    // ДАННЫЕ ДЛЯ 5 ЭТАПА - СТАНЦИЯ НАЗНАЧЕНИЯ
                    containerType = "40-футовый контейнер",
                    containerCount = 1,
                    clientLegalName = "ОБЩЕСТВО С ОГРАНИЧЕННОЙ ОТВЕТСТВЕННОСТЬЮ 'ТЕХНОИМПОРТ'",
                    clientPostalAddress = "123456, г. Москва, ул. Промышленная, д. 25",
                    cargoName = "Электронные компоненты и оборудование",
                    cargoPieces = 120,
                    destinationStation = "Станция Новосибирск-Главный",
                    consigneeName = "ООО 'Сибирь-Электроникс'",
                    consigneePostalAddress = "630000, г. Новосибирск, ул. Ленина, д. 12",
                    unloadingContactPerson = "Андрей Семенов +7-913-456-78-90",
                    notes = "Хрупкий груз! Требуется осторожная разгрузка. Температурный режим: +15°C до +25°C",
                    emptyContainerTerminal = "Терминал 'Сибирь-Логистик', ул. Транспортная, 5",

                    // Статусы этапов
                    stage1Completed = true,
                    stage2Completed = true,
                    stage3Completed = true,
                    stage4Completed = true,
                    stage5Completed = false, // 5 этап в процессе

                    // Данные для этапа 5 - Станция назначения
                    destinationStationStage = StationStage(
                        arrivedTime = System.currentTimeMillis() - 3600000, // 1 час назад - прибыл
                        departedTime = 0L, // Еще не выехал
                        stationName = "Станция Новосибирск-Главный",
                        trainNumber = "101Ч",
                        driverNotes = "Ожидаю оформления документов на выдачу груза"
                    ),

                    // Время принятия заявки водителем
                    terminalStage = TerminalStage(
                        acceptedTime = System.currentTimeMillis() - 172800000 // 2 дня назад
                    ),

                    // Фото и документы для 5 этапа
                    destinationStationDocuments = listOf(
                        "station_arrival.jpg",
                        "cargo_inspection.pdf",
                        "customs_declaration.pdf"
                    )
                ),
                Order1C(
                    id = "1C002",
                    orderNumber = "2023-0002",
                    orderDate = currentTime - 2 * dayInMillis,
                    clientName = "ИП Сидоров",
                    fromAddress = "пр. Мира 15",
                    toAddress = "ул. Садовая 8",
                    cargoType = "Мебель",
                    weight = 300.0,
                    volume = 8.0,
                    status = "IN_PROGRESS",
                    assignedDriverId = "driver1",
                    departureStation = "Станция Москва-Товарная",

                    // Данные для этапа 1
                    containerType = "40-футовый рефрижератор",
                    containerCount = 2,
                    containerDeliveryDateTime = currentTime - dayInMillis,
                    containerDeliveryAddress = "ул. Логистическая, 10, терминал №3",
                    loadingContactPerson = "Сергей Петров +7-999-123-45-67",
                    clientLegalName = "Индивидуальный предприниматель Сидоров Алексей Владимирович",
                    clientPostalAddress = "123456, г. Москва, ул. Центральная, д. 15, оф. 204",
                    cargoName = "Офисная мебель",
                    cargoPieces = 45,
                    consigneeName = "ООО 'Бизнес-Интерьер'",
                    consigneePostalAddress = "198095, г. Санкт-Петербург, пр. Стачек, д. 48",
                    unloadingContactPerson = "Мария Иванова +7-911-234-56-78",
                    emptyContainerTerminal = "Терминал 'Логистик-Центр', ул. Промышленная, 25",
                    notes = "Требуется бережная погрузка. Груз хрупкий.",

                    // Статусы этапов
                    stage1Completed = true, // Этап 1 завершен
                    stage2Completed = true,

                    // Данные для этапа 2
                    terminalStage = TerminalStage(
                        acceptedTime = System.currentTimeMillis() - 3600000,
                        arrivedTime = System.currentTimeMillis() - 1800000,
                        departedTime = System.currentTimeMillis() - 900000,
                        terminalName = "Терминал 'Логистик-Центр'",
                        containerNumber = "CONT-789123",
                        driverNotes = "Контейнер получен, пломба цела"
                    ),
                    terminalPhotos = listOf("container_front.jpg", "container_back.jpg", "container_seal.jpg"),
                    terminalDocuments = listOf(
                        "waybill.pdf",
                        "acceptance_certificate.jpg",
                        "invoice.pdf",
                        "contract.pdf"
                    ),
                    // Данные для этапа 3 - Склад
                    warehouseStage = WarehouseStage(
                        arrivedTime = System.currentTimeMillis() - 7200000, // 2 часа назад
                        departedTime = System.currentTimeMillis() - 5400000, // 1.5 часа назад
                        warehouseName = "Склад ООО 'Ромашка'",
                        loadingTime = "2 часа 15 минут",
                        cargoCondition = "Груз погружен полностью, упаковка цела",
                        driverNotes = "Погрузка прошла без замечаний, груз закреплен"
                    ),
                    warehousePhotos = listOf("loading_1.jpg", "loading_2.jpg", "loading_3.jpg", "cargo_inside.jpg"),
                    warehouseDocuments = listOf("loading_act.pdf", "cargo_declaration.jpg", "quality_certificate.pdf"),

                    stage3Completed = true,

                    // Данные для этапа 4 - Станция отправления
                    departureStationStage = StationStage(
                        arrivedTime = System.currentTimeMillis() - 10800000, // 3 часа назад
                        departedTime = System.currentTimeMillis() - 9000000, // 2.5 часа назад
                        stationName = "Станция Москва-Товарная",
                        trainNumber = "8352",
                        departureTime = System.currentTimeMillis() - 7200000, // 2 часа назад
                        driverNotes = "Контейнер сдан на станции, оформлены все документы"
                    ),
                    departureStationDocuments = listOf(
                        "railway_bill.pdf",
                        "station_acceptance.jpg",
                        "customs_declaration.pdf"
                    ),

                    stage4Completed = true



                ),

                Order1C(
                    id = "1C005",
                    orderNumber = "2023-0005",
                    orderDate = currentTime,
                    clientName = "АО 'СтройМаш'",
                    fromAddress = "ул. Заводская 3",
                    toAddress = "ул. Строителей 15",
                    cargoType = "Строительные материалы",
                    weight = 800.0,
                    volume = 20.0,
                    status = "LOADING",
                    assignedDriverId = "driver2",
                    destinationStation = "Станция СПб-Финляндская", // Заполнено - показываем этапы 5-7
                    stage1Completed = true
                ),
                // ДОБАВИТЬ В СПИСОК allOrders - для тестирования 6 этапа
                Order1C(
                    id = "1C010",
                    orderNumber = "2024-0003",
                    orderDate = currentTime - 259200000, // 3 дня назад
                    clientName = "ООО 'Продукты Севера'",
                    fromAddress = "ул. Рыбная 8",
                    toAddress = "ул. Торговая 15",
                    cargoType = "Продукты питания",
                    weight = 800.0,
                    volume = 22.0,
                    status = "COMPLETED",
                    assignedDriverId = "driver7",

                    // Данные для предыдущих этапов
                    containerType = "20-футовый рефрижератор",
                    containerCount = 1,
                    destinationStation = "Станция Мурманск-Товарная",
                    stage1Completed = true,
                    stage2Completed = true,
                    stage3Completed = true,
                    stage4Completed = true,
                    stage5Completed = true,

                    // ДАННЫЕ ДЛЯ ЭТАПА 6 - ВЫДАЧА ГРУЗА
                    cargoIssueStage = CargoIssueStage(
                        arrivedTime = System.currentTimeMillis() - 172800000, // 2 дня назад
                        departedTime = System.currentTimeMillis() - 165600000, // 1 день 22 часа назад
//                        warehouseName = "Склад ООО 'Северные Продукты'",
//                        unloadingTime = "3 часа 20 минут",
//                        cargoCondition = "Груз в хорошем состоянии, температурный режим соблюден",
//                        recipientName = "Семенов Алексей Петрович",
//                        driverNotes = "Груз получен, все документы подписаны. Получатель претензий не имеет."
                    ),
                    cargoIssuePhotos = listOf("unloading_1.jpg", "unloading_2.jpg", "cargo_delivered.jpg"),
                    cargoIssueDocuments = listOf("delivery_act.pdf", "recipient_signature.jpg", "quality_certificate.pdf"),
                    stage6Completed = true
                ),

                // Добавить в список allOrders
                Order1C(
                    id = "1C007",
                    orderNumber = "2023-0007",
                    orderDate = currentTime - 432000000, // 5 дней назад
                    clientName = "ИП Козлов",
                    fromAddress = "ул. Промышленная 7",
                    toAddress = "ул. Коммерческая 22",
                    cargoType = "Бытовая техника",
                    weight = 600.0,
                    volume = 15.0,
                    status = "COMPLETED",
                    assignedDriverId = "driver4",

                    // ДАННЫЕ ДЛЯ 5 ЭТАПА - СТАНЦИЯ НАЗНАЧЕНИЯ (все поля заполнены)
                    containerType = "20-футовый рефрижератор",
                    containerCount = 1,
                    clientLegalName = "Индивидуальный предприниматель Козлов Дмитрий Сергеевич",
                    clientPostalAddress = "123456, г. Москва, ул. Промышленная, д. 7",
                    cargoName = "Бытовая техника: холодильники, стиральные машины",
                    cargoPieces = 35,
                    destinationStation = "Станция Екатеринбург-Сортировочный",
                    consigneeName = "ООО 'ТехноМир'",
                    consigneePostalAddress = "620000, г. Екатеринбург, ул. Коммерческая, д. 22",
                    unloadingContactPerson = "Смирнова Анна Викторовна +7-912-567-89-01",
                    notes = "Температурный режим: +5°C. Требуется подключение к электросети.",
                    emptyContainerTerminal = "Терминал 'Урал-Логистик', ул. Транспортная, 15",

                    // Статусы этапов
                    stage1Completed = true,
                    stage2Completed = true,
                    stage3Completed = true,
                    stage4Completed = true,
                    stage5Completed = true,
                    stage6Completed = true,
                    stage7Completed = true,

                    // Данные для этапа 5 - Станция назначения (полностью завершен)
                    destinationStationStage = StationStage(
                        arrivedTime = System.currentTimeMillis() - 86400000, // 1 день назад
                        departedTime = System.currentTimeMillis() - 43200000, // 12 часов назад
                        stationName = "Станция Екатеринбург-Сортировочный",
                        trainNumber = "142Ч",
                        driverNotes = "Груз доставлен, получатель доволен"
                    ),

                    // Время принятия заявки водителем
                    terminalStage = TerminalStage(
                        acceptedTime = System.currentTimeMillis() - 259200000 // 3 дня назад
                    )
                )
            ))

            applyFilters()
            showLoading(false)

        }, 1000)
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        ordersListView.visibility = if (show) View.GONE else View.VISIBLE
        tvEmpty.visibility = if (show) View.GONE else tvEmpty.visibility
    }

    private fun hideKeyboard() {
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(etSearch.windowToken, 0)
        imm.hideSoftInputFromWindow(etAdvancedSearch.windowToken, 0)
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun applyStoredPriorities() {
        allOrders.forEach { order ->
            OrderPriorityStore.getPriority(order.id)?.let { savedPriority ->
                order.priority = savedPriority
            }
        }
    }
}
