package com.example.drivermobileapp.logist

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.models.Order1C
import com.example.drivermobileapp.data.models.User
import java.text.SimpleDateFormat
import java.util.*

class Stage7ContainerReturnActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var btnDocuments: Button
    private lateinit var btnComplete: Button

    private lateinit var tvTerminalInfo: TextView
    private lateinit var etArrivedTime: EditText
    private lateinit var etDepartedTime: EditText
    private lateinit var etTerminalName: EditText
    private lateinit var etContainerCondition: EditText
    private lateinit var etDriverNotes: EditText

    private var currentOrder: Order1C? = null
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stage7_container_return)

        currentOrder = intent.getSerializableExtra("ORDER_DATA") as? Order1C
        currentUser = intent.getSerializableExtra("USER_DATA") as? User

        initViews()
        setupClickListeners()
        displayStage7Data()
        disableAllFields()
        updateCompleteButtonState()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnDocuments = findViewById(R.id.btnDocuments)
        btnComplete = findViewById(R.id.btnComplete)

        tvTerminalInfo = findViewById(R.id.tvTerminalInfo)
        etArrivedTime = findViewById(R.id.etArrivedTime)
        etDepartedTime = findViewById(R.id.etDepartedTime)
        etTerminalName = findViewById(R.id.etTerminalName)
        etContainerCondition = findViewById(R.id.etContainerCondition)
        etDriverNotes = findViewById(R.id.etDriverNotes)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish() // Возврат к экрану этапов заявки
        }

        btnDocuments.setOnClickListener {
            openDocumentsActivity()
        }

        btnComplete.setOnClickListener {
            markStageAsCompleted()
        }
    }

    private fun displayStage7Data() {
        currentOrder?.let { order ->
            // Заголовок
            title = "Этап №7. Терминал сдачи - Заявка №${order.orderNumber}"

            // Информация о терминале
            val terminalInfo = """
                Терминал сдачи порожнего контейнера:
                ${order.emptyContainerTerminal}
            """.trimIndent()
            tvTerminalInfo.text = terminalInfo

            // Временные отметки
            val timeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

            if (order.containerReturnStage.arrivedTime > 0) {
                etArrivedTime.setText(timeFormat.format(Date(order.containerReturnStage.arrivedTime)))
            } else {
                etArrivedTime.setText("Не прибыл")
            }

            if (order.containerReturnStage.departedTime > 0) {
                etDepartedTime.setText(timeFormat.format(Date(order.containerReturnStage.departedTime)))
            } else {
                etDepartedTime.setText("Не выехал")
            }

            // Дополнительная информация
            etTerminalName.setText(order.containerReturnStage.terminalName.ifEmpty { "Не указано" })
            etContainerCondition.setText(order.containerReturnStage.containerCondition.ifEmpty { "Не указано" })
            etDriverNotes.setText(order.containerReturnStage.driverNotes.ifEmpty { "Нет заметок" })
        }
    }

    private fun disableAllFields() {
        val allFields = listOf(
            etArrivedTime, etDepartedTime, etTerminalName,
            etContainerCondition, etDriverNotes
        )

        allFields.forEach { field ->
            field.isEnabled = false
            field.setTextColor(Color.BLACK)
            field.setBackgroundColor(Color.parseColor("#F0F0F0"))
            field.setPadding(40, 25, 40, 25)
            field.typeface = Typeface.DEFAULT_BOLD
        }
    }

    private fun updateCompleteButtonState() {
        currentOrder?.let { order ->
            if (order.stage7Completed) {
                btnComplete.isEnabled = false
                btnComplete.text = "Этап завершен"
                btnComplete.setBackgroundColor(Color.parseColor("#9E9E9E"))
            }
        }
    }

    private fun openDocumentsActivity() {
        val intent = Intent(this, DocumentsActivity::class.java).apply {
            putExtra("ORDER_DATA", currentOrder)
            putExtra("USER_DATA", currentUser)
            putExtra("STAGE_TYPE", "container_return")
        }
        startActivity(intent)
    }

    private fun markStageAsCompleted() {
        currentOrder?.let { order ->
            order.stage7Completed = true

            Toast.makeText(this, "Этап 7 'Терминал сдачи' завершен", Toast.LENGTH_LONG).show()

            // Обновляем интерфейс
            updateCompleteButtonState()

            // Здесь будет вызов API для сохранения статуса
            // orderRepository.updateOrder(order)
        }
    }
}