package com.example.drivermobileapp.logist

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.models.Order1C
import com.example.drivermobileapp.data.models.User
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale

class AllOrdersActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var etSearch: EditText
    private lateinit var etAdvancedSearch: EditText
    private lateinit var btnSearch: Button
    private lateinit var btnAdvancedSearch: Button
    private lateinit var advancedSearchLayout: LinearLayout
    private lateinit var spinnerPageSize: Spinner
    private lateinit var spinnerSort: Spinner
    private lateinit var progressBar: ProgressBar
    private lateinit var ordersListView: ListView
    private lateinit var tvEmpty: TextView

    private var currentUser: User? = null
    private val allOrders = mutableListOf<Order1C>()
    private val filteredOrders = mutableListOf<Order1C>()

    private var isAdvancedSearchVisible = false
    private var currentPageSize = 10
    private var currentSortType = 0 // 0 - по дате добавления, 1 - по номеру заявки, 2 - по грузоотправителю

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_orders)

        currentUser = intent.getSerializableExtra("USER_DATA") as? User

        initViews()
        setupClickListeners()
        setupSpinners()
        loadAllOrders()
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
        progressBar = findViewById(R.id.progressBar)
        ordersListView = findViewById(R.id.ordersListView)
        tvEmpty = findViewById(R.id.tvEmpty)

        // Настройка EmptyView
        ordersListView.emptyView = tvEmpty
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnSearch.setOnClickListener {
            performSearch(false)
        }

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

        // Клик по заявке в списке
        ordersListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            if (position < filteredOrders.size) {
                val order = filteredOrders[position]
                openOrderDetails(order)
            }
        }

        // Автопоиск при изменении параметров в расширенном поиске
        spinnerPageSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isAdvancedSearchVisible) {
                    currentPageSize = when (position) {
                        0 -> 10
                        1 -> 50
                        2 -> 100
                        else -> 10
                    }
                    performSearch(true)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isAdvancedSearchVisible) {
                    currentSortType = position
                    performSearch(true)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
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
            currentPageSize
        } else {
            10
        }

        val sortType = if (isAdvanced) {
            currentSortType
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
                        formatDate(order.orderDate).contains(searchQuery, ignoreCase = true) ||
                        getStatusName(order.status).contains(searchQuery, ignoreCase = true)
            })
        }

        // Применяем сортировку
        when (sortType) {
            0 -> filteredOrders.sortByDescending { it.orderDate } // по дате добавления (новые сначала)
            1 -> filteredOrders.sortBy { it.orderNumber } // по номеру заявки
            2 -> filteredOrders.sortBy { it.clientName } // по грузоотправителю
        }

        // Ограничение количества результатов
        if (filteredOrders.size > pageSize) {
            val limitedList = filteredOrders.subList(0, pageSize)
            filteredOrders.clear()
            filteredOrders.addAll(limitedList)
        }

        updateOrdersList()
    }

    private fun loadAllOrders() {
        showLoading(true)
        tvEmpty.text = "Загрузка всех заявок..."

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            allOrders.clear()
            allOrders.addAll(createAllOrders())
            applyFilters() // Применяем фильтры по умолчанию
            showLoading(false)
        }, 1000)
    }

    private fun createAllOrders(): List<Order1C> {
        val currentTime = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000L

        return listOf(
            // НОВЫЕ ЗАЯВКИ
            Order1C(
                id = "NEW001",
                orderNumber = "2024-0001",
                orderDate = currentTime - 1 * dayInMillis,
                clientName = "ООО 'Ромашка'",
                fromAddress = "ул. Ленина 10",
                toAddress = "ул. Пушкина 25",
                cargoType = "Оборудование",
                weight = 150.0,
                volume = 2.5,
                status = "NEW"
            ),
            Order1C(
                id = "NEW002",
                orderNumber = "2024-0002",
                orderDate = currentTime - 2 * dayInMillis,
                clientName = "ИП Сидоров",
                fromAddress = "пр. Мира 15",
                toAddress = "ул. Садовая 8",
                cargoType = "Мебель",
                weight = 300.0,
                volume = 8.0,
                status = "NEW"
            ),

            // ТЕКУЩИЕ ЗАЯВКИ
            Order1C(
                id = "CUR001",
                orderNumber = "2024-1001",
                orderDate = currentTime - 3 * dayInMillis,
                clientName = "АО 'СтройМаш'",
                fromAddress = "ул. Заводская 3",
                toAddress = "ул. Строителей 15",
                cargoType = "Строительные материалы",
                weight = 800.0,
                volume = 20.0,
                status = "IN_PROGRESS",
                stage1Completed = true,
                stage2Completed = true
            ),
            Order1C(
                id = "CUR002",
                orderNumber = "2024-1002",
                orderDate = currentTime - 4 * dayInMillis,
                clientName = "ООО 'Логистик'",
                fromAddress = "ул. Транспортная 5",
                toAddress = "ул. Складская 12",
                cargoType = "Оборудование",
                weight = 450.0,
                volume = 12.0,
                status = "IN_PROGRESS",
                stage1Completed = true,
                stage2Completed = true,
                stage3Completed = true
            ),

            // ВЫПОЛНЕННЫЕ ЗАЯВКИ
            Order1C(
                id = "COMP001",
                orderNumber = "2024-2001",
                orderDate = currentTime - 7 * dayInMillis,
                clientName = "ООО 'ТехноИмпорт'",
                fromAddress = "ул. Промышленная 25",
                toAddress = "ул. Логистическая 8",
                cargoType = "Электроника",
                weight = 500.0,
                volume = 18.0,
                status = "COMPLETED",
                stage1Completed = true,
                stage2Completed = true,
                stage3Completed = true,
                stage4Completed = true,
                stage5Completed = true,
                stage6Completed = true,
                stage7Completed = true
            ),
            Order1C(
                id = "COMP002",
                orderNumber = "2024-2002",
                orderDate = currentTime - 5 * dayInMillis,
                clientName = "ИП Козлов",
                fromAddress = "ул. Промышленная 7",
                toAddress = "ул. Коммерческая 22",
                cargoType = "Бытовая техника",
                weight = 600.0,
                volume = 15.0,
                status = "COMPLETED",
                stage1Completed = true,
                stage2Completed = true,
                stage3Completed = true,
                stage4Completed = true,
                stage5Completed = true,
                stage6Completed = true,
                stage7Completed = true
            )
        )
    }

    private fun formatDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

    private fun getStatusName(status: String): String {
        return when (status) {
            "NEW" -> "Новая"
            "IN_PROGRESS" -> "Текущая"
            "COMPLETED" -> "Выполнена"
            "ARCHIVED" -> "Архив"
            else -> status
        }
    }

    private fun getStatusIcon(status: String): String {
        return when (status) {
            "NEW" -> "🆕"
            "IN_PROGRESS" -> "🔄"
            "COMPLETED" -> "✅"
            "ARCHIVED" -> "📁"
            else -> "📄"
        }
    }

    private fun updateOrdersList() {
        val orderStrings = filteredOrders.map { order ->
            val statusIcon = getStatusIcon(order.status)
            val statusName = getStatusName(order.status)
            val dateString = formatDate(order.orderDate)

            "$statusIcon ${order.orderNumber} - ${order.clientName}\n" +
                    "📦 ${order.cargoType}, ${order.weight} брутто(нетто)\n" +
                    "📅 $dateString • ${statusName}"
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, orderStrings)
        ordersListView.adapter = adapter

        // Обновляем текст пустого списка
        tvEmpty.text = if (etSearch.text.isNotEmpty() || etAdvancedSearch.text.isNotEmpty()) {
            "По вашему запросу ничего не найдено"
        } else {
            "Заявок нет"
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            tvEmpty.text = "Загрузка заявок..."
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(etSearch.windowToken, 0)
        imm.hideSoftInputFromWindow(etAdvancedSearch.windowToken, 0)
    }

    private fun openOrderDetails(order: Order1C) {
        when (order.status) {
            "COMPLETED" -> {
                val intent = Intent(this, CompletedOrderDetailActivity::class.java).apply {
                    putExtra("ORDER_DATA", order)
                    putExtra("USER_DATA", currentUser)
                }
                startActivity(intent)
            }
            "IN_PROGRESS" -> {
                val intent = Intent(this, OrderStagesActivity::class.java).apply {
                    putExtra("ORDER_DATA", order)
                    putExtra("USER_DATA", currentUser)
                }
                startActivity(intent)
            }
            else -> {
                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                val message = """
                    Заявка: ${order.orderNumber}
                    Клиент: ${order.clientName}
                    Груз: ${order.cargoType}, ${order.weight} брутто(нетто)
                    Дата подачи: ${dateFormat.format(Date(order.orderDate))}
                    Статус: ${getStatusName(order.status)}
                """.trimIndent()

                AlertDialog.Builder(this)
                    .setTitle("Детали заявки")
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }
}
