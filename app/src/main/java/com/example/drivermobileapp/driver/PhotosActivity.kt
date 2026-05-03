package com.example.drivermobileapp.driver

import OrderDriver
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.drivermobileapp.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PhotosActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var btnAddPhoto: Button
    private lateinit var tvTitle: TextView
    private lateinit var photosListView: ListView
    private lateinit var tvEmpty: TextView

    private var currentOrder: OrderDriver? = null
    private var stageNumber: Int = 2
    private var photoListKey: String = "containerPhotos"
    private val photoPaths = mutableListOf<String>()

    private val REQUEST_CAMERA = 100
    private val REQUEST_GALLERY = 101
    private var currentPhotoPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photos)

        currentOrder = intent.getSerializableExtra("ORDER") as? OrderDriver
        stageNumber = intent.getIntExtra("STAGE_NUMBER", 2)
        photoListKey = intent.getStringExtra("PHOTO_LIST_KEY") ?: "containerPhotos"

        initViews()
        setupClickListeners()
        loadPhotos()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnAddPhoto = findViewById(R.id.btnAddPhoto)
        tvTitle = findViewById(R.id.tvTitle)
        photosListView = findViewById(R.id.photosListView)
        tvEmpty = findViewById(R.id.tvEmpty)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener { finish() }

        btnAddPhoto.setOnClickListener {
            showAddPhotoDialog()
        }

        photosListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            showDeleteDialog(position)
        }
    }

    private fun showAddPhotoDialog() {
        val options = arrayOf("📷 Сделать фото", "🖼️ Выбрать из галереи")
        AlertDialog.Builder(this)
            .setTitle("Добавить фото")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> dispatchTakePictureIntent()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    val photoURI = FileProvider.getUriForFile(
                        this,
                        "${packageName}.fileprovider",
                        it
                    )
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(intent, REQUEST_CAMERA)
                }
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CAMERA -> {
                    photoPaths.add(currentPhotoPath)
                    savePhotosToOrder()
                    Toast.makeText(this, "Фото сохранено", Toast.LENGTH_SHORT).show()
                    loadPhotos()
                }
                REQUEST_GALLERY -> {
                    data?.data?.let { uri ->
                        photoPaths.add(uri.toString())
                        savePhotosToOrder()
                        Toast.makeText(this, "Фото сохранено", Toast.LENGTH_SHORT).show()
                        loadPhotos()
                    }
                }
            }
        }
    }

    private fun savePhotosToOrder() {
        currentOrder?.let { order ->
            val updatedStage = when (stageNumber) {
                2 -> order.stages.stage2.copy(containerPhotos = photoPaths.toList())
                3 -> order.stages.stage3.copy(loadingPhotos = photoPaths.toList())
                5 -> order.stages.stage5.copy(destinationContainerPhotos = photoPaths.toList())
                6 -> order.stages.stage6.copy(unloadingPhotos = photoPaths.toList())
                else -> return
            }
            // В реальном приложении здесь нужно сохранить обновленный заказ
        }
    }

    private fun loadPhotos() {
        currentOrder?.let { order ->
            val photos = when (stageNumber) {
                2 -> order.stages.stage2.containerPhotos
                3 -> order.stages.stage3.loadingPhotos
                5 -> order.stages.stage5.destinationContainerPhotos
                6 -> order.stages.stage6.unloadingPhotos
                else -> emptyList()
            }

            photoPaths.clear()
            photoPaths.addAll(photos)

            val requiredCount = 2
            val titleText = when (stageNumber) {
                2 -> "📷 Фото контейнера"
                3 -> "📷 Фото погрузки"
                5 -> "📷 Фото станции"
                6 -> "📷 Фото выгрузки"
                else -> "📷 Фото"
            }
            tvTitle.text = "$titleText (${photoPaths.size} / $requiredCount)"

            if (photoPaths.isEmpty()) {
                photosListView.visibility = ListView.GONE
                tvEmpty.visibility = TextView.VISIBLE
                tvEmpty.text = "📷 Нет фото. Нажмите '+' чтобы добавить"
            } else {
                photosListView.visibility = ListView.VISIBLE
                tvEmpty.visibility = TextView.GONE

                val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, photoPaths)
                photosListView.adapter = adapter
            }
        }
    }

    private fun showDeleteDialog(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Удалить фото")
            .setMessage("Вы уверены?")
            .setPositiveButton("Удалить") { _, _ ->
                photoPaths.removeAt(position)
                savePhotosToOrder()
                loadPhotos()
                Toast.makeText(this, "Фото удалено", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}