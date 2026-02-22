package com.example.drivermobileapp.logist

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.models.Driver
import com.example.drivermobileapp.data.models.User
import com.example.drivermobileapp.data.models.Vehicle

class DriversListActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var etSearch: EditText
    private lateinit var etAdvancedSearch: EditText
    private lateinit var btnSearch: Button
    private lateinit var btnAdvancedSearch: Button
    private lateinit var advancedSearchLayout: LinearLayout
    private lateinit var spinnerPageSize: Spinner
    private lateinit var cbSortByRating: CheckBox
    private lateinit var progressBar: ProgressBar
    private lateinit var driversListView: ListView
    private lateinit var tvEmpty: TextView

    private var currentUser: User? = null
    private val allDrivers = mutableListOf<Driver>()
    private val filteredDrivers = mutableListOf<Driver>()
    private val driversVehicles = mutableMapOf<String, Vehicle>()

    private var isAdvancedSearchVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drivers_list)

        currentUser = intent.getSerializableExtra("USER_DATA") as? User

        initViews()
        setupClickListeners()
        setupPageSizeSpinner()
        loadDrivers()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        etSearch = findViewById(R.id.etSearch)
        etAdvancedSearch = findViewById(R.id.etAdvancedSearch)
        btnSearch = findViewById(R.id.btnSearch)
        btnAdvancedSearch = findViewById(R.id.btnAdvancedSearch)
        advancedSearchLayout = findViewById(R.id.advancedSearchLayout)
        spinnerPageSize = findViewById(R.id.spinnerPageSize)
        cbSortByRating = findViewById(R.id.cbSortByRating)
        progressBar = findViewById(R.id.progressBar)
        driversListView = findViewById(R.id.driversListView)
        tvEmpty = findViewById(R.id.tvEmpty)

        // Настройка EmptyView
        driversListView.emptyView = tvEmpty
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnSearch.setOnClickListener {
            performSearch(false)
        }

        btnAdvancedSearch.setOnClickListener {
            toggleAdvancedSearch()
        }

        // Обработка поиска по Enter
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                performSearch(false)
                true
            } else {
                false
            }
        }

        etAdvancedSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                performSearch(true)
                true
            } else {
                false
            }
        }

        // Клик по элементу списка
        driversListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            if (position < filteredDrivers.size) {
                val driver = filteredDrivers[position]
                openDriverDetails(driver)
            }
        }

        // Автопоиск при изменении параметров в расширенном поиске
        spinnerPageSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isAdvancedSearchVisible) {
                    performSearch(true)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        cbSortByRating.setOnCheckedChangeListener { _, isChecked ->
            if (isAdvancedSearchVisible) {
                performSearch(true)
            }
        }
    }

    private fun setupPageSizeSpinner() {
        val pageSizes = arrayOf("10", "50", "100")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, pageSizes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPageSize.adapter = adapter
        spinnerPageSize.setSelection(0) // По умолчанию 10
    }

    private fun toggleAdvancedSearch() {
        if (isAdvancedSearchVisible) {
            advancedSearchLayout.visibility = View.GONE
            btnAdvancedSearch.text = "Перейти к расширенному поиску"
        } else {
            advancedSearchLayout.visibility = View.VISIBLE
            btnAdvancedSearch.text = "Скрыть расширенный поиск"
            // Копируем текст из основного поиска
            if (!etSearch.text.isNullOrEmpty()) {
                etAdvancedSearch.setText(etSearch.text)
            }
        }
        isAdvancedSearchVisible = !isAdvancedSearchVisible
    }

    private fun performSearch(isAdvanced: Boolean) {
        val searchQuery = if (isAdvanced) {
            etAdvancedSearch.text.toString().trim()
        } else {
            etSearch.text.toString().trim()
        }

        val pageSize = if (isAdvanced) {
            spinnerPageSize.selectedItem.toString().toInt()
        } else {
            10
        }

        val sortByRating = isAdvanced && cbSortByRating.isChecked

        hideKeyboard()
        applyFilters(searchQuery, pageSize, sortByRating)
    }

    private fun applyFilters(searchQuery: String = "", pageSize: Int = 10, sortByRating: Boolean = false) {
        filteredDrivers.clear()

        if (searchQuery.isEmpty()) {
            filteredDrivers.addAll(allDrivers)
        } else {
            filteredDrivers.addAll(allDrivers.filter { driver ->
                driver.fullName.contains(searchQuery, ignoreCase = true) ||
                        driver.driverLicenseNumber.contains(searchQuery, ignoreCase = true) ||
                        driver.phoneNumber.contains(searchQuery, ignoreCase = true)
            })
        }

        // Сортировка по рейтингу
        if (sortByRating) {
            filteredDrivers.sortByDescending { it.rating }
        }

        // Ограничение количества результатов
        if (filteredDrivers.size > pageSize) {
            val limitedList = filteredDrivers.subList(0, pageSize)
            filteredDrivers.clear()
            filteredDrivers.addAll(limitedList)
        }

        updateDriversList()
    }

    private fun loadDrivers() {
        showLoading(true)

        // Имитация загрузки
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            allDrivers.clear()
            driversVehicles.clear()

            val drivers = createTestDrivers()
            allDrivers.addAll(drivers)

            applyFilters() // Показываем всех водителей
            showLoading(false)
        }, 1000)
    }

    private fun createTestDrivers(): List<Driver> {
        val currentTime = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000L

        return listOf(
            Driver(
                id = "driver1", firstName = "Иван", lastName = "Петров", middleName = "Сергеевич",
                phoneNumber = "+7-999-123-45-67", passportData = "4510 123456",
                driverLicenseNumber = "77 АБ 123456", rating = 4.8,
                registrationDate = currentTime - 180 * dayInMillis
            ),
            Driver(
                id = "driver2", firstName = "Алексей", lastName = "Сидоров", middleName = "Николаевич",
                phoneNumber = "+7-999-234-56-78", passportData = "4510 234567",
                driverLicenseNumber = "77 ВГ 234567", rating = 4.5,
                registrationDate = currentTime - 120 * dayInMillis
            ),
            Driver(
                id = "driver3", firstName = "Дмитрий", lastName = "Козлов", middleName = "Андреевич",
                phoneNumber = "+7-999-345-67-89", passportData = "4510 345678",
                driverLicenseNumber = "77 ДЕ 345678", rating = 4.9,
                registrationDate = currentTime - 90 * dayInMillis
            ),
            Driver(
                id = "driver4", firstName = "Сергей", lastName = "Иванов", middleName = "Петрович",
                phoneNumber = "+7-999-456-78-90", passportData = "4510 456789",
                driverLicenseNumber = "77 ЖЗ 456789", rating = 4.7,
                registrationDate = currentTime - 60 * dayInMillis
            )
        ).also { drivers ->
            // Создаем автомобили для водителей
            drivers.forEachIndexed { index, driver ->
                driversVehicles[driver.id] = Vehicle(
                    id = "vehicle${index + 1}", driverId = driver.id,
                    brand = listOf("Volve", "JAC", "ГАЗ", "Hyundai")[index],
                    model = listOf("FH16", "N200", "ГАЗель", "Trago")[index],
                    licensePlate = listOf("А123БВ77", "В234ГД77", "Е345ЖЗ77", "К456ЛМ77")[index],
                    loadCapacity = 500.0 - index * 20.0,
                    volume = 3.0 - index * 0.1,
                    vehicleType = "Легковой"
                )
            }
        }
    }

    private fun updateDriversList() {
        val driverStrings = filteredDrivers.map { driver ->
            val vehicle = driversVehicles[driver.id]
            val vehicleInfo = vehicle?.let { "${it.brand} ${it.model} (${it.licensePlate})" } ?: "Авто не указано"

            "👤 ${driver.fullName}\n" +
                    "📞 ${driver.phoneNumber}\n" +
                    "📄 В/у: ${driver.driverLicenseNumber}\n" +
                    "⭐ Рейтинг: ${driver.rating}/5.0 • 🚗 $vehicleInfo"
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, driverStrings)
        driversListView.adapter = adapter

        // Обновляем текст пустого списка
        tvEmpty.text = if (etSearch.text.isNotEmpty() || etAdvancedSearch.text.isNotEmpty()) {
            "По вашему запросу ничего не найдено"
        } else {
            "Водителей нет"
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            tvEmpty.text = "Загрузка водителей..."
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(etSearch.windowToken, 0)
        imm.hideSoftInputFromWindow(etAdvancedSearch.windowToken, 0)
    }

    private fun openDriverDetails(driver: Driver) {
        val vehicle = driversVehicles[driver.id]
        val intent = Intent(this, DriverDetailActivity::class.java).apply {
            putExtra("DRIVER_DATA", driver)
            putExtra("VEHICLE_DATA", vehicle)
            putExtra("USER_DATA", currentUser)
        }
        startActivity(intent)
    }

    // Computed property for full name
    private val Driver.fullName: String
        get() = "$lastName $firstName ${middleName ?: ""}".trim()
}