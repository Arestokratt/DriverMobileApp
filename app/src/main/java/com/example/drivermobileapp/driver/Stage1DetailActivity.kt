package com.example.drivermobileapp.driver

import OrderDriver
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.content.Intent
import com.example.drivermobileapp.R
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Locale

class Stage1DetailActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var tvStageTitle: TextView

    // Поля формы
    private lateinit var etTerminalPickupAddress: TextInputEditText
    private lateinit var etContainerType: TextInputEditText
    private lateinit var etContainerCount: TextInputEditText
    private lateinit var etContainerDeliveryTime: TextInputEditText
    private lateinit var etLoadingAddress: TextInputEditText
    private lateinit var etCargoName: TextInputEditText
    private lateinit var etCargoWeight: TextInputEditText
    private lateinit var etLoadingContact: TextInputEditText
    private lateinit var etDepartureStation: TextInputEditText
    private lateinit var etDepartureContact: TextInputEditText
    private lateinit var etDestinationStation: TextInputEditText
    private lateinit var etDestinationContact: TextInputEditText
    private lateinit var etUnloadingAddress: TextInputEditText
    private lateinit var etUnloadingContact: TextInputEditText
    private lateinit var etTerminalReturnAddress: TextInputEditText

    private var currentOrder: OrderDriver? = null
    private var stageNumber: Int = 1
    private var isCurrentStage: Boolean = false
    private var hasBeenViewed: Boolean = false // Флаг, что заявка была просмотрена

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stage1_detail)

        currentOrder = intent.getSerializableExtra("ORDER") as? OrderDriver
        stageNumber = intent.getIntExtra("STAGE_NUMBER", 1)
        isCurrentStage = intent.getBooleanExtra("IS_CURRENT_STAGE", false)

        initViews()
        setupClickListeners()
        populateFormData()
        disableAllFields()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvStageTitle = findViewById(R.id.tvStageTitle)

        // Инициализируем все поля формы
        etTerminalPickupAddress = findViewById(R.id.etTerminalPickupAddress)
        etContainerType = findViewById(R.id.etContainerType)
        etContainerCount = findViewById(R.id.etContainerCount)
        etContainerDeliveryTime = findViewById(R.id.etContainerDeliveryTime)
        etLoadingAddress = findViewById(R.id.etLoadingAddress)
        etCargoName = findViewById(R.id.etCargoName)
        etCargoWeight = findViewById(R.id.etCargoWeight)
        etLoadingContact = findViewById(R.id.etLoadingContact)
        etDepartureStation = findViewById(R.id.etDepartureStation)
        etDepartureContact = findViewById(R.id.etDepartureContact)
        etDestinationStation = findViewById(R.id.etDestinationStation)
        etDestinationContact = findViewById(R.id.etDestinationContact)
        etUnloadingAddress = findViewById(R.id.etUnloadingAddress)
        etUnloadingContact = findViewById(R.id.etUnloadingContact)
        etTerminalReturnAddress = findViewById(R.id.etTerminalReturnAddress)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            // Перед выходом проверяем, нужно ли завершить этап
            completeStageIfNeeded()
            finish()
        }
    }

    private fun completeStageIfNeeded() {
        if (isCurrentStage && !hasBeenViewed) {
            hasBeenViewed = true

            val resultIntent = Intent().apply {
                putExtra("STAGE_COMPLETED", true)
                putExtra("STAGE_NUMBER", stageNumber)
            }
            setResult(RESULT_OK, resultIntent)
            // Не вызываем finish() здесь, он вызовется после onBackPressed
        }
    }

    private fun populateFormData() {
        currentOrder?.let { order ->
            tvStageTitle.text = "Этап №1: Заявка №${order.number}"

            etTerminalPickupAddress.setText(order.terminalPickupAddress ?: "")
            etContainerType.setText(order.containerType ?: "")
            etContainerCount.setText(order.containerCount?.toString() ?: "")

            val deliveryTimeFormatted = order.containerDeliveryTime?.let { time ->
                SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(time)
            } ?: ""
            etContainerDeliveryTime.setText(deliveryTimeFormatted)

            etLoadingAddress.setText(order.loadingAddress ?: "")
            etCargoName.setText(order.cargoName ?: "")
            etCargoWeight.setText(order.cargoWeight?.let { "$it кг" } ?: "")
            etLoadingContact.setText(order.loadingContact ?: "")
            etDepartureStation.setText(order.departureStation ?: "")
            etDepartureContact.setText(order.departureContact ?: "")
            etDestinationStation.setText(order.destinationStation ?: "")
            etDestinationContact.setText(order.destinationContact ?: "")
            etUnloadingAddress.setText(order.unloadingAddress ?: "")
            etUnloadingContact.setText(order.unloadingContact ?: "")
            etTerminalReturnAddress.setText(order.terminalReturnAddress ?: "")
        }
    }

    private fun disableAllFields() {
        val allFields = listOf(
            etTerminalPickupAddress, etContainerType, etContainerCount,
            etContainerDeliveryTime, etLoadingAddress, etCargoName,
            etCargoWeight, etLoadingContact, etDepartureStation,
            etDepartureContact, etDestinationStation, etDestinationContact,
            etUnloadingAddress, etUnloadingContact, etTerminalReturnAddress
        )

        allFields.forEach { field ->
            field.isEnabled = false
            field.isFocusable = false
            field.isClickable = false
        }
    }
}