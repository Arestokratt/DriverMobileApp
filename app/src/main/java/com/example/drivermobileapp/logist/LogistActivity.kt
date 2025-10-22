package com.example.drivermobileapp.logist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.models.User

class LogistActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var btnCreateOrder: Button
    private lateinit var btnViewDrivers: Button
    private lateinit var btnAssignOrders: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logist)

        initViews()
        setupClickListeners()
        displayUserInfo()
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        btnCreateOrder = findViewById(R.id.btnCreateOrder)
        btnViewDrivers = findViewById(R.id.btnViewDrivers)
        btnAssignOrders = findViewById(R.id.btnAssignOrders)
    }

    private fun setupClickListeners() {
        btnCreateOrder.setOnClickListener {
            showMessage("Создание заявки")
        }

        btnViewDrivers.setOnClickListener {
            showMessage("Просмотр водителей")
        }

        btnAssignOrders.setOnClickListener {
            showMessage("Назначение заказов")
        }
    }

    private fun displayUserInfo() {
        val user = intent.getSerializableExtra("USER_DATA") as? User
        user?.let {
            tvWelcome.text = "Логист: ${it.fullName}"
        }
    }

    private fun showMessage(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}