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
import com.example.drivermobileapp.data.models.PhotoItem
import java.text.SimpleDateFormat
import java.util.*

class PhotosActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var tvTitle: TextView
    private lateinit var photosListView: ListView
    private lateinit var tvEmpty: TextView

    private var currentOrder: OrderDriver? = null
    private val photoItems = mutableListOf<PhotoItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photos)

        currentOrder = intent.getSerializableExtra("ORDER") as? OrderDriver
        val photoType = intent.getStringExtra("PHOTO_TYPE") ?: "terminal"

        initViews()
        setupClickListeners()
        loadPhotos(photoType)
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.tvTitle)
        photosListView = findViewById(R.id.photosListView)
        tvEmpty = findViewById(R.id.tvEmpty)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        photosListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val photoItem = photoItems[position]
            showPhotoDetails(photoItem)
        }
    }

    private fun loadPhotos(photoType: String) {
        currentOrder?.let { order ->
            // Получаем фото из этапа 2 (терминал)
            val photos = order.stages.stage2.containerPhotos

            tvTitle.text = when (photoType) {
                "terminal" -> "📷 Фото контейнера (${photos.size})"
                else -> "📷 Фото (${photos.size})"
            }

            // Создаем тестовые данные для фото
            photoItems.clear()
            photos.forEachIndexed { index, photoUrl ->
                val description = when (photoType) {
                    "terminal" -> when {
                        photoUrl.contains("front") -> "Передняя часть контейнера"
                        photoUrl.contains("back") -> "Задняя часть контейнера"
                        photoUrl.contains("seal") -> "Пломба контейнера"
                        else -> "Фото контейнера"
                    }
                    else -> "Фото"
                }

                photoItems.add(
                    PhotoItem(
                        id = "photo_${photoType}_$index",
                        fileName = "photo_${index + 1}.jpg",
                        description = description,
                        timestamp = System.currentTimeMillis() - (index * 600000L),
                        photoUrl = photoUrl
                    )
                )
            }

            if (photoItems.isEmpty()) {
                photosListView.visibility = ListView.GONE
                tvEmpty.visibility = TextView.VISIBLE
                tvEmpty.text = "📷 Фотографии не загружены"
            } else {
                photosListView.visibility = ListView.VISIBLE
                tvEmpty.visibility = TextView.GONE

                val adapter = PhotoAdapter(photoItems)
                photosListView.adapter = adapter
            }
        }
    }

    private fun showPhotoDetails(photoItem: PhotoItem) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        val dateString = dateFormat.format(Date(photoItem.timestamp))

        val message = """
            📸 ${photoItem.description}
            
            Файл: ${photoItem.fileName}
            Время загрузки: $dateString
            Статус: ✅ Загружено водителем
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Детали фотографии")
            .setMessage(message)
            .setPositiveButton("Просмотреть") { dialog, _ ->
                showPhotoViewer(photoItem)
                dialog.dismiss()
            }
            .setNegativeButton("Закрыть", null)
            .show()
    }

    private fun showPhotoViewer(photoItem: PhotoItem) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_photo_view, null)
        val imageView = dialogView.findViewById<ImageView>(R.id.imageView)
        val tvPhotoDescription = dialogView.findViewById<TextView>(R.id.tvPhotoDescription)

        // TODO: Заменить на реальную загрузку фото
        imageView.setImageResource(android.R.drawable.ic_menu_camera)
        tvPhotoDescription.text = photoItem.description

        AlertDialog.Builder(this)
            .setTitle("Просмотр фото: ${photoItem.fileName}")
            .setView(dialogView)
            .setPositiveButton("Закрыть", null)
            .show()
    }

    private inner class PhotoAdapter(private val photos: List<PhotoItem>) : BaseAdapter() {
        override fun getCount(): Int = photos.size
        override fun getItem(position: Int): PhotoItem = photos[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(parent.context)
                .inflate(R.layout.item_photo, parent, false)

            val photoItem = photos[position]
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

            val tvPhotoName = view.findViewById<TextView>(R.id.tvPhotoName)
            val tvPhotoDescription = view.findViewById<TextView>(R.id.tvPhotoDescription)
            val tvPhotoTime = view.findViewById<TextView>(R.id.tvPhotoTime)
            val ivPhotoIcon = view.findViewById<ImageView>(R.id.ivPhotoIcon)

            tvPhotoName.text = photoItem.fileName
            tvPhotoDescription.text = photoItem.description
            tvPhotoTime.text = dateFormat.format(Date(photoItem.timestamp))

            when {
                photoItem.description.contains("пломба") ->
                    ivPhotoIcon.setImageResource(android.R.drawable.ic_lock_lock)
                else ->
                    ivPhotoIcon.setImageResource(android.R.drawable.ic_menu_camera)
            }

            return view
        }
    }
}