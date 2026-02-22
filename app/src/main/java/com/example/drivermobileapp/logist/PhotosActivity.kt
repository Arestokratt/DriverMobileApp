package com.example.drivermobileapp.logist

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.drivermobileapp.R
import com.example.drivermobileapp.data.models.Order1C
import com.example.drivermobileapp.data.models.PhotoItem
import com.example.drivermobileapp.data.models.User
import java.text.SimpleDateFormat
import java.util.*

class PhotosActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var tvTitle: TextView
    private lateinit var photosListView: ListView
    private lateinit var tvEmpty: TextView

    private var currentOrder: Order1C? = null
    private val photoItems = mutableListOf<PhotoItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photos)

        currentOrder = intent.getSerializableExtra("ORDER_DATA") as? Order1C
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

        // Обработчик клика по фото в списке
        photosListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val photoItem = photoItems[position]
            showPhotoDetails(photoItem)
        }
    }

    private fun loadPhotos(photoType: String) {
        currentOrder?.let { order ->
            val photos = when (photoType) {
                "terminal" -> order.terminalPhotos
                "warehouse" -> order.warehousePhotos
                else -> order.terminalPhotos
            }

            tvTitle.text = when (photoType) {
                "terminal" -> "📷 Фото контейнера (${photos.size})"
                "warehouse" -> "📷 Фото погрузки (${photos.size})"
                else -> "📷 Фото (${photos.size})"
            }

            // Создаем тестовые данные для фото
            photoItems.clear()
            photos.forEachIndexed { index, photoName ->
                val description = when (photoType) {
                    "terminal" -> when (photoName) {
                        "container_front.jpg" -> "Передняя часть контейнера"
                        "container_back.jpg" -> "Задняя часть контейнера"
                        "container_seal.jpg" -> "Пломба контейнера"
                        else -> "Фото контейнера"
                    }
                    "warehouse" -> when (photoName) {
                        "loading_1.jpg" -> "Начало погрузки"
                        "loading_2.jpg" -> "Процесс погрузки"
                        "loading_3.jpg" -> "Завершение погрузки"
                        "cargo_inside.jpg" -> "Груз внутри контейнера"
                        else -> "Фото погрузки"
                    }
                    else -> "Фото"
                }

                photoItems.add(
                    PhotoItem(
                        id = "photo_${photoType}_$index",
                        fileName = photoName,
                        description = description,
                        timestamp = System.currentTimeMillis() - (index * 600000L),
                        photoUrl = "https://example.com/photos/$photoType/$photoName"
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

                // Используем кастомный адаптер для красивого отображения
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
            
            ${if (photoItem.photoUrl.isNotEmpty()) "URL: ${photoItem.photoUrl}" else "Фото доступно для просмотра"}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Детали фотографии")
            .setMessage(message)
            .setPositiveButton("Просмотреть") { dialog, _ ->
                // Здесь можно добавить реальный просмотр фото
                showPhotoViewer(photoItem)
                dialog.dismiss()
            }
            .setNegativeButton("Закрыть", null)
            .show()
    }

    private fun showPhotoViewer(photoItem: PhotoItem) {
        // Заглушка для просмотрщика фото
        // В реальном приложении здесь будет ImageView с загрузкой фото

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_photo_view, null)
        val imageView = dialogView.findViewById<ImageView>(R.id.imageView)
        val tvPhotoDescription = dialogView.findViewById<TextView>(R.id.tvPhotoDescription)

        // Устанавливаем заглушку изображения
        imageView.setImageResource(R.drawable.ic_photo_placeholder)
        tvPhotoDescription.text = photoItem.description

        AlertDialog.Builder(this)
            .setTitle("Просмотр фото: ${photoItem.fileName}")
            .setView(dialogView)
            .setPositiveButton("Закрыть", null)
            .show()
    }

    // Кастомный адаптер для красивого отображения фото
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

            // Устанавливаем иконку в зависимости от типа фото
            when {
                photoItem.fileName.contains("seal") ->
                    ivPhotoIcon.setImageResource(android.R.drawable.ic_lock_lock)
                photoItem.fileName.contains("front") || photoItem.fileName.contains("back") ->
                    ivPhotoIcon.setImageResource(android.R.drawable.ic_menu_camera)
                else ->
                    ivPhotoIcon.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            return view
        }
    }
}