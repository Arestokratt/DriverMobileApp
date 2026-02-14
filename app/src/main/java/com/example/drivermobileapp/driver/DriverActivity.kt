package com.example.drivermobileapp.driver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.models.User
import com.google.android.material.textfield.TextInputEditText

class DriverActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var btnShiftControl: Button
    private lateinit var btnIncomingOrders: Button
    private lateinit var btnMyOrders: Button

    private var isShiftActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver)

        initViews()
        setupClickListeners()
        displayUserInfo()
        updateUIState()
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        btnShiftControl = findViewById(R.id.btnShiftControl)
        btnIncomingOrders = findViewById(R.id.btnIncomingOrders)
        btnMyOrders = findViewById(R.id.btnMyOrders)
    }

    private fun setupClickListeners() {
        btnShiftControl.setOnClickListener {
            if (isShiftActive) {
                showEndShiftDialog()
            } else {
                showStartShiftDialog()
            }
        }

        btnIncomingOrders.setOnClickListener {
            val intent = Intent(this, OrdersListActivity::class.java).apply {
                putExtra("ORDERS_TYPE", "incoming")
            }
            startActivity(intent)
        }

        btnMyOrders.setOnClickListener {
            val intent = Intent(this, OrdersListActivity::class.java).apply {
                putExtra("ORDERS_TYPE", "my_orders")
            }
            startActivity(intent)
        }
    }

    private fun displayUserInfo() {
        val user = intent.getSerializableExtra("USER_DATA") as? User
        user?.let {
            tvWelcome.text = "Водитель: ${it.fullName}"
        }
    }

    private fun updateUIState() {
        if (isShiftActive) {
            btnShiftControl.text = "Завершить смену"
            btnIncomingOrders.isEnabled = true
            btnMyOrders.isEnabled = true
            btnIncomingOrders.alpha = 1.0f
            btnMyOrders.alpha = 1.0f
        } else {
            btnShiftControl.text = "Начать смену"
            btnIncomingOrders.isEnabled = false
            btnMyOrders.isEnabled = false
            btnIncomingOrders.alpha = 0.5f
            btnMyOrders.alpha = 0.5f
        }
    }

    private fun showStartShiftDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_start_shift, null)
        val etCarBrand = dialogView.findViewById<TextInputEditText>(R.id.etCarBrand)
        val etLicensePlate = dialogView.findViewById<TextInputEditText>(R.id.etLicensePlate)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelStart)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirmStart)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            val carBrand = etCarBrand.text?.toString()?.trim() ?: ""
            val licensePlate = etLicensePlate.text?.toString()?.trim() ?: ""

            if (carBrand.isEmpty() || licensePlate.isEmpty()) {
                showMessage("Заполните все поля")
                return@setOnClickListener
            }

            // Сохраняем данные и начинаем смену
            isShiftActive = true
            updateUIState()

            val startTime = System.currentTimeMillis()
            // saveShiftStart(startTime, carBrand, licensePlate)

            showMessage("Смена начата")
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showEndShiftDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_end_shift, null)
        val btnNo = dialogView.findViewById<Button>(R.id.btnNo)
        val btnYes = dialogView.findViewById<Button>(R.id.btnYes)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnNo.setOnClickListener {
            dialog.dismiss()
        }

        btnYes.setOnClickListener {
            // Завершаем смену
            isShiftActive = false
            updateUIState()

            val endTime = System.currentTimeMillis()
            // saveShiftEnd(endTime)

            showMessage("Смена завершена")
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}