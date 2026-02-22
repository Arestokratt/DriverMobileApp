package com.example.drivermobileapp.driver

import OrderDriver
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.drivermobileapp.R
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Locale

class Stage3DetailActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var tvStageTitle: TextView
    private lateinit var etWarehouseAddress: TextInputEditText
    private lateinit var etWarehouseContact: TextInputEditText
    private lateinit var etWarehouseArrivalTime: TextInputEditText
    private lateinit var etWarehouseDepartureTime: TextInputEditText
    private lateinit var btnArrived: Button
    private lateinit var btnDeparted: Button
    private lateinit var btnPhotos: Button
    private lateinit var btnDocuments: Button
    private lateinit var tvPhotosCount: TextView
    private lateinit var tvDocumentsCount: TextView
    private lateinit var tvStageStatus: TextView

    private var currentOrder: OrderDriver? = null
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stage3_detail)

        currentOrder = intent.getSerializableExtra("ORDER") as? OrderDriver

        initViews()
        setupClickListeners()
        populateFormData()
        updateUIState()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvStageTitle = findViewById(R.id.tvStageTitle)
        etWarehouseAddress = findViewById(R.id.etWarehouseAddress)
        etWarehouseContact = findViewById(R.id.etWarehouseContact)
        etWarehouseArrivalTime = findViewById(R.id.etWarehouseArrivalTime)
        etWarehouseDepartureTime = findViewById(R.id.etWarehouseDepartureTime)
        btnArrived = findViewById(R.id.btnArrived)
        btnDeparted = findViewById(R.id.btnDeparted)
        btnPhotos = findViewById(R.id.btnPhotos)
        btnDocuments = findViewById(R.id.btnDocuments)
        tvPhotosCount = findViewById(R.id.tvPhotosCount)
        tvDocumentsCount = findViewById(R.id.tvDocumentsCount)
        tvStageStatus = findViewById(R.id.tvStageStatus)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish() // Возврат к этапам заявки
        }

        btnArrived.setOnClickListener {
            recordWarehouseArrivalTime()
        }

        btnDeparted.setOnClickListener {
            recordWarehouseDepartureTime()
        }

        btnPhotos.setOnClickListener {
            // Переход к активности фотографий погрузки
            val intent = Intent(this, PhotosActivity::class.java).apply {
                putExtra("ORDER", currentOrder)
                putExtra("PHOTO_TYPE", "warehouse") // Тип фото для склада
            }
            startActivity(intent)
        }

        btnDocuments.setOnClickListener {
            // Переход к активности документов склада
            val intent = Intent(this, DocumentsActivity::class.java).apply {
                putExtra("ORDER", currentOrder)
                putExtra("DOCUMENT_TYPE", "warehouse") // Тип документов для склада
            }
            startActivity(intent)
        }
    }

    private fun populateFormData() {
        currentOrder?.let { order ->
            tvStageTitle.text = "Этап №3. Склад №${order.number}"
            etWarehouseAddress.setText(order.loadingAddress ?: "Адрес не указан")
            etWarehouseContact.setText(order.loadingContact ?: "Контакт не указан")

            // Заполняем временные метки если они есть
            order.stages.stage3.warehouseArrivalTime?.let { time ->
                etWarehouseArrivalTime.setText(dateFormat.format(time))
            }

            order.stages.stage3.warehouseDepartureTime?.let { time ->
                etWarehouseDepartureTime.setText(dateFormat.format(time))
            }

            // Обновляем счетчики
            updateCounters()
        }
    }

    private fun updateUIState() {
        currentOrder?.let { order ->
            val stage = order.stages.stage3

            // Блокируем кнопки в зависимости от состояния
            btnArrived.isEnabled = stage.warehouseArrivalTime == null
            btnDeparted.isEnabled = stage.warehouseArrivalTime != null && stage.warehouseDepartureTime == null

            // Обновляем статус этапа
            when {
                stage.warehouseDepartureTime != null -> {
                    tvStageStatus.text = "Статус: ✅ Завершено"
                    tvStageStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.green_light))
                }
                stage.warehouseArrivalTime != null -> {
                    tvStageStatus.text = "Статус: 🏭 На складе"
                    tvStageStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.orange_light))
                }
                else -> {
                    tvStageStatus.text = "Статус: ⏳ В процессе"
                    tvStageStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_light))
                }
            }
        }
    }

    private fun recordWarehouseArrivalTime() {
        currentOrder?.let { order ->
            val arrivalTime = System.currentTimeMillis()

            // TODO: Сохранить warehouseArrivalTime в базу данных/на сервер
            // orderRepository.updateStage3ArrivalTime(order.id, arrivalTime)

            // Обновляем UI
            etWarehouseArrivalTime.setText(dateFormat.format(arrivalTime))
            showMessage("Время прибытия на склад зафиксировано")
            updateUIState()
        }
    }

    private fun recordWarehouseDepartureTime() {
        currentOrder?.let { order ->
            val departureTime = System.currentTimeMillis()

            // TODO: Сохранить warehouseDepartureTime в базу данных/на сервер
            // orderRepository.updateStage3DepartureTime(order.id, departureTime)

            // Обновляем UI
            etWarehouseDepartureTime.setText(dateFormat.format(departureTime))
            showMessage("Время выезда со склада зафиксировано")
            updateUIState()
        }
    }

    private fun updateCounters() {
        currentOrder?.let { order ->
            val stage = order.stages.stage3
            tvPhotosCount.text = "Фото: ${stage.loadingPhotos.size}"
            tvDocumentsCount.text = "Документы: ${stage.warehouseDocuments.size}"
        }
    }

    private fun showMessage(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}