package com.example.drivermobileapp.driver

import OrderDriver
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.local.PreferencesManager
import com.example.drivermobileapp.data.repository.OrderRepository
import kotlinx.coroutines.launch

class OrderStagesActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var tvOrderNumber: TextView
    private lateinit var tvInfo: TextView
    private lateinit var layoutStages: LinearLayout

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

    private var currentOrder: OrderDriver? = null
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var orderRepository: OrderRepository

    // Текущий активный этап с сервера
    private var currentActiveStage: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_driver_stages)

        currentOrder = intent.getSerializableExtra("ORDER") as? OrderDriver
        preferencesManager = PreferencesManager(this)
        orderRepository = OrderRepository(preferencesManager)

        initViews()
        setupClickListeners()
        displayOrderInfo()
        setupStagesVisibility()

        // Загружаем статусы с сервера
        loadStagesFromServer()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvOrderNumber = findViewById(R.id.tvOrderNumber)
        tvInfo = findViewById(R.id.tvInfo)
        layoutStages = findViewById(R.id.layoutStages)

        stage1Layout = findViewById(R.id.stage1Layout)
        stage2Layout = findViewById(R.id.stage2Layout)
        stage3Layout = findViewById(R.id.stage3Layout)
        stage4Layout = findViewById(R.id.stage4Layout)
        stage5Layout = findViewById(R.id.stage5Layout)
        stage6Layout = findViewById(R.id.stage6Layout)
        stage7Layout = findViewById(R.id.stage7Layout)

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
            finish()
        }

        stage1Layout.setOnClickListener { onStageClick(1) }
        stage2Layout.setOnClickListener { onStageClick(2) }
        stage3Layout.setOnClickListener { onStageClick(3) }
        stage4Layout.setOnClickListener { onStageClick(4) }

        stage5Layout.setOnClickListener(null)
        stage6Layout.setOnClickListener(null)
        stage7Layout.setOnClickListener(null)
    }

    private fun displayOrderInfo() {
        currentOrder?.let { order ->
            tvOrderNumber.text = "Этапы заявки №${order.number}"
        }
    }

    private fun setupStagesVisibility() {
        stage1Layout.visibility = View.GONE
        stage2Layout.visibility = View.GONE
        stage3Layout.visibility = View.GONE
        stage4Layout.visibility = View.GONE
        stage5Layout.visibility = View.GONE
        stage6Layout.visibility = View.GONE
        stage7Layout.visibility = View.GONE

        stage1Layout.visibility = View.VISIBLE
        stage2Layout.visibility = View.VISIBLE
        stage3Layout.visibility = View.VISIBLE
        stage4Layout.visibility = View.VISIBLE
    }

    private fun loadStagesFromServer() {
        currentOrder?.let { order ->
            lifecycleScope.launch {
                val response = orderRepository.loadOrderStages(order.number)  // ← Изменено
                if (response != null) {
                    currentActiveStage = response.currentStage
                    updateAllStagesStatus()
                } else {
                    showMessage("Ошибка загрузки статусов")
                    currentActiveStage = preferencesManager.getCurrentStage(order.number)
                    updateAllStagesStatus()
                }
            }
        }
    }

    private fun updateAllStagesStatus() {
        val orderNumber = currentOrder?.number ?: ""

        for (stageNumber in 1..4) {
            val isCompleted = preferencesManager.isStageCompleted(orderNumber, stageNumber)
            val status = when {
                isCompleted -> StageUiStatus.COMPLETED
                stageNumber == currentActiveStage -> StageUiStatus.IN_PROGRESS
                else -> StageUiStatus.PENDING
            }
            updateStageStatusUI(stageNumber, status)
        }
    }

    private fun updateStageStatusUI(stageNumber: Int, status: StageUiStatus) {
        val statusView = when (stageNumber) {
            1 -> stage1Status
            2 -> stage2Status
            3 -> stage3Status
            4 -> stage4Status
            else -> return
        }

        val layout = when (stageNumber) {
            1 -> stage1Layout
            2 -> stage2Layout
            3 -> stage3Layout
            4 -> stage4Layout
            else -> return
        }

        when (status) {
            StageUiStatus.PENDING -> {
                statusView.text = "⏰ Ожидает"
                statusView.setTextColor(ContextCompat.getColor(this, R.color.gray))
                layout.alpha = 0.6f
            }
            StageUiStatus.IN_PROGRESS -> {
                statusView.text = "🔄 В процессе"
                statusView.setTextColor(ContextCompat.getColor(this, R.color.orange))
                layout.alpha = 1.0f
                layout.setBackgroundColor(ContextCompat.getColor(this, R.color.light_orange))
            }
            StageUiStatus.COMPLETED -> {
                statusView.text = "✅ Выполнено"
                statusView.setTextColor(ContextCompat.getColor(this, R.color.green))
                layout.alpha = 1.0f
                layout.setBackgroundResource(R.drawable.stage_background_completed)
            }
        }
    }

    private fun onStageClick(stageNumber: Int) {
        if (stageNumber > currentActiveStage) {
            showMessage("Сначала завершите текущий этап")
            return
        }

        currentOrder?.let { order ->
            when (stageNumber) {
                1 -> {
                    val intent = Intent(this, Stage1DetailActivity::class.java).apply {
                        putExtra("ORDER", order)
                        putExtra("STAGE_NUMBER", 1)
                        putExtra("IS_CURRENT_STAGE", stageNumber == currentActiveStage)
                    }
                    startActivityForResult(intent, stageNumber)
                }
                2 -> {
                    val intent = Intent(this, Stage2DetailActivity::class.java).apply {
                        putExtra("ORDER", order)
                        putExtra("STAGE_NUMBER", 2)
                        putExtra("IS_CURRENT_STAGE", stageNumber == currentActiveStage)
                    }
                    startActivityForResult(intent, stageNumber)
                }
                3 -> {
                    val intent = Intent(this, Stage3DetailActivity::class.java).apply {
                        putExtra("ORDER", order)
                        putExtra("STAGE_NUMBER", 3)
                        putExtra("IS_CURRENT_STAGE", stageNumber == currentActiveStage)
                    }
                    startActivityForResult(intent, stageNumber)
                }
                4 -> {
                    val intent = Intent(this, Stage4DetailActivity::class.java).apply {
                        putExtra("ORDER", order)
                        putExtra("STAGE_NUMBER", 4)
                        putExtra("IS_CURRENT_STAGE", stageNumber == currentActiveStage)
                    }
                    startActivityForResult(intent, stageNumber)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data?.getBooleanExtra("STAGE_COMPLETED", false) == true) {
            val completedStage = requestCode

            if (completedStage == currentActiveStage) {
                // Отправляем запрос на сервер
                completeStageOnServer(completedStage)
            }
        }
    }

    private fun completeStageOnServer(stageNumber: Int) {
        currentOrder?.let { order ->
            lifecycleScope.launch {
                val driverId = getCurrentDriverId()
                val response = orderRepository.completeStage(order.number, stageNumber, driverId)

                // Добавь отладочный вывод
                println("DEBUG completeStage: response = $response")
                println("DEBUG completeStage: success = ${response?.success}")
                println("DEBUG completeStage: message = ${response?.message}")

                if (response != null && response.success) {
                    currentActiveStage = response.nextStage
                    updateAllStagesStatus()
                    showMessage("Этап $stageNumber завершен! Переход к этапу ${response.nextStage}")
                } else {
                    showMessage(response?.message ?: "Ошибка при завершении этапа")
                }
            }
        }
    }

    private fun getCurrentDriverId(): String {
        // Временное решение - потом замените на реальное
        // Получите ID из PreferencesManager или из сессии
        return "driver_123"
    }

    private fun showMessage(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}

enum class StageUiStatus {
    PENDING, IN_PROGRESS, COMPLETED
}