package com.example.drivermobileapp.logist

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.models.Order1C
import com.example.drivermobileapp.data.models.TerminalStage
import com.example.drivermobileapp.data.models.User
import java.text.SimpleDateFormat
import java.util.*

class Stage5DestinationStationActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var btnAddTerminal: Button
    private lateinit var btnAssignDriver: Button
    private lateinit var btnPhotos: Button
    private lateinit var btnDocuments: Button

    // Все поля для отображения (только чтение)
    private lateinit var etOrderNumber: EditText
    private lateinit var etOrderDate: EditText
    private lateinit var etContainerType: EditText
    private lateinit var etContainerCount: EditText
    private lateinit var etClientName: EditText
    private lateinit var etClientAddress: EditText
    private lateinit var etCargoName: EditText
    private lateinit var etCargoPieces: EditText
    private lateinit var etCargoWeight: EditText
    private lateinit var etDestinationStation: EditText
    private lateinit var etConsigneeName: EditText
    private lateinit var etConsigneeAddress: EditText
    private lateinit var etUnloadingContact: EditText
    private lateinit var etNotes: EditText

    // Поля статусов этапа
    private lateinit var etAcceptedTime: EditText
    private lateinit var etArrivedTime: EditText
    private lateinit var etDepartedTime: EditText

    private var currentOrder: Order1C? = null
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stage5_destination_station)

        currentOrder = intent.getSerializableExtra("ORDER_DATA") as? Order1C
        currentUser = intent.getSerializableExtra("USER_DATA") as? User

        initViews()
        setupClickListeners()
        displayStage5Data()
        disableAllFields()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnAddTerminal = findViewById(R.id.btnAddTerminal)
        btnAssignDriver = findViewById(R.id.btnAssignDriver)
        btnPhotos = findViewById(R.id.btnPhotos)
        btnDocuments = findViewById(R.id.btnDocuments)

        // Инициализация всех полей
        etOrderNumber = findViewById(R.id.etOrderNumber)
        etOrderDate = findViewById(R.id.etOrderDate)
        etContainerType = findViewById(R.id.etContainerType)
        etContainerCount = findViewById(R.id.etContainerCount)
        etClientName = findViewById(R.id.etClientName)
        etClientAddress = findViewById(R.id.etClientAddress)
        etCargoName = findViewById(R.id.etCargoName)
        etCargoPieces = findViewById(R.id.etCargoPieces)
        etCargoWeight = findViewById(R.id.etCargoWeight)
        etDestinationStation = findViewById(R.id.etDestinationStation)
        etConsigneeName = findViewById(R.id.etConsigneeName)
        etConsigneeAddress = findViewById(R.id.etConsigneeAddress)
        etUnloadingContact = findViewById(R.id.etUnloadingContact)
        etNotes = findViewById(R.id.etNotes)

        // Поля статусов
        etAcceptedTime = findViewById(R.id.etAcceptedTime)
        etArrivedTime = findViewById(R.id.etArrivedTime)
        etDepartedTime = findViewById(R.id.etDepartedTime)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnAddTerminal.setOnClickListener {
            showAddTerminalDialog()
        }

        btnAssignDriver.setOnClickListener {
            openDriverMap()
        }

        btnPhotos.setOnClickListener {
            openPhotosActivity()
        }

        btnDocuments.setOnClickListener {
            openDocumentsActivity()
        }
    }

    private fun displayStage5Data() {
        currentOrder?.let { order ->
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val timeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

            // Заполняем все поля данными из заявки
            etOrderNumber.setText(order.orderNumber)
            etOrderDate.setText(dateFormat.format(Date(order.orderDate)))
            etContainerType.setText(order.containerType.ifEmpty { "Не указано" })
            etContainerCount.setText(if (order.containerCount > 0) order.containerCount.toString() else "Не указано")
            etClientName.setText(order.clientLegalName.ifEmpty { order.clientName })
            etClientAddress.setText(order.clientPostalAddress.ifEmpty { "Не указано" })
            etCargoName.setText(order.cargoName.ifEmpty { order.cargoType })
            etCargoPieces.setText(if (order.cargoPieces > 0) order.cargoPieces.toString() else "Не указано")
            etCargoWeight.setText("${order.weight} кг")
            etDestinationStation.setText(order.destinationStation.ifEmpty { "Не указано" })
            etConsigneeName.setText(order.consigneeName.ifEmpty { "Не указано" })
            etConsigneeAddress.setText(order.consigneePostalAddress.ifEmpty { "Не указано" })
            etUnloadingContact.setText(order.unloadingContactPerson.ifEmpty { "Не указано" })
            etNotes.setText(order.notes.ifEmpty { "Нет примечаний" })

            // Заполняем поля статусов - ИСПРАВЛЕННЫЕ ССЫЛКИ
            if (order.terminalStage.acceptedTime > 0) {
                etAcceptedTime.setText(timeFormat.format(Date(order.terminalStage.acceptedTime)))
            } else {
                etAcceptedTime.setText("Не принято")
            }

            if (order.destinationStationStage.arrivedTime > 0) {
                etArrivedTime.setText(timeFormat.format(Date(order.destinationStationStage.arrivedTime)))
            } else {
                etArrivedTime.setText("Не прибыл")
            }

            if (order.destinationStationStage.departedTime > 0) {
                etDepartedTime.setText(timeFormat.format(Date(order.destinationStationStage.departedTime)))
            } else {
                etDepartedTime.setText("Не выехал")
            }
        }
    }

    private fun saveTerminalInfo(terminalInfo: String) {
        currentOrder?.emptyContainerTerminal = terminalInfo
        Toast.makeText(this, "Терминал сохранен: $terminalInfo", Toast.LENGTH_LONG).show()
    }

    private fun assignDriverToOrder(driverName: String) {
        currentOrder?.assignedDriverId = driverName
        Toast.makeText(this, "Заявка отправлена водителю: $driverName", Toast.LENGTH_LONG).show()
        simulateDriverResponse(driverName)
    }

    private fun simulateDriverResponse(driverName: String) {
        Handler(Looper.getMainLooper()).postDelayed({
            val randomResponse = Random().nextBoolean()
            if (randomResponse) {
                // ИСПРАВЛЕННАЯ СТРОКА:
                currentOrder?.terminalStage = currentOrder?.terminalStage?.copy(
                    acceptedTime = System.currentTimeMillis()
                ) ?: TerminalStage(acceptedTime = System.currentTimeMillis())

                Toast.makeText(this, "$driverName принял заявку", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "$driverName отклонил заявку. Выберите другого водителя", Toast.LENGTH_LONG).show()
            }
            displayStage5Data()
        }, 3000)
    }

    private fun disableAllFields() {
        val allFields = listOf(
            etOrderNumber, etOrderDate, etContainerType, etContainerCount,
            etClientName, etClientAddress, etCargoName, etCargoPieces,
            etCargoWeight, etDestinationStation, etConsigneeName,
            etConsigneeAddress, etUnloadingContact, etNotes,
            etAcceptedTime, etArrivedTime, etDepartedTime
        )

        allFields.forEach { field ->
            field.isEnabled = false
            field.setTextColor(Color.BLACK)
            field.setBackgroundColor(Color.parseColor("#F0F0F0"))
            field.setPadding(40, 25, 40, 25)
            field.typeface = Typeface.DEFAULT_BOLD
        }
    }

    private fun showAddTerminalDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_terminal, null)
        val etTerminal = dialogView.findViewById<EditText>(R.id.etTerminal)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Добавить терминал сдачи порожнего контейнера")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { dialogInterface, which ->
                val terminalInfo = etTerminal.text.toString().trim()
                if (terminalInfo.isNotEmpty()) {
                    saveTerminalInfo(terminalInfo)
                } else {
                    Toast.makeText(this, "Введите наименование и адрес терминала", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена") { dialogInterface, which ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun openDriverMap() {
        try {
            val intent = Intent().apply {
                // Intent для открытия карты в "Авилон"
                action = "com.avilon.action.OPEN_MAP"
                setPackage("com.avilon.app") // Замени на реальный package name
                putExtra("search_radius", 100) // Радиус 100 км
                putExtra("destination_address", currentOrder?.destinationStation ?: "")
                putExtra("order_id", currentOrder?.id)
                putExtra("order_number", currentOrder?.orderNumber)
            }

            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, REQUEST_CODE_AVIRON)
            } else {
                showAvironNotInstalledDialog()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка открытия карты: ${e.message}", Toast.LENGTH_SHORT).show()
            showAvironNotInstalledDialog()
        }
    }

    // Добавляем константу
    companion object {
        private const val REQUEST_CODE_AVIRON = 1001
    }

    private fun showAvironNotInstalledDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Приложение 'Авилон' не установлено")
            .setMessage("Для поиска водителей необходимо установить приложение 'Авилон'. Хотите установить его?")
            .setPositiveButton("Установить") { dialogInterface, which ->
                openPlayStoreForAviron()
            }
            .setNegativeButton("Ручной выбор") { dialogInterface, which ->
                showDriverSelectionDialog() // Возвращаемся к ручному выбору
            }
            .setNeutralButton("Отмена") { dialogInterface, which ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun openPlayStoreForAviron() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=com.avilon.app") // Замени на реальный package name
                setPackage("com.android.vending")
            }
            startActivity(intent)
        } catch (e: Exception) {
            // Если Play Store нет, открываем в браузере
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=com.avilon.app")
            }
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_AVIRON && resultCode == RESULT_OK) {
            data?.let { intent ->
                val driverId = intent.getStringExtra("driver_id")
                val driverName = intent.getStringExtra("driver_name")
                val driverVehicle = intent.getStringExtra("driver_vehicle")

                if (driverId != null && driverName != null) {
                    assignDriverFromAviron(driverId, driverName, driverVehicle)
                }
            }
        }
    }

    private fun assignDriverFromAviron(driverId: String, driverName: String, vehicle: String?) {
        val driverInfo = if (vehicle != null) {
            "$driverName ($vehicle)"
        } else {
            driverName
        }

        currentOrder?.assignedDriverId = driverId
        Toast.makeText(this, "Водитель выбран: $driverInfo", Toast.LENGTH_LONG).show()

        // Отправляем заявку водителю
        sendOrderToDriver(driverId, driverInfo)
    }

    private fun openAvironViaDeepLink() {
        try {
            val deepLink = Uri.parse("avilon://map/search").buildUpon()
                .appendQueryParameter("radius", "100")
                .appendQueryParameter("destination", currentOrder?.destinationStation ?: "")
                .appendQueryParameter("orderId", currentOrder?.id)
                .build()

            val intent = Intent(Intent.ACTION_VIEW, deepLink)

            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, REQUEST_CODE_AVIRON)
            } else {
                showAvironNotInstalledDialog()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка открытия карты", Toast.LENGTH_SHORT).show()
            showDriverSelectionDialog() // Fallback на ручной выбор
        }
    }

    private fun sendOrderToDriver(driverId: String, driverInfo: String) {
        // Здесь будет API вызов для отправки заявки водителю
        Toast.makeText(this, "Заявка отправлена водителю: $driverInfo", Toast.LENGTH_LONG).show()

        // Имитация ответа водителя
        simulateDriverResponse(driverInfo)
    }

    private fun showDriverSelectionDialog() {
        val drivers = listOf(
            "Иванов А.В. (Toyota Camry)",
            "Петров С.М. (Kia Rio)",
            "Сидоров Н.П. (Hyundai Solaris)",
            "Козлов Д.И. (Lada Vesta)"
        )

        val driverItems = drivers.toTypedArray()

        val dialog = AlertDialog.Builder(this)
            .setTitle("Выберите водителя")
            .setItems(driverItems) { dialogInterface, which ->
                val selectedDriver = drivers[which]
                assignDriverToOrder(selectedDriver)
            }
            .setNegativeButton("Отмена") { dialogInterface, which ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun openPhotosActivity() {
        val intent = Intent(this, PhotosActivity::class.java).apply {
            putExtra("ORDER_DATA", currentOrder)
            putExtra("USER_DATA", currentUser)
            putExtra("STAGE_TYPE", "destination_station")
        }
        startActivity(intent)
    }

    private fun openDocumentsActivity() {
        val intent = Intent(this, DocumentsActivity::class.java).apply {
            putExtra("ORDER_DATA", currentOrder)
            putExtra("USER_DATA", currentUser)
            putExtra("STAGE_TYPE", "destination_station")
        }
        startActivity(intent)
    }
}