package com.example.drivermobileapp.admin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.models.User

class AdminActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var btnManageUsers: Button
    private lateinit var btnChangePasswords: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        initViews()
        setupClickListeners()
        displayUserInfo()
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        btnManageUsers = findViewById(R.id.btnManageUsers)
        btnChangePasswords = findViewById(R.id.btnChangePasswords)
    }

    private fun setupClickListeners() {
        btnManageUsers.setOnClickListener {
            // TODO: Переход к управлению пользователями
            showMessage("Управление пользователями")
        }

        btnChangePasswords.setOnClickListener {
            // TODO: Переход к смене паролей
            showMessage("Смена паролей")
        }
    }

    private fun displayUserInfo() {
        val user = intent.getSerializableExtra("USER_DATA") as? User
        user?.let {
            tvWelcome.text = "Администратор: ${it.fullName}"
        }
    }

    private fun showMessage(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}