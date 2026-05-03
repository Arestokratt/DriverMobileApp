package com.example.drivermobileapp.logist

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.models.Order1C
import java.text.SimpleDateFormat
import java.util.*

class DocumentsActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var tvTitle: TextView
    private lateinit var documentsListView: ListView
    private lateinit var tvEmpty: TextView

    private var currentOrder: Order1C? = null
    private val documentItems = mutableListOf<DocumentItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_documents)

        currentOrder = intent.getSerializableExtra("ORDER_DATA") as? Order1C
        val documentType = intent.getStringExtra("DOCUMENT_TYPE") ?: "terminal"

        initViews()
        setupClickListeners()
        loadDocuments(documentType)
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

    private fun loadDocuments(documentType: String) {
        currentOrder?.let { order ->
            val documents: List<String> = when (documentType) {
                "terminal" -> order.terminalDocuments
                "warehouse" -> order.warehouseDocuments
                "departure_station" -> order.departureStationDocuments
                "destination_station" -> order.destinationStationDocuments
                "cargo_issue" -> order.cargoIssueDocuments
                "container_return" -> order.containerReturnDocuments
                else -> emptyList()
            }

            val titleText = when (documentType) {
                "terminal" -> "📄 Документы терминала"
                "warehouse" -> "📄 Документы склада"
                "departure_station" -> "📄 Документы станции отправления"
                "destination_station" -> "📄 Документы станции назначения"
                "cargo_issue" -> "📄 Документы выдачи груза"
                "container_return" -> "📄 Документы сдачи контейнера"
                else -> "📄 Документы"
            }
            tvTitle.text = "$titleText (${documents.size})"

            documentItems.clear()
            for ((index, docUrl) in documents.withIndex()) {
                val description = when (documentType) {
                    "terminal" -> "Документ терминала вывоза"
                    "warehouse" -> "Документ склада"
                    "departure_station" -> "Документ станции отправления"
                    "destination_station" -> "Документ станции назначения"
                    "cargo_issue" -> "Документ выдачи груза"
                    "container_return" -> "Документ сдачи контейнера"
                    else -> "Документ"
                }

                documentItems.add(
                    DocumentItem(
                        id = "doc_${documentType}_$index",
                        fileName = "Документ_${index + 1}.pdf",
                        description = description,
                        timestamp = System.currentTimeMillis() - (index * 3600000L),
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
            📄 ${documentItem.description}
            
            Файл: ${documentItem.fileName}
            Время загрузки: $dateString
            Статус: ✅ Загружено
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Детали документа")
            .setMessage(message)
            .setPositiveButton("Просмотреть") { dialog, _ ->
                Toast.makeText(this, "Просмотр документа: ${documentItem.fileName}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Закрыть", null)
            .show()
    }

    // Внутренний класс DocumentItem для логиста
    data class DocumentItem(
        val id: String,
        val fileName: String,
        val description: String,
        val timestamp: Long,
        val documentUrl: String
    )

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
            val tvDocumentDescription = view.findViewById<TextView>(R.id.tvDocumentDescription)
            val tvDocumentTime = view.findViewById<TextView>(R.id.tvDocumentTime)
            val ivDocumentIcon = view.findViewById<ImageView>(R.id.ivDocumentIcon)

            tvDocumentName.text = documentItem.fileName
            tvDocumentDescription.text = documentItem.description
            tvDocumentTime.text = dateFormat.format(Date(documentItem.timestamp))

            ivDocumentIcon.setImageResource(android.R.drawable.ic_menu_edit)

            return view
        }
    }
}