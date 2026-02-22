package com.example.drivermobileapp.logist

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.models.Order1C
import com.example.drivermobileapp.data.models.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderStagesActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var tvOrderNumber: TextView
    private lateinit var layoutStages: LinearLayout

    // Элементы этапов
    private lateinit var stage1Layout: LinearLayout
    private lateinit var stage2Layout: LinearLayout
    private lateinit var stage3Layout: LinearLayout
    private lateinit var stage4Layout: LinearLayout
    private lateinit var stage5Layout: LinearLayout
    private lateinit var stage6Layout: LinearLayout
    private lateinit var stage7Layout: LinearLayout

    private lateinit var stage1Status: TextView
    private lateinit var stage2Status: TextView
    private lateinit var stage3Status: TextView
    private lateinit var stage4Status: TextView
    private lateinit var stage5Status: TextView
    private lateinit var stage6Status: TextView
    private lateinit var stage7Status: TextView

    private var currentOrder: Order1C? = null
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_stages)

        currentOrder = intent.getSerializableExtra("ORDER_DATA") as? Order1C
        currentUser = intent.getSerializableExtra("USER_DATA") as? User

        initViews()
        setupClickListeners()
        displayOrderStages()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvOrderNumber = findViewById(R.id.tvOrderNumber)
        layoutStages = findViewById(R.id.layoutStages)

        // Инициализация layout этапов
        stage1Layout = findViewById(R.id.stage1Layout)
        stage2Layout = findViewById(R.id.stage2Layout)
        stage3Layout = findViewById(R.id.stage3Layout)
        stage4Layout = findViewById(R.id.stage4Layout)
        stage5Layout = findViewById(R.id.stage5Layout)
        stage6Layout = findViewById(R.id.stage6Layout)
        stage7Layout = findViewById(R.id.stage7Layout)

        // Инициализация статусов этапов
        stage1Status = findViewById(R.id.stage1Status)
        stage2Status = findViewById(R.id.stage2Status)
        stage3Status = findViewById(R.id.stage3Status)
        stage4Status = findViewById(R.id.stage4Status)
        stage5Status = findViewById(R.id.stage5Status)
        stage6Status = findViewById(R.id.stage6Status)
        stage7Status = findViewById(R.id.stage7Status)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish() // Возврат к списку текущих заявок
        }

        // Обработчики кликов по этапам
        stage1Layout.setOnClickListener { showStageDetails(1) }
        stage2Layout.setOnClickListener { showStageDetails(2) }
        stage3Layout.setOnClickListener { showStageDetails(3) }
        stage4Layout.setOnClickListener { showStageDetails(4) }
        stage5Layout.setOnClickListener { showStageDetails(5) }
        stage6Layout.setOnClickListener { showStageDetails(6) }
        stage7Layout.setOnClickListener { showStageDetails(7) }
    }

    private fun displayOrderStages() {
        currentOrder?.let { order ->
            tvOrderNumber.text = "Этапы заявки №${order.orderNumber}"

            // Определяем какие этапы показывать на основе заполненных полей
            val showStages1to4 = order.departureStation.isNotEmpty()
            val showStages5to7 = order.destinationStation.isNotEmpty()

            // Показываем/скрываем этапы в зависимости от заполнения полей
            stage1Layout.visibility = if (showStages1to4 || showStages5to7) LinearLayout.VISIBLE else LinearLayout.GONE
            stage2Layout.visibility = if (showStages1to4) LinearLayout.VISIBLE else LinearLayout.GONE
            stage3Layout.visibility = if (showStages1to4) LinearLayout.VISIBLE else LinearLayout.GONE
            stage4Layout.visibility = if (showStages1to4) LinearLayout.VISIBLE else LinearLayout.GONE
            stage5Layout.visibility = if (showStages5to7) LinearLayout.VISIBLE else LinearLayout.GONE
            stage6Layout.visibility = if (showStages5to7) LinearLayout.VISIBLE else LinearLayout.GONE
            stage7Layout.visibility = if (showStages5to7) LinearLayout.VISIBLE else LinearLayout.GONE

            // Обновляем статусы этапов
            updateStageStatus(1, order.stage1Completed, stage1Status)
            updateStageStatus(2, order.stage2Completed, stage2Status)
            updateStageStatus(3, order.stage3Completed, stage3Status)
            updateStageStatus(4, order.stage4Completed, stage4Status)
            updateStageStatus(5, order.stage5Completed, stage5Status)
            updateStageStatus(6, order.stage6Completed, stage6Status)
            updateStageStatus(7, order.stage7Completed, stage7Status)

            // Показываем сообщение если нет этапов
            if (!showStages1to4 && !showStages5to7) {
                showNoStagesMessage()
            }
        }
    }

    private fun updateStageStatus(stageNumber: Int, isCompleted: Boolean, statusView: TextView) {
        if (isCompleted) {
            statusView.text = "✅ Выполнено"
            statusView.setTextColor(ContextCompat.getColor(this, R.color.green))
        } else {
            statusView.text = "⏳ В процессе"
            statusView.setTextColor(ContextCompat.getColor(this, R.color.orange))
        }
    }

    private fun showStageDetails(stageNumber: Int) {
        currentOrder?.let { order ->
            val title = when (stageNumber) {
                1 -> "Этап №1. Заявка"
                2 -> "Этап №2. Терминал вывоза"
                3 -> "Этап №3. Склад"
                4 -> "Этап №4. Станция отправления"
                5 -> "Этап №5. Станция назначения"
                6 -> "Этап №6. Выдача груза"
                7 -> "Этап №7. Сдача порожнего контейнера"
                else -> "Этап"
            }

            val message = when (stageNumber) {
                1 -> getStage1Details(order)
                2 -> getStage2Details(order)
                3 -> getStage3Details(order)
                4 -> getStage4Details(order)
                5 -> getStage5Details(order)
                6 -> getStage6Details(order)
                7 -> getStage7Details(order)
                else -> "Информация об этапе"
            }

            val status = if (getStageCompleted(order, stageNumber)) "✅ Выполнено" else "⏳ В процессе"

            when (stageNumber) {
                1 -> {
                    AlertDialog.Builder(this)
                        .setTitle(title)
                        .setMessage("$message\n\nСтатус: $status")
                        .setPositiveButton("Просмотреть заявку") { dialog, _ ->
                            val intent = Intent(this, Stage1ViewActivity::class.java)
                            intent.putExtra("ORDER_DATA", order)
                            intent.putExtra("USER_DATA", currentUser)
                            startActivity(intent)
                            dialog.dismiss()
                        }
                        .setNegativeButton("Закрыть", null)
                        .show()
                }
                2 -> {
                    AlertDialog.Builder(this)
                        .setTitle(title)
                        .setMessage("$message\n\nСтатус: $status")
                        .setPositiveButton("Просмотреть терминал") { dialog, _ ->
                            val intent = Intent(this, Stage2TerminalActivity::class.java)
                            intent.putExtra("ORDER_DATA", order)
                            intent.putExtra("USER_DATA", currentUser)
                            startActivity(intent)
                            dialog.dismiss()
                        }
                        .setNegativeButton("Закрыть", null)
                        .show()
                }
                3 -> {
                    AlertDialog.Builder(this)
                        .setTitle(title)
                        .setMessage("$message\n\nСтатус: $status")
                        .setPositiveButton("Просмотреть склад") { dialog, _ ->
                            val intent = Intent(this, Stage3WarehouseActivity::class.java)
                            intent.putExtra("ORDER_DATA", order)
                            intent.putExtra("USER_DATA", currentUser)
                            startActivity(intent)
                            dialog.dismiss()
                        }
                        .setNegativeButton("Закрыть", null)
                        .show()
                }
                4 -> {
                    AlertDialog.Builder(this)
                        .setTitle(title)
                        .setMessage("$message\n\nСтатус: $status")
                        .setPositiveButton("Просмотреть станцию") { dialog, _ ->
                            val intent = Intent(this, Stage4DepartureStationActivity::class.java)
                            intent.putExtra("ORDER_DATA", order)
                            intent.putExtra("USER_DATA", currentUser)
                            startActivity(intent)
                            dialog.dismiss()
                        }
                        .setNegativeButton("Закрыть", null)
                        .show()
                }
                5 -> {
                    // Для этапа 5 - переход на Activity
                    val intent = Intent(this, Stage5DestinationStationActivity::class.java)
                    intent.putExtra("ORDER_DATA", order)
                    intent.putExtra("USER_DATA", currentUser)
                    startActivity(intent)
                }
                6 -> {
                    // Для этапа 6 - переход на Activity
                    val intent = Intent(this, Stage6CargoIssueActivity::class.java)
                    intent.putExtra("ORDER_DATA", order)
                    intent.putExtra("USER_DATA", currentUser)
                    startActivity(intent)
                }
                7 -> {
                    // Для этапа 7 - переход на Activity
                    val intent = Intent(this, Stage7ContainerReturnActivity::class.java)
                    intent.putExtra("ORDER_DATA", order)
                    intent.putExtra("USER_DATA", currentUser)
                    startActivity(intent)
                }
                else -> {
                    AlertDialog.Builder(this)
                        .setTitle(title)
                        .setMessage("$message\n\nСтатус: $status")
                        .setPositiveButton("Закрыть", null)
                        .show()
                }
            }
        }
    }

    private fun getStageCompleted(order: Order1C, stageNumber: Int): Boolean {
        return when (stageNumber) {
            1 -> order.stage1Completed
            2 -> order.terminalStage.acceptedTime > 0 &&
                    order.terminalStage.arrivedTime > 0 &&
                    order.terminalStage.departedTime > 0
            3 -> order.warehouseStage.arrivedTime > 0 &&
                    order.warehouseStage.departedTime > 0
            4 -> order.departureStationStage.arrivedTime > 0 &&
                    order.departureStationStage.departedTime > 0
            5 -> order.stage5Completed
            6 -> order.stage6Completed
            7 -> order.stage7Completed
            else -> false
        }
    }

    private fun getStage1Details(order: Order1C): String {
        return """
            Этап заявки успешно создан и назначен водителю.
            
            Номер заявки: ${order.orderNumber}
            Грузоотправитель: ${order.clientName}
            Тип груза: ${order.cargoType}
            Вес: ${order.weight} кг
            Водитель: ${getDriverName(order.assignedDriverId)}
            
            ${if (order.stage1Completed) "✅ Заявка принята водителем" else "⏳ Ожидание подтверждения водителя"}
        """.trimIndent()
    }

    private fun getStage2Details(order: Order1C): String {
        val terminalStage = order.terminalStage
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())

        val acceptedTime = if (terminalStage.acceptedTime > 0)
            dateFormat.format(Date(terminalStage.acceptedTime)) else "Не принято"

        val arrivedTime = if (terminalStage.arrivedTime > 0)
            dateFormat.format(Date(terminalStage.arrivedTime)) else "Не прибыл"

        val departedTime = if (terminalStage.departedTime > 0)
            dateFormat.format(Date(terminalStage.departedTime)) else "Не выехал"

        return """
        Терминал вывоза порожнего контейнера.
        
        Терминал: ${terminalStage.terminalName}
        Номер контейнера: ${terminalStage.containerNumber}
        
        Временные метки:
        • Принято водителем: $acceptedTime
        • Прибытие на терминал: $arrivedTime  
        • Выезд с терминала: $departedTime
        
        Фотографии: ${order.terminalPhotos.size} шт.
        Документы: ${order.terminalDocuments.size} шт.
        
        ${if (order.stage2Completed) "✅ Терминал вывоза завершен" else "⏳ В процессе работы на терминале"}
    """.trimIndent()
    }

    private fun getStage3Details(order: Order1C): String {
        val warehouseStage = order.warehouseStage
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())

        val arrivedTime = if (warehouseStage.arrivedTime > 0)
            dateFormat.format(Date(warehouseStage.arrivedTime)) else "Не прибыл"

        val departedTime = if (warehouseStage.departedTime > 0)
            dateFormat.format(Date(warehouseStage.departedTime)) else "Не выехал"

        return """
        Склад грузоотправителя - погрузка груза.
        
        Склад: ${warehouseStage.warehouseName}
        Время погрузки: ${warehouseStage.loadingTime}
        Состояние груза: ${warehouseStage.cargoCondition}
        
        Временные метки:
        • Прибытие на склад: $arrivedTime
        • Выезд со склада: $departedTime
        
        Заметки водителя: ${warehouseStage.driverNotes}
        
        Фото погрузки: ${order.warehousePhotos.size} шт.
        Документы: ${order.warehouseDocuments.size} шт.
        
        ${if (order.stage3Completed) "✅ Погрузка на складе завершена" else "⏳ В процессе погрузки"}
    """.trimIndent()
    }

    private fun getStage4Details(order: Order1C): String {
        val stationStage = order.departureStationStage
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())

        val arrivedTime = if (stationStage.arrivedTime > 0)
            dateFormat.format(Date(stationStage.arrivedTime)) else "Не прибыл"

        val departedTime = if (stationStage.departedTime > 0)
            dateFormat.format(Date(stationStage.departedTime)) else "Не выехал"

        val trainDepartureTime = if (stationStage.departureTime > 0)
            dateFormat.format(Date(stationStage.departureTime)) else "Не установлено"

        return """
        Станция отправления - сдача контейнера для отправки.
        
        Станция: ${stationStage.stationName}
        Номер поезда: ${stationStage.trainNumber}
        Отправление поезда: $trainDepartureTime
        
        Временные метки:
        • Прибытие на станцию: $arrivedTime
        • Отправление поезда: $trainDepartureTime
        • Выезд со станции: $departedTime
        
        Заметки водителя: ${stationStage.driverNotes}
        
        Документы: ${order.departureStationDocuments.size} шт.
        
        ${if (order.stage4Completed) "✅ Контейнер сдан на станции отправления" else "⏳ В процессе сдачи на станции"}
    """.trimIndent()
    }

    private fun getStage5Details(order: Order1C): String {
        return """
            Станция назначения.
            
            Станция: ${order.destinationStation}
            Доп. информация: ${if (order.destinationStationInfo.isNotEmpty()) order.destinationStationInfo else "Нет информации"}
            
            ${if (order.stage5Completed) "✅ Груз прибыл на станцию" else "⏳ В пути"}
        """.trimIndent()
    }

    private fun getStage6Details(order: Order1C): String {
        val timeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

        val arrivedTime = if (order.cargoIssueStage.arrivedTime > 0)
            timeFormat.format(Date(order.cargoIssueStage.arrivedTime)) else "Не прибыл"

        val departedTime = if (order.cargoIssueStage.departedTime > 0)
            timeFormat.format(Date(order.cargoIssueStage.departedTime)) else "Не выехал"

        val cargoIssuedStatus = if (order.stage6Completed) "✅ Груз выдан получателю" else "⏳ Ожидание выдачи груза"
        val stageStatus = if (order.stage6Completed) "✅ Выполнено" else "⏳ В процессе"

        return """
        Выдача груза грузополучателю.
        
        Грузополучатель: ${order.consigneeName}
        Адрес: ${order.consigneePostalAddress}
        Контактное лицо: ${order.unloadingContactPerson}
        Доп. информация: ${if (order.notes.isNotEmpty()) order.notes else "Нет информации"}
        
        Временные отметки:
        • Время прибытия: $arrivedTime
        • Время выезда: $departedTime
        
        $cargoIssuedStatus
        
        Статус этапа: $stageStatus
        
        Фото: ${order.cargoIssuePhotos.size} шт.
        Документы: ${order.cargoIssueDocuments.size} шт.
    """.trimIndent()
    }

    private fun openPhotosActivity(order: Order1C, stageType: String) {
        val intent = Intent(this, PhotosActivity::class.java).apply {
            putExtra("ORDER_DATA", order)
            putExtra("STAGE_TYPE", stageType)
        }
        startActivity(intent)
    }

    private fun openDocumentsActivity(order: Order1C, stageType: String) {
        val intent = Intent(this, DocumentsActivity::class.java).apply {
            putExtra("ORDER_DATA", order)
            putExtra("STAGE_TYPE", stageType)
        }
        startActivity(intent)
    }

    private fun markOrderAsCompleted(order: Order1C) {
        order.stage6Completed = true
        order.status = "COMPLETED"
        // Здесь вызов API для сохранения
    }

    private fun getStage7Details(order: Order1C): String {
        val timeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

        val arrivedTime = if (order.containerReturnStage.arrivedTime > 0)
            timeFormat.format(Date(order.containerReturnStage.arrivedTime)) else "Не прибыл"

        val departedTime = if (order.containerReturnStage.departedTime > 0)
            timeFormat.format(Date(order.containerReturnStage.departedTime)) else "Не выехал"

        val stageStatus = if (order.stage7Completed) "✅ Выполнено" else "⏳ В процессе"

        return """
        Терминал сдачи порожнего контейнера.
        
        Терминал: ${order.emptyContainerTerminal}
        
        Временные отметки:
        • Время прибытия: $arrivedTime
        • Время выезда: $departedTime
        
        Дополнительная информация:
        • Название терминала: ${order.containerReturnStage.terminalName}
        • Состояние контейнера: ${order.containerReturnStage.containerCondition}
        
        Документы: ${order.containerReturnDocuments.size} шт.
        
        Статус этапа: $stageStatus
    """.trimIndent()
    }

    private fun getDriverName(driverId: String?): String {
        return when (driverId) {
            "driver1" -> "Петров П.П."
            "driver2" -> "Иванов И.И."
            "driver3" -> "Сидоров А.А."
            else -> "Не назначен"
        }
    }

    private fun showNoStagesMessage() {
        val message = """
            Для отображения этапов перевозки необходимо заполнить информацию о станциях:
            
            • Для этапов 1-4: заполните "Наименование станции отправления"
            • Для этапов 5-7: заполните "Наименование станции назначения"
            • Для всех этапов: заполните обе станции
            
            Перейдите в детали заявки для заполнения информации.
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Этапы не доступны")
            .setMessage(message)
            .setPositiveButton("Понятно", null)
            .show()
    }
}