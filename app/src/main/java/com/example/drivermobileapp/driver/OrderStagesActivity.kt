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
import com.example.drivermobileapp.R
import java.io.Serializable

class OrderStagesActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var tvOrderNumber: TextView
    private lateinit var tvInfo: TextView
    private lateinit var layoutStages: LinearLayout

    // Stage layouts
    private lateinit var stage1Layout: LinearLayout
    private lateinit var stage2Layout: LinearLayout
    private lateinit var stage3Layout: LinearLayout
    private lateinit var stage4Layout: LinearLayout
    private lateinit var stage5Layout: LinearLayout
    private lateinit var stage6Layout: LinearLayout
    private lateinit var stage7Layout: LinearLayout

    // Stage status texts
    private lateinit var stage1Status: TextView
    private lateinit var stage2Status: TextView
    private lateinit var stage3Status: TextView
    private lateinit var stage4Status: TextView
    private lateinit var stage5Status: TextView
    private lateinit var stage6Status: TextView
    private lateinit var stage7Status: TextView

    private var currentOrder: OrderDriver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_driver_stages)

        currentOrder = intent.getSerializableExtra("ORDER") as? OrderDriver

        initViews()
        setupClickListeners()
        displayOrderInfo()
        setupStagesVisibility()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvOrderNumber = findViewById(R.id.tvOrderNumber)
        tvInfo = findViewById(R.id.tvInfo)
        layoutStages = findViewById(R.id.layoutStages)

        // Initialize stage layouts
        stage1Layout = findViewById(R.id.stage1Layout)
        stage2Layout = findViewById(R.id.stage2Layout)
        stage3Layout = findViewById(R.id.stage3Layout)
        stage4Layout = findViewById(R.id.stage4Layout)
        stage5Layout = findViewById(R.id.stage5Layout)
        stage6Layout = findViewById(R.id.stage6Layout)
        stage7Layout = findViewById(R.id.stage7Layout)

        // Initialize status texts
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
            finish() // Возврат к списку "Мои заявки"
        }

        stage1Layout.setOnClickListener { onStageClick(1) }
        stage2Layout.setOnClickListener { onStageClick(2) }
        stage3Layout.setOnClickListener { onStageClick(3) }
        stage4Layout.setOnClickListener { onStageClick(4) }
        stage5Layout.setOnClickListener { onStageClick(5) }
        stage6Layout.setOnClickListener { onStageClick(6) }
        stage7Layout.setOnClickListener { onStageClick(7) }
    }

    private fun displayOrderInfo() {
        currentOrder?.let { order ->
            tvOrderNumber.text = "Этапы заявки №${order.number}"
        }
    }

    private fun setupStagesVisibility() {
        currentOrder?.let { order ->
            // ПОКАЗЫВАЕМ ВСЕ ЭТАПЫ ВСЕГДА
            stage1Layout.visibility = View.VISIBLE
            stage2Layout.visibility = View.VISIBLE
            stage3Layout.visibility = View.VISIBLE
            stage4Layout.visibility = View.VISIBLE
            stage5Layout.visibility = View.VISIBLE
            stage6Layout.visibility = View.VISIBLE
            stage7Layout.visibility = View.VISIBLE

            // Обновляем статусы всех этапов
            updateStageStatus(1, order.stages.stage1.isCompleted, stage1Status)
            updateStageStatus(2, order.stages.stage2.isCompleted, stage2Status)
            updateStageStatus(3, order.stages.stage3.isCompleted, stage3Status)
            updateStageStatus(4, order.stages.stage4.isCompleted, stage4Status)
            updateStageStatus(5, order.stages.stage5.isCompleted, stage5Status)
            updateStageStatus(6, order.stages.stage6.isCompleted, stage6Status)
            updateStageStatus(7, order.stages.stage7.isCompleted, stage7Status)
        }
    }

    private fun updateStageStatus(stageNumber: Int, isCompleted: Boolean, statusView: TextView) {
        if (isCompleted) {
            statusView.text = "✅ Завершено"
            statusView.setTextColor(ContextCompat.getColor(this, R.color.green))
        } else {
            statusView.text = "⏳ В процессе"
            statusView.setTextColor(ContextCompat.getColor(this, R.color.orange))
        }
    }

    private fun onStageClick(stageNumber: Int) {
        currentOrder?.let { order ->
            when (stageNumber) {
                1 -> {
                    val intent = Intent(this, Stage1DetailActivity::class.java).apply {
                        putExtra("ORDER", order)
                    }
                    startActivity(intent)
                }
                2 -> {
                    val intent = Intent(this, Stage2DetailActivity::class.java).apply {
                        putExtra("ORDER", order)
                    }
                    startActivity(intent)
                }
                3 -> {
                    val intent = Intent(this, Stage3DetailActivity::class.java).apply {
                        putExtra("ORDER", order)
                    }
                    startActivity(intent)
                }
                4 -> {
                    val intent = Intent(this, Stage4DetailActivity::class.java).apply {
                        putExtra("ORDER", order)
                    }
                    startActivity(intent)
                }
                5 -> {
                    val intent = Intent(this, Stage5DetailActivity::class.java).apply {
                        putExtra("ORDER", order)
                    }
                    startActivity(intent)
                }
                6 -> {
                    val intent = Intent(this, Stage6DetailActivity::class.java).apply {
                        putExtra("ORDER", order)
                    }
                    startActivity(intent)
                }
                7 -> {
                    val intent = Intent(this, Stage7DetailActivity::class.java).apply {
                        putExtra("ORDER", order)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    private fun showMessage(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}