package com.example.drivermobileapp.logist

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.models.Order
import com.example.drivermobileapp.data.models.OrderStatus
import com.example.drivermobileapp.data.models.User

class CreateOrderActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var btnCreate: Button
    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etFromAddress: EditText
    private lateinit var etToAddress: EditText
    private lateinit var etCargoType: EditText
    private lateinit var etWeight: EditText
    private lateinit var etVolume: EditText
    private lateinit var spinnerPriority: Spinner

    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_order)

        currentUser = intent.getSerializableExtra("USER_DATA") as? User
        initViews()
        setupSpinner()
        setupClickListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnCreate = findViewById(R.id.btnCreate)
        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        etFromAddress = findViewById(R.id.etFromAddress)
        etToAddress = findViewById(R.id.etToAddress)
        etCargoType = findViewById(R.id.etCargoType)
        etWeight = findViewById(R.id.etWeight)
        etVolume = findViewById(R.id.etVolume)
        spinnerPriority = findViewById(R.id.spinnerPriority)
    }

    private fun setupSpinner() {
        val priorities = arrayOf("Обычный", "Срочный", "Очень срочный")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPriority.adapter = adapter
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish() // Возврат к списку заявок
        }

        btnCreate.setOnClickListener {
            if (validateInput()) {
                createOrder()
            }
        }
    }

    private fun validateInput(): Boolean {
        if (etTitle.text.toString().trim().isEmpty()) {
            etTitle.error = "Введите название заявки"
            return false
        }

        if (etFromAddress.text.toString().trim().isEmpty()) {
            etFromAddress.error = "Введите адрес отправления"
            return false
        }

        if (etToAddress.text.toString().trim().isEmpty()) {
            etToAddress.error = "Введите адрес назначения"
            return false
        }

        if (etCargoType.text.toString().trim().isEmpty()) {
            etCargoType.error = "Введите тип груза"
            return false
        }

        val weightText = etWeight.text.toString().trim()
        if (weightText.isEmpty()) {
            etWeight.error = "Введите вес"
            return false
        }

        try {
            val weight = weightText.toDouble()
            if (weight <= 0) {
                etWeight.error = "Вес должен быть больше 0"
                return false
            }
        } catch (e: NumberFormatException) {
            etWeight.error = "Введите корректный вес"
            return false
        }

        val volumeText = etVolume.text.toString().trim()
        if (volumeText.isEmpty()) {
            etVolume.error = "Введите объем"
            return false
        }

        try {
            val volume = volumeText.toDouble()
            if (volume <= 0) {
                etVolume.error = "Объем должен быть больше 0"
                return false
            }
        } catch (e: NumberFormatException) {
            etVolume.error = "Введите корректный объем"
            return false
        }

        return true
    }

    private fun createOrder() {
        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val fromAddress = etFromAddress.text.toString().trim()
        val toAddress = etToAddress.text.toString().trim()
        val cargoType = etCargoType.text.toString().trim()
        val weight = etWeight.text.toString().trim().toDouble()
        val volume = etVolume.text.toString().trim().toDouble()
        val priority = spinnerPriority.selectedItemPosition

        // Создаем новую заявку
        val newOrder = Order(
            id = System.currentTimeMillis().toString(), // Временный ID
            title = title,
            description = description,
            fromAddress = fromAddress,
            toAddress = toAddress,
            cargoType = cargoType,
            weight = weight,
            volume = volume,
            status = OrderStatus.NEW,
            createdBy = currentUser?.id ?: ""
        )

        // TODO: Сохранить заявку в базу данных

        // Показываем подтверждение
        showSuccessDialog(newOrder)
    }

    private fun showSuccessDialog(order: Order) {
        AlertDialog.Builder(this)
            .setTitle("Заявка создана!")
            .setMessage(
                "Заявка '${order.title}' успешно создана.\n\n" +
                        "Статус: Новая\n" +
                        "Груз: ${order.cargoType}\n" +
                        "Вес: ${order.weight} кг\n" +
                        "Объем: ${order.volume} м³"
            )
            .setPositiveButton("OK") { dialog, _ ->
                // Передаем созданную заявку обратно в OrdersActivity
                val resultIntent = Intent()
                resultIntent.putExtra("NEW_ORDER", order)
                setResult(RESULT_OK, resultIntent)

                dialog.dismiss()
                finish() // Возвращаемся к списку заявок
            }
            .setCancelable(false)
            .show()
    }
}