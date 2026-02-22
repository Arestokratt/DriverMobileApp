package com.example.drivermobileapp.logist

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.models.Order1C
import com.example.drivermobileapp.data.models.User
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.content.ContextCompat
import android.graphics.Typeface
import android.graphics.Color

class Stage1ViewActivity : AppCompatActivity() {

    private lateinit var btnBack: Button

    // Все поля для отображения (только чтение)
    private lateinit var etOrderNumber: EditText
    private lateinit var etOrderDate: EditText
    private lateinit var etContainerType: EditText
    private lateinit var etContainerCount: EditText
    private lateinit var etContainerDateTime: EditText
    private lateinit var etContainerAddress: EditText
    private lateinit var etLoadingContact: EditText
    private lateinit var etClientName: EditText
    private lateinit var etClientAddress: EditText
    private lateinit var etCargoName: EditText
    private lateinit var etCargoPieces: EditText
    private lateinit var etCargoWeight: EditText
    private lateinit var etDepartureStation: EditText
    private lateinit var etDestinationStation: EditText
    private lateinit var etConsigneeName: EditText
    private lateinit var etConsigneeAddress: EditText
    private lateinit var etUnloadingContact: EditText
    private lateinit var etTerminal: EditText
    private lateinit var etDriverName: EditText
    private lateinit var etNotes: EditText

    private var currentOrder: Order1C? = null
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stage1_view)

        currentOrder = intent.getSerializableExtra("ORDER_DATA") as? Order1C
        currentUser = intent.getSerializableExtra("USER_DATA") as? User

        initViews()
        setupClickListeners()
        displayStage1Data()
        disableAllFields() // Делаем все поля неактивными
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)

        // Инициализация всех полей
        etOrderNumber = findViewById(R.id.etOrderNumber)
        etOrderDate = findViewById(R.id.etOrderDate)
        etContainerType = findViewById(R.id.etContainerType)
        etContainerCount = findViewById(R.id.etContainerCount)
        etContainerDateTime = findViewById(R.id.etContainerDateTime)
        etContainerAddress = findViewById(R.id.etContainerAddress)
        etLoadingContact = findViewById(R.id.etLoadingContact)
        etClientName = findViewById(R.id.etClientName)
        etClientAddress = findViewById(R.id.etClientAddress)
        etCargoName = findViewById(R.id.etCargoName)
        etCargoPieces = findViewById(R.id.etCargoPieces)
        etCargoWeight = findViewById(R.id.etCargoWeight)
        etDepartureStation = findViewById(R.id.etDepartureStation)
        etDestinationStation = findViewById(R.id.etDestinationStation)
        etConsigneeName = findViewById(R.id.etConsigneeName)
        etConsigneeAddress = findViewById(R.id.etConsigneeAddress)
        etUnloadingContact = findViewById(R.id.etUnloadingContact)
        etTerminal = findViewById(R.id.etTerminal)
        etDriverName = findViewById(R.id.etDriverName)
        etNotes = findViewById(R.id.etNotes)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish() // Возврат к экрану этапов заявки
        }
    }

    private fun displayStage1Data() {
        currentOrder?.let { order ->
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val containerDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

            // Заполняем все поля данными из заявки
            etOrderNumber.setText(order.orderNumber)
            etOrderDate.setText(dateFormat.format(Date(order.orderDate)))
            etContainerType.setText(order.containerType.ifEmpty { "Не указано" })
            etContainerCount.setText(if (order.containerCount > 0) order.containerCount.toString() else "Не указано")
            etContainerDateTime.setText(
                if (order.containerDeliveryDateTime > 0)
                    containerDateFormat.format(Date(order.containerDeliveryDateTime))
                else "Не указано"
            )
            etContainerAddress.setText(order.containerDeliveryAddress.ifEmpty { "Не указано" })
            etLoadingContact.setText(order.loadingContactPerson.ifEmpty { "Не указано" })
            etClientName.setText(order.clientLegalName.ifEmpty { order.clientName })
            etClientAddress.setText(order.clientPostalAddress.ifEmpty { "Не указано" })
            etCargoName.setText(order.cargoName.ifEmpty { order.cargoType })
            etCargoPieces.setText(if (order.cargoPieces > 0) order.cargoPieces.toString() else "Не указано")
            etCargoWeight.setText("${order.weight} кг")
            etDepartureStation.setText(order.departureStation.ifEmpty { "Не указано" })
            etDestinationStation.setText(order.destinationStation.ifEmpty { "Не указано" })
            etConsigneeName.setText(order.consigneeName.ifEmpty { "Не указано" })
            etConsigneeAddress.setText(order.consigneePostalAddress.ifEmpty { "Не указано" })
            etUnloadingContact.setText(order.unloadingContactPerson.ifEmpty { "Не указано" })
            etTerminal.setText(order.emptyContainerTerminal.ifEmpty { "Не указано" })
            etDriverName.setText(getDriverName(order.assignedDriverId))
            etNotes.setText(order.notes.ifEmpty { "Нет примечаний" })
        }
    }

    private fun disableAllFields() {
        val allFields = listOf(
            etOrderNumber, etOrderDate, etContainerType, etContainerCount,
            etContainerDateTime, etContainerAddress, etLoadingContact,
            etClientName, etClientAddress, etCargoName, etCargoPieces,
            etCargoWeight, etDepartureStation, etDestinationStation,
            etConsigneeName, etConsigneeAddress, etUnloadingContact,
            etTerminal, etDriverName, etNotes
        )

        allFields.forEach { field ->
            field.isEnabled = false

            // ЧЕРНЫЙ текст на СВЕТЛО-СЕРОМ фоне
            field.setTextColor(Color.BLACK) // Черный текст
            field.setBackgroundColor(Color.parseColor("#F0F0F0")) // Светло-серый фон

            // Хорошие отступы
            field.setPadding(40, 25, 40, 25)

            // Делаем текст жирным для лучшей читаемости
            field.typeface = Typeface.DEFAULT_BOLD
        }
    }

    private fun getDriverName(driverId: String?): String {
        return when (driverId) {
            "driver1" -> "Петров Петр Петрович"
            "driver2" -> "Иванов Иван Иванович"
            "driver3" -> "Сидоров Алексей Владимирович"
            else -> "Не назначен"
        }
    }
}