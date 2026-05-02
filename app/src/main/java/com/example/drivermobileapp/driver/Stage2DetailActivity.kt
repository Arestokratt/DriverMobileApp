package com.example.drivermobileapp.driver

import OrderDriver
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.drivermobileapp.R
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import androidx.core.content.ContextCompat
import java.util.Locale

class Stage2DetailActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var tvStageTitle: TextView
    private lateinit var etTerminalAddress: TextInputEditText
    private lateinit var etArrivalTime: TextInputEditText
    private lateinit var etDepartureTime: TextInputEditText
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
        setContentView(R.layout.activity_stage2_detail)

        currentOrder = intent.getSerializableExtra("ORDER") as? OrderDriver

        initViews()
        setupClickListeners()
        populateFormData()
        updateUIState()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvStageTitle = findViewById(R.id.tvStageTitle)
        etTerminalAddress = findViewById(R.id.etTerminalAddress)
        etArrivalTime = findViewById(R.id.etArrivalTime)
        etDepartureTime = findViewById(R.id.etDepartureTime)
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
            recordArrivalTime()
        }

        btnDeparted.setOnClickListener {
            recordDepartureTime()
        }

        btnPhotos.setOnClickListener {
            // Переход к активности фотографий
            val intent = Intent(this, PhotosActivity::class.java).apply {
                putExtra("ORDER", currentOrder)
                putExtra("PHOTO_TYPE", "terminal") // Тип фото для терминала
            }
            startActivity(intent)
        }

        btnDocuments.setOnClickListener {
            // Переход к активности документов
            val intent = Intent(this, DocumentsActivity::class.java).apply {
                putExtra("ORDER", currentOrder)
                putExtra("DOCUMENT_TYPE", "terminal") // Тип документов для терминала
            }
            startActivity(intent)
        }
    }

    private fun populateFormData() {
        currentOrder?.let { order ->
            tvStageTitle.text = "Этап №2: Получение документов №${order.number}"
            etTerminalAddress.setText(order.terminalPickupAddress ?: "Адрес не указан")

            // Заполняем временные метки если они есть
            order.stages.stage2.arrivalTime?.let { time ->
                etArrivalTime.setText(dateFormat.format(time))
            }

            order.stages.stage2.departureTime?.let { time ->
                etDepartureTime.setText(dateFormat.format(time))
            }

            // Обновляем счетчики
            updateCounters()
        }
    }

    private fun updateUIState() {
        currentOrder?.let { order ->
            val stage = order.stages.stage2

            // Блокируем кнопки в зависимости от состояния
            btnArrived.isEnabled = stage.arrivalTime == null
            btnDeparted.isEnabled = stage.arrivalTime != null && stage.departureTime == null

            // Обновляем статус этапа
            when {
                stage.departureTime != null -> {
                    tvStageStatus.text = "Статус: ✅ Завершено"
                    tvStageStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.green_light))
                }
                stage.arrivalTime != null -> {
                    tvStageStatus.text = "Статус: 🚚 На терминале"
                    tvStageStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.orange_light))
                }
                else -> {
                    tvStageStatus.text = "Статус: ⏳ В процессе"
                    tvStageStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_light))
                }
            }
        }
    }

    private fun recordArrivalTime() {
        currentOrder?.let { order ->
            val arrivalTime = System.currentTimeMillis()

            // TODO: Сохранить arrivalTime в базу данных/на сервер
            // orderRepository.updateStage2ArrivalTime(order.id, arrivalTime)

            // Обновляем UI
            etArrivalTime.setText(dateFormat.format(arrivalTime))
            showMessage("Время прибытия зафиксировано")
            updateUIState()
        }
    }

    private fun recordDepartureTime() {
        currentOrder?.let { order ->
            val departureTime = System.currentTimeMillis()

            // TODO: Сохранить departureTime в базу данных/на сервер
            // orderRepository.updateStage2DepartureTime(order.id, departureTime)

            // Обновляем UI
            etDepartureTime.setText(dateFormat.format(departureTime))
            showMessage("Время выезда зафиксировано")
            updateUIState()
        }
    }

    private fun updateCounters() {
        currentOrder?.let { order ->
            val stage = order.stages.stage2
            tvPhotosCount.text = "Фото: ${stage.containerPhotos.size}"
            tvDocumentsCount.text = "Документы: ${stage.terminalDocuments.size}"
        }
    }

    private fun showMessage(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}