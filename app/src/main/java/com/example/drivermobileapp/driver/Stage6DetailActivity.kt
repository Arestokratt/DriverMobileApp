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

class Stage6DetailActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var tvStageTitle: TextView
    private lateinit var etUnloadingAddress: TextInputEditText
    private lateinit var etUnloadingContact: TextInputEditText
    private lateinit var etCargoName: TextInputEditText
    private lateinit var etCargoWeight: TextInputEditText
    private lateinit var etUnloadingArrivalTime: TextInputEditText
    private lateinit var etUnloadingDepartureTime: TextInputEditText
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
        setContentView(R.layout.activity_stage6_detail)

        currentOrder = intent.getSerializableExtra("ORDER") as? OrderDriver

        initViews()
        setupClickListeners()
        populateFormData()
        updateUIState()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvStageTitle = findViewById(R.id.tvStageTitle)
        etUnloadingAddress = findViewById(R.id.etUnloadingAddress)
        etUnloadingContact = findViewById(R.id.etUnloadingContact)
        etCargoName = findViewById(R.id.etCargoName)
        etCargoWeight = findViewById(R.id.etCargoWeight)
        etUnloadingArrivalTime = findViewById(R.id.etUnloadingArrivalTime)
        etUnloadingDepartureTime = findViewById(R.id.etUnloadingDepartureTime)
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
            recordUnloadingArrivalTime()
        }

        btnDeparted.setOnClickListener {
            recordUnloadingDepartureTime()
        }

        btnPhotos.setOnClickListener {
            // Переход к активности фотографий выгрузки
            val intent = Intent(this, PhotosActivity::class.java).apply {
                putExtra("ORDER", currentOrder)
                putExtra("PHOTO_TYPE", "unloading") // Тип фото для выгрузки
            }
            startActivity(intent)
        }

        btnDocuments.setOnClickListener {
            // Переход к активности документов выгрузки
            val intent = Intent(this, DocumentsActivity::class.java).apply {
                putExtra("ORDER", currentOrder)
                putExtra("DOCUMENT_TYPE", "unloading") // Тип документов для выгрузки
            }
            startActivity(intent)
        }
    }

    private fun populateFormData() {
        currentOrder?.let { order ->
            tvStageTitle.text = "Этап №6. Выдача груза №${order.number}"
            etUnloadingAddress.setText(order.unloadingAddress ?: "Адрес не указан")
            etUnloadingContact.setText(order.unloadingContact ?: "Контакт не указан")
            etCargoName.setText(order.cargoName ?: "")
            etCargoWeight.setText(order.cargoWeight?.let { "$it кг" } ?: "")

            // Заполняем временные метки если они есть
            order.stages.stage6.unloadingArrivalTime?.let { time ->
                etUnloadingArrivalTime.setText(dateFormat.format(time))
            }

            order.stages.stage6.unloadingDepartureTime?.let { time ->
                etUnloadingDepartureTime.setText(dateFormat.format(time))
            }

            // Обновляем счетчики
            updateCounters()
        }
    }

    private fun updateUIState() {
        currentOrder?.let { order ->
            val stage = order.stages.stage6

            // Блокируем кнопки в зависимости от состояния
            btnArrived.isEnabled = stage.unloadingArrivalTime == null
            btnDeparted.isEnabled = stage.unloadingArrivalTime != null && stage.unloadingDepartureTime == null

            // Обновляем статус этапа
            when {
                stage.unloadingDepartureTime != null -> {
                    tvStageStatus.text = "Статус: ✅ Завершено"
                    tvStageStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.green_light))
                }
                stage.unloadingArrivalTime != null -> {
                    tvStageStatus.text = "Статус: 🏭 На складе выгрузки"
                    tvStageStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.orange_light))
                }
                else -> {
                    tvStageStatus.text = "Статус: ⏳ В процессе"
                    tvStageStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_light))
                }
            }
        }
    }

    private fun recordUnloadingArrivalTime() {
        currentOrder?.let { order ->
            val arrivalTime = System.currentTimeMillis()

            // TODO: Сохранить unloadingArrivalTime в базу данных/на сервер
            // orderRepository.updateStage6ArrivalTime(order.id, arrivalTime)

            // Обновляем UI
            etUnloadingArrivalTime.setText(dateFormat.format(arrivalTime))
            showMessage("Время прибытия на склад выгрузки зафиксировано")
            updateUIState()
        }
    }

    private fun recordUnloadingDepartureTime() {
        currentOrder?.let { order ->
            val departureTime = System.currentTimeMillis()

            // TODO: Сохранить unloadingDepartureTime в базу данных/на сервер
            // orderRepository.updateStage6DepartureTime(order.id, departureTime)

            // Обновляем UI
            etUnloadingDepartureTime.setText(dateFormat.format(departureTime))
            showMessage("Время выезда со склада выгрузки зафиксировано")
            updateUIState()
        }
    }

    private fun updateCounters() {
        currentOrder?.let { order ->
            val stage = order.stages.stage6
            tvPhotosCount.text = "Фото: ${stage.unloadingPhotos.size}"
            tvDocumentsCount.text = "Документы: ${stage.unloadingDocuments.size}"
        }
    }

    private fun showMessage(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}