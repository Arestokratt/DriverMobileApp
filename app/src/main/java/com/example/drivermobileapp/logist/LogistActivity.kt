package com.example.drivermobileapp.logist

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.drivermobileapp.BaseActivity
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.models.User

class LogistActivity : BaseActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var btnViewOrders: Button
    private lateinit var btnAddDriver: Button
    private lateinit var btnDriversList: Button
    private lateinit var btnAssignOrders: Button
    private lateinit var btnLogout: Button

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
        btnViewOrders = findViewById(R.id.btnViewOrders)
        btnAddDriver = findViewById(R.id.btnAddDriver)
        btnDriversList = findViewById(R.id.btnDriversList)
        btnAssignOrders = findViewById(R.id.btnAssignOrders)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun setupClickListeners() {
        btnViewOrders.setOnClickListener {
            val intent = Intent(this, OrdersActivity::class.java)
            intent.putExtra("USER_DATA", currentUser)
            startActivity(intent)
        }

        btnAddDriver.setOnClickListener {
            val intent = Intent(this, AddDriverActivity::class.java)
            startActivity(intent)
        }

        btnDriversList.setOnClickListener {
            val intent = Intent(this, DriversListActivity::class.java)
            intent.putExtra("USER_DATA", currentUser)
            startActivity(intent)
        }

        btnAssignOrders.setOnClickListener {
            showMessage("Назначение заказов")
        }

        btnLogout.setOnClickListener {
            showLogoutDialog()
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