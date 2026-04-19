package com.example.drivermobileapp.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.drivermobileapp.BaseActivity
import com.example.drivermobileapp.R
import com.example.drivermobileapp.authoriz.AuthActivity
import com.example.drivermobileapp.data.models.User

class AdminActivity : BaseActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var btnManageUsers: Button
    private lateinit var btnChangePasswords: Button
    private lateinit var btnLogout: Button  // Добавляем

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
        btnLogout = findViewById(R.id.btnLogout)  // Инициализируем
    }

    private fun setupClickListeners() {
        btnManageUsers.setOnClickListener {
            val intent = Intent(this, UserManagementActivity::class.java)
            startActivity(intent)
        }

        // Кнопка выхода
        btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun displayUserInfo() {
        val user = intent.getSerializableExtra("USER_DATA") as? User
        user?.let {
            tvWelcome.text = "Администратор: ${it.fullName}"
        }
    }
}