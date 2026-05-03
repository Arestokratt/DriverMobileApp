package com.example.drivermobileapp.logist

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.example.drivermobileapp.BaseActivity
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.api.RetrofitClient
import com.example.drivermobileapp.data.api.CreateUserRequest
import com.example.drivermobileapp.data.api.CreateDriverRequest
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddDriverActivity : BaseActivity() {

    private lateinit var etLogin: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etFullName: TextInputEditText
    private lateinit var etDriverLicense: TextInputEditText
    private lateinit var btnCreateDriver: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_driver)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        etLogin = findViewById(R.id.etLogin)
        etPassword = findViewById(R.id.etPassword)
        etFullName = findViewById(R.id.etFullName)
        etDriverLicense = findViewById(R.id.etDriverLicense)
        btnCreateDriver = findViewById(R.id.btnCreateDriver)
        btnCancel = findViewById(R.id.btnCancel)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupClickListeners() {
        btnCreateDriver.setOnClickListener {
            createDriver()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun createDriver() {
        val login = etLogin.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val fullName = etFullName.text.toString().trim()
        val driverLicense = etDriverLicense.text.toString().trim()

        // Валидация
        if (login.isEmpty()) {
            etLogin.error = "Введите логин"
            return
        }
        if (password.isEmpty()) {
            etPassword.error = "Введите пароль"
            return
        }
        if (fullName.isEmpty()) {
            etFullName.error = "Введите полное имя"
            return
        }
        if (driverLicense.isEmpty()) {
            etDriverLicense.error = "Введите ВУ"
            return
        }

        showLoading(true)

        // ПРОВЕРКА: сначала проверим что сервер доступен
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = okhttp3.OkHttpClient()
                val request = okhttp3.Request.Builder()
                    .url("http://10.0.2.2:8080/users")
                    .build()
                val response = client.newCall(request).execute()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@AddDriverActivity, "Сервер доступен! Пытаемся создать...", Toast.LENGTH_SHORT).show()
                        // Теперь создаем водителя
                        createDriverRequest(login, password, fullName, driverLicense)
                    } else {
                        showLoading(false)
                        Toast.makeText(this@AddDriverActivity, "Сервер недоступен: ${response.code}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(this@AddDriverActivity, "Ошибка подключения: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun createDriverRequest(login: String, password: String, fullName: String, driverLicense: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Создаем пользователя
                val createUserRequest = CreateUserRequest(
                    login = login,
                    password = password,
                    role = "DRIVER",
                    fullName = fullName,
                    isActive = true
                )

                val userResponse = RetrofitClient.authApi.createUser(createUserRequest)

                // 2. Создаем запись водителя
                val createDriverRequest = CreateDriverRequest(
                    userLogin = login,
                    driverLicense = driverLicense
                )

                val driverResponse = RetrofitClient.authApi.createDriver(createDriverRequest)

                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(this@AddDriverActivity, "Водитель успешно создан!", Toast.LENGTH_LONG).show()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(this@AddDriverActivity, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnCreateDriver.isEnabled = !show
        btnCancel.isEnabled = !show
    }
}