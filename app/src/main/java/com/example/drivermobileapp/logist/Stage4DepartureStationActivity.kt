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

class Stage4DepartureStationActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var btnDocuments: Button
    private lateinit var tvOrderNumber: TextView
    private lateinit var tvArrivedTime: TextView
    private lateinit var tvDepartedTime: TextView
    private lateinit var tvStationName: TextView
    private lateinit var tvTrainNumber: TextView
    private lateinit var tvDepartureTime: TextView
    private lateinit var tvDriverNotes: TextView
    private lateinit var progressBar: ProgressBar

    private var currentOrder: Order1C? = null
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stage4_departure_station)

        currentOrder = intent.getSerializableExtra("ORDER_DATA") as? Order1C
        currentUser = intent.getSerializableExtra("USER_DATA") as? User

        initViews()
        setupClickListeners()
        displayDepartureStationData()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnDocuments = findViewById(R.id.btnDocuments)
        tvOrderNumber = findViewById(R.id.tvOrderNumber)
        tvArrivedTime = findViewById(R.id.tvArrivedTime)
        tvDepartedTime = findViewById(R.id.tvDepartedTime)
        tvStationName = findViewById(R.id.tvStationName)
        tvTrainNumber = findViewById(R.id.tvTrainNumber)
        tvDepartureTime = findViewById(R.id.tvDepartureTime)
        tvDriverNotes = findViewById(R.id.tvDriverNotes)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish() // Возврат к экрану этапов заявки
        }

        btnDocuments.setOnClickListener {
            // Переход к просмотру документов станции отправления
            val intent = Intent(this, DocumentsActivity::class.java)
            intent.putExtra("ORDER_DATA", currentOrder)
            intent.putExtra("USER_DATA", currentUser)
            intent.putExtra("DOCUMENT_TYPE", "departure_station") // Тип документов - станция отправления
            startActivity(intent)
        }
    }

    private fun displayDepartureStationData() {
        currentOrder?.let { order ->
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())

            tvOrderNumber.text = "Заявка №${order.orderNumber}"
            tvStationName.text = order.departureStationStage.stationName.ifEmpty {
                order.departureStation.ifEmpty { "Не указано" }
            }
            tvTrainNumber.text = order.departureStationStage.trainNumber.ifEmpty { "Не указан" }
            tvDriverNotes.text = order.departureStationStage.driverNotes.ifEmpty { "Нет заметок" }

            // Отображаем время отправления поезда
            if (order.departureStationStage.departureTime > 0) {
                tvDepartureTime.text = dateFormat.format(Date(order.departureStationStage.departureTime))
                tvDepartureTime.setTextColor(getColor(R.color.blue))
            } else {
                tvDepartureTime.text = "Время отправления не установлено"
                tvDepartureTime.setTextColor(getColor(R.color.orange))
            }

            // Отображаем время прибытия на станцию
            if (order.departureStationStage.arrivedTime > 0) {
                tvArrivedTime.text = dateFormat.format(Date(order.departureStationStage.arrivedTime))
                tvArrivedTime.setTextColor(getColor(R.color.green))
            } else {
                tvArrivedTime.text = "Ожидание прибытия на станцию"
                tvArrivedTime.setTextColor(getColor(R.color.orange))
            }

            // Отображаем время выезда со станции
            if (order.departureStationStage.departedTime > 0) {
                tvDepartedTime.text = dateFormat.format(Date(order.departureStationStage.departedTime))
                tvDepartedTime.setTextColor(getColor(R.color.green))
            } else {
                tvDepartedTime.text = "Ожидание выезда со станции"
                tvDepartedTime.setTextColor(getColor(R.color.orange))
            }

            // Обновляем статус этапа
            updateStageStatus()
        }
    }

    private fun updateStageStatus() {
        currentOrder?.let { order ->
            val stationStage = order.departureStationStage

            // Этап считается завершенным, если все временные метки заполнены
            val isCompleted = stationStage.arrivedTime > 0 &&
                    stationStage.departedTime > 0

            if (isCompleted) {
                showMessage("Этап 'Станция отправления' завершен")
            }
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}