package com.example.drivermobileapp.logist

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.models.Driver
import com.example.drivermobileapp.data.models.DriverShift
import com.example.drivermobileapp.data.models.User
import com.example.drivermobileapp.data.models.Vehicle
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class DriverShiftsActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var shiftsListView: ListView
    private lateinit var tvEmpty: TextView
    private lateinit var tvDriverName: TextView

    private var currentDriver: Driver? = null
    private var currentUser: User? = null
    private val shifts = mutableListOf<DriverShift>()
    private val vehicles = mutableMapOf<String, Vehicle>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_shifts)

        currentDriver = intent.getSerializableExtra("DRIVER_DATA") as? Driver
        currentUser = intent.getSerializableExtra("USER_DATA") as? User

        initViews()
        setupClickListeners()
        displayDriverInfo()
        loadDriverShifts()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        progressBar = findViewById(R.id.progressBar)
        shiftsListView = findViewById(R.id.shiftsListView)
        tvEmpty = findViewById(R.id.tvEmpty)
        tvDriverName = findViewById(R.id.tvDriverName)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish() // Возврат к карточке водителя
        }
    }

    private fun displayDriverInfo() {
        currentDriver?.let { driver ->
            tvDriverName.text = "Смены водителя: ${driver.fullName}"
        }
    }

    private fun loadDriverShifts() {
        showLoading(true)

        // Имитация загрузки данных
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            shifts.clear()
            vehicles.clear()

            // Создаем тестовые смены
            val testShifts = createTestShifts()
            shifts.addAll(testShifts)

            // Сортируем по дате (новые сверху)
            shifts.sortByDescending { it.startTime }

            updateShiftsList()
            showLoading(false)

        }, 1000)
    }

    private fun createTestShifts(): List<DriverShift> {
        val currentTime = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000L
        val hourInMillis = 60 * 60 * 1000L

        // Создаем тестовые автомобили
        vehicles["vehicle1"] = Vehicle(
            id = "vehicle1",
            driverId = "driver1",
            brand = "Volvo",
            model = "FH16",
            licensePlate = "А123БВ77",
            loadCapacity = 500.0,
            volume = 3.0,
            vehicleType = "Грузовик"
        )

        vehicles["vehicle2"] = Vehicle(
            id = "vehicle2",
            driverId = "driver1",
            brand = "Volvo",
            model = "FMX",
            licensePlate = "В234ГД77",
            loadCapacity = 400.0,
            volume = 2.5,
            vehicleType = "Хэтчбек"
        )

        return listOf(
            DriverShift(
                id = "shift1",
                driverId = "driver1",
                startTime = currentTime - 1 * dayInMillis - 8 * hourInMillis, // Вчера 08:00
                endTime = currentTime - 1 * dayInMillis - 17 * hourInMillis,  // Вчера 17:00
                vehicleId = "vehicle1"
            ),
            DriverShift(
                id = "shift2",
                driverId = "driver1",
                startTime = currentTime - 2 * dayInMillis - 9 * hourInMillis, // 2 дня назад 09:00
                endTime = currentTime - 2 * dayInMillis - 18 * hourInMillis,  // 2 дня назад 18:00
                vehicleId = "vehicle2"
            ),
            DriverShift(
                id = "shift3",
                driverId = "driver1",
                startTime = currentTime - 3 * dayInMillis - 10 * hourInMillis, // 3 дня назад 10:00
                endTime = currentTime - 3 * dayInMillis - 19 * hourInMillis,   // 3 дня назад 19:00
                vehicleId = "vehicle1"
            ),
            DriverShift(
                id = "shift4",
                driverId = "driver1",
                startTime = currentTime - 5 * dayInMillis - 8 * hourInMillis, // 5 дней назад 08:00
                endTime = currentTime - 5 * dayInMillis - 16 * hourInMillis,  // 5 дней назад 16:00
                vehicleId = "vehicle2"
            )
        )
    }

    private fun updateShiftsList() {
        if (shifts.isEmpty()) {
            shiftsListView.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = "Смен нет"
        } else {
            shiftsListView.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE

            val shiftStrings = shifts.map { shift ->
                val vehicle = vehicles[shift.vehicleId]
                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                val duration = calculateShiftDuration(shift.startTime, shift.endTime)

                "📅 ${dateFormat.format(Date(shift.startTime))}\n" +
                        "⏱️ Длительность: $duration\n" +
                        "🚗 ${vehicle?.brand ?: ""} ${vehicle?.model ?: ""} (${vehicle?.licensePlate ?: ""})\n" +
                        "⚖️ Грузоподъемность: ${vehicle?.loadCapacity ?: 0} кг"
            }

            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, shiftStrings)
            shiftsListView.adapter = adapter
        }
    }

    private fun calculateShiftDuration(startTime: Long, endTime: Long): String {
        val durationMillis = endTime - startTime
        val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) - TimeUnit.HOURS.toMinutes(hours)

        return if (hours > 0) {
            "${hours}ч ${minutes}м"
        } else {
            "${minutes}м"
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            shiftsListView.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = "Загрузка смен..."
        }
    }
}