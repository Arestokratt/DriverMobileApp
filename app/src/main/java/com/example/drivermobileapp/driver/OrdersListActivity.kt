package com.example.drivermobileapp.driver

import OrderDriver
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.drivermobileapp.R
//import com.example.drivermobileapp.data.models.OrderDriver
import com.example.drivermobileapp.data.repositories.OrderRepository
//import com.example.drivermobileapp.driver.adapters.OrderAdapter

class OrdersListActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var tvTitle: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var rvOrders: RecyclerView
    private lateinit var tvEmptyList: TextView
    private lateinit var orderAdapter: OrderAdapter

    private var ordersType: String = ""
    private val orderRepository = OrderRepository()
    private val currentDriverId = "driver1" // TODO: Получать из настроек/логина

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders_list)

        ordersType = intent.getStringExtra("ORDERS_TYPE") ?: ""
        println("DEBUG: OrdersListActivity started with type: '$ordersType'")

        // Добавляем тестовые данные
        orderRepository.addTestOrders()

        initViews()
        setupClickListeners()
        setupRecyclerView()
        loadOrders()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.tvTitle)
        progressBar = findViewById(R.id.progressBar)
        rvOrders = findViewById(R.id.rvOrders)
        tvEmptyList = findViewById(R.id.tvEmptyList)

        val titleText = when (ordersType) {
            "incoming" -> "Входящие заявки"
            "my_orders" -> "Мои заявки"
            else -> "Заявки"
        }
        tvTitle.text = titleText
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(emptyList()) { order ->
            onOrderClick(order)
        }

        rvOrders.layoutManager = LinearLayoutManager(this)
        rvOrders.adapter = orderAdapter
    }

    private fun loadOrders() {
        progressBar.visibility = ProgressBar.VISIBLE
        rvOrders.visibility = RecyclerView.GONE
        tvEmptyList.visibility = TextView.GONE

        // Имитируем загрузку
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val orders = when (ordersType) {
                "incoming" -> orderRepository.getIncomingOrders()
                "my_orders" -> orderRepository.getMyOrders(currentDriverId)
                else -> emptyList()
            }

            orderAdapter.updateOrders(orders)

            progressBar.visibility = ProgressBar.GONE

            if (orders.isEmpty()) {
                tvEmptyList.visibility = TextView.VISIBLE
                rvOrders.visibility = RecyclerView.GONE
                tvEmptyList.text = when (ordersType) {
                    "incoming" -> "Нет входящих заявок"
                    "my_orders" -> "У вас нет принятых заявок"
                    else -> "Нет заявок"
                }
            } else {
                tvEmptyList.visibility = TextView.GONE
                rvOrders.visibility = RecyclerView.VISIBLE
            }
        }, 500)
    }

    private fun onOrderClick(order: OrderDriver) {
        println("DEBUG: Clicked order: ${order.number}, type: $ordersType")

        if (ordersType == "incoming") {
            // Для ВХОДЯЩИХ заявок - открываем OrderDetailActivity с кнопками Принять/Отклонить
            val intent = Intent(this, OrderDetailActivity::class.java).apply {
                putExtra("ORDER", order)
                putExtra("ORDERS_TYPE", ordersType)
            }
            startActivityForResult(intent, REQUEST_ORDER_DETAIL)
        } else if (ordersType == "my_orders") {
            // Для МОИХ ЗАЯВОК - открываем OrderStagesActivity с этапами
            val intent = Intent(this, OrderStagesActivity::class.java).apply {
                putExtra("ORDER", order)
            }
            startActivity(intent)
        } else {
            // Запасной вариант
            showMessage("Неизвестный тип заявок: $ordersType")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ORDER_DETAIL && resultCode == RESULT_OK) {
            // Обновляем список после действий с заявкой
            loadOrders()
        }
    }

    companion object {
        private const val REQUEST_ORDER_DETAIL = 1001
    }

    private fun showMessage(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}