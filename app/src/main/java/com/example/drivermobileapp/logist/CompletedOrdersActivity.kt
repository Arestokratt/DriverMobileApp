package com.example.drivermobileapp.logist

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.models.Order1C
import com.example.drivermobileapp.data.models.User
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale

class CompletedOrdersActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var spinnerFilter: Spinner
    private lateinit var etSearch: EditText
    private lateinit var btnSearch: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var ordersListView: ListView
    private lateinit var tvEmpty: TextView

    private var currentUser: User? = null
    private val allOrders = mutableListOf<Order1C>()
    private val filteredOrders = mutableListOf<Order1C>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_completed_orders)

        currentUser = intent.getSerializableExtra("USER_DATA") as? User

        initViews()
        setupClickListeners()
        setupFilterSpinner()
        loadCompletedOrders()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        spinnerFilter = findViewById(R.id.spinnerFilter)
        etSearch = findViewById(R.id.etSearch)
        btnSearch = findViewById(R.id.btnSearch)
        progressBar = findViewById(R.id.progressBar)
        ordersListView = findViewById(R.id.ordersListView)
        tvEmpty = findViewById(R.id.tvEmpty)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish() // Возврат к OrdersActivity
        }

        btnSearch.setOnClickListener {
            applyFilters()
        }

        // Поиск при нажатии Enter
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                applyFilters()
                true
            } else {
                false
            }
        }

        // Клик по заявке в списке
        ordersListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val order = filteredOrders[position]
            openOrderStages(order)
        }
    }

    private fun setupFilterSpinner() {
        val filterOptions = arrayOf(
            "Все поля",
            "По номеру заявки",
            "По грузоотправителю",
            "По дате выполнения"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = adapter
    }

    private fun loadCompletedOrders() {
        showLoading(true)
        tvEmpty.text = "Загрузка выполненных заявок..."

        // Имитация загрузки данных
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            allOrders.clear()
            allOrders.addAll(createCompletedOrders())

            applyFilters()
            showLoading(false)

        }, 1000)
    }

    private fun createCompletedOrders(): List<Order1C> {
        val currentTime = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000L

        return listOf(
            Order1C(
                id = "COMP001",
                orderNumber = "2024-1001",
                orderDate = currentTime - 7 * dayInMillis,
                clientName = "ООО 'Ромашка'",
                fromAddress = "ул. Ленина 10",
                toAddress = "ул. Пушкина 25",
                cargoType = "Оборудование",
                weight = 150.0,
                volume = 2.5,
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
                orderNumber = "2024-1002",
                orderDate = currentTime - 5 * dayInMillis,
                clientName = "ИП Сидоров",
                fromAddress = "пр. Мира 15",
                toAddress = "ул. Садовая 8",
                cargoType = "Мебель",
                weight = 300.0,
                volume = 8.0,
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
                id = "COMP003",
                orderNumber = "2024-1003",
                orderDate = currentTime - 3 * dayInMillis,
                clientName = "АО 'СтройМаш'",
                fromAddress = "ул. Заводская 3",
                toAddress = "ул. Строителей 15",
                cargoType = "Строительные материалы",
                weight = 800.0,
                volume = 20.0,
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
                id = "COMP004",
                orderNumber = "2024-1004",
                orderDate = currentTime - 1 * dayInMillis,
                clientName = "ООО 'Логистик'",
                fromAddress = "ул. Транспортная 5",
                toAddress = "ул. Складская 12",
                cargoType = "Оборудование",
                weight = 450.0,
                volume = 12.0,
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

    private fun applyFilters() {
        val query = etSearch.text.toString().trim()
        val filterType = spinnerFilter.selectedItemPosition

        filteredOrders.clear()

        if (query.isEmpty()) {
            filteredOrders.addAll(allOrders)
        } else {
            filteredOrders.addAll(allOrders.filter { order ->
                when (filterType) {
                    1 -> { // По номеру заявки
                        order.orderNumber.contains(query, ignoreCase = true)
                    }
                    2 -> { // По грузоотправителю
                        order.clientName.contains(query, ignoreCase = true)
                    }
                    3 -> { // По дате выполнения
                        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        val date = dateFormat.format(Date(order.orderDate))
                        date.contains(query, ignoreCase = true)
                    }
                    else -> { // Все поля
                        order.orderNumber.contains(query, ignoreCase = true) ||
                                order.clientName.contains(query, ignoreCase = true)
                    }
                }
            })
        }

        updateOrdersList()
    }

    private fun updateOrdersList() {
        if (filteredOrders.isEmpty()) {
            ordersListView.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = if (etSearch.text.toString().isNotEmpty()) {
                "По вашему запросу ничего не найдено"
            } else {
                "Выполненных заявок нет"
            }
        } else {
            ordersListView.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE

            val orderStrings = filteredOrders.map { order ->
                "✅ ${order.orderNumber} - ${order.clientName}\n" +
                        "Груз: ${order.cargoType}, ${order.weight} кг\n" +
                        "Дата: ${SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(order.orderDate))}"
            }

            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, orderStrings)
            ordersListView.adapter = adapter
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            ordersListView.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        }
    }

    private fun openOrderStages(order: Order1C) {
        val intent = Intent(this, CompletedOrderDetailActivity::class.java).apply {
            putExtra("ORDER_DATA", order)
            putExtra("USER_DATA", currentUser)
        }
        startActivity(intent)
    }
}