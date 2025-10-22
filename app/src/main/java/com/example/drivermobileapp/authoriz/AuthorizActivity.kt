package com.example.drivermobileapp.authoriz

import com.example.drivermobileapp.R  // ← ЭТА СТРОКА ВАЖНА!
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.example.drivermobileapp.data.models.User
import com.example.drivermobileapp.data.models.UserRole
import com.example.drivermobileapp.admin.AdminActivity
import com.example.drivermobileapp.logist.LogistActivity
import com.example.drivermobileapp.driver.DriverActivity
import com.google.android.material.textfield.TextInputEditText

class AuthActivity : AppCompatActivity() {

    private lateinit var etLogin: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar

    // Временные данные для тестирования
    private val users = listOf(
        User("1", "admin", "admin123", UserRole.ADMIN, "Администратор Системы"),
        User("2", "logist1", "logist123", UserRole.LOGIST, "Иванов Иван"),
        User("3", "driver1", "driver123", UserRole.DRIVER, "Петров Петр")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authoriz)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        etLogin = findViewById(R.id.etLogin)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            val login = etLogin.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (validateInput(login, password)) {
                authenticateUser(login, password)
            }
        }
    }

    private fun validateInput(login: String, password: String): Boolean {
        if (login.isEmpty()) {
            etLogin.error = "Введите логин"
            return false
        }

        if (password.isEmpty()) {
            etPassword.error = "Введите пароль"
            return false
        }

        return true
    }

    private fun authenticateUser(login: String, password: String) {
        showLoading(true)

        // Имитация проверки (позже заменим на реальную)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val user = users.find { it.login == login && it.password == password }

            if (user != null && user.isActive) {
                navigateToRoleScreen(user)
            } else {
                showError("Неверный логин или пароль")
            }

            showLoading(false)
        }, 1000)
    }

    private fun navigateToRoleScreen(user: User) {
        val intent = when (user.role) {
            UserRole.ADMIN -> Intent(this, AdminActivity::class.java)
            UserRole.LOGIST -> Intent(this, LogistActivity::class.java)
            UserRole.DRIVER -> Intent(this, DriverActivity::class.java)
        }
        intent.putExtra("USER_DATA", user)
        startActivity(intent)
        finish()
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !show
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}