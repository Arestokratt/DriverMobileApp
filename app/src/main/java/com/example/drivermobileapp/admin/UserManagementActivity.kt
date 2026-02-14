package com.example.drivermobileapp.admin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.models.User
import com.example.drivermobileapp.data.models.UserRole

import android.app.AlertDialog
import android.widget.EditText

class UserManagementActivity : AppCompatActivity() {

    private lateinit var spinnerRoleFilter: Spinner
    private lateinit var btnAddUser: Button
    private lateinit var btnEditUser: Button
    private lateinit var btnDeleteUser: Button
    private lateinit var usersListView: ListView
    private lateinit var tvEmptyList: TextView

    // Временное хранилище пользователей (позже заменим на БД)
    private val users = mutableListOf(
        User("1", "admin", "admin123", UserRole.ADMIN, "Администратор Системы"),
        User("2", "logist1", "logist123", UserRole.LOGIST, "Иванов Иван"),
        User("3", "driver1", "driver123", UserRole.DRIVER, "Петров Петр"),
        User("4", "driver2", "driver456", UserRole.DRIVER, "Сидоров Алексей")
    )

    private var filteredUsers = mutableListOf<User>()
    private var selectedUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_management)

        initViews()
        setupSpinner()
        setupListView()
        setupClickListeners()
        filterUsers(UserRole.ADMIN) // По умолчанию показываем всех
    }

    private fun initViews() {
        spinnerRoleFilter = findViewById(R.id.spinnerRoleFilter)
        btnAddUser = findViewById(R.id.btnAddUser)
        btnEditUser = findViewById(R.id.btnEditUser)
        btnDeleteUser = findViewById(R.id.btnDeleteUser)
        usersListView = findViewById(R.id.usersListView)
        tvEmptyList = findViewById(R.id.tvEmptyList)
    }

    private fun setupSpinner() {
        val roles = arrayOf("Все", "Администраторы", "Логисты", "Водители")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRoleFilter.adapter = adapter

        spinnerRoleFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> filterUsers(null) // Все
                    1 -> filterUsers(UserRole.ADMIN)
                    2 -> filterUsers(UserRole.LOGIST)
                    3 -> filterUsers(UserRole.DRIVER)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupListView() {
        usersListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            selectedUser = filteredUsers[position]
            showMessage("Выбран: ${selectedUser!!.fullName}")
        }
    }

    private fun setupClickListeners() {
        btnAddUser.setOnClickListener {
            showAddUserDialog()
        }

        btnEditUser.setOnClickListener {
            if (selectedUser != null) {
                showEditUserDialog(selectedUser!!)
            } else {
                showMessage("Выберите пользователя для редактирования")
            }
        }

        btnDeleteUser.setOnClickListener {
            if (selectedUser != null) {
                showDeleteUserDialog(selectedUser!!)
            } else {
                showMessage("Выберите пользователя для удаления")
            }
        }
    }

    private fun filterUsers(role: UserRole?) {
        filteredUsers.clear()

        if (role == null) {
            filteredUsers.addAll(users)
        } else {
            filteredUsers.addAll(users.filter { it.role == role })
        }

        updateUsersList()
    }

    private fun updateUsersList() {
        if (filteredUsers.isEmpty()) {
            usersListView.visibility = View.GONE
            tvEmptyList.visibility = View.VISIBLE
        } else {
            usersListView.visibility = View.VISIBLE
            tvEmptyList.visibility = View.GONE

            val userStrings = filteredUsers.map {
                "${it.fullName} (${it.login}) - ${getRoleName(it.role)}"
            }

            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, userStrings)
            usersListView.adapter = adapter
        }
    }

    private fun getRoleName(role: UserRole): String {
        return when (role) {
            UserRole.ADMIN -> "Администратор"
            UserRole.LOGIST -> "Логист"
            UserRole.DRIVER -> "Водитель"
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showAddUserDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_user, null)
        val spinnerRole = dialogView.findViewById<Spinner>(R.id.spinnerRole)
        val etLogin = dialogView.findViewById<EditText>(R.id.etLogin)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
        val etFullName = dialogView.findViewById<EditText>(R.id.etFullName)

        // Настройка спиннера ролей
        val roles = arrayOf("Водитель", "Логист", "Администратор")
        val roleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = roleAdapter

        val dialog = AlertDialog.Builder(this)
            .setTitle("Добавить пользователя")
            .setView(dialogView)
            .setPositiveButton("Добавить") { dialogInterface, _ ->
                val login = etLogin.text.toString().trim()
                val password = etPassword.text.toString().trim()
                val fullName = etFullName.text.toString().trim()
                val selectedRole = when (spinnerRole.selectedItemPosition) {
                    0 -> UserRole.DRIVER
                    1 -> UserRole.LOGIST
                    2 -> UserRole.ADMIN
                    else -> UserRole.DRIVER
                }

                if (validateUserInput(login, password, fullName)) {
                    addNewUser(login, password, selectedRole, fullName)
                } else {
                    showMessage("Заполните все поля")
                }
            }
            .setNegativeButton("Отмена", null)
            .create()

        dialog.show()
    }

    private fun validateUserInput(login: String, password: String, fullName: String): Boolean {
        return login.isNotEmpty() && password.isNotEmpty() && fullName.isNotEmpty()
    }

    private fun addNewUser(login: String, password: String, role: UserRole, fullName: String) {
        // Проверяем, нет ли уже пользователя с таким логином
        if (users.any { it.login == login }) {
            showMessage("Пользователь с таким логином уже существует")
            return
        }

        val newUser = User(
            id = (users.size + 1).toString(),
            login = login,
            password = password,
            role = role,
            fullName = fullName
        )

        users.add(newUser)
        filterUsers(spinnerRoleFilter.selectedItemPosition.let {
            when (it) {
                0 -> null
                1 -> UserRole.ADMIN
                2 -> UserRole.LOGIST
                3 -> UserRole.DRIVER
                else -> null
            }
        })
        showMessage("Пользователь $fullName добавлен")
    }

    private fun showEditUserDialog(user: User) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_user, null)
        val etLogin = dialogView.findViewById<EditText>(R.id.etLogin)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
        val etFullName = dialogView.findViewById<EditText>(R.id.etFullName)
        val tvCurrentRole = dialogView.findViewById<TextView>(R.id.tvCurrentRole)

        // Заполняем текущие данные
        etLogin.setText(user.login)
        etPassword.setText(user.password)
        etFullName.setText(user.fullName)
        tvCurrentRole.text = "Текущая роль: ${getRoleName(user.role)}"

        val dialog = AlertDialog.Builder(this)
            .setTitle("Редактировать пользователя")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { dialogInterface, _ ->
                val newLogin = etLogin.text.toString().trim()
                val newPassword = etPassword.text.toString().trim()
                val newFullName = etFullName.text.toString().trim()

                if (validateUserInput(newLogin, newPassword, newFullName)) {
                    updateUser(user, newLogin, newPassword, newFullName)
                } else {
                    showMessage("Заполните все поля")
                }
            }
            .setNegativeButton("Отмена", null)
            .create()

        dialog.show()
    }

    private fun updateUser(oldUser: User, newLogin: String, newPassword: String, newFullName: String) {
        // Проверяем, не занят ли новый логин другим пользователем
        if (newLogin != oldUser.login && users.any { it.login == newLogin }) {
            showMessage("Пользователь с логином $newLogin уже существует")
            return
        }

        val updatedUser = oldUser.copy(
            login = newLogin,
            password = newPassword,
            fullName = newFullName
        )

        // Обновляем пользователя в списке
        val index = users.indexOfFirst { it.id == oldUser.id }
        if (index != -1) {
            users[index] = updatedUser
            filterUsers(spinnerRoleFilter.selectedItemPosition.let {
                when (it) {
                    0 -> null
                    1 -> UserRole.ADMIN
                    2 -> UserRole.LOGIST
                    3 -> UserRole.DRIVER
                    else -> null
                }
            })
            selectedUser = updatedUser
            showMessage("Данные пользователя обновлены")
        }
    }

    private fun showDeleteUserDialog(user: User) {
        AlertDialog.Builder(this)
            .setTitle("Удаление пользователя")
            .setMessage("Вы уверены, что хотите удалить пользователя ${user.fullName} (${user.login})?")
            .setPositiveButton("Удалить") { dialogInterface, _ ->
                deleteUser(user)
            }
            .setNegativeButton("Отмена", null)
            .create()
            .show()
    }

    private fun deleteUser(user: User) {
        // Не позволяем удалить самого себя
        val currentUser = intent.getSerializableExtra("USER_DATA") as? User
        if (currentUser?.id == user.id) {
            showMessage("Нельзя удалить самого себя")
            return
        }

        users.removeAll { it.id == user.id }
        selectedUser = null

        filterUsers(spinnerRoleFilter.selectedItemPosition.let {
            when (it) {
                0 -> null
                1 -> UserRole.ADMIN
                2 -> UserRole.LOGIST
                3 -> UserRole.DRIVER
                else -> null
            }
        })

        showMessage("Пользователь ${user.fullName} удален")
    }

}