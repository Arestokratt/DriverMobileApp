package com.example.drivermobileapp.driver

import OrderDriver
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.drivermobileapp.R

class DocumentsActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var btnAddDocument: Button
    private lateinit var tvTitle: TextView
    private lateinit var documentsListView: ListView
    private lateinit var tvEmpty: TextView

    private var currentOrder: OrderDriver? = null
    private var stageNumber: Int = 2
    private var documentListKey: String = "terminalDocuments"
    private val documentPaths = mutableListOf<String>()

    private val REQUEST_DOCUMENT = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_documents)

        currentOrder = intent.getSerializableExtra("ORDER") as? OrderDriver
        stageNumber = intent.getIntExtra("STAGE_NUMBER", 2)
        documentListKey = intent.getStringExtra("DOCUMENT_LIST_KEY") ?: "terminalDocuments"

        initViews()
        setupClickListeners()
        loadDocuments()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnAddDocument = findViewById(R.id.btnAddDocument)
        tvTitle = findViewById(R.id.tvTitle)
        documentsListView = findViewById(R.id.documentsListView)
        tvEmpty = findViewById(R.id.tvEmpty)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener { finish() }

        btnAddDocument.setOnClickListener {
            openFilePicker()
        }

        documentsListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            showDeleteDialog(position)
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf", "image/jpeg", "image/png", "image/jpg"))
        }
        startActivityForResult(intent, REQUEST_DOCUMENT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_DOCUMENT) {
            data?.data?.let { uri ->
                documentPaths.add(uri.toString())
                saveDocumentsToOrder()
                Toast.makeText(this, "Документ сохранен", Toast.LENGTH_SHORT).show()
                loadDocuments()
            }
        }
    }

    private fun saveDocumentsToOrder() {
        currentOrder?.let { order ->
            val updatedStage = when (stageNumber) {
                2 -> order.stages.stage2.copy(terminalDocuments = documentPaths.toList())
                3 -> order.stages.stage3.copy(warehouseDocuments = documentPaths.toList())
                5 -> order.stages.stage5.copy(destinationStationDocuments = documentPaths.toList())
                6 -> order.stages.stage6.copy(unloadingDocuments = documentPaths.toList())
                else -> return
            }
            // В реальном приложении здесь нужно сохранить обновленный заказ
        }
    }

    private fun loadDocuments() {
        currentOrder?.let { order ->
            val documents = when (stageNumber) {
                2 -> order.stages.stage2.terminalDocuments
                3 -> order.stages.stage3.warehouseDocuments
                5 -> order.stages.stage5.destinationStationDocuments
                6 -> order.stages.stage6.unloadingDocuments
                else -> emptyList()
            }

            documentPaths.clear()
            documentPaths.addAll(documents)

            val requiredCount = 2
            val titleText = when (stageNumber) {
                2 -> "📄 Документы терминала"
                3 -> "📄 Документы склада"
                5 -> "📄 Документы станции"
                6 -> "📄 Документы выдачи"
                else -> "📄 Документы"
            }
            tvTitle.text = "$titleText (${documentPaths.size} / $requiredCount)"

            if (documentPaths.isEmpty()) {
                documentsListView.visibility = ListView.GONE
                tvEmpty.visibility = TextView.VISIBLE
                tvEmpty.text = "📄 Нет документов. Нажмите '+' чтобы добавить"
            } else {
                documentsListView.visibility = ListView.VISIBLE
                tvEmpty.visibility = TextView.GONE

                val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, documentPaths)
                documentsListView.adapter = adapter
            }
        }
    }

    private fun showDeleteDialog(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Удалить документ")
            .setMessage("Вы уверены?")
            .setPositiveButton("Удалить") { _, _ ->
                documentPaths.removeAt(position)
                saveDocumentsToOrder()
                loadDocuments()
                Toast.makeText(this, "Документ удален", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}