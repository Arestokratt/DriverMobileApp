package com.example.drivermobileapp.logist

import android.app.AlertDialog
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
import android.os.Handler
import androidx.core.content.ContextCompat
import com.example.drivermobileapp.data.repository.Order1CRepository

class NewOrdersActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var etSearch: EditText
    private lateinit var btnSearch: Button
    private lateinit var spinnerFilter: Spinner
    private lateinit var ordersListView: ListView
    private lateinit var tvEmpty: TextView
    private lateinit var progressBar: ProgressBar

    private var currentUser: User? = null
    private val allOrders = mutableListOf<Order1C>()
    private var filteredOrders = mutableListOf<Order1C>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_orders)

        currentUser = intent.getSerializableExtra("USER_DATA") as? User
        initViews()
        setupSpinner()
        setupClickListeners()
        loadSample1COrders()
        applyFilters() // Показываем все заявки initially
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        etSearch = findViewById(R.id.etSearch)
        btnSearch = findViewById(R.id.btnSearch)
        spinnerFilter = findViewById(R.id.spinnerFilter)
        ordersListView = findViewById(R.id.ordersListView)
        tvEmpty = findViewById(R.id.tvEmpty)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupSpinner() {
        val filters = arrayOf("Все", "По дате добавления", "По номеру заявки", "По грузоотправителю")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filters)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = adapter
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish() // Возврат к вкладке "Заявки"
        }

        btnSearch.setOnClickListener {
            applyFilters()
        }

        // Поиск при нажатии Enter
        etSearch.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == android.view.KeyEvent.KEYCODE_ENTER) {
                applyFilters()
                true
            } else {
                false
            }
        }

        // Обработчик клика по заявке в списке
        ordersListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val order = filteredOrders[position]
            // ПЕРЕХОД К ДЕТАЛЯМ ЗАЯВКИ
            val intent = Intent(this, OrderDetailActivity::class.java)
            intent.putExtra("ORDER_DATA", order)
            intent.putExtra("USER_DATA", currentUser)
            startActivity(intent)
        }

        // Обработчик изменения фильтра
        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun applyFilters() {
        val searchText = etSearch.text.toString().trim().lowercase()
        val filterType = spinnerFilter.selectedItemPosition

        filteredOrders.clear()

        if (searchText.isEmpty()) {
            // Если поисковый запрос пустой, показываем все заявки
            filteredOrders.addAll(allOrders)
        } else {
            // Фильтруем по типу фильтра и поисковому запросу
            filteredOrders.addAll(allOrders.filter { order ->
                when (filterType) {
                    1 -> { // По дате добавления
                        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        val dateString = dateFormat.format(Date(order.orderDate))
                        dateString.contains(searchText, ignoreCase = true)
                    }
                    2 -> { // По номеру заявки
                        order.orderNumber.contains(searchText, ignoreCase = true)
                    }
                    3 -> { // По грузоотправителю
                        order.clientName.contains(searchText, ignoreCase = true)
                    }
                    else -> { // Все (ищем во всех полях)
                        order.orderNumber.contains(searchText, ignoreCase = true) ||
                                order.clientName.contains(searchText, ignoreCase = true) ||
                                order.fromAddress.contains(searchText, ignoreCase = true) ||
                                order.toAddress.contains(searchText, ignoreCase = true) ||
                                order.cargoType.contains(searchText, ignoreCase = true)
                    }
                }
            })
        }

        // Сортируем по дате (новые сверху)
        filteredOrders.sortByDescending { it.orderDate }

        updateOrdersList()
    }

    private fun updateOrdersList() {
        if (filteredOrders.isEmpty()) {
            ordersListView.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = if (etSearch.text.isNotEmpty()) {
                "Заявки по вашему запросу не найдены"
            } else {
                "Новых заявок нет"
            }
        } else {
            ordersListView.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE

            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

            val orderStrings = filteredOrders.map { order ->
                val dateString = dateFormat.format(Date(order.orderDate))
                "№${order.orderNumber} • $dateString\n" +
                        "От: ${order.clientName}\n" +
                        "Груз: ${order.cargoType}, ${order.weight}кг"
            }

            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, orderStrings)
            ordersListView.adapter = adapter
        }
    }

    private fun showOrderDetails(order: Order1C) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val dateString = dateFormat.format(Date(order.orderDate))

        val message = """
            Номер заявки: ${order.orderNumber}
            Дата: $dateString
            Грузоотправитель: ${order.clientName}
            Адрес отправления: ${order.fromAddress}
            Адрес назначения: ${order.toAddress}
            Тип груза: ${order.cargoType}
            Вес: ${order.weight} нетто(брутто)
            Объем: ${order.volume} м³
            Приоритет: ${order.priority}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Детали заявки из 1С")
            .setMessage(message)
            .setPositiveButton("Назначить водителя") { dialog, _ ->
                // TODO: Переход к назначению водителя
                showMessage("Назначение водителя - в разработке")
                dialog.dismiss()
            }
            .setNegativeButton("Закрыть", null)
            .show()
    }

    private fun loadSample1COrders() {
        showLoading(true)

        // Имитация загрузки данных из 1С
        Handler(android.os.Looper.getMainLooper()).postDelayed({
            val repository = Order1CRepository()

            // Очищаем старые данные и загружаем новые
            allOrders.clear()
            allOrders.addAll(repository.getNewOrdersFrom1C())

            applyFilters()
            showLoading(false)

        }, 1500)
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        ordersListView.visibility = if (show) View.GONE else View.VISIBLE
        tvEmpty.visibility = if (show) View.GONE else tvEmpty.visibility
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}