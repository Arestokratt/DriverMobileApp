package repositories

import config.DatabaseConfig
import java.sql.Timestamp
import models.entities.Order
import models.entities.OrderStage
import models.requests.CreateOrderRequest

class OrderRepository {

    // Создать новую заявку (триггер сам создаст этапы)
    fun createOrder(request: CreateOrderRequest): Order? {
        val query = """
            INSERT INTO orders (
                order_number, empty_container_terminal_address, container_type,
                container_count, container_delivery_date_time, container_loading_address,
                cargo_name, cargo_weight, loading_contact_person, loading_contact_phone,
                departure_station_name, departure_station_contact, departure_station_phone,
                destination_station_name, destination_station_contact, destination_station_phone,
                unloading_address, unloading_contact_person, unloading_contact_phone,
                return_terminal_address, assigned_driver_id, assigned_driver_name, notes
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING *
        """.trimIndent()

        return try {
            DatabaseConfig.getConnection().use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, request.orderNumber)
                    stmt.setString(2, request.emptyContainerTerminalAddress)
                    stmt.setString(3, request.containerType)
                    stmt.setInt(4, request.containerCount)
                    stmt.setTimestamp(5, Timestamp(request.containerDeliveryDateTime))
                    stmt.setString(6, request.containerLoadingAddress)
                    stmt.setString(7, request.cargoName)
                    stmt.setDouble(8, request.cargoWeight)
                    stmt.setString(9, request.loadingContactPerson)
                    stmt.setString(10, request.loadingContactPhone)
                    stmt.setString(11, request.departureStationName)
                    stmt.setString(12, request.departureStationContact)
                    stmt.setString(13, request.departureStationPhone)
                    stmt.setString(14, request.destinationStationName)
                    stmt.setString(15, request.destinationStationContact)
                    stmt.setString(16, request.destinationStationPhone)
                    stmt.setString(17, request.unloadingAddress)
                    stmt.setString(18, request.unloadingContactPerson)
                    stmt.setString(19, request.unloadingContactPhone)
                    stmt.setString(20, request.returnTerminalAddress)
                    stmt.setString(21, request.assignedDriverId)
                    stmt.setString(22, request.assignedDriverName)
                    stmt.setString(23, request.notes)

                    val rs = stmt.executeQuery()
                    if (rs.next()) {
                        mapToOrder(rs)
                    } else null
                }
            }
        } catch (e: Exception) {
            println("Error in createOrder: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    // Получить заявку по номеру
    fun getOrderByNumber(orderNumber: String): Order? {
        val query = "SELECT * FROM orders WHERE order_number = ?"

        return try {
            DatabaseConfig.getConnection().use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, orderNumber)
                    val rs = stmt.executeQuery()
                    if (rs.next()) mapToOrder(rs) else null
                }
            }
        } catch (e: Exception) {
            println("Error in getOrderByNumber: ${e.message}")
            null
        }
    }

    // Получить статусы всех этапов заявки
    fun getOrderStages(orderNumber: String): List<OrderStage> {
        val query = """
            SELECT os.* FROM order_stages os
            JOIN orders o ON o.id = os.order_id
            WHERE o.order_number = ?
            ORDER BY os.stage_number
        """.trimIndent()

        return try {
            DatabaseConfig.getConnection().use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, orderNumber)
                    val rs = stmt.executeQuery()
                    val stages = mutableListOf<OrderStage>()
                    while (rs.next()) {
                        stages.add(mapToOrderStage(rs))
                    }
                    stages
                }
            }
        } catch (e: Exception) {
            println("Error in getOrderStages: ${e.message}")
            emptyList()
        }
    }

    // Завершить этап
    fun completeStage(
        orderNumber: String,
        stageNumber: Int,
        driverId: String,
        driverNotes: String?,
        arrivalTime: Long?,
        departureTime: Long?,
        gpsLatitude: Double?,
        gpsLongitude: Double?,
        photoUrl: String?
    ): Boolean {
        val updateStageQuery = """
            UPDATE order_stages
            SET status = 'COMPLETED',
                completed_at = NOW(),
                driver_notes = ?,
                arrival_time = ?,
                departure_time = ?,
                gps_latitude = ?,
                gps_longitude = ?,
                photo_url = ?
            WHERE order_id = (SELECT id FROM orders WHERE order_number = ?)
              AND stage_number = ?
              AND status != 'COMPLETED'
        """.trimIndent()

        return try {
            DatabaseConfig.getConnection().use { conn ->
                conn.prepareStatement(updateStageQuery).use { stmt ->
                    stmt.setString(1, driverNotes)
                    stmt.setTimestamp(2, arrivalTime?.let { Timestamp(it) })
                    stmt.setTimestamp(3, departureTime?.let { Timestamp(it) })
                    stmt.setDouble(4, gpsLatitude ?: 0.0)
                    stmt.setDouble(5, gpsLongitude ?: 0.0)
                    stmt.setString(6, photoUrl)
                    stmt.setString(7, orderNumber)
                    stmt.setInt(8, stageNumber)

                    stmt.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            println("Error in completeStage: ${e.message}")
            false
        }
    }

    // Получить текущий активный этап
    fun getCurrentActiveStage(orderNumber: String): Int {
        val query = """
        SELECT stage_number, status FROM order_stages
        WHERE order_id = (SELECT id FROM orders WHERE order_number = ?)
        ORDER BY stage_number
    """.trimIndent()

        return try {
            DatabaseConfig.getConnection().use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, orderNumber)
                    val rs = stmt.executeQuery()
                    while (rs.next()) {
                        val status = rs.getString("status")
                        // Первый этап, который НЕ ЗАВЕРШЕН
                        if (status != "COMPLETED") {
                            val stageNumber = rs.getInt("stage_number")
                            println("DEBUG: getCurrentActiveStage = $stageNumber (status=$status)")
                            return stageNumber
                        }
                    }
                    // Если все этапы завершены, возвращаем 7
                    println("DEBUG: getCurrentActiveStage = 7 (all completed)")
                    7
                }
            }
        } catch (e: Exception) {
            println("Error in getCurrentActiveStage: ${e.message}")
            1
        }
    }

    // Получить заявки водителя
    fun getDriverOrders(driverId: String): List<Order> {
        val query = "SELECT * FROM orders WHERE assigned_driver_id = ? ORDER BY order_date DESC"

        return try {
            DatabaseConfig.getConnection().use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, driverId)
                    val rs = stmt.executeQuery()
                    val orders = mutableListOf<Order>()
                    while (rs.next()) {
                        orders.add(mapToOrder(rs))
                    }
                    orders
                }
            }
        } catch (e: Exception) {
            println("Error in getDriverOrders: ${e.message}")
            emptyList()
        }
    }

    // Вспомогательные функции маппинга
    private fun mapToOrder(rs: java.sql.ResultSet): Order {
        return Order(
            id = rs.getLong("id"),
            orderNumber = rs.getString("order_number"),
            orderDate = rs.getTimestamp("order_date"),
            emptyContainerTerminalAddress = rs.getString("empty_container_terminal_address"),
            containerType = rs.getString("container_type"),
            containerCount = rs.getInt("container_count"),
            containerDeliveryDateTime = rs.getTimestamp("container_delivery_date_time")?.time ?: 0L,
            containerLoadingAddress = rs.getString("container_loading_address"),
            cargoName = rs.getString("cargo_name"),
            cargoWeight = rs.getDouble("cargo_weight"),
            loadingContactPerson = rs.getString("loading_contact_person"),
            loadingContactPhone = rs.getString("loading_contact_phone"),
            departureStationName = rs.getString("departure_station_name"),
            departureStationContact = rs.getString("departure_station_contact"),
            departureStationPhone = rs.getString("departure_station_phone"),
            destinationStationName = rs.getString("destination_station_name"),
            destinationStationContact = rs.getString("destination_station_contact"),
            destinationStationPhone = rs.getString("destination_station_phone"),
            unloadingAddress = rs.getString("unloading_address"),
            unloadingContactPerson = rs.getString("unloading_contact_person"),
            unloadingContactPhone = rs.getString("unloading_contact_phone"),
            returnTerminalAddress = rs.getString("return_terminal_address"),
            assignedDriverId = rs.getString("assigned_driver_id"),
            assignedDriverName = rs.getString("assigned_driver_name"),
            status = rs.getString("status"),
            notes = rs.getString("notes"),
            createdAt = rs.getTimestamp("created_at"),
            updatedAt = rs.getTimestamp("updated_at")
        )
    }

    private fun mapToOrderStage(rs: java.sql.ResultSet): OrderStage {
        return OrderStage(
            id = rs.getLong("id"),
            orderId = rs.getLong("order_id"),
            stageNumber = rs.getInt("stage_number"),
            status = rs.getString("status"),
            startedAt = rs.getTimestamp("started_at"),
            completedAt = rs.getTimestamp("completed_at"),
            driverNotes = rs.getString("driver_notes"),
            arrivalTime = rs.getTimestamp("arrival_time"),
            departureTime = rs.getTimestamp("departure_time"),
            gpsLatitude = rs.getDouble("gps_latitude"),
            gpsLongitude = rs.getDouble("gps_longitude"),
            photoUrl = rs.getString("photo_url")
        )
    }

    // Получить входящие заявки (статус PENDING)
    fun getIncomingOrders(driverId: String): List<Order> {
        println("DEBUG SERVER: getIncomingOrders called with driverId = '$driverId'")

        val query = "SELECT * FROM orders WHERE assigned_driver_id = ? AND status = 'PENDING'"

        return try {
            DatabaseConfig.getConnection().use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, driverId)
                    val rs = stmt.executeQuery()
                    val orders = mutableListOf<Order>()
                    while (rs.next()) {
                        orders.add(mapToOrder(rs))
                    }
                    println("DEBUG SERVER: found ${orders.size} orders")
                    orders
                }
            }
        } catch (e: Exception) {
            println("DEBUG SERVER: error = ${e.message}")
            emptyList()
        }
    }

    // Получить мои заявки (статус ACCEPTED или IN_PROGRESS)
    fun getMyOrders(driverId: String): List<Order> {
        val query = """
        SELECT * FROM orders 
        WHERE assigned_driver_id = ? 
        AND status IN ('ACCEPTED', 'IN_PROGRESS')
        ORDER BY order_date DESC
    """.trimIndent()

        return try {
            DatabaseConfig.getConnection().use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, driverId)
                    val rs = stmt.executeQuery()
                    val orders = mutableListOf<Order>()
                    while (rs.next()) {
                        orders.add(mapToOrder(rs))
                    }
                    orders
                }
            }
        } catch (e: Exception) {
            println("DEBUG: getMyOrders error = ${e.message}")
            emptyList()
        }
    }

    // Принять заявку
    fun acceptOrder(orderNumber: String, driverId: String): Boolean {
        val query = """
        UPDATE orders 
        SET status = 'ACCEPTED', 
            assigned_driver_id = ?,
            updated_at = NOW()
        WHERE order_number = ? AND status = 'PENDING'
    """.trimIndent()

        return try {
            DatabaseConfig.getConnection().use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, driverId)  // driverId = "9393fa81-8fae-4ec9-b763-b7d054f637b7"
                    stmt.setString(2, orderNumber)
                    val rowsUpdated = stmt.executeUpdate()
                    println("DEBUG: acceptOrder - rows updated = $rowsUpdated")
                    rowsUpdated > 0
                }
            }
        } catch (e: Exception) {
            println("DEBUG: acceptOrder error = ${e.message}")
            false
        }
    }

    // Отклонить заявку
    fun rejectOrder(orderNumber: String): Boolean {
        val query = "UPDATE orders SET status = 'REJECTED' WHERE order_number = ? AND status = 'PENDING'"

        return try {
            DatabaseConfig.getConnection().use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, orderNumber)
                    stmt.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            println("Error in rejectOrder: ${e.message}")
            false
        }
    }
}