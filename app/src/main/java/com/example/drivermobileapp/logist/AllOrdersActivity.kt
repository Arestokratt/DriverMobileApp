package com.example.drivermobileapp.logist

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.models.Order1C
import com.example.drivermobileapp.data.models.OrderPriority
import com.example.drivermobileapp.data.models.OrderPriorityStore
import com.example.drivermobileapp.data.models.User
import java.text.SimpleDateFormat
import java.util.Date
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
    private var currentSortType = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_orders)

        currentUser = intent.getSerializableExtra("USER_DATA") as? User

        initViews()
        setupClickListeners()
        setupSpinners()
        loadAllOrders()
    }

    override fun onResume() {
        super.onResume()
        applyStoredPriorities()
        applyFilters(
            if (isAdvancedSearchVisible) etAdvancedSearch.text.toString().trim() else etSearch.text.toString().trim(),
            if (isAdvancedSearchVisible) currentPageSize else 10,
            if (isAdvancedSearchVisible) currentSortType else 0
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
        progressBar = findViewById(R.id.progressBar)
        ordersListView = findViewById(R.id.ordersListView)
        tvEmpty = findViewById(R.id.tvEmpty)
        ordersListView.emptyView = tvEmpty
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener { finish() }

        btnSearch.setOnClickListener {
            performSearch(false)
        }

        btnAdvancedSearch.setOnClickListener {
            toggleAdvancedSearch()
        }

        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(false)
                true
            } else {
                false
            }
        }

        etAdvancedSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(true)
                true
            } else {
                false
            }
        }

        ordersListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            if (position < filteredOrders.size) {
                openOrderDetails(filteredOrders[position])
            }
        }

        spinnerPageSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isAdvancedSearchVisible) {
                    currentPageSize = when (position) {
                        1 -> 50
                        2 -> 100
                        else -> 10
                    }
                    performSearch(true)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isAdvancedSearchVisible) {
                    currentSortType = position
                    performSearch(true)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun setupSpinners() {
        val pageSizes = arrayOf("10", "50", "100")
        val pageSizeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, pageSizes)
        pageSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPageSize.adapter = pageSizeAdapter
        spinnerPageSize.setSelection(0)

        val sortOptions = arrayOf("по дате добавления", "по номеру заявки", "по грузоотправителю")
        val sortAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSort.adapter = sortAdapter
        spinnerSort.setSelection(0)
    }

    private fun toggleAdvancedSearch() {
        if (isAdvancedSearchVisible) {
            advancedSearchLayout.visibility = View.GONE
            btnAdvancedSearch.text = "Перейти к расширенному поиску"
        } else {
            advancedSearchLayout.visibility = View.VISIBLE
            btnAdvancedSearch.text = "Скрыть расширенный поиск"
            if (!etSearch.text.isNullOrEmpty()) {
                etAdvancedSearch.setText(etSearch.text)
            }
        }
        isAdvancedSearchVisible = !isAdvancedSearchVisible
    }

    private fun performSearch(isAdvanced: Boolean) {
        val searchQuery = if (isAdvanced) etAdvancedSearch.text.toString().trim() else etSearch.text.toString().trim()
        val pageSize = if (isAdvanced) currentPageSize else 10
        val sortType = if (isAdvanced) currentSortType else 0

        hideKeyboard()
        applyFilters(searchQuery, pageSize, sortType)
    }

    private fun applyFilters(searchQuery: String = "", pageSize: Int = 10, sortType: Int = 0) {
        filteredOrders.clear()

        if (searchQuery.isEmpty()) {
            filteredOrders.addAll(allOrders)
        } else {
            filteredOrders.addAll(
                allOrders.filter { order ->
                    order.orderNumber.contains(searchQuery, ignoreCase = true) ||
                        order.clientName.contains(searchQuery, ignoreCase = true) ||
                        formatDate(order.orderDate).contains(searchQuery, ignoreCase = true) ||
                        getStatusName(order.status).contains(searchQuery, ignoreCase = true)
                }
            )
        }

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

    private fun loadAllOrders() {
        showLoading(true)
        tvEmpty.text = "Загрузка всех заявок..."

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            allOrders.clear()
            allOrders.addAll(createAllOrders())
            applyFilters()
            showLoading(false)
        }, 1000)
    }

    private fun createAllOrders(): List<Order1C> {
        val currentTime = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000L

        return listOf(
            Order1C(
                id = "NEW001",
                orderNumber = "2024-0001",
                orderDate = currentTime - dayInMillis,
                clientName = "ООО 'Ромашка'",
                fromAddress = "ул. Ленина 10",
                toAddress = "ул. Пушкина 25",
                cargoType = "Оборудование",
                weight = 150.0,
                volume = 2.5,
                status = "NEW",
                priority = OrderPriority.HIGH
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
                status = "NEW",
                priority = OrderPriority.URGENT
            ),
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
                stage2Completed = true,
                priority = OrderPriority.URGENT
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
                stage7Completed = true,
                priority = OrderPriority.HIGH
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
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(timestamp))
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
            "NEW" -> "[NEW]"
            "IN_PROGRESS" -> "[RUN]"
            "COMPLETED" -> "[OK]"
            "ARCHIVED" -> "[ARC]"
            else -> "[ORD]"
        }
    }

    private fun updateOrdersList() {
        val orderStrings = filteredOrders.map { order ->
            val statusIcon = getStatusIcon(order.status)
            val statusName = getStatusName(order.status)
            val priorityMarker = OrderPriority.marker(order.priority)
            val firstLine = if (priorityMarker.isEmpty()) {
                "$statusIcon ${order.orderNumber} - ${order.clientName}"
            } else {
                "$priorityMarker $statusIcon ${order.orderNumber} - ${order.clientName}"
            }

            "$firstLine\n" +
                "Груз: ${order.cargoType}, ${order.weight} брутто(нетто)\n" +
                "Дата: ${formatDate(order.orderDate)} • ${statusName} • ${OrderPriority.label(order.priority)}"
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, orderStrings)
        ordersListView.adapter = adapter

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
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
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
                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                val message = """
                    Заявка: ${order.orderNumber}
                    Клиент: ${order.clientName}
                    Груз: ${order.cargoType}, ${order.weight} брутто(нетто)
                    Дата подачи: ${dateFormat.format(Date(order.orderDate))}
                    Приоритет: ${OrderPriority.label(order.priority)}
                    Статус: ${getStatusName(order.status)}
                """.trimIndent()

                AlertDialog.Builder(this)
                    .setTitle("Текущая заявка")
                    .setMessage(message)
                    .setPositiveButton("Этапы") { dialog, _ ->
                        val intent = Intent(this, OrderStagesActivity::class.java).apply {
                            putExtra("ORDER_DATA", order)
                            putExtra("USER_DATA", currentUser)
                        }
                        startActivity(intent)
                        dialog.dismiss()
                    }
                    .setNeutralButton("Изменить приоритет") { dialog, _ ->
                        showPriorityDialog(order)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Подробнее") { dialog, _ ->
                        val intent = Intent(this, OrderDetailActivity::class.java).apply {
                            putExtra("ORDER_DATA", order)
                            putExtra("USER_DATA", currentUser)
                        }
                        startActivity(intent)
                        dialog.dismiss()
                    }
                    .show()
            }

            else -> {
                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                val message = """
                    Заявка: ${order.orderNumber}
                    Клиент: ${order.clientName}
                    Груз: ${order.cargoType}, ${order.weight} брутто(нетто)
                    Дата подачи: ${dateFormat.format(Date(order.orderDate))}
                    Приоритет: ${OrderPriority.label(order.priority)}
                    Статус: ${getStatusName(order.status)}
                """.trimIndent()

                AlertDialog.Builder(this)
                    .setTitle("Детали заявки")
                    .setMessage(message)
                    .setPositiveButton("Изменить приоритет") { dialog, _ ->
                        showPriorityDialog(order)
                        dialog.dismiss()
                    }
                    .setNeutralButton("Подробнее") { dialog, _ ->
                        val intent = Intent(this, OrderDetailActivity::class.java).apply {
                            putExtra("ORDER_DATA", order)
                            putExtra("USER_DATA", currentUser)
                        }
                        startActivity(intent)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Закрыть", null)
                    .show()
            }
        }
    }

    private fun showPriorityDialog(order: Order1C) {
        val priorities = OrderPriority.spinnerItems()

        AlertDialog.Builder(this)
            .setTitle("Изменить приоритет")
            .setSingleChoiceItems(priorities, OrderPriority.toSpinnerPosition(order.priority)) { dialog, which ->
                order.priority = OrderPriority.fromSpinnerPosition(which)
                OrderPriorityStore.setPriority(order.id, order.priority)
                applyFilters(
                    if (isAdvancedSearchVisible) etAdvancedSearch.text.toString().trim() else etSearch.text.toString().trim(),
                    if (isAdvancedSearchVisible) currentPageSize else 10,
                    if (isAdvancedSearchVisible) currentSortType else 0
                )
                Toast.makeText(this, "Приоритет обновлён: ${OrderPriority.label(order.priority)}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun applyStoredPriorities() {
        allOrders.forEach { order ->
            OrderPriorityStore.getPriority(order.id)?.let { savedPriority ->
                order.priority = savedPriority
            }
        }
    }
}
