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

class Stage7DetailActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var tvStageTitle: TextView
    private lateinit var etTerminalAddress: TextInputEditText
    private lateinit var etContainerType: TextInputEditText
    private lateinit var etContainerCount: TextInputEditText
    private lateinit var etTerminalArrivalTime: TextInputEditText
    private lateinit var etTerminalDepartureTime: TextInputEditText
    private lateinit var btnArrived: Button
    private lateinit var btnDeparted: Button
    private lateinit var btnDocuments: Button
    private lateinit var tvDocumentsCount: TextView
    private lateinit var tvStageStatus: TextView
    private lateinit var tvCompletionInfo: TextView

    private var currentOrder: OrderDriver? = null
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stage7_detail)

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
        etContainerType = findViewById(R.id.etContainerType)
        etContainerCount = findViewById(R.id.etContainerCount)
        etTerminalArrivalTime = findViewById(R.id.etTerminalArrivalTime)
        etTerminalDepartureTime = findViewById(R.id.etTerminalDepartureTime)
        btnArrived = findViewById(R.id.btnArrived)
        btnDeparted = findViewById(R.id.btnDeparted)
        btnDocuments = findViewById(R.id.btnDocuments)
        tvDocumentsCount = findViewById(R.id.tvDocumentsCount)
        tvStageStatus = findViewById(R.id.tvStageStatus)
        tvCompletionInfo = findViewById(R.id.tvCompletionInfo)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish() // Возврат к этапам заявки
        }

        btnArrived.setOnClickListener {
            recordTerminalArrivalTime()
        }

        btnDeparted.setOnClickListener {
            recordTerminalDepartureTime()
        }

        btnDocuments.setOnClickListener {
            // Переход к активности документов терминала сдачи
            val intent = Intent(this, DocumentsActivity::class.java).apply {
                putExtra("ORDER", currentOrder)
                putExtra("DOCUMENT_TYPE", "return_terminal") // Тип документов для терминала сдачи
            }
            startActivity(intent)
        }
    }

    private fun populateFormData() {
        currentOrder?.let { order ->
            tvStageTitle.text = "Этап №7. Терминал сдачи №${order.number}"

            // Заполняем информацию о терминале
            etTerminalAddress.setText(order.terminalReturnAddress ?: "Адрес не указан")

            // Заполняем информацию о контейнере
            etContainerType.setText(order.containerType ?: "")
            etContainerCount.setText(order.containerCount?.toString() ?: "")

            // Заполняем временные метки если они есть
            order.stages.stage7.returnTerminalArrivalTime?.let { time ->
                etTerminalArrivalTime.setText(dateFormat.format(time))
            }

            order.stages.stage7.returnTerminalDepartureTime?.let { time ->
                etTerminalDepartureTime.setText(dateFormat.format(time))
            }

            // Обновляем счетчики
            updateCounters()
        }
    }

    private fun updateUIState() {
        currentOrder?.let { order ->
            val stage = order.stages.stage7

            // Блокируем кнопки в зависимости от состояния
            btnArrived.isEnabled = stage.returnTerminalArrivalTime == null
            btnDeparted.isEnabled = stage.returnTerminalArrivalTime != null && stage.returnTerminalDepartureTime == null

            // Обновляем статус этапа
            when {
                stage.returnTerminalDepartureTime != null -> {
                    tvStageStatus.text = "Статус: ✅ Заявка завершена"
                    tvStageStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.green_light))
                    tvCompletionInfo.text = "🎉 Заявка успешно завершена! Контейнер сдан на терминал."
                }
                stage.returnTerminalArrivalTime != null -> {
                    tvStageStatus.text = "Статус: 🏭 На терминале сдачи"
                    tvStageStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.orange_light))
                    tvCompletionInfo.text = "ℹ️ После выезда с терминала заявка будет завершена"
                }
                else -> {
                    tvStageStatus.text = "Статус: ⏳ В процессе"
                    tvStageStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_light))
                    tvCompletionInfo.text = "ℹ️ После завершения этого этапа заявка будет считаться выполненной"
                }
            }
        }
    }

    private fun recordTerminalArrivalTime() {
        currentOrder?.let { order ->
            val arrivalTime = System.currentTimeMillis()

            // TODO: Сохранить returnTerminalArrivalTime в базу данных/на сервер
            // orderRepository.updateStage7ArrivalTime(order.id, arrivalTime)

            // Обновляем UI
            etTerminalArrivalTime.setText(dateFormat.format(arrivalTime))
            showMessage("Время прибытия на терминал сдачи зафиксировано")
            updateUIState()
        }
    }

    private fun recordTerminalDepartureTime() {
        currentOrder?.let { order ->
            val departureTime = System.currentTimeMillis()

            // TODO: Сохранить returnTerminalDepartureTime в базу данных/на сервер
            // orderRepository.updateStage7DepartureTime(order.id, departureTime)

            // Обновляем UI
            etTerminalDepartureTime.setText(dateFormat.format(departureTime))
            showMessage("Время выезда с терминала сдачи зафиксировано. Заявка завершена!")
            updateUIState()

            // Помечаем заявку как завершенную
            // orderRepository.completeOrder(order.id)
        }
    }

    private fun updateCounters() {
        currentOrder?.let { order ->
            val stage = order.stages.stage7
            tvDocumentsCount.text = "Документы: ${stage.returnTerminalDocuments.size}"
        }
    }

    private fun showMessage(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}