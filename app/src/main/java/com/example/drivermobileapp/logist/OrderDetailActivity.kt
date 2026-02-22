package com.example.drivermobileapp.logist

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.models.Order1C
import com.example.drivermobileapp.data.models.OrderStage
import com.example.drivermobileapp.data.models.User
import java.text.SimpleDateFormat
import java.util.*
import android.os.Handler
import java.util.Random


class OrderDetailActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var btnAddTerminal: Button
    private lateinit var btnAssignDriver: Button
    private lateinit var btnSendOrder: Button
    private lateinit var tvOrderNumber: TextView
    private lateinit var tvOrderDate: TextView
    private lateinit var tvContainerType: TextView
    private lateinit var tvContainerCount: TextView
    private lateinit var tvContainerDateTime: TextView
    private lateinit var tvContainerAddress: TextView
    private lateinit var tvLoadingContact: TextView
    private lateinit var tvClientName: TextView
    private lateinit var tvClientAddress: TextView
    private lateinit var tvCargoName: TextView
    private lateinit var tvCargoPieces: TextView
    private lateinit var tvCargoWeight: TextView
    private lateinit var tvDepartureStation: TextView
    private lateinit var tvDestinationStation: TextView
    private lateinit var tvConsigneeName: TextView
    private lateinit var tvConsigneeAddress: TextView
    private lateinit var tvUnloadingContact: TextView
    private lateinit var tvNotes: TextView
    private lateinit var tvTerminal: TextView
    private lateinit var tvDriverStatus: TextView
    private lateinit var progressBar: ProgressBar

    private var currentOrder: Order1C? = null
    private var currentUser: User? = null
    private var selectedDriverId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        currentOrder = intent.getSerializableExtra("ORDER_DATA") as? Order1C
        currentUser = intent.getSerializableExtra("USER_DATA") as? User

        initViews()
        setupClickListeners()
        displayOrderDetails()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnAddTerminal = findViewById(R.id.btnAddTerminal)
        btnAssignDriver = findViewById(R.id.btnAssignDriver)
        btnSendOrder = findViewById(R.id.btnSendOrder)

        // Инициализация всех TextViews
        tvOrderNumber = findViewById(R.id.tvOrderNumber)
        tvOrderDate = findViewById(R.id.tvOrderDate)
        tvContainerType = findViewById(R.id.tvContainerType)
        tvContainerCount = findViewById(R.id.tvContainerCount)
        tvContainerDateTime = findViewById(R.id.tvContainerDateTime)
        tvContainerAddress = findViewById(R.id.tvContainerAddress)
        tvLoadingContact = findViewById(R.id.tvLoadingContact)
        tvClientName = findViewById(R.id.tvClientName)
        tvClientAddress = findViewById(R.id.tvClientAddress)
        tvCargoName = findViewById(R.id.tvCargoName)
        tvCargoPieces = findViewById(R.id.tvCargoPieces)
        tvCargoWeight = findViewById(R.id.tvCargoWeight)
        tvDepartureStation = findViewById(R.id.tvDepartureStation)
        tvDestinationStation = findViewById(R.id.tvDestinationStation)
        tvConsigneeName = findViewById(R.id.tvConsigneeName)
        tvConsigneeAddress = findViewById(R.id.tvConsigneeAddress)
        tvUnloadingContact = findViewById(R.id.tvUnloadingContact)
        tvNotes = findViewById(R.id.tvNotes)
        tvTerminal = findViewById(R.id.tvTerminal)
        tvDriverStatus = findViewById(R.id.tvDriverStatus)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish() // Возврат к списку новых заявок
        }

        btnAddTerminal.setOnClickListener {
            showAddTerminalDialog()
        }

        btnAssignDriver.setOnClickListener {
            showDriverSelectionDialog()
        }

        btnSendOrder.setOnClickListener {
            if (selectedDriverId != null) {
                sendOrderToDriver()
            } else {
                showMessage("Сначала выберите водителя")
            }
        }
    }

    private fun displayOrderDetails() {
        currentOrder?.let { order ->
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val containerDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

            tvOrderNumber.text = order.orderNumber
            tvOrderDate.text = dateFormat.format(Date(order.orderDate))
            tvContainerType.text = order.containerType.ifEmpty { "Не указано" }
            tvContainerCount.text = if (order.containerCount > 0) order.containerCount.toString() else "Не указано"
            tvContainerDateTime.text = if (order.containerDeliveryDateTime > 0)
                containerDateFormat.format(Date(order.containerDeliveryDateTime)) else "Не указано"
            tvContainerAddress.text = order.containerDeliveryAddress.ifEmpty { "Не указано" }
            tvLoadingContact.text = order.loadingContactPerson.ifEmpty { "Не указано" }
            tvClientName.text = order.clientLegalName.ifEmpty { order.clientName }
            tvClientAddress.text = order.clientPostalAddress.ifEmpty { "Не указано" }
            tvCargoName.text = order.cargoName.ifEmpty { order.cargoType }
            tvCargoPieces.text = if (order.cargoPieces > 0) order.cargoPieces.toString() else "Не указано"
            tvCargoWeight.text = "${order.weight} кг"
            tvDepartureStation.text = order.departureStation.ifEmpty { "Не указано" }
            tvDestinationStation.text = order.destinationStation.ifEmpty { "Не указано" }
            tvConsigneeName.text = order.consigneeName.ifEmpty { "Не указано" }
            tvConsigneeAddress.text = order.consigneePostalAddress.ifEmpty { "Не указано" }
            tvUnloadingContact.text = order.unloadingContactPerson.ifEmpty { "Не указано" }
            tvNotes.text = order.notes.ifEmpty { "Нет примечаний" }
            tvTerminal.text = order.emptyContainerTerminal.ifEmpty { "Не указано" }

            updateDriverStatus()
        }
    }

    private fun showAddTerminalDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_terminal, null)
        val etTerminal = dialogView.findViewById<EditText>(R.id.etTerminal)

        AlertDialog.Builder(this)
            .setTitle("Добавить терминал вывоза")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { dialog, _ ->
                val terminalName = etTerminal.text.toString().trim()
                if (terminalName.isNotEmpty()) {
                    // Обновляем заявку
                    currentOrder = currentOrder?.copy(emptyContainerTerminal = terminalName)
                    tvTerminal.text = terminalName
                    showMessage("Терминал добавлен")
                } else {
                    showMessage("Введите название терминала")
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showDriverSelectionDialog() {
        showLoading(true)

        // Имитация загрузки списка водителей
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            showLoading(false)

            // Временный список водителей (в реальном приложении из базы)
            val drivers = listOf(
                Driver("driver1", "Петров Петр", "5 км", "10 мин"),
                Driver("driver2", "Иванов Иван", "8 км", "15 мин"),
                Driver("driver3", "Сидоров Алексей", "3 км", "8 мин")
            )

            val driverNames = drivers.map { "${it.name} (${it.distance}, ${it.time})" }

            AlertDialog.Builder(this)
                .setTitle("Выберите водителя")
                .setItems(driverNames.toTypedArray()) { dialog, which ->
                    selectedDriverId = drivers[which].id
                    tvDriverStatus.text = "Выбран: ${drivers[which].name}"
                    showMessage("Водитель ${drivers[which].name} выбран")
                    dialog.dismiss()
                }
                .setNegativeButton("Отмена", null)
                .show()

        }, 1000)
    }

    private fun sendOrderToDriver() {
        showLoading(true)

        // Имитация отправки заявки водителю
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            showLoading(false)

            // Случайный результат принятия/отклонения для демонстрации
            val isAccepted = Random().nextBoolean()

            if (isAccepted) {
                // Водитель принял заявку
                currentOrder = currentOrder?.copy(
                    assignedDriverId = selectedDriverId,
                    status = "IN_PROGRESS"
                )
                tvDriverStatus.text = "✅ Принято водителем"
                showMessage("Заявка принята водителем!")

                // TODO: Переместить заявку в "Текущие"

            } else {
                // Водитель отклонил заявку
                tvDriverStatus.text = "❌ Отклонено водителем"
                showMessage("Водитель отклонил заявку. Выберите другого водителя.")
                selectedDriverId = null
            }

        }, 2000)
    }

    private fun updateDriverStatus() {
        when (currentOrder?.assignedDriverId) {
            null -> tvDriverStatus.text = "Водитель не назначен"
            else -> {
                if (currentOrder?.status == "IN_PROGRESS") {
                    tvDriverStatus.text = "✅ Принято водителем"
                } else {
                    tvDriverStatus.text = "Водитель назначен (ожидание)"
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Временный класс водителя для демонстрации
    data class Driver(
        val id: String,
        val name: String,
        val distance: String,
        val time: String
    )
}