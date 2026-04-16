package com.example.drivermobileapp.authoriz

import com.example.drivermobileapp.R
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
import com.example.drivermobileapp.data.api.RetrofitClient
import com.example.drivermobileapp.data.api.LoginRequest
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*

class AuthActivity : AppCompatActivity() {

    private lateinit var etLogin: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar

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

        // Запускаем запрос к API в фоновом потоке
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Отправляем запрос на сервер
                val response = RetrofitClient.instance.login(LoginRequest(login, password))

                // Переключаемся на главный поток для обновления UI
                withContext(Dispatchers.Main) {
                    showLoading(false)

                    // Создаем объект User из ответа API
                    val user = User(
                        id = response.id.toString(),
                        login = response.login,
                        password = "", // Пароль не возвращается с сервера
                        role = enumValueOf<UserRole>(response.role.uppercase()),
                        fullName = response.name,
                        isActive = true
                    )

                    navigateToRoleScreen(user)
                }
            } catch (e: Exception) {
                // Ошибка подключения или неверные данные
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showError("Ошибка: ${e.message ?: "Не удалось подключиться к серверу"}")
                }
            }
        }
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