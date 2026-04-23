package com.example.drivermobileapp.logist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.models.User

class LogistActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var btnCreateOrder: Button
    private lateinit var btnViewOrders: Button
    private lateinit var btnViewDrivers: Button
    private lateinit var btnAssignOrders: Button

    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logist)

        currentUser = intent.getSerializableExtra("USER_DATA") as? User
        initViews()
        setupClickListeners()
        displayUserInfo()
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        btnCreateOrder = findViewById(R.id.btnCreateOrder)
        btnViewOrders = findViewById(R.id.btnViewOrders)
        btnViewDrivers = findViewById(R.id.btnViewDrivers)
        btnAssignOrders = findViewById(R.id.btnAssignOrders)
    }

    private fun setupClickListeners() {
//        btnCreateOrder.setOnClickListener {
//            showMessage("Создание заявки - в разработке")
//        }

        // НОВАЯ КНОПКА - переход к заявкам
        btnViewOrders.setOnClickListener {
            val intent = Intent(this, OrdersActivity::class.java)
            intent.putExtra("USER_DATA", currentUser)
            startActivity(intent)
        }

        btnViewDrivers.setOnClickListener {
            val intent = Intent(this, DriversListActivity::class.java)
            intent.putExtra("USER_DATA", currentUser)
            startActivity(intent)
        }

        btnAssignOrders.setOnClickListener {
            showMessage("Назначение заказов")
        }
    }

    private fun displayUserInfo() {
        currentUser?.let {
            tvWelcome.text = "Логист: ${it.fullName}"
        }
    }

    private fun showMessage(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}