package com.example.drivermobileapp.driver

import OrderDriver
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.models.DocumentItem
import java.text.SimpleDateFormat
import java.util.*

class DocumentsActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var tvTitle: TextView
    private lateinit var documentsListView: ListView
    private lateinit var tvEmpty: TextView

    private var currentOrder: OrderDriver? = null
    private val documentItems = mutableListOf<DocumentItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_documents)

        currentOrder = intent.getSerializableExtra("ORDER") as? OrderDriver

        initViews()
        setupClickListeners()
        loadDocuments()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.tvTitle)
        documentsListView = findViewById(R.id.documentsListView)
        tvEmpty = findViewById(R.id.tvEmpty)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        documentsListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val documentItem = documentItems[position]
            showDocumentDetails(documentItem)
        }
    }

    private fun loadDocuments() {
        currentOrder?.let { order ->
            val documentType = intent.getStringExtra("DOCUMENT_TYPE") ?: "terminal"
            val documents = order.stages.stage2.terminalDocuments

            tvTitle.text = when (documentType) {
                "terminal" -> "📄 Документы терминала (${documents.size})"
                else -> "📄 Документы (${documents.size})"
            }

            // Создаем тестовые данные для документов
            documentItems.clear()
            documents.forEachIndexed { index, docUrl ->
                val (docType, description) = when (documentType) {
                    "terminal" -> when {
                        docUrl.contains("waybill") -> "ТТН" to "Товарно-транспортная накладная"
                        docUrl.contains("acceptance") -> "Акт" to "Акт приема-передачи контейнера"
                        else -> "Документ" to "Сопроводительный документ"
                    }
                    else -> "Документ" to "Документ"
                }

                documentItems.add(
                    DocumentItem(
                        id = "doc_${documentType}_$index",
                        fileName = "document_${index + 1}.pdf",
                        documentType = docType,
                        description = description,
                        timestamp = System.currentTimeMillis() - (index * 600000L),
                        fileSize = "245 КБ",
                        documentUrl = docUrl
                    )
                )
            }

            if (documentItems.isEmpty()) {
                documentsListView.visibility = ListView.GONE
                tvEmpty.visibility = TextView.VISIBLE
                tvEmpty.text = "📄 Документы не загружены"
            } else {
                documentsListView.visibility = ListView.VISIBLE
                tvEmpty.visibility = TextView.GONE

                val adapter = DocumentAdapter(documentItems)
                documentsListView.adapter = adapter
            }
        }
    }

    private fun showDocumentDetails(documentItem: DocumentItem) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        val dateString = dateFormat.format(Date(documentItem.timestamp))

        val message = """
            📋 ${documentItem.description}
            
            Тип: ${documentItem.documentType}
            Файл: ${documentItem.fileName}
            Размер: ${documentItem.fileSize}
            Время загрузки: $dateString
            Статус: ✅ Загружено водителем
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Детали документа")
            .setMessage(message)
            .setPositiveButton("Просмотреть") { dialog, _ ->
                showDocumentViewer(documentItem)
                dialog.dismiss()
            }
            .setNeutralButton("Скачать") { dialog, _ ->
                showMessage("Загрузка документа ${documentItem.fileName}...")
                dialog.dismiss()
            }
            .setNegativeButton("Закрыть", null)
            .show()
    }

    private fun showDocumentViewer(documentItem: DocumentItem) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_document_view, null)
        val ivDocumentIcon = dialogView.findViewById<ImageView>(R.id.ivDocumentIcon)
        val tvDocumentDescription = dialogView.findViewById<TextView>(R.id.tvDocumentDescription)
        val tvDocumentInfo = dialogView.findViewById<TextView>(R.id.tvDocumentInfo)

        when {
            documentItem.fileName.endsWith(".pdf") ->
                ivDocumentIcon.setImageResource(android.R.drawable.ic_menu_agenda)
            else ->
                ivDocumentIcon.setImageResource(android.R.drawable.ic_menu_edit)
        }

        tvDocumentDescription.text = documentItem.description
        tvDocumentInfo.text = "Файл: ${documentItem.fileName}\nРазмер: ${documentItem.fileSize}"

        AlertDialog.Builder(this)
            .setTitle("Просмотр документа: ${documentItem.documentType}")
            .setView(dialogView)
            .setPositiveButton("Закрыть", null)
            .show()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private inner class DocumentAdapter(private val documents: List<DocumentItem>) : BaseAdapter() {
        override fun getCount(): Int = documents.size
        override fun getItem(position: Int): DocumentItem = documents[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(parent.context)
                .inflate(R.layout.item_document, parent, false)

            val documentItem = documents[position]
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

            val tvDocumentName = view.findViewById<TextView>(R.id.tvDocumentName)
            val tvDocumentType = view.findViewById<TextView>(R.id.tvDocumentType)
            val tvDocumentDescription = view.findViewById<TextView>(R.id.tvDocumentDescription)
            val tvDocumentTime = view.findViewById<TextView>(R.id.tvDocumentTime)
            val tvFileSize = view.findViewById<TextView>(R.id.tvFileSize)
            val ivDocumentIcon = view.findViewById<ImageView>(R.id.ivDocumentIcon)

            tvDocumentName.text = documentItem.fileName
            tvDocumentType.text = documentItem.documentType
            tvDocumentDescription.text = documentItem.description
            tvDocumentTime.text = dateFormat.format(Date(documentItem.timestamp))
            tvFileSize.text = documentItem.fileSize

            when {
                documentItem.fileName.endsWith(".pdf") -> {
                    ivDocumentIcon.setImageResource(android.R.drawable.ic_menu_agenda)
                    tvDocumentType.setBackgroundColor(0xFFE3F2FD.toInt())
                }
                else -> {
                    ivDocumentIcon.setImageResource(android.R.drawable.ic_menu_edit)
                    tvDocumentType.setBackgroundColor(0xFFFFF3E0.toInt())
                }
            }

            return view
        }
    }
}