package com.example.drivermobileapp.driver

import OrderDriver
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stage1_detail)

        currentOrder = intent.getSerializableExtra("ORDER") as? OrderDriver

        initViews()
        setupClickListeners()
        populateFormData()
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
            finish() // Возврат к этапам заявки
        }
    }

    private fun populateFormData() {
        currentOrder?.let { order ->
            tvStageTitle.text = "Этап №1. Заявка №${order.number}"

            // Заполняем поля данными из заявки
            etTerminalPickupAddress.setText(order.terminalPickupAddress ?: "")
            etContainerType.setText(order.containerType ?: "")
            etContainerCount.setText(order.containerCount?.toString() ?: "")

            // Форматируем дату и время
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
}