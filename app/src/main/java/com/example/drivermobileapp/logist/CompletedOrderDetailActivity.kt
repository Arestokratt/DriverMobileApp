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

class CompletedOrderDetailActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var btnPhotos: Button
    private lateinit var btnDocuments: Button

    // Все поля для отображения
    private lateinit var etOrderNumber: EditText
    private lateinit var etOrderDate: EditText
    private lateinit var etContainerType: EditText
    private lateinit var etContainerCount: EditText
    private lateinit var etContainerDeliveryDateTime: EditText
    private lateinit var etContainerDeliveryAddress: EditText
    private lateinit var etLoadingContactPerson: EditText
    private lateinit var etClientLegalName: EditText
    private lateinit var etClientPostalAddress: EditText
    private lateinit var etCargoName: EditText
    private lateinit var etCargoPieces: EditText
    private lateinit var etCargoWeight: EditText
    private lateinit var etDepartureStation: EditText
    private lateinit var etDestinationStation: EditText
    private lateinit var etConsigneeName: EditText
    private lateinit var etConsigneePostalAddress: EditText
    private lateinit var etUnloadingContactPerson: EditText
    private lateinit var etEmptyContainerTerminal: EditText
    private lateinit var etContainerReturnTerminal: EditText
    private lateinit var etNotes: EditText

    private var currentOrder: Order1C? = null
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_completed_order_detail)

        currentOrder = intent.getSerializableExtra("ORDER_DATA") as? Order1C
        currentUser = intent.getSerializableExtra("USER_DATA") as? User

        initViews()
        setupClickListeners()
        displayOrderData()
        disableAllFields()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnPhotos = findViewById(R.id.btnPhotos)
        btnDocuments = findViewById(R.id.btnDocuments)

        // Инициализация всех полей
        etOrderNumber = findViewById(R.id.etOrderNumber)
        etOrderDate = findViewById(R.id.etOrderDate)
        etContainerType = findViewById(R.id.etContainerType)
        etContainerCount = findViewById(R.id.etContainerCount)
        etContainerDeliveryDateTime = findViewById(R.id.etContainerDeliveryDateTime)
        etContainerDeliveryAddress = findViewById(R.id.etContainerDeliveryAddress)
        etLoadingContactPerson = findViewById(R.id.etLoadingContactPerson)
        etClientLegalName = findViewById(R.id.etClientLegalName)
        etClientPostalAddress = findViewById(R.id.etClientPostalAddress)
        etCargoName = findViewById(R.id.etCargoName)
        etCargoPieces = findViewById(R.id.etCargoPieces)
        etCargoWeight = findViewById(R.id.etCargoWeight)
        etDepartureStation = findViewById(R.id.etDepartureStation)
        etDestinationStation = findViewById(R.id.etDestinationStation)
        etConsigneeName = findViewById(R.id.etConsigneeName)
        etConsigneePostalAddress = findViewById(R.id.etConsigneePostalAddress)
        etUnloadingContactPerson = findViewById(R.id.etUnloadingContactPerson)
        etEmptyContainerTerminal = findViewById(R.id.etEmptyContainerTerminal)
        etContainerReturnTerminal = findViewById(R.id.etContainerReturnTerminal)
        etNotes = findViewById(R.id.etNotes)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish() // Возврат к списку выполненных заявок
        }

        btnPhotos.setOnClickListener {
            openPhotosActivity()
        }

        btnDocuments.setOnClickListener {
            openDocumentsActivity()
        }
    }

    private fun displayOrderData() {
        currentOrder?.let { order ->
            // Обновляем заголовок
            title = "Заявка №${order.orderNumber}"

            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

            // Заполняем все поля данными из заявки
            etOrderNumber.setText(order.orderNumber)
            etOrderDate.setText(dateFormat.format(Date(order.orderDate)))
            etContainerType.setText(order.containerType.ifEmpty { "Не указано" })
            etContainerCount.setText(if (order.containerCount > 0) order.containerCount.toString() else "Не указано")

            if (order.containerDeliveryDateTime > 0) {
                etContainerDeliveryDateTime.setText(dateTimeFormat.format(Date(order.containerDeliveryDateTime)))
            } else {
                etContainerDeliveryDateTime.setText("Не указано")
            }

            etContainerDeliveryAddress.setText(order.containerDeliveryAddress.ifEmpty { "Не указано" })
            etLoadingContactPerson.setText(order.loadingContactPerson.ifEmpty { "Не указано" })
            etClientLegalName.setText(order.clientLegalName.ifEmpty { order.clientName })
            etClientPostalAddress.setText(order.clientPostalAddress.ifEmpty { "Не указано" })
            etCargoName.setText(order.cargoName.ifEmpty { order.cargoType })
            etCargoPieces.setText(if (order.cargoPieces > 0) order.cargoPieces.toString() else "Не указано")
            etCargoWeight.setText("${order.weight} брутто(нетто)")
            etDepartureStation.setText(order.departureStation.ifEmpty { "Не указано" })
            etDestinationStation.setText(order.destinationStation.ifEmpty { "Не указано" })
            etConsigneeName.setText(order.consigneeName.ifEmpty { "Не указано" })
            etConsigneePostalAddress.setText(order.consigneePostalAddress.ifEmpty { "Не указано" })
            etUnloadingContactPerson.setText(order.unloadingContactPerson.ifEmpty { "Не указано" })
            etEmptyContainerTerminal.setText(order.emptyContainerTerminal.ifEmpty { "Не указано" })
            etContainerReturnTerminal.setText(order.containerReturnInfo.ifEmpty { "Не указано" })
            etNotes.setText(order.notes.ifEmpty { "Нет примечаний" })
        }
    }

    private fun disableAllFields() {
        // Делаем все поля неактивными (только для чтения)
        val allFields = listOf(
            etOrderNumber, etOrderDate, etContainerType, etContainerCount,
            etContainerDeliveryDateTime, etContainerDeliveryAddress, etLoadingContactPerson,
            etClientLegalName, etClientPostalAddress, etCargoName, etCargoPieces,
            etCargoWeight, etDepartureStation, etDestinationStation, etConsigneeName,
            etConsigneePostalAddress, etUnloadingContactPerson, etEmptyContainerTerminal,
            etContainerReturnTerminal, etNotes
        )

        allFields.forEach { field ->
            field.isEnabled = false
            field.setTextColor(Color.BLACK)
            field.setBackgroundColor(Color.parseColor("#F0F0F0"))
            field.setPadding(40, 25, 40, 25)
            field.typeface = Typeface.DEFAULT_BOLD
        }
    }

    private fun openPhotosActivity() {
        val intent = Intent(this, PhotosActivity::class.java).apply {
            putExtra("ORDER_DATA", currentOrder)
            putExtra("USER_DATA", currentUser)
            putExtra("STAGE_TYPE", "all") // Все фото по заявке
        }
        startActivity(intent)
    }

    private fun openDocumentsActivity() {
        val intent = Intent(this, DocumentsActivity::class.java).apply {
            putExtra("ORDER_DATA", currentOrder)
            putExtra("USER_DATA", currentUser)
            putExtra("STAGE_TYPE", "all") // Все документы по заявке
        }
        startActivity(intent)
    }
}