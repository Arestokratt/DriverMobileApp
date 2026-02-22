package com.example.drivermobileapp.logist

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.models.Driver
import com.example.drivermobileapp.data.models.User

class ChatActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var tvChatTitle: TextView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var messagesContainer: LinearLayout
    private lateinit var scrollView: ScrollView

    private var currentDriver: Driver? = null
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        currentDriver = intent.getSerializableExtra("DRIVER_DATA") as? Driver
        currentUser = intent.getSerializableExtra("USER_DATA") as? User

        initViews()
        setupClickListeners()
        displayChatInfo()

        // Добавляем приветственное сообщение
        addMessage("Чат с водителем. Функционал в разработке.", false)
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvChatTitle = findViewById(R.id.tvChatTitle)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        messagesContainer = findViewById(R.id.messagesContainer)
        scrollView = findViewById(R.id.scrollView)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish() // Возврат к карточке водителя
        }

        btnSend.setOnClickListener {
            val message = etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                addMessage(message, true) // Сообщение от логиста
                etMessage.text.clear()

                // Имитация ответа водителя
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    addMessage("Сообщение получено. Чат находится в разработке.", false)
                }, 1000)
            }
        }
    }

    private fun displayChatInfo() {
        currentDriver?.let { driver ->
            tvChatTitle.text = "Чат с ${driver.fullName}"
        }
    }

    private fun addMessage(text: String, isFromUser: Boolean) {
        val messageLayout = if (isFromUser) {
            R.layout.item_message_sent
        } else {
            R.layout.item_message_received
        }

        val messageView = layoutInflater.inflate(messageLayout, null)
        val tvMessage = messageView.findViewById<TextView>(R.id.tvMessage)
        tvMessage.text = text

        messagesContainer.addView(messageView)

        // Прокрутка к последнему сообщению
        scrollView.post {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
}