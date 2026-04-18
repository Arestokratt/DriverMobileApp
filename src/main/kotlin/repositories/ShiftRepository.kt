package repositories

import config.DatabaseConfig
import models.responses.TransportCheckResponse
import models.responses.ActiveShiftResponse
import java.sql.Timestamp

class ShiftRepository {

    fun checkTransport(driverLicense: String, licensePlate: String): TransportCheckResponse {
        val query = """
            SELECT id, license_plate, driver_license, car_brand 
            FROM vehicles 
            WHERE license_plate = ? AND driver_license = ?
        """

        return DatabaseConfig.getConnection().use { conn ->
            conn.prepareStatement(query).use { stmt ->
                stmt.setString(1, licensePlate)
                stmt.setString(2, driverLicense)
                val rs = stmt.executeQuery()

                if (rs.next()) {
                    val vehicleId = rs.getInt("id")
                    val carBrand = rs.getString("car_brand")

                    // Проверяем, не в смене ли транспорт
                    if (isVehicleInActiveShift(licensePlate)) {
                        TransportCheckResponse(
                            isValid = false,
                            message = "Этот автомобиль уже в смене",
                            vehicleId = vehicleId,
                            carBrand = carBrand
                        )
                    } else {
                        TransportCheckResponse(
                            isValid = true,
                            message = "Транспорт найден и свободен",
                            vehicleId = vehicleId,
                            carBrand = carBrand
                        )
                    }
                } else {
                    TransportCheckResponse(
                        isValid = false,
                        message = "Транспорт не найден или ВУ не соответствует"
                    )
                }
            }
        }
    }

    private fun isVehicleInActiveShift(licensePlate: String): Boolean {
        val query = "SELECT id FROM shifts WHERE license_plate = ? AND status = 'active'"

        return DatabaseConfig.getConnection().use { conn ->
            conn.prepareStatement(query).use { stmt ->
                stmt.setString(1, licensePlate)
                val rs = stmt.executeQuery()
                rs.next()
            }
        }
    }

    fun hasActiveShift(userId: Int): Boolean {
        val query = "SELECT id FROM shifts WHERE user_id = ? AND status = 'active'"

        return DatabaseConfig.getConnection().use { conn ->
            conn.prepareStatement(query).use { stmt ->
                stmt.setInt(1, userId)
                val rs = stmt.executeQuery()
                rs.next()
            }
        }
    }

    fun startShift(userId: Int, driverLicense: String, licensePlate: String, startTime: Long): Int {
        val query = """
            INSERT INTO shifts (user_id, driver_license, license_plate, start_time, status)
            VALUES (?, ?, ?, ?, 'active')
            RETURNING id
        """

        return DatabaseConfig.getConnection().use { conn ->
            conn.prepareStatement(query).use { stmt ->
                stmt.setInt(1, userId)
                stmt.setString(2, driverLicense)
                stmt.setString(3, licensePlate)
                stmt.setTimestamp(4, Timestamp(startTime))
                val rs = stmt.executeQuery()

                if (rs.next()) rs.getInt("id") else -1
            }
        }
    }

    fun endShift(shiftId: Int, endTime: Long): Boolean {
        val query = """
            UPDATE shifts 
            SET end_time = ?, status = 'completed'
            WHERE id = ? AND status = 'active'
        """

        return DatabaseConfig.getConnection().use { conn ->
            conn.prepareStatement(query).use { stmt ->
                stmt.setTimestamp(1, Timestamp(endTime))
                stmt.setInt(2, shiftId)
                val rowsUpdated = stmt.executeUpdate()
                rowsUpdated > 0
            }
        }
    }

    fun getActiveShiftByUserId(userId: Int): ActiveShiftResponse? {
        val query = """
            SELECT s.id, s.driver_license, s.license_plate, s.start_time, v.car_brand
            FROM shifts s
            LEFT JOIN vehicles v ON s.license_plate = v.license_plate
            WHERE s.user_id = ? AND s.status = 'active'
        """

        return DatabaseConfig.getConnection().use { conn ->
            conn.prepareStatement(query).use { stmt ->
                stmt.setInt(1, userId)
                val rs = stmt.executeQuery()

                if (rs.next()) {
                    ActiveShiftResponse(
                        shiftId = rs.getInt("id"),
                        driverLicense = rs.getString("driver_license"),
                        licensePlate = rs.getString("license_plate"),
                        startTime = rs.getTimestamp("start_time").time,
                        carBrand = rs.getString("car_brand")
                    )
                } else null
            }
        }
    }
}