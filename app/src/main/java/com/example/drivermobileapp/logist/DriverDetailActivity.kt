package com.example.drivermobileapp.logist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.models.Driver
import com.example.drivermobileapp.data.models.DriverShift
import com.example.drivermobileapp.data.models.User
import com.example.drivermobileapp.data.models.Vehicle
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale

class DriverDetailActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var btnChat: Button
    private lateinit var btnCall: Button
    private lateinit var btnShifts: Button

    private lateinit var ivDriverPhoto: ImageView
    private lateinit var tvDriverName: TextView
    private lateinit var tvPhoneNumber: TextView
    private lateinit var tvPassportData: TextView
    private lateinit var tvDriverLicense: TextView
    private lateinit var tvRating: TextView
    private lateinit var tvVehicleInfo: TextView

    private var currentDriver: Driver? = null
    private var currentVehicle: Vehicle? = null
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_detail)

        currentDriver = intent.getSerializableExtra("DRIVER_DATA") as? Driver
        currentVehicle = intent.getSerializableExtra("VEHICLE_DATA") as? Vehicle
        currentUser = intent.getSerializableExtra("USER_DATA") as? User

        initViews()
        setupClickListeners()
        displayDriverData()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnChat = findViewById(R.id.btnChat)
        btnCall = findViewById(R.id.btnCall)
        btnShifts = findViewById(R.id.btnShifts)

        ivDriverPhoto = findViewById(R.id.ivDriverPhoto)
        tvDriverName = findViewById(R.id.tvDriverName)
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber)
        tvPassportData = findViewById(R.id.tvPassportData)
        tvDriverLicense = findViewById(R.id.tvDriverLicense)
        tvRating = findViewById(R.id.tvRating)
        tvVehicleInfo = findViewById(R.id.tvVehicleInfo)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish() // Возврат к списку водителей
        }

        btnChat.setOnClickListener {
            openChatWithDriver()
        }

        btnCall.setOnClickListener {
            makeCallToDriver()
        }

        btnShifts.setOnClickListener {
            openDriverShifts()
        }
    }

    private fun displayDriverData() {
        currentDriver?.let { driver ->
            // Заголовок
            title = driver.fullName

            // Основная информация
            tvDriverName.text = driver.fullName
            tvPhoneNumber.text = driver.phoneNumber
            tvPassportData.text = driver.passportData
            tvDriverLicense.text = driver.driverLicenseNumber
            tvRating.text = "⭐ ${driver.rating}/5.0"

            // Информация о машине
            currentVehicle?.let { vehicle ->
                tvVehicleInfo.text = "${vehicle.brand} ${vehicle.model}\n" +
                        "Гос. номер: ${vehicle.licensePlate}\n" +
                        "Грузоподъемность: ${vehicle.loadCapacity} кг\n" +
                        "Объем: ${vehicle.volume} м³"
            } ?: run {
                tvVehicleInfo.text = "Автомобиль не назначен"
            }

            // TODO: Загрузка фото водителя
            // Glide.with(this).load(driver.photoUrl).into(ivDriverPhoto)
        }
    }

    private fun openChatWithDriver() {
        currentDriver?.let { driver ->
            // TODO: Реализовать переход в чат
            Toast.makeText(this, "Открытие чата с ${driver.fullName}", Toast.LENGTH_SHORT).show()

            // Временная заглушка
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("DRIVER_DATA", driver)
                putExtra("USER_DATA", currentUser)
            }
            startActivity(intent)
        }
    }

    private fun makeCallToDriver() {
        currentDriver?.let { driver ->
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${driver.phoneNumber}")
            }
            startActivity(intent)
        }
    }

    private fun openDriverShifts() {
        currentDriver?.let { driver ->
            val intent = Intent(this, DriverShiftsActivity::class.java).apply {
                putExtra("DRIVER_DATA", driver)
                putExtra("USER_DATA", currentUser)
            }
            startActivity(intent)
        }
    }
}