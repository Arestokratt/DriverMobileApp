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

class Stage6CargoIssueActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var btnPhotos: Button
    private lateinit var btnDocuments: Button
    private lateinit var btnComplete: Button

    private lateinit var tvConsigneeInfo: TextView
    private lateinit var tvArrivedTime: TextView
    private lateinit var tvDepartedTime: TextView
    private lateinit var cbCargoIssued: CheckBox
    private lateinit var tvStatus: TextView

    private var currentOrder: Order1C? = null
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stage6_cargo_issue)

        currentOrder = intent.getSerializableExtra("ORDER_DATA") as? Order1C
        currentUser = intent.getSerializableExtra("USER_DATA") as? User

        initViews()
        setupClickListeners()
        displayStage6Data()
        updateCompleteButtonState()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnPhotos = findViewById(R.id.btnPhotos)
        btnDocuments = findViewById(R.id.btnDocuments)
        btnComplete = findViewById(R.id.btnComplete)

        tvConsigneeInfo = findViewById(R.id.tvConsigneeInfo)
        tvArrivedTime = findViewById(R.id.tvArrivedTime)
        tvDepartedTime = findViewById(R.id.tvDepartedTime)
        cbCargoIssued = findViewById(R.id.cbCargoIssued)
        tvStatus = findViewById(R.id.tvStatus)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish() // Возврат к экрану этапов заявки
        }

        btnPhotos.setOnClickListener {
            openPhotosActivity()
        }

        btnDocuments.setOnClickListener {
            openDocumentsActivity()
        }

        btnComplete.setOnClickListener {
            markOrderAsCompleted()
        }
    }

    private fun displayStage6Data() {
        currentOrder?.let { order ->
            // Заголовок
            title = "Этап №6. Выдача груза - Заявка №${order.orderNumber}"

            // Информация о грузополучателе
            val consigneeInfo = """
                Грузополучатель: ${order.consigneeName}
                Адрес: ${order.consigneePostalAddress}
                Контактное лицо: ${order.unloadingContactPerson}
                Доп. информация: ${if (order.notes.isNotEmpty()) order.notes else "Нет информации"}
            """.trimIndent()
            tvConsigneeInfo.text = consigneeInfo

            // Временные отметки
            val timeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

            if (order.cargoIssueStage.arrivedTime > 0) {
                tvArrivedTime.text = timeFormat.format(Date(order.cargoIssueStage.arrivedTime))
            } else {
                tvArrivedTime.text = "Не прибыл"
            }

            if (order.cargoIssueStage.departedTime > 0) {
                tvDepartedTime.text = timeFormat.format(Date(order.cargoIssueStage.departedTime))
            } else {
                tvDepartedTime.text = "Не выехал"
            }

            // Статусы
            cbCargoIssued.isChecked = order.stage6Completed

            if (order.stage6Completed) {
                tvStatus.text = "✔ Выполнено"
                tvStatus.setTextColor(Color.parseColor("#4CAF50"))
            } else {
                tvStatus.text = "Не выполнено"
                tvStatus.setTextColor(Color.RED)
            }
        }
    }

    private fun updateCompleteButtonState() {
        currentOrder?.let { order ->
            if (order.stage6Completed) {
                btnComplete.isEnabled = false
                btnComplete.text = "Заявка выполнена"
                btnComplete.setBackgroundColor(Color.parseColor("#9E9E9E"))
            }
        }
    }

    private fun openPhotosActivity() {
        val intent = Intent(this, PhotosActivity::class.java).apply {
            putExtra("ORDER_DATA", currentOrder)
            putExtra("USER_DATA", currentUser)
            putExtra("STAGE_TYPE", "cargo_issue")
        }
        startActivity(intent)
    }

    private fun openDocumentsActivity() {
        val intent = Intent(this, DocumentsActivity::class.java).apply {
            putExtra("ORDER_DATA", currentOrder)
            putExtra("USER_DATA", currentUser)
            putExtra("STAGE_TYPE", "cargo_issue")
        }
        startActivity(intent)
    }

    private fun markOrderAsCompleted() {
        currentOrder?.let { order ->
            order.stage6Completed = true
            order.status = "COMPLETED"

            Toast.makeText(this, "Заявка перемещена в выполненные", Toast.LENGTH_LONG).show()

            // Обновляем интерфейс
            displayStage6Data()
            updateCompleteButtonState()

            // Здесь будет вызов API для сохранения статуса
            // orderRepository.markOrderAsCompleted(order.id)
        }
    }
}