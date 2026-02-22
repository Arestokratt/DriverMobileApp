package com.example.drivermobileapp.driver

import OrderDriver
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.drivermobileapp.R
//import com.example.drivermobileapp.data.models.OrderDriver

class StageDetailActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var tvStageTitle: TextView
    private lateinit var tvOrderInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stage_detail)

        val order = intent.getSerializableExtra("ORDER") as? OrderDriver
        val stageNumber = intent.getIntExtra("STAGE_NUMBER", 1)

        initViews()
        setupClickListeners()
        displayStageInfo(order, stageNumber)
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvStageTitle = findViewById(R.id.tvStageTitle)
        tvOrderInfo = findViewById(R.id.tvOrderInfo)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun displayStageInfo(order: OrderDriver?, stageNumber: Int) {
        val stageTitles = mapOf(
            1 to "Этап №1. Заявка",
            2 to "Этап №2. Терминал вывоза",
            3 to "Этап №3. Склад",
            4 to "Этап №4. Станция отправления",
            5 to "Этап №5. Станция назначения",
            6 to "Этап №6. Выдача груза",
            7 to "Этап №7. Терминал сдачи"
        )

        tvStageTitle.text = stageTitles[stageNumber] ?: "Этап №$stageNumber"

        order?.let {
            tvOrderInfo.text = "Заявка №${it.number}\nЭтап в разработке..."
        }
    }
}