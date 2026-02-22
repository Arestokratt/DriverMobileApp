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

class Stage2TerminalActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var btnPhotos: Button
    private lateinit var btnDocuments: Button
    private lateinit var tvOrderNumber: TextView
    private lateinit var tvAcceptedTime: TextView
    private lateinit var tvArrivedTime: TextView
    private lateinit var tvDepartedTime: TextView
    private lateinit var tvTerminalName: TextView
    private lateinit var tvContainerNumber: TextView
    private lateinit var tvDriverNotes: TextView
    private lateinit var progressBar: ProgressBar

    private var currentOrder: Order1C? = null
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stage2_terminal)

        currentOrder = intent.getSerializableExtra("ORDER_DATA") as? Order1C
        currentUser = intent.getSerializableExtra("USER_DATA") as? User

        initViews()
        setupClickListeners()
        displayTerminalData()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnPhotos = findViewById(R.id.btnPhotos)
        btnDocuments = findViewById(R.id.btnDocuments)
        tvOrderNumber = findViewById(R.id.tvOrderNumber)
        tvAcceptedTime = findViewById(R.id.tvAcceptedTime)
        tvArrivedTime = findViewById(R.id.tvArrivedTime)
        tvDepartedTime = findViewById(R.id.tvDepartedTime)
        tvTerminalName = findViewById(R.id.tvTerminalName)
        tvContainerNumber = findViewById(R.id.tvContainerNumber)
        tvDriverNotes = findViewById(R.id.tvDriverNotes)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish() // Возврат к экрану этапов заявки
        }

        btnPhotos.setOnClickListener {
            // Переход к просмотру фотографий
            val intent = Intent(this, PhotosActivity::class.java)
            intent.putExtra("ORDER_DATA", currentOrder)
            intent.putExtra("USER_DATA", currentUser)
            intent.putExtra("PHOTO_TYPE", "terminal") // Тип фото - терминал
            startActivity(intent)
        }

        btnDocuments.setOnClickListener {
            // Переход к просмотру документов
            val intent = Intent(this, DocumentsActivity::class.java)
            intent.putExtra("ORDER_DATA", currentOrder)
            intent.putExtra("USER_DATA", currentUser)
            startActivity(intent)
        }
    }

    private fun displayTerminalData() {
        currentOrder?.let { order ->
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())

            tvOrderNumber.text = "Заявка №${order.orderNumber}"
            tvTerminalName.text = order.terminalStage.terminalName.ifEmpty { "Не указано" }
            tvContainerNumber.text = order.terminalStage.containerNumber.ifEmpty { "Не указано" }
            tvDriverNotes.text = order.terminalStage.driverNotes.ifEmpty { "Нет заметок" }

            // Отображаем время принятия заявки
            if (order.terminalStage.acceptedTime > 0) {
                tvAcceptedTime.text = dateFormat.format(Date(order.terminalStage.acceptedTime))
                tvAcceptedTime.setTextColor(getColor(R.color.green))
            } else {
                tvAcceptedTime.text = "Ожидание принятия водителем"
                tvAcceptedTime.setTextColor(getColor(R.color.orange))
            }

            // Отображаем время прибытия на терминал
            if (order.terminalStage.arrivedTime > 0) {
                tvArrivedTime.text = dateFormat.format(Date(order.terminalStage.arrivedTime))
                tvArrivedTime.setTextColor(getColor(R.color.green))
            } else {
                tvArrivedTime.text = "Ожидание прибытия на терминал"
                tvArrivedTime.setTextColor(getColor(R.color.orange))
            }

            // Отображаем время выезда с терминала
            if (order.terminalStage.departedTime > 0) {
                tvDepartedTime.text = dateFormat.format(Date(order.terminalStage.departedTime))
                tvDepartedTime.setTextColor(getColor(R.color.green))
            } else {
                tvDepartedTime.text = "Ожидание выезда с терминала"
                tvDepartedTime.setTextColor(getColor(R.color.orange))
            }

            // Обновляем статус этапа
            updateStageStatus()
        }
    }

    private fun updateStageStatus() {
        currentOrder?.let { order ->
            val terminalStage = order.terminalStage

            // Этап считается завершенным, если все временные метки заполнены
            val isCompleted = terminalStage.acceptedTime > 0 &&
                    terminalStage.arrivedTime > 0 &&
                    terminalStage.departedTime > 0

            if (isCompleted) {
                showMessage("Этап 'Терминал вывоза' завершен")
            }
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}