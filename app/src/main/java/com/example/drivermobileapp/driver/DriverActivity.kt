package com.example.drivermobileapp.driver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.models.User

class DriverActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var btnViewOrders: Button
    private lateinit var btnCurrentOrder: Button
    private lateinit var btnCompleteOrder: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver)

        initViews()
        setupClickListeners()
        displayUserInfo()
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        btnViewOrders = findViewById(R.id.btnViewOrders)
        btnCurrentOrder = findViewById(R.id.btnCurrentOrder)
        btnCompleteOrder = findViewById(R.id.btnCompleteOrder)
    }

    private fun setupClickListeners() {
        btnViewOrders.setOnClickListener {
            showMessage("Мои заказы")
        }

        btnCurrentOrder.setOnClickListener {
            showMessage("Текущий заказ")
        }

        btnCompleteOrder.setOnClickListener {
            showMessage("Завершить заказ")
        }
    }

    private fun displayUserInfo() {
        val user = intent.getSerializableExtra("USER_DATA") as? User
        user?.let {
            tvWelcome.text = "Водитель: ${it.fullName}"
        }
    }

    private fun showMessage(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}