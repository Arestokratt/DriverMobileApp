package com.example.drivermobileapp.driver.DriverCheck

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.drivermobileapp.BaseActivity  // ← Изменено
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.api.RetrofitClient
import com.example.drivermobileapp.data.models.User
import com.example.drivermobileapp.data.repositories.ShiftRepository
import com.example.drivermobileapp.driver.OrdersListActivity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DriverActivity : BaseActivity() {  // ← Изменено: AppCompatActivity → BaseActivity

    companion object {
        private val DRIVER_LICENSE_REGEX = Regex("""^\d{10}$|^\d{2}\s\d{2}\s\d{6}$""")
        private val LICENSE_PLATE_REGEX =
            Regex("""^[АВЕКМНОРСТУХA-Z]\d{3}[АВЕКМНОРСТУХA-Z]{2}\d{2,3}$""")
    }

    private lateinit var tvWelcome: TextView
    private lateinit var btnShiftControl: Button
    private lateinit var btnIncomingOrders: Button
    private lateinit var btnMyOrders: Button
    private lateinit var btnLogout: Button  // ← Добавлено

    private var isShiftActive = false
    private lateinit var shiftRepository: ShiftRepository
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver)

        // Инициализируем Repository с нашим API
        shiftRepository = ShiftRepository(RetrofitClient.instance)

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
        btnLogout = findViewById(R.id.btnLogout)  // ← Добавлено
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

        // ← Добавлено: кнопка выхода
        btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun displayUserInfo() {
        user = intent.getSerializableExtra("USER_DATA") as? User ?: return
        tvWelcome.text = "Водитель: ${user.fullName}"
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
        val etDriverLicense = dialogView.findViewById<TextInputEditText>(R.id.etDriverLicense)
        val etLicensePlate = dialogView.findViewById<TextInputEditText>(R.id.etLicensePlate)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelStart)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirmStart)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progressBar)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            var driverLicense = etDriverLicense.text?.toString()?.trim() ?: ""
            val licensePlate = etLicensePlate.text?.toString()?.trim()?.uppercase() ?: ""

            // Очищаем от пробелов для проверки
            driverLicense = driverLicense.replace("\\s".toRegex(), "")

            if (driverLicense.isEmpty() || licensePlate.isEmpty()) {
                showMessage("Заполните все поля")
                return@setOnClickListener
            }

            val validationError = validateInput(driverLicense, licensePlate)
            if (validationError != null) {
                showMessage(validationError)
                return@setOnClickListener
            }

            // Нормализуем для отображения
            val normalizedLicense = normalizeDriverLicense(driverLicense)
            etDriverLicense.setText(normalizedLicense)
            etLicensePlate.setText(licensePlate)

            progressBar.visibility = View.VISIBLE
            btnConfirm.isEnabled = false

            CoroutineScope(Dispatchers.IO).launch {
                // Проверяем по очищенному ВУ и гос. номеру
                val result = shiftRepository.checkTransport(driverLicense, licensePlate)

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnConfirm.isEnabled = true

                    result.fold(
                        onSuccess = { isValid ->
                            if (isValid) {
                                startShiftConfirmed(driverLicense, licensePlate, dialog)
                            } else {
                                showMessage("Транспорт не найден")
                            }
                        },
                        onFailure = { exception ->
                            showMessage(exception.message ?: "Ошибка проверки")
                        }
                    )
                }
            }
        }

        dialog.show()
    }

    private fun startShiftConfirmed(driverLicense: String, licensePlate: String, dialog: AlertDialog) {
        val progressBar = dialog.findViewById<ProgressBar>(R.id.progressBar)!!
        val btnConfirm = dialog.findViewById<Button>(R.id.btnConfirmStart)!!


        progressBar.visibility = View.VISIBLE
        btnConfirm.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            // Передаем ВУ и гос. номер
            val result = shiftRepository.startShift(user.id, driverLicense, licensePlate)  // ← изменено

            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                btnConfirm.isEnabled = true

                result.fold(
                    onSuccess = { shiftId ->
                        isShiftActive = true
                        updateUIState()
                        showMessage("Смена начата. ID: $shiftId")
                        dialog.dismiss()
                    },
                    onFailure = { exception ->
                        showMessage(exception.message ?: "Ошибка начала смены")
                    }
                )
            }
        }
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
            isShiftActive = false
            updateUIState()
            showMessage("Смена завершена")
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun validateInput(driverLicense: String, licensePlate: String): String? {
        // Очищаем от пробелов для проверки
        val cleanLicense = driverLicense.replace("\\s".toRegex(), "")

        if (cleanLicense.length != 10) {
            return "ВУ должно содержать 10 цифр (пример: 99 34 126970 или 9934126970)"
        }

        if (!cleanLicense.all { it.isDigit() }) {
            return "Номер ВУ может содержать только цифры"
        }

        if (!LICENSE_PLATE_REGEX.matches(licensePlate.uppercase())) {
            return "Гос. номер должен быть в формате: А123БВ77 или А123БВ777"
        }

        return null
    }

    private fun normalizeDriverLicense(value: String?): String {
        val cleaned = value
            ?.trim()
            ?.replace("\\s".toRegex(), "")
            .orEmpty()

        // Если 10 цифр, форматируем как "99 34 126970" (2-2-6)
        return if (cleaned.length == 10 && cleaned.all { it.isDigit() }) {
            "${cleaned.substring(0, 2)} ${cleaned.substring(2, 4)} ${cleaned.substring(4)}"
        } else {
            cleaned
        }
    }

    private fun normalizeLicensePlate(value: String?): String {
        return value
            ?.trim()
            ?.uppercase()
            ?.replace(" ", "")
            .orEmpty()
    }
}
