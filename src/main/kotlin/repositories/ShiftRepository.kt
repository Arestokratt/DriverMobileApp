package repositories

import config.DatabaseConfig
import java.sql.Timestamp
import models.entities.Driver
import models.entities.Vehicle
import models.responses.ActiveShiftResponse
import models.responses.TransportCheckResponse

class ShiftRepository {

    fun checkTransport(driverLicense: String, licensePlate: String): TransportCheckResponse {
        println("🔍 [checkTransport] START")
        println("   driverLicense: '$driverLicense'")
        println("   licensePlate: '$licensePlate'")

        try {
            val normalizedDriverLicense = driverLicense.trim().uppercase()
            val normalizedLicensePlate = licensePlate.trim().uppercase()
            println("   Normalized driverLicense: '$normalizedDriverLicense'")
            println("   Normalized licensePlate: '$normalizedLicensePlate'")

            println("   Getting vehicle by license plate...")
            val vehicle = getVehicleByLicensePlate(normalizedLicensePlate)
            if (vehicle == null) {
                println("   ❌ Vehicle not found")
                return TransportCheckResponse(
                    isValid = false,
                    message = "Автомобиль с гос. номером '$normalizedLicensePlate' не найден в автопарке"
                )
            }
            println("   ✅ Vehicle found: id=${vehicle.id}, brand=${vehicle.carBrand}")

            println("   Getting driver by license...")
            val driver = getDriverByLicense(normalizedDriverLicense)
            if (driver == null) {
                println("   ❌ Driver not found")
                return TransportCheckResponse(
                    isValid = false,
                    message = "Водитель с удостоверением '$normalizedDriverLicense' не найден"
                )
            }
            println("   ✅ Driver found: id=${driver.id}, name=${driver.fullName}")

            println("   Checking if vehicle in active shift...")
            if (isVehicleInActiveShift(normalizedLicensePlate)) {
                println("   ❌ Vehicle already in active shift")
                return TransportCheckResponse(
                    isValid = false,
                    message = "Этот автомобиль уже в смене",
                    vehicleId = vehicle.id,
                    carBrand = vehicle.carBrand
                )
            }
            println("   ✅ Vehicle is free")

            println("   Checking if driver has active shift...")
            if (hasActiveShiftByDriverLicense(normalizedDriverLicense)) {
                println("   ❌ Driver already has active shift")
                return TransportCheckResponse(
                    isValid = false,
                    message = "Этот водитель уже в смене",
                    vehicleId = vehicle.id,
                    carBrand = vehicle.carBrand
                )
            }
            println("   ✅ Driver is free")

            println("✅ [checkTransport] SUCCESS - all checks passed")
            return TransportCheckResponse(
                isValid = true,
                message = "Проверка пройдена. Водитель и автомобиль найдены",
                vehicleId = vehicle.id,
                carBrand = vehicle.carBrand
            )
        } catch (e: Exception) {
            println("❌ [checkTransport] EXCEPTION: ${e.javaClass.simpleName}")
            println("   Message: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    private fun getVehicleByLicensePlate(licensePlate: String): Vehicle? {
        println("      [getVehicleByLicensePlate] Querying for: $licensePlate")
        val query = """
        SELECT id, "licensePlate", "carBrand"
        FROM vehicles
        WHERE UPPER("licensePlate") = UPPER(?)
    """.trimIndent()

        return try {
            DatabaseConfig.getConnection().use { conn ->
                println("      [getVehicleByLicensePlate] Connection obtained")
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, licensePlate)
                    println("      [getVehicleByLicensePlate] Executing query...")
                    val rs = stmt.executeQuery()

                    if (rs.next()) {
                        // Используйте getString, который может вернуть null, и обработайте это
                        val id = rs.getString("id")
                        val licensePlateValue = rs.getString("licensePlate")
                        val carBrand = rs.getString("carBrand")

                        println("      Raw values - id: $id, licensePlate: $licensePlateValue, carBrand: $carBrand")

                        // Проверьте, что id не null (он должен быть, если это первичный ключ)
                        if (id == null) {
                            println("      ❌ id is null, skipping vehicle")
                            return@use null
                        }

                        val vehicle = Vehicle(
                            id = id,  // Теперь id может быть null? Нет, мы проверили
                            licensePlate = licensePlateValue ?: "",  // Если null, используем пустую строку
                            carBrand = carBrand ?: ""  // Если null, используем пустую строку
                        )
                        println("      [getVehicleByLicensePlate] Found: $vehicle")
                        vehicle
                    } else {
                        println("      [getVehicleByLicensePlate] No vehicle found")
                        null
                    }
                }
            }
        } catch (e: Exception) {
            println("      ❌ [getVehicleByLicensePlate] ERROR: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    private fun getDriverByLicense(driverLicense: String): Driver? {
        val query = """
        SELECT id, "driverLicense", "user_login" as fullName
        FROM drivers
        WHERE UPPER("driverLicense") = UPPER(?)
    """.trimIndent()

        return DatabaseConfig.getConnection().use { conn ->
            conn.prepareStatement(query).use { stmt ->
                stmt.setString(1, driverLicense)
                val rs = stmt.executeQuery()

                if (rs.next()) {
                    val id = rs.getString("id")
                    if (id.isNullOrBlank()) {
                        println("Driver found but id is null/blank")
                        return@use null
                    }

                    Driver(
                        id = id,
                        driverLicense = rs.getString("driverLicense") ?: "",
                        fullName = rs.getString("fullName") ?: ""
                    )
                } else {
                    null
                }
            }
        }
    }

    private fun isVehicleInActiveShift(licensePlate: String): Boolean {
        println("      [isVehicleInActiveShift] Checking: $licensePlate")
        val query = """
        SELECT id FROM shifts 
        WHERE UPPER("licensePlate") = UPPER(?) AND status = 'active'
    """.trimIndent()  // ← ДОБАВЬТЕ КАВЫЧКИ!

        return try {
            DatabaseConfig.getConnection().use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, licensePlate)
                    val result = stmt.executeQuery().next()
                    println("      [isVehicleInActiveShift] Result: $result")
                    result
                }
            }
        } catch (e: Exception) {
            println("      ❌ [isVehicleInActiveShift] ERROR: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    fun hasActiveShiftByDriverLicense(driverLicense: String): Boolean {
        println("      [hasActiveShiftByDriverLicense] Checking: $driverLicense")
        val query = """
        SELECT id FROM shifts 
        WHERE UPPER("driverLicense") = UPPER(?) AND status = 'active'
    """.trimIndent()  // ← ДОБАВЬТЕ КАВЫЧКИ!

        return try {
            DatabaseConfig.getConnection().use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, driverLicense)
                    val result = stmt.executeQuery().next()
                    println("      [hasActiveShiftByDriverLicense] Result: $result")
                    result
                }
            }
        } catch (e: Exception) {
            println("      ❌ [hasActiveShiftByDriverLicense] ERROR: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    fun hasActiveShift(userId: String): Boolean {
        println("   [hasActiveShift] Checking for userId: $userId")
        val query = """
        SELECT id FROM shifts 
        WHERE "userId" = ?::uuid AND status = 'active'
    """.trimIndent()

        return try {
            DatabaseConfig.getConnection().use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, userId)
                    val result = stmt.executeQuery().next()
                    println("   [hasActiveShift] Result: $result")
                    result
                }
            }
        } catch (e: Exception) {
            println("   ❌ [hasActiveShift] ERROR: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    fun startShift(userId: String, driverLicense: String, licensePlate: String, startTime: Long): String {
        println("📝 [startShift] START")
        println("   userId: $userId")
        println("   driverLicense: $driverLicense")
        println("   licensePlate: $licensePlate")
        println("   startTime: $startTime (${Timestamp(startTime)})")

        return try {
            val shiftId = java.util.UUID.randomUUID().toString()
            println("   Generated shiftId: $shiftId")

            val query = """
            INSERT INTO shifts (id, "userId", "driverLicense", "licensePlate", "startTime", status)
            VALUES (?::uuid, ?::uuid, ?, ?, ?, 'active')
        """.trimIndent()
            println("   Query: $query")

            DatabaseConfig.getConnection().use { conn ->
                println("   Connection obtained")
                conn.prepareStatement(query).use { stmt ->
                    stmt.setString(1, shiftId)
                    stmt.setString(2, userId)
                    stmt.setString(3, driverLicense.trim().uppercase())
                    stmt.setString(4, licensePlate.trim().uppercase())
                    stmt.setTimestamp(5, Timestamp(startTime))

                    println("   Executing insert...")
                    val affectedRows = stmt.executeUpdate()
                    println("   Affected rows: $affectedRows")

                    if (affectedRows > 0) {
                        println("✅ [startShift] SUCCESS, shiftId: $shiftId")
                        shiftId
                    } else {
                        println("❌ [startShift] No rows affected")
                        "-1"
                    }
                }
            }
        } catch (e: Exception) {
            println("❌ [startShift] EXCEPTION: ${e.javaClass.simpleName}")
            println("   Message: ${e.message}")
            e.printStackTrace()
            "-1"
        }
    }

    fun endShift(shiftId: String, endTime: Long): Boolean {
        println("🏁 [endShift] START")
        println("   shiftId: $shiftId")
        println("   endTime: $endTime (${Timestamp(endTime)})")

        val query = """
            UPDATE shifts
            SET endTime = ?, status = 'completed'
            WHERE id = ? AND status = 'active'
        """.trimIndent()

        return try {
            DatabaseConfig.getConnection().use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setTimestamp(1, Timestamp(endTime))
                    stmt.setString(2, shiftId)
                    val updated = stmt.executeUpdate() > 0
                    println("   Result: $updated")
                    updated
                }
            }
        } catch (e: Exception) {
            println("❌ [endShift] ERROR: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    fun getActiveShiftByUserId(userId: String): ActiveShiftResponse? {
        println("📊 [getActiveShiftByUserId] START for userId: $userId")
        val query = """
    SELECT s.id, s."driverLicense", s."licensePlate", s.startTime, v."carBrand"
    FROM shifts s
    LEFT JOIN vehicles v ON UPPER(s."licensePlate") = UPPER(v."licensePlate")
    WHERE s."userId" = ? AND s.status = 'active'
""".trimIndent()

        return try {
            DatabaseConfig.getConnection().use { conn ->
                conn.prepareStatement(query).use { stmt ->
                    stmt.setInt(1, userId.toInt())
                    val rs = stmt.executeQuery()

                    if (rs.next()) {
                        val response = ActiveShiftResponse(
                            shiftId = rs.getString("id"),
                            driverLicense = rs.getString("driverLicense"),
                            licensePlate = rs.getString("licensePlate"),
                            startTime = rs.getTimestamp("startTime").time,
                            carBrand = rs.getString("carBrand")
                        )
                        println("✅ [getActiveShiftByUserId] Found: $response")
                        response
                    } else {
                        println("   No active shift found")
                        null
                    }
                }
            }
        } catch (e: Exception) {
            println("❌ [getActiveShiftByUserId] ERROR: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}
