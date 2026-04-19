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
import androidx.appcompat.app.AppCompatActivity
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.api.RetrofitClient
import com.example.drivermobileapp.data.models.User
import com.example.drivermobileapp.driver.OrdersListActivity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DriverActivity : AppCompatActivity() {

    companion object {
        private val DRIVER_LICENSE_REGEX = Regex("""^\d{2}\s?[A-ZА-ЯЁ]{2}\s?\d{6}$""")
        private val LICENSE_PLATE_REGEX =
            Regex("""^[АВЕКМНОРСТУХA-Z]\d{3}[АВЕКМНОРСТУХA-Z]{2}\d{2,3}$""")
    }

    private lateinit var tvWelcome: TextView
    private lateinit var btnShiftControl: Button
    private lateinit var btnIncomingOrders: Button
    private lateinit var btnMyOrders: Button

    private var isShiftActive = false
    private lateinit var shiftRepository: ShiftRepository
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver)

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
            val driverLicense = normalizeDriverLicense(etDriverLicense.text?.toString())
            val licensePlate = normalizeLicensePlate(etLicensePlate.text?.toString())

            if (driverLicense.isEmpty() || licensePlate.isEmpty()) {
                showMessage("Заполните все поля")
                return@setOnClickListener
            }

            val validationError = validateInput(driverLicense, licensePlate)
            if (validationError != null) {
                showMessage(validationError)
                return@setOnClickListener
            }

            etDriverLicense.setText(driverLicense)
            etLicensePlate.setText(licensePlate)

            progressBar.visibility = View.VISIBLE
            btnConfirm.isEnabled = false

            CoroutineScope(Dispatchers.IO).launch {
                val result = shiftRepository.checkTransport(driverLicense, licensePlate)

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnConfirm.isEnabled = true

                    result.fold(
                        onSuccess = { isValid ->
                            if (isValid) {
                                startShiftConfirmed(driverLicense, licensePlate, dialog)
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
            val result = shiftRepository.startShift(user.id, driverLicense, licensePlate)

            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                btnConfirm.isEnabled = true

                result.fold(
                    onSuccess = { shiftId ->  // ← shiftId теперь String
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
        if (!DRIVER_LICENSE_REGEX.matches(driverLicense)) {
            return "Введите ВУ в формате 77 АБ 123456"
        }

        if (!LICENSE_PLATE_REGEX.matches(licensePlate)) {
            return "Введите гос. номер в формате А123БВ77"
        }

        return null
    }

    private fun normalizeDriverLicense(value: String?): String {
        val cleaned = value
            ?.trim()
            ?.uppercase()
            ?.replace(Regex("\\s+"), " ")
            .orEmpty()

        val compact = cleaned.replace(" ", "")
        if (compact.length != 10) {
            return cleaned
        }

        return "${compact.substring(0, 2)} ${compact.substring(2, 4)} ${compact.substring(4)}"
    }

    private fun normalizeLicensePlate(value: String?): String {
        return value
            ?.trim()
            ?.uppercase()
            ?.replace(" ", "")
            .orEmpty()
    }
}
