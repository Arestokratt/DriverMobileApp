package com.example.drivermobileapp.data.models

import java.io.Serializable

data class SearchParams(
    val query: String = "",
    val pageSize: Int = 10,
    val sortBy: SortBy = SortBy.DATE_DESC,
    val orderStatus: OrderStatus? = null
) : Serializable

enum class SortBy {
    DATE_DESC,    // по дате добавления (новые сначала)
    DATE_ASC,     // по дате добавления (старые сначала)
    ORDER_NUMBER, // по номеру заявки
    CLIENT_NAME   // по грузоотправителю
}