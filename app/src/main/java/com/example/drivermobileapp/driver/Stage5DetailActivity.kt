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

class Stage5DetailActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var tvStageTitle: TextView
    private lateinit var etStationName: TextInputEditText
    private lateinit var etStationContact: TextInputEditText
    private lateinit var etContainerType: TextInputEditText
    private lateinit var etContainerCount: TextInputEditText
    private lateinit var etCargoName: TextInputEditText
    private lateinit var etCargoWeight: TextInputEditText
    private lateinit var etStationArrivalTime: TextInputEditText
    private lateinit var etStationDepartureTime: TextInputEditText
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
        setContentView(R.layout.activity_stage5_detail)

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
        etContainerType = findViewById(R.id.etContainerType)
        etContainerCount = findViewById(R.id.etContainerCount)
        etCargoName = findViewById(R.id.etCargoName)
        etCargoWeight = findViewById(R.id.etCargoWeight)
        etStationArrivalTime = findViewById(R.id.etStationArrivalTime)
        etStationDepartureTime = findViewById(R.id.etStationDepartureTime)
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
            recordStationArrivalTime()
        }

        btnDeparted.setOnClickListener {
            recordStationDepartureTime()
        }

        btnPhotos.setOnClickListener {
            // Переход к активности фотографий контейнера
            val intent = Intent(this, PhotosActivity::class.java).apply {
                putExtra("ORDER", currentOrder)
                putExtra("PHOTO_TYPE", "destination_station") // Тип фото для станции назначения
            }
            startActivity(intent)
        }

        btnDocuments.setOnClickListener {
            // Переход к активности документов станции
            val intent = Intent(this, DocumentsActivity::class.java).apply {
                putExtra("ORDER", currentOrder)
                putExtra("DOCUMENT_TYPE", "destination_station") // Тип документов для станции назначения
            }
            startActivity(intent)
        }
    }

    private fun populateFormData() {
        currentOrder?.let { order ->
            tvStageTitle.text = "Этап №5. Станция назначения №${order.number}"

            // Заполняем информацию о станции
            etStationName.setText(order.destinationStation ?: "Станция не указана")
            etStationContact.setText(order.destinationContact ?: "Контакт не указан")

            // Заполняем информацию о контейнере
            etContainerType.setText(order.containerType ?: "")
            etContainerCount.setText(order.containerCount?.toString() ?: "")

            // Заполняем информацию о грузе
            etCargoName.setText(order.cargoName ?: "")
            etCargoWeight.setText(order.cargoWeight?.let { "$it кг" } ?: "")

            // Заполняем временные метки если они есть
            order.stages.stage5.destinationStationArrivalTime?.let { time ->
                etStationArrivalTime.setText(dateFormat.format(time))
            }

            order.stages.stage5.destinationStationDepartureTime?.let { time ->
                etStationDepartureTime.setText(dateFormat.format(time))
            }

            // Обновляем счетчики
            updateCounters()
        }
    }

    private fun updateUIState() {
        currentOrder?.let { order ->
            val stage = order.stages.stage5

            // Блокируем кнопки в зависимости от состояния
            btnArrived.isEnabled = stage.destinationStationArrivalTime == null
            btnDeparted.isEnabled = stage.destinationStationArrivalTime != null && stage.destinationStationDepartureTime == null

            // Обновляем статус этапа
            when {
                stage.destinationStationDepartureTime != null -> {
                    tvStageStatus.text = "Статус: ✅ Завершено"
                    tvStageStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.green_light))
                }
                stage.destinationStationArrivalTime != null -> {
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

            // TODO: Сохранить destinationStationArrivalTime в базу данных/на сервер
            // orderRepository.updateStage5ArrivalTime(order.id, arrivalTime)

            // Обновляем UI
            etStationArrivalTime.setText(dateFormat.format(arrivalTime))
            showMessage("Время прибытия на станцию назначения зафиксировано")
            updateUIState()
        }
    }

    private fun recordStationDepartureTime() {
        currentOrder?.let { order ->
            val departureTime = System.currentTimeMillis()

            // TODO: Сохранить destinationStationDepartureTime в базу данных/на сервер
            // orderRepository.updateStage5DepartureTime(order.id, departureTime)

            // Обновляем UI
            etStationDepartureTime.setText(dateFormat.format(departureTime))
            showMessage("Время выезда со станции назначения зафиксировано")
            updateUIState()
        }
    }

    private fun updateCounters() {
        currentOrder?.let { order ->
            val stage = order.stages.stage5
            tvPhotosCount.text = "Фото: ${stage.destinationContainerPhotos.size}"
            tvDocumentsCount.text = "Документы: ${stage.destinationStationDocuments.size}"
        }
    }

    private fun showMessage(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}