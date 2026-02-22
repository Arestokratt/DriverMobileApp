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

class Stage4DetailActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var tvStageTitle: TextView
    private lateinit var etStationName: TextInputEditText
    private lateinit var etStationContact: TextInputEditText
    private lateinit var etStationArrivalTime: TextInputEditText
    private lateinit var etStationDepartureTime: TextInputEditText
    private lateinit var btnArrived: Button
    private lateinit var btnDeparted: Button
    private lateinit var btnDocuments: Button
    private lateinit var tvDocumentsCount: TextView
    private lateinit var tvStageStatus: TextView

    private var currentOrder: OrderDriver? = null
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stage4_detail)

        currentOrder = intent.getSerializableExtra("ORDER") as? OrderDriver

        initViews()
        setupClickListeners()
        populateFormData()
        updateUIState()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvStageTitle = findViewById(R.id.tvStageTitle)
        etStationName = findViewById(R.id.etStationName)
        etStationContact = findViewById(R.id.etStationContact)
        etStationArrivalTime = findViewById(R.id.etStationArrivalTime)
        etStationDepartureTime = findViewById(R.id.etStationDepartureTime)
        btnArrived = findViewById(R.id.btnArrived)
        btnDeparted = findViewById(R.id.btnDeparted)
        btnDocuments = findViewById(R.id.btnDocuments)
        tvDocumentsCount = findViewById(R.id.tvDocumentsCount)
        tvStageStatus = findViewById(R.id.tvStageStatus)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish() // Возврат к этапам заявки
        }

        btnArrived.setOnClickListener {
            recordStationArrivalTime()
        }

        btnDeparted.setOnClickListener {
            recordStationDepartureTime()
        }

        btnDocuments.setOnClickListener {
            // Переход к активности документов станции
            val intent = Intent(this, DocumentsActivity::class.java).apply {
                putExtra("ORDER", currentOrder)
                putExtra("DOCUMENT_TYPE", "departure_station") // Тип документов для станции отправления
            }
            startActivity(intent)
        }
    }

    private fun populateFormData() {
        currentOrder?.let { order ->
            tvStageTitle.text = "Этап №4. Станция отправления №${order.number}"
            etStationName.setText(order.departureStation ?: "Станция не указана")
            etStationContact.setText(order.departureContact ?: "Контакт не указан")

            // Заполняем временные метки если они есть
            order.stages.stage4.departureStationArrivalTime?.let { time ->
                etStationArrivalTime.setText(dateFormat.format(time))
            }

            order.stages.stage4.departureStationDepartureTime?.let { time ->
                etStationDepartureTime.setText(dateFormat.format(time))
            }

            // Обновляем счетчики
            updateCounters()
        }
    }

    private fun updateUIState() {
        currentOrder?.let { order ->
            val stage = order.stages.stage4

            // Блокируем кнопки в зависимости от состояния
            btnArrived.isEnabled = stage.departureStationArrivalTime == null
            btnDeparted.isEnabled = stage.departureStationArrivalTime != null && stage.departureStationDepartureTime == null

            // Обновляем статус этапа
            when {
                stage.departureStationDepartureTime != null -> {
                    tvStageStatus.text = "Статус: ✅ Завершено"
                    tvStageStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.green_light))
                }
                stage.departureStationArrivalTime != null -> {
                    tvStageStatus.text = "Статус: 🚉 На станции"
                    tvStageStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.orange_light))
                }
                else -> {
                    tvStageStatus.text = "Статус: ⏳ В процессе"
                    tvStageStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_light))
                }
            }
        }
    }

    private fun recordStationArrivalTime() {
        currentOrder?.let { order ->
            val arrivalTime = System.currentTimeMillis()

            // TODO: Сохранить departureStationArrivalTime в базу данных/на сервер
            // orderRepository.updateStage4ArrivalTime(order.id, arrivalTime)

            // Обновляем UI
            etStationArrivalTime.setText(dateFormat.format(arrivalTime))
            showMessage("Время прибытия на станцию зафиксировано")
            updateUIState()
        }
    }

    private fun recordStationDepartureTime() {
        currentOrder?.let { order ->
            val departureTime = System.currentTimeMillis()

            // TODO: Сохранить departureStationDepartureTime в базу данных/на сервер
            // orderRepository.updateStage4DepartureTime(order.id, departureTime)

            // Обновляем UI
            etStationDepartureTime.setText(dateFormat.format(departureTime))
            showMessage("Время выезда со станции зафиксировано")
            updateUIState()
        }
    }

    private fun updateCounters() {
        currentOrder?.let { order ->
            val stage = order.stages.stage4
            tvDocumentsCount.text = "Документы: ${stage.departureStationDocuments.size}"
        }
    }

    private fun showMessage(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}