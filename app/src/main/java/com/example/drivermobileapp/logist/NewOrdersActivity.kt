package com.example.drivermobileapp.logist

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.models.Order1C
import com.example.drivermobileapp.data.models.OrderPriority
import com.example.drivermobileapp.data.models.OrderPriorityStore
import com.example.drivermobileapp.data.models.User
import com.example.drivermobileapp.data.repository.Order1CRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        applyFilters()
    }

    override fun onResume() {
        super.onResume()
        applyStoredPriorities()
        applyFilters()
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
        btnBack.setOnClickListener { finish() }

        btnSearch.setOnClickListener { applyFilters() }

        etSearch.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                applyFilters()
                true
            } else {
                false
            }
        }

        ordersListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val order = filteredOrders[position]
            val intent = Intent(this, OrderDetailActivity::class.java)
            intent.putExtra("ORDER_DATA", order)
            intent.putExtra("USER_DATA", currentUser)
            startActivity(intent)
        }

        ordersListView.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
            showPriorityDialog(filteredOrders[position])
            true
        }

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun applyFilters() {
        val searchText = etSearch.text.toString().trim().lowercase()
        val filterType = spinnerFilter.selectedItemPosition

        filteredOrders.clear()

        if (searchText.isEmpty()) {
            filteredOrders.addAll(allOrders)
        } else {
            filteredOrders.addAll(
                allOrders.filter { order ->
                    when (filterType) {
                        1 -> {
                            val dateString = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                                .format(Date(order.orderDate))
                            dateString.contains(searchText, ignoreCase = true)
                        }
                        2 -> order.orderNumber.contains(searchText, ignoreCase = true)
                        3 -> order.clientName.contains(searchText, ignoreCase = true)
                        else -> {
                            order.orderNumber.contains(searchText, ignoreCase = true) ||
                                order.clientName.contains(searchText, ignoreCase = true) ||
                                order.fromAddress.contains(searchText, ignoreCase = true) ||
                                order.toAddress.contains(searchText, ignoreCase = true) ||
                                order.cargoType.contains(searchText, ignoreCase = true)
                        }
                    }
                }
            )
        }

        filteredOrders.sortWith(
            compareByDescending<Order1C> { OrderPriority.rank(it.priority) }
                .thenByDescending { it.orderDate }
        )

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
            return
        }

        ordersListView.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE

        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val orderStrings = filteredOrders.map { order ->
            val priorityMarker = OrderPriority.marker(order.priority)
            val dateString = dateFormat.format(Date(order.orderDate))
            val numberLine = if (priorityMarker.isEmpty()) {
                "№${order.orderNumber} • $dateString"
            } else {
                "$priorityMarker №${order.orderNumber} • $dateString"
            }

            "$numberLine\n" +
                "От: ${order.clientName}\n" +
                "Груз: ${order.cargoType}, ${order.weight}кг"
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, orderStrings)
        ordersListView.adapter = adapter
    }

    private fun showOrderDetails(order: Order1C) {
        val dateString = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            .format(Date(order.orderDate))

        val message = """
            Номер заявки: ${order.orderNumber}
            Дата: $dateString
            Грузоотправитель: ${order.clientName}
            Адрес отправления: ${order.fromAddress}
            Адрес назначения: ${order.toAddress}
            Тип груза: ${order.cargoType}
            Вес: ${order.weight} нетто(брутто)
            Объём: ${order.volume} м³
            Приоритет: ${OrderPriority.label(order.priority)}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Детали заявки из 1С")
            .setMessage(message)
            .setPositiveButton("Назначить водителя") { dialog, _ ->
                showMessage("Назначение водителя - в разработке")
                dialog.dismiss()
            }
            .setNegativeButton("Закрыть", null)
            .show()
    }

    private fun loadSample1COrders() {
        showLoading(true)

        Handler(android.os.Looper.getMainLooper()).postDelayed({
            val repository = Order1CRepository()

            allOrders.clear()
            allOrders.addAll(repository.getNewOrdersFrom1C())

            if (allOrders.isNotEmpty()) {
                allOrders[0].priority = OrderPriority.HIGH
            }
            if (allOrders.size > 1) {
                allOrders[1].priority = OrderPriority.URGENT
            }

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

    private fun applyStoredPriorities() {
        allOrders.forEach { order ->
            OrderPriorityStore.getPriority(order.id)?.let { savedPriority ->
                order.priority = savedPriority
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
                applyFilters()
                showMessage("Приоритет обновлён: ${OrderPriority.label(order.priority)}")
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}
