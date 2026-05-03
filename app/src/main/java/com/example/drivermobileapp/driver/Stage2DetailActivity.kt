package com.example.drivermobileapp.driver

import OrderDriver
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.drivermobileapp.R
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

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

    private val MIN_PHOTOS = 2
    private val MIN_DOCUMENTS = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stage2_detail)

        currentOrder = intent.getSerializableExtra("ORDER") as? OrderDriver

        initViews()
        setupClickListeners()
        populateFormData()
        updateUIState()
    }

    override fun onResume() {
        super.onResume()
        updateCounters()
        updateDepartureButtonState()
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
        btnBack.setOnClickListener { finish() }

        btnArrived.setOnClickListener { recordArrivalTime() }

        btnDeparted.setOnClickListener { checkBeforeDeparture() }

        btnPhotos.setOnClickListener {
            val intent = Intent(this, PhotosActivity::class.java).apply {
                putExtra("ORDER", currentOrder)
                putExtra("STAGE_NUMBER", 2)
                putExtra("PHOTO_LIST_KEY", "containerPhotos")
            }
            startActivity(intent)
        }

        btnDocuments.setOnClickListener {
            val intent = Intent(this, DocumentsActivity::class.java).apply {
                putExtra("ORDER", currentOrder)
                putExtra("STAGE_NUMBER", 2)
                putExtra("DOCUMENT_LIST_KEY", "terminalDocuments")
            }
            startActivity(intent)
        }
    }

    private fun checkBeforeDeparture() {
        currentOrder?.let { order ->
            val photosCount = order.stages.stage2.containerPhotos.size
            val documentsCount = order.stages.stage2.terminalDocuments.size

            val missingPhotos = MIN_PHOTOS - photosCount
            val missingDocs = MIN_DOCUMENTS - documentsCount

            when {
                photosCount < MIN_PHOTOS && documentsCount < MIN_DOCUMENTS -> {
                    showError("Необходимо загрузить:\n📷 Фото: еще $missingPhotos (нужно $MIN_PHOTOS)\n📄 Документы: еще $missingDocs (нужно $MIN_DOCUMENTS)")
                }
                photosCount < MIN_PHOTOS -> {
                    showError("Необходимо загрузить еще $missingPhotos фото контейнера. Нужно минимум $MIN_PHOTOS")
                }
                documentsCount < MIN_DOCUMENTS -> {
                    showError("Необходимо загрузить еще $missingDocs документов терминала. Нужно минимум $MIN_DOCUMENTS")
                }
                else -> {
                    recordDepartureTime()
                }
            }
        }
    }

    private fun updateDepartureButtonState() {
        currentOrder?.let { order ->
            val stage = order.stages.stage2
            val photosCount = stage.containerPhotos.size
            val documentsCount = stage.terminalDocuments.size

            val hasRequired = photosCount >= MIN_PHOTOS && documentsCount >= MIN_DOCUMENTS
            val canDepart = stage.arrivalTime != null && stage.departureTime == null

            if (canDepart) {
                if (hasRequired) {
                    btnDeparted.text = "✅ Уехал (завершить этап)"
                    btnDeparted.isEnabled = true
                    btnDeparted.alpha = 1f
                } else {
                    btnDeparted.text = "⏳ Фото: $photosCount/$MIN_PHOTOS Доки: $documentsCount/$MIN_DOCUMENTS"
                    btnDeparted.isEnabled = false
                    btnDeparted.alpha = 0.6f
                }
            }
        }
    }

    private fun populateFormData() {
        currentOrder?.let { order ->
            tvStageTitle.text = "Этап №2. Терминал вывоза №${order.number}"
            etTerminalAddress.setText(order.terminalPickupAddress ?: "Адрес не указан")

            order.stages.stage2.arrivalTime?.let { time ->
                etArrivalTime.setText(dateFormat.format(time))
            }
            order.stages.stage2.departureTime?.let { time ->
                etDepartureTime.setText(dateFormat.format(time))
            }
            updateCounters()
        }
    }

    private fun updateUIState() {
        currentOrder?.let { order ->
            val stage = order.stages.stage2

            btnArrived.isEnabled = stage.arrivalTime == null

            if (stage.departureTime != null) {
                btnDeparted.isEnabled = false
                btnDeparted.text = "✅ Завершено"
            }

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
                    tvStageStatus.text = "Статус: ⏳ Ожидание прибытия"
                    tvStageStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_light))
                }
            }
        }
    }

    private fun recordArrivalTime() {
        currentOrder?.let { order ->
            val time = System.currentTimeMillis()
            val updatedStage = order.stages.stage2.copy(arrivalTime = time)
            val updatedStages = order.stages.copy(stage2 = updatedStage)
            // В реальном приложении здесь нужно сохранить обновленный заказ
            Toast.makeText(this, "Время прибытия зафиксировано", Toast.LENGTH_SHORT).show()
            updateUIState()
            updateDepartureButtonState()
        }
    }

    private fun recordDepartureTime() {
        currentOrder?.let { order ->
            val time = System.currentTimeMillis()
            val updatedStage = order.stages.stage2.copy(departureTime = time, isCompleted = true)
            val updatedStages = order.stages.copy(stage2 = updatedStage)
            Toast.makeText(this, "Этап №2 успешно завершен!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun updateCounters() {
        currentOrder?.let { order ->
            val stage = order.stages.stage2
            val photosCount = stage.containerPhotos.size
            val docsCount = stage.terminalDocuments.size

            tvPhotosCount.text = "📷 Фото: $photosCount / $MIN_PHOTOS"
            tvDocumentsCount.text = "📄 Документы: $docsCount / $MIN_DOCUMENTS"

            val photosOk = photosCount >= MIN_PHOTOS
            val docsOk = docsCount >= MIN_DOCUMENTS

            tvPhotosCount.setTextColor(ContextCompat.getColor(this,
                if (photosOk) R.color.green else R.color.red))
            tvDocumentsCount.setTextColor(ContextCompat.getColor(this,
                if (docsOk) R.color.green else R.color.red))
        }
    }

    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Необходимо завершить этап")
            .setMessage(message)
            .setPositiveButton("Понятно", null)
            .show()
    }
}