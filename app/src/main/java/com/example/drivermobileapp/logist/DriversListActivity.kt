package com.example.drivermobileapp.logist

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.drivermobileapp.BaseActivity
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.api.DriverResponse
import com.example.drivermobileapp.data.api.RetrofitClient
import com.example.drivermobileapp.data.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DriversListActivity : BaseActivity() {

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
    private val allDrivers = mutableListOf<DriverResponse>()
    private val filteredDrivers = mutableListOf<DriverResponse>()

    private var isAdvancedSearchVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drivers_list)

        currentUser = intent.getSerializableExtra("USER_DATA") as? User

        initViews()
        setupClickListeners()
        setupPageSizeSpinner()
        loadDriversFromApi()
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

        driversListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            if (position < filteredDrivers.size) {
                val driver = filteredDrivers[position]
                openDriverDetails(driver)
            }
        }

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
        spinnerPageSize.setSelection(0)
    }

    private fun toggleAdvancedSearch() {
        if (isAdvancedSearchVisible) {
            advancedSearchLayout.visibility = View.GONE
            btnAdvancedSearch.text = "Перейти к расширенному поиску"
        } else {
            advancedSearchLayout.visibility = View.VISIBLE
            btnAdvancedSearch.text = "Скрыть расширенный поиск"
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
                driver.name.contains(searchQuery, ignoreCase = true) ||
                        driver.login.contains(searchQuery, ignoreCase = true) ||
                        driver.driverLicense.contains(searchQuery, ignoreCase = true)
            })
        }

        // Сортировка по имени
        if (sortByRating) {
            filteredDrivers.sortBy { it.name }
        }

        // Ограничение количества результатов
        if (filteredDrivers.size > pageSize) {
            val limitedList = filteredDrivers.subList(0, pageSize)
            filteredDrivers.clear()
            filteredDrivers.addAll(limitedList)
        }

        updateDriversList()
    }

    private fun loadDriversFromApi() {
        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getDriversList()

                withContext(Dispatchers.Main) {
                    allDrivers.clear()
                    allDrivers.addAll(response)
                    applyFilters()
                    showLoading(false)

                    if (allDrivers.isEmpty()) {
                        Toast.makeText(this@DriversListActivity, "Список водителей пуст", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@DriversListActivity, "Загружено: ${allDrivers.size} водителей", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(this@DriversListActivity, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateDriversList() {
        val driverStrings = filteredDrivers.map { driver ->
            "👤 ${driver.name}\n" +
                    "🔑 Логин: ${driver.login}\n" +
                    "📄 ВУ: ${driver.driverLicense}\n" +
                    "${if (driver.isActive) "✅ Активен" else "❌ Неактивен"}"
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, driverStrings)
        driversListView.adapter = adapter

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

    private fun openDriverDetails(driver: DriverResponse) {
        val intent = Intent(this, DriverDetailActivity::class.java).apply {
            putExtra("DRIVER_ID", driver.id)
            putExtra("DRIVER_NAME", driver.name)
            putExtra("DRIVER_LOGIN", driver.login)
            putExtra("DRIVER_LICENSE", driver.driverLicense)
            putExtra("DRIVER_ACTIVE", driver.isActive)
            putExtra("USER_DATA", currentUser)
        }
        startActivity(intent)
    }
}