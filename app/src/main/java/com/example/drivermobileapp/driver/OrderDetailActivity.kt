package com.example.drivermobileapp.driver

import OrderDriver
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.drivermobileapp.R
//import com.example.drivermobileapp.data.models.Order
//import com.example.drivermobileapp.data.models.OrderDriver
import com.example.drivermobileapp.data.repositories.OrderRepository

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var tvOrderNumber: TextView
    private lateinit var btnAccept: Button
    private lateinit var btnReject: Button
    private lateinit var llOrderDetails: LinearLayout

    private var currentOrder: OrderDriver? = null
    private var ordersType: String = ""
    private val orderRepository = OrderRepository()
    private val currentDriverId = "driver1" // TODO: Получать из настроек/логина

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_driver_detail)

        currentOrder = intent.getSerializableExtra("ORDER") as? OrderDriver
        ordersType = intent.getStringExtra("ORDERS_TYPE") ?: ""

        initViews()
        setupClickListeners()
        displayOrderDetails()
        setupButtonsVisibility()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvOrderNumber = findViewById(R.id.tvOrderNumber)
        btnAccept = findViewById(R.id.btnAccept)
        btnReject = findViewById(R.id.btnReject)
        llOrderDetails = findViewById(R.id.llOrderDetails)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnAccept.setOnClickListener {
            currentOrder?.let { order ->
                acceptOrder(order)
            }
        }

        btnReject.setOnClickListener {
            currentOrder?.let { order ->
                rejectOrder(order)
            }
        }
    }

    private fun displayOrderDetails() {
        currentOrder?.let { order ->
            tvOrderNumber.text = "Заявка №${order.number}"
            llOrderDetails.removeAllViews()

            addDetailField("Адрес терминала приема порожнего контейнера", order.terminalPickupAddress)
            addDetailField("Условия перевозки", "Требуется защита от воздействия влаги")
            addDetailField("Тип контейнера", order.containerType)
            addDetailField("Количество контейнеров", order.containerCount?.toString())
            addDetailField("Адрес подачи контейнера под загрузку", order.loadingAddress)
            addDetailField("Наименование груза", order.cargoName)
            addDetailField("Вес груза", order.cargoWeight?.let { "$it кг" })
            addDetailField("Контактное лицо на складе погрузки", order.loadingContact)
            addDetailField("Наименование станции отправления", order.departureStation)
            addDetailField("Контактное лицо на станции отправления", order.departureContact)
            addDetailField("Наименование станции назначения", order.destinationStation)
            addDetailField("Контактное лицо на станции назначения", order.destinationContact)
            addDetailField("Адрес выгрузки контейнера", order.unloadingAddress)
            addDetailField("Контактное лицо на складе выгрузки", order.unloadingContact)
            addDetailField("Адрес терминала сдачи порожнего контейнера", order.terminalReturnAddress)


            if (llOrderDetails.childCount == 0) {
                addDetailField("Информация", "Детальная информация по заявке отсутствует")
            }
        }
    }

    private fun setupButtonsVisibility() {
        // Скрываем кнопки принятия/отклонения для уже принятых заявок
        if (ordersType == "my_orders" || currentOrder?.status == OrderDriver.OrderStatus.ACCEPTED) {
            btnAccept.visibility = Button.GONE
            btnReject.visibility = Button.GONE
        }
    }

    private fun addDetailField(label: String, value: String?) {
        if (!value.isNullOrEmpty()) {
            val fieldView = layoutInflater.inflate(R.layout.item_order_detail_field, llOrderDetails, false)

            val tvLabel = fieldView.findViewById<TextView>(R.id.tvLabel)
            val tvValue = fieldView.findViewById<TextView>(R.id.tvValue)

            tvLabel.text = label
            tvValue.text = value

            llOrderDetails.addView(fieldView)
        }
    }

    private fun acceptOrder(order: OrderDriver) {
        val success = orderRepository.acceptOrder(order.id, currentDriverId)

        if (success) {
            showMessage("Заявка №${order.number} принята")
            setResult(RESULT_OK)
            finish()
        } else {
            showMessage("Ошибка при принятии заявки")
        }
    }

    private fun rejectOrder(order: OrderDriver) {
        val success = orderRepository.rejectOrder(order.id)

        if (success) {
            showMessage("Заявка №${order.number} отклонена")
            setResult(RESULT_OK)
            finish()
        } else {
            showMessage("Ошибка при отклонении заявки")
        }
    }

    private fun showMessage(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}