package com.example.drivermobileapp.driver

import OrderDriver
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.drivermobileapp.R
//import com.example.drivermobileapp.data.models.OrderDriver

class   OrderAdapter(
    private var orders: List<OrderDriver>,
    private val onOrderClick: (OrderDriver) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderNumber: TextView = itemView.findViewById(R.id.tvOrderNumber)
        val tvCustomerName: TextView = itemView.findViewById(R.id.tvCustomerName)
        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    // В методе onBindViewHolder добавляем цвет статуса
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        holder.tvOrderNumber.text = "Заявка №${order.number}"
        holder.tvCustomerName.text = order.customerName
        holder.tvAddress.text = order.address

        val statusText = when (order.status) {
            OrderDriver.OrderStatus.NEW -> "Новая"
            OrderDriver.OrderStatus.ACCEPTED -> "Принята"
            OrderDriver.OrderStatus.IN_PROGRESS -> "В процессе"
            OrderDriver.OrderStatus.COMPLETED -> "Завершена"
            OrderDriver.OrderStatus.CANCELLED -> "Отменена"
        }

        holder.tvStatus.text = statusText

        // Цвет статуса
        val statusColor = when (order.status) {
            OrderDriver.OrderStatus.NEW -> "#FF9800" // Оранжевый
            OrderDriver.OrderStatus.ACCEPTED -> "#4CAF50" // Зеленый
            OrderDriver.OrderStatus.IN_PROGRESS -> "#2196F3" // Синий
            OrderDriver.OrderStatus.COMPLETED -> "#9E9E9E" // Серый
            OrderDriver.OrderStatus.CANCELLED -> "#F44336" // Красный
        }
        holder.tvStatus.setTextColor(Color.parseColor(statusColor))

        holder.itemView.setOnClickListener {
            onOrderClick(order)
        }
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<OrderDriver>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}