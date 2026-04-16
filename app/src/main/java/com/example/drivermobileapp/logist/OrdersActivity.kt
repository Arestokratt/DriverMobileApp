package com.example.drivermobileapp.logist

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.models.Order
import com.example.drivermobileapp.data.models.OrderPriority
import com.example.drivermobileapp.data.models.OrderPriorityStore
import com.example.drivermobileapp.data.models.OrderStatus
import com.example.drivermobileapp.data.models.User

class OrdersActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var tabNew: Button
    private lateinit var tabCurrent: Button
    private lateinit var tabArchive: Button
    private lateinit var tabAll: Button
    private lateinit var tabCompleted: Button
    private lateinit var btnCreateOrder: Button
    private lateinit var ordersListView: ListView
    private lateinit var tvEmpty: TextView

    private var currentUser: User? = null
    private val orders = mutableListOf<Order>()
    private var filteredOrders = mutableListOf<Order>()
    private var currentFilter: OrderStatus? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        currentUser = intent.getSerializableExtra("USER_DATA") as? User
        initViews()
        setupTabs()
        setupClickListeners()
        loadSampleOrders()
        filterOrders(OrderStatus.NEW)
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tabNew = findViewById(R.id.tabNew)
        tabCurrent = findViewById(R.id.tabCurrent)
        tabArchive = findViewById(R.id.tabArchive)
        tabAll = findViewById(R.id.tabAll)
        tabCompleted = findViewById(R.id.tabCompleted)
        btnCreateOrder = findViewById(R.id.btnCreateOrder)
        ordersListView = findViewById(R.id.ordersListView)
        tvEmpty = findViewById(R.id.tvEmpty)
    }

    private fun setupTabs() {
        setActiveTab(tabNew)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener { finish() }

        btnCreateOrder.setOnClickListener {
            val intent = Intent(this, CreateOrderActivity::class.java)
            intent.putExtra("USER_DATA", currentUser)
            startActivityForResult(intent, 1)
        }

        tabNew.setOnClickListener {
            val intent = Intent(this, NewOrdersActivity::class.java)
            intent.putExtra("USER_DATA", currentUser)
            startActivity(intent)
        }

        tabCurrent.setOnClickListener {
            val intent = Intent(this, CurrentOrdersActivity::class.java)
            intent.putExtra("USER_DATA", currentUser)
            startActivity(intent)
        }

        tabCompleted.setOnClickListener {
            val intent = Intent(this, CompletedOrdersActivity::class.java)
            intent.putExtra("USER_DATA", currentUser)
            startActivity(intent)
        }

        tabArchive.setOnClickListener {
            filterOrders(OrderStatus.ARCHIVED)
            setActiveTab(tabArchive)
        }

        tabAll.setOnClickListener {
            val intent = Intent(this, AllOrdersActivity::class.java)
            intent.putExtra("USER_DATA", currentUser)
            startActivity(intent)
        }

        ordersListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val order = filteredOrders[position]
            showOrderDetails(order)
        }
    }

    private fun filterOrders(status: OrderStatus?) {
        currentFilter = status
        filteredOrders.clear()

        if (status == null) {
            filteredOrders.addAll(orders)
        } else {
            filteredOrders.addAll(orders.filter { it.status == status })
        }

        filteredOrders.sortWith(
            compareByDescending<Order> { OrderPriority.rank(it.priority) }
                .thenByDescending { it.createdAt }
        )

        updateOrdersList()
    }

    private fun updateOrdersList() {
        if (filteredOrders.isEmpty()) {
            ordersListView.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = when (currentFilter) {
                OrderStatus.NEW -> "Новых заявок нет"
                OrderStatus.IN_PROGRESS -> "Текущих заявок нет"
                OrderStatus.COMPLETED -> "Выполненных заявок нет"
                OrderStatus.ARCHIVED -> "Архивных заявок нет"
                null -> "Заявок нет"
            }
            return
        }

        ordersListView.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE

        val orderStrings = filteredOrders.map { order ->
            val priorityMarker = OrderPriority.marker(order.priority)
            val titleLine = if (priorityMarker.isEmpty()) order.title else "$priorityMarker ${order.title}"

            "$titleLine\nОт: ${order.fromAddress} → К: ${order.toAddress}\n" +
                "Груз: ${order.cargoType}, ${order.weight}кг"
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, orderStrings)
        ordersListView.adapter = adapter
    }

    private fun setActiveTab(activeTab: Button) {
        val tabs = listOf(tabNew, tabCurrent, tabCompleted, tabArchive, tabAll)
        tabs.forEach { tab ->
            tab.setBackgroundColor(ContextCompat.getColor(this, R.color.grey))
            tab.setTextColor(ContextCompat.getColor(this, R.color.black))
        }

        activeTab.setBackgroundColor(ContextCompat.getColor(this, R.color.blue))
        activeTab.setTextColor(ContextCompat.getColor(this, R.color.white))
    }

    private fun showOrderDetails(order: Order) {
        val message = """
        Заявка: ${order.title}
        Описание: ${order.description}
        От: ${order.fromAddress}
        К: ${order.toAddress}
        Груз: ${order.cargoType}
        Вес: ${order.weight}кг
        Объём: ${order.volume}м³
        Приоритет: ${OrderPriority.label(order.priority)}
        Статус: ${getStatusName(order.status)}
    """.trimIndent()

        val builder = AlertDialog.Builder(this)
            .setTitle("Детали заявки")
            .setMessage(message)
            .setPositiveButton("Изменить приоритет") { dialog, _ ->
                showPriorityDialog(order)
                dialog.dismiss()
            }
            .setNegativeButton("Закрыть", null)

        if (order.status == OrderStatus.COMPLETED) {
            builder.setNeutralButton("Просмотреть этапы") { dialog, _ ->
                openOrderStages(convertToOrder1C(order))
                dialog.dismiss()
            }
        }

        builder.show()
    }

    private fun convertToOrder1C(order: Order): com.example.drivermobileapp.data.models.Order1C {
        return com.example.drivermobileapp.data.models.Order1C(
            id = order.id,
            orderNumber = "COMP-${order.id}",
            orderDate = order.createdAt,
            clientName = order.title,
            fromAddress = order.fromAddress,
            toAddress = order.toAddress,
            cargoType = order.cargoType,
            weight = order.weight,
            volume = order.volume,
            status = "COMPLETED",
            priority = order.priority,
            stage1Completed = true,
            stage2Completed = true,
            stage3Completed = true,
            stage4Completed = true,
            stage5Completed = true,
            stage6Completed = true,
            stage7Completed = true
        )
    }

    private fun openOrderStages(order: com.example.drivermobileapp.data.models.Order1C) {
        val intent = Intent(this, OrderStagesActivity::class.java).apply {
            putExtra("ORDER_DATA", order)
            putExtra("USER_DATA", currentUser)
        }
        startActivity(intent)
    }

    private fun getStatusName(status: OrderStatus): String {
        return when (status) {
            OrderStatus.NEW -> "Новая"
            OrderStatus.IN_PROGRESS -> "В процессе"
            OrderStatus.COMPLETED -> "Выполнена"
            OrderStatus.ARCHIVED -> "В архиве"
        }
    }

    private fun loadSampleOrders() {
        orders.addAll(
            listOf(
                Order(
                    "1",
                    "Доставка оборудования",
                    "Срочная доставка серверного оборудования",
                    "ул. Ленина 10",
                    "ул. Пушкина 25",
                    "Оборудование",
                    150.0,
                    2.5,
                    OrderStatus.NEW,
                    currentUser?.id ?: "",
                    priority = OrderPriority.HIGH
                ),
                Order(
                    "2",
                    "Перевозка мебели",
                    "Переезд офиса",
                    "пр. Мира 15",
                    "ул. Садовая 8",
                    "Мебель",
                    300.0,
                    8.0,
                    OrderStatus.IN_PROGRESS,
                    currentUser?.id ?: "",
                    "driver1",
                    priority = OrderPriority.URGENT
                ),
                Order(
                    "3",
                    "Доставка документов в офис",
                    "Важные документы для подписания директором",
                    "б-р. Комарова 12",
                    "ул. Гагарина 5",
                    "Документы",
                    2.0,
                    0.1,
                    OrderStatus.COMPLETED,
                    currentUser?.id ?: "",
                    "driver2"
                ),
                Order(
                    "4",
                    "Перевозка товаров в магазин",
                    "Товары для нового магазина",
                    "склад №1",
                    "ТЦ 'Центральный'",
                    "Товары",
                    500.0,
                    15.0,
                    OrderStatus.COMPLETED,
                    currentUser?.id ?: "",
                    "driver3"
                ),
                Order(
                    "5",
                    "Доставка строительных материалов",
                    "Материалы для стройки",
                    "ул. Промышленная 8",
                    "ул. Строителей 15",
                    "Строительные материалы",
                    800.0,
                    20.0,
                    OrderStatus.COMPLETED,
                    currentUser?.id ?: "",
                    "driver1"
                ),
                Order(
                    "6",
                    "Перевозка электроники",
                    "Компьютерная техника для офиса",
                    "ул. Техническая 3",
                    "пр. Ленинградский 25",
                    "Электроника",
                    200.0,
                    5.0,
                    OrderStatus.COMPLETED,
                    currentUser?.id ?: "",
                    "driver2"
                ),
                Order(
                    "7",
                    "Доставка медицинского оборудования",
                    "Оборудование для больницы",
                    "ул. Медицинская 1",
                    "Городская больница №1",
                    "Медицинское оборудование",
                    350.0,
                    8.5,
                    OrderStatus.COMPLETED,
                    currentUser?.id ?: "",
                    "driver3"
                )
            )
        )
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showPriorityDialog(order: Order) {
        val priorities = OrderPriority.spinnerItems()

        AlertDialog.Builder(this)
            .setTitle("Изменить приоритет")
            .setSingleChoiceItems(priorities, OrderPriority.toSpinnerPosition(order.priority)) { dialog, which ->
                val updatedOrder = order.copy(
                    priority = OrderPriority.fromSpinnerPosition(which),
                    updatedAt = System.currentTimeMillis()
                )
                OrderPriorityStore.setPriority(updatedOrder.id, updatedOrder.priority)
                replaceOrder(updatedOrder)
                filterOrders(currentFilter)
                showMessage("Приоритет обновлён: ${OrderPriority.label(updatedOrder.priority)}")
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun replaceOrder(updatedOrder: Order) {
        val orderIndex = orders.indexOfFirst { it.id == updatedOrder.id }
        if (orderIndex >= 0) {
            orders[orderIndex] = updatedOrder
        }
    }

    fun addNewOrder(order: Order) {
        orders.add(order)
        filterOrders(currentFilter)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            val newOrder = data.getSerializableExtra("NEW_ORDER") as? Order
            newOrder?.let {
                orders.add(it)
                filterOrders(currentFilter)
                showMessage("Новая заявка добавлена!")
            }
        }
    }
}
