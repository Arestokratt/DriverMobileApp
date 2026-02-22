package com.example.drivermobileapp.logist

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.models.Order1C
import com.example.drivermobileapp.data.models.User
import java.text.SimpleDateFormat
import java.util.*

class Stage3WarehouseActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var btnPhotos: Button
    private lateinit var btnDocuments: Button
    private lateinit var tvOrderNumber: TextView
    private lateinit var tvArrivedTime: TextView
    private lateinit var tvDepartedTime: TextView
    private lateinit var tvWarehouseName: TextView
    private lateinit var tvLoadingTime: TextView
    private lateinit var tvCargoCondition: TextView
    private lateinit var tvDriverNotes: TextView
    private lateinit var progressBar: ProgressBar

    private var currentOrder: Order1C? = null
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stage3_warehouse)

        currentOrder = intent.getSerializableExtra("ORDER_DATA") as? Order1C
        currentUser = intent.getSerializableExtra("USER_DATA") as? User

        initViews()
        setupClickListeners()
        displayWarehouseData()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnPhotos = findViewById(R.id.btnPhotos)
        btnDocuments = findViewById(R.id.btnDocuments)
        tvOrderNumber = findViewById(R.id.tvOrderNumber)
        tvArrivedTime = findViewById(R.id.tvArrivedTime)
        tvDepartedTime = findViewById(R.id.tvDepartedTime)
        tvWarehouseName = findViewById(R.id.tvWarehouseName)
        tvLoadingTime = findViewById(R.id.tvLoadingTime)
        tvCargoCondition = findViewById(R.id.tvCargoCondition)
        tvDriverNotes = findViewById(R.id.tvDriverNotes)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish() // Возврат к экрану этапов заявки
        }

        btnPhotos.setOnClickListener {
            // Переход к просмотру фотографий погрузки
            val intent = Intent(this, PhotosActivity::class.java)
            intent.putExtra("ORDER_DATA", currentOrder)
            intent.putExtra("USER_DATA", currentUser)
            intent.putExtra("PHOTO_TYPE", "warehouse") // Тип фото - склад
            startActivity(intent)
        }

        btnDocuments.setOnClickListener {
            // Переход к просмотру документов склада
            val intent = Intent(this, DocumentsActivity::class.java)
            intent.putExtra("ORDER_DATA", currentOrder)
            intent.putExtra("USER_DATA", currentUser)
            intent.putExtra("DOCUMENT_TYPE", "warehouse") // Тип документов - склад
            startActivity(intent)
        }
    }

    private fun displayWarehouseData() {
        currentOrder?.let { order ->
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())

            tvOrderNumber.text = "Заявка №${order.orderNumber}"
            tvWarehouseName.text = order.warehouseStage.warehouseName.ifEmpty {
                order.fromAddress.ifEmpty { "Не указано" }
            }
            tvLoadingTime.text = order.warehouseStage.loadingTime.ifEmpty { "Не указано" }
            tvCargoCondition.text = order.warehouseStage.cargoCondition.ifEmpty { "Нет информации" }
            tvDriverNotes.text = order.warehouseStage.driverNotes.ifEmpty { "Нет заметок" }

            // Отображаем время прибытия на склад
            if (order.warehouseStage.arrivedTime > 0) {
                tvArrivedTime.text = dateFormat.format(Date(order.warehouseStage.arrivedTime))
                tvArrivedTime.setTextColor(getColor(R.color.green))
            } else {
                tvArrivedTime.text = "Ожидание прибытия на склад"
                tvArrivedTime.setTextColor(getColor(R.color.orange))
            }

            // Отображаем время выезда со склада
            if (order.warehouseStage.departedTime > 0) {
                tvDepartedTime.text = dateFormat.format(Date(order.warehouseStage.departedTime))
                tvDepartedTime.setTextColor(getColor(R.color.green))
            } else {
                tvDepartedTime.text = "Ожидание выезда со склада"
                tvDepartedTime.setTextColor(getColor(R.color.orange))
            }

            // Обновляем статус этапа
            updateStageStatus()
        }
    }

    private fun updateStageStatus() {
        currentOrder?.let { order ->
            val warehouseStage = order.warehouseStage

            // Этап считается завершенным, если все временные метки заполнены
            val isCompleted = warehouseStage.arrivedTime > 0 &&
                    warehouseStage.departedTime > 0

            if (isCompleted) {
                showMessage("Этап 'Склад' завершен")
            }
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}