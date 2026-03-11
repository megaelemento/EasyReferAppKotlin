package com.christelldev.easyreferplus.data.network

import com.christelldev.easyreferplus.data.model.GenerateQRRequest
import com.christelldev.easyreferplus.data.model.ProcessQRRequest
import com.christelldev.easyreferplus.data.model.ProcessReceiptResponse
import com.christelldev.easyreferplus.data.model.ScanQRRequest
import com.christelldev.easyreferplus.data.model.QRValidationError
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

sealed class QRResult {
    data class GenerateSuccess(
        val message: String,
        val qrCode: String?,
        val qrPayload: String?,
        val qrData: String?,
        val amount: Double?,
        val companyName: String?,
        val expiresAt: String?,
        // Legacy fields
        val qrTransaction: com.christelldev.easyreferplus.data.model.QRTransaction?,
        val qrImageUrl: String?
    ) : QRResult()

    data class ScanSuccess(
        val message: String,
        val qrTransaction: com.christelldev.easyreferplus.data.model.QRTransaction?
    ) : QRResult()

    data class ProcessSuccess(
        val message: String,
        val transaction: com.christelldev.easyreferplus.data.model.ProcessedTransaction?,
        val qrTransaction: com.christelldev.easyreferplus.data.model.QRTransaction?,
        val referralDistribution: com.christelldev.easyreferplus.data.model.ReferralDistribution?,
        val receipt: ProcessReceiptResponse? = null
    ) : QRResult()

    data class StatusSuccess(
        val qrTransaction: com.christelldev.easyreferplus.data.model.QRTransaction?
    ) : QRResult()

    data class ListSuccess(
        val qrTransactions: List<com.christelldev.easyreferplus.data.model.QRTransaction>,
        val pagination: com.christelldev.easyreferplus.data.model.QRPagination?
    ) : QRResult()

    data class Error(
        val message: String,
        val code: Int? = null,
        val errorType: String? = null,
        val errors: List<QRValidationError>? = null
    ) : QRResult()
}

sealed class HistoryResult {
    data class Success(
        val data: List<com.christelldev.easyreferplus.data.model.TransactionHistoryItem>,
        val stats: com.christelldev.easyreferplus.data.model.HistoryStats?
    ) : HistoryResult()

    data class Error(val message: String) : HistoryResult()
}

sealed class TransactionDetailResult {
    data class Success(
        val receipt: com.christelldev.easyreferplus.data.model.TransactionReceipt
    ) : TransactionDetailResult()

    data class Error(val message: String) : TransactionDetailResult()
}

class QRRepository(
    private val apiService: ApiService
) {
    companion object {
        private val BASE_URL = AppConfig.BASE_URL.trimEnd('/')
    }

    private val gson = Gson()

    // Función para convertir URL relativa a absoluta
    private fun getFullImageUrl(relativeUrl: String?): String? {
        if (relativeUrl == null) return null
        return if (relativeUrl.startsWith("http")) {
            relativeUrl
        } else {
            "$BASE_URL$relativeUrl"
        }
    }

    // Generar código QR
    suspend fun generateQR(authorization: String, request: GenerateQRRequest): QRResult {
        return try {
            val response = apiService.generateQR(authorization, request)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.success) {
                        QRResult.GenerateSuccess(
                            message = body.message,
                            qrCode = body.qrCode,
                            qrPayload = body.qrPayload,
                            qrData = body.qrData,
                            amount = body.amount,
                            companyName = body.companyName,
                            expiresAt = body.expiresAt,
                            // Legacy
                            qrTransaction = body.qrTransaction,
                            qrImageUrl = getFullImageUrl(body.qrImageUrl)
                        )
                    } else {
                        parseError(response.code(), response.errorBody()?.string())
                    }
                } ?: QRResult.Error("Respuesta vacía")
            } else {
                parseError(response.code(), response.errorBody()?.string())
            }
        } catch (e: Exception) {
            parseException(e)
        }
    }

    // Escanear código QR - ahora procesa automáticamente y devuelve recibo
    suspend fun scanQR(authorization: String, request: ScanQRRequest): QRResult {
        return try {
            val response = apiService.scanQR(authorization, request)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.success) {
                        // El backend ahora devuelve ProcessReceiptResponse (recibo completo)
                        // Mapear a ProcessSuccess para mostrar el recibo
                        val processedTransaction = com.christelldev.easyreferplus.data.model.ProcessedTransaction(
                            id = 0,
                            companyId = body.companyId,
                            buyerDocument = body.buyerDocument,
                            buyerName = body.buyerName,
                            buyerPhone = body.buyerPhone,
                            buyerEmail = body.buyerEmail,
                            totalAmount = body.amount,
                            totalCommissionAmount = 0.0,
                            referralCodeUsed = body.referralCodeUsed,
                            transactionStatus = "completed",
                            description = null,
                            purchaseDate = body.transactionDate
                        )

                        QRResult.ProcessSuccess(
                            message = body.message,
                            transaction = processedTransaction,
                            qrTransaction = null,
                            referralDistribution = null,
                            receipt = body
                        )
                    } else {
                        parseError(response.code(), response.errorBody()?.string())
                    }
                } ?: QRResult.Error("Respuesta vacía")
            } else {
                parseError(response.code(), response.errorBody()?.string())
            }
        } catch (e: Exception) {
            parseException(e)
        }
    }

    // Procesar transacción QR
    suspend fun processQR(authorization: String, request: ProcessQRRequest): QRResult {
        return try {
            val response = apiService.processQR(authorization, request)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    // The new API returns ProcessReceiptResponse directly
                    // We need to map it to our existing format for compatibility
                    // Create a compatible ProcessedTransaction from the new response
                    val processedTransaction = com.christelldev.easyreferplus.data.model.ProcessedTransaction(
                        id = 0,
                        companyId = body.companyId,
                        buyerDocument = body.buyerDocument,
                        buyerName = body.buyerName,
                        buyerPhone = body.buyerPhone,
                        buyerEmail = body.buyerEmail,
                        totalAmount = body.amount,
                        totalCommissionAmount = 0.0,
                        referralCodeUsed = body.referralCodeUsed,
                        transactionStatus = "completed",
                        description = null,
                        purchaseDate = body.transactionDate
                    )

                    QRResult.ProcessSuccess(
                        message = body.message,
                        transaction = processedTransaction,
                        qrTransaction = null,
                        referralDistribution = null,
                        receipt = body
                    )
                } ?: QRResult.Error("Respuesta vacía")
            } else {
                parseError(response.code(), response.errorBody()?.string())
            }
        } catch (e: Exception) {
            parseException(e)
        }
    }

    // Obtener estado de QR
    suspend fun getQRStatus(authorization: String, qrCode: String): QRResult {
        return try {
            val response = apiService.getQRStatus(authorization, qrCode)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.success) {
                        QRResult.StatusSuccess(body.qrTransaction)
                    } else {
                        parseError(response.code(), response.errorBody()?.string())
                    }
                } ?: QRResult.Error("Respuesta vacía")
            } else {
                parseError(response.code(), response.errorBody()?.string())
            }
        } catch (e: Exception) {
            parseException(e)
        }
    }

    // Obtener QR generados por el usuario
    suspend fun getMyGeneratedQRs(authorization: String, page: Int = 1, perPage: Int = 20): QRResult {
        return try {
            val response = apiService.getMyGeneratedQRs(authorization, page, perPage)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.success) {
                        QRResult.ListSuccess(
                            qrTransactions = body.qrTransactions ?: emptyList(),
                            pagination = body.pagination
                        )
                    } else {
                        parseError(response.code(), response.errorBody()?.string())
                    }
                } ?: QRResult.Error("Respuesta vacía")
            } else {
                parseError(response.code(), response.errorBody()?.string())
            }
        } catch (e: Exception) {
            parseException(e)
        }
    }

    // Obtener QR escaneados por el usuario
    suspend fun getMyScannedQRs(authorization: String, page: Int = 1, perPage: Int = 20): QRResult {
        return try {
            val response = apiService.getMyScannedQRs(authorization, page, perPage)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.success) {
                        QRResult.ListSuccess(
                            qrTransactions = body.qrTransactions ?: emptyList(),
                            pagination = body.pagination
                        )
                    } else {
                        parseError(response.code(), response.errorBody()?.string())
                    }
                } ?: QRResult.Error("Respuesta vacía")
            } else {
                parseError(response.code(), response.errorBody()?.string())
            }
        } catch (e: Exception) {
            parseException(e)
        }
    }

    // Obtener historial de transacciones
    suspend fun getHistory(authorization: String, transactionType: String? = null, page: Int = 1, perPage: Int = 20): HistoryResult {
        return try {
            val response = apiService.getTransactionHistory(authorization, transactionType, page, perPage)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.success) {
                        HistoryResult.Success(
                            data = body.data ?: emptyList(),
                            stats = body.stats
                        )
                    } else {
                        HistoryResult.Error("Error al obtener historial")
                    }
                } ?: HistoryResult.Error("Respuesta vacía")
            } else {
                HistoryResult.Error(parseHistoryError(response.code()))
            }
        } catch (e: Exception) {
            HistoryResult.Error(e.message ?: "Error desconocido")
        }
    }

    // Obtener detalles de una transacción específica
    suspend fun getTransactionDetail(authorization: String, transactionId: String): TransactionDetailResult {
        return try {
            val response = apiService.getTransactionReceipt(authorization, transactionId)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.success && body.receipt != null) {
                        TransactionDetailResult.Success(body.receipt)
                    } else {
                        TransactionDetailResult.Error("Error al obtener detalles de la transacción")
                    }
                } ?: TransactionDetailResult.Error("Respuesta vacía")
            } else {
                TransactionDetailResult.Error(parseHistoryError(response.code()))
            }
        } catch (e: Exception) {
            TransactionDetailResult.Error(e.message ?: "Error desconocido")
        }
    }

    private fun parseHistoryError(code: Int): String {
        return when (code) {
            401 -> "Sesión expirada"
            403 -> "No tienes permiso"
            500 -> "Error del servidor"
            else -> "Error al procesar la solicitud"
        }
    }

    private fun parseError(code: Int, errorBody: String?): QRResult {
        return try {
            if (errorBody != null) {
                val errorMap = gson.fromJson(errorBody, Map::class.java)

                // Intentar parsear con el modelo de error
                val errorType = errorMap["error_type"] as? String
                val message = errorMap["message"] as? String ?: "Error desconocido"

                // Parsear errores de validación si existen
                val errorsField = errorMap["errors"]
                val validationErrors = if (errorsField is List<*>) {
                    errorsField.mapNotNull { item ->
                        if (item is Map<*, *>) {
                            val field = item["field"] as? String
                            val msg = item["message"] as? String
                            if (field != null && msg != null) {
                                QRValidationError(field, msg)
                            } else null
                        } else null
                    }
                } else null

                // Mensajes específicos según el tipo de error
                val specificMessage = when (errorType) {
                    "validation_error" -> message
                    "invalid_qr" -> "Código QR inválido o datos incorrectos"
                    "qr_not_found" -> "Código QR no encontrado"
                    "qr_already_used" -> "Este código QR ya ha sido utilizado"
                    "qr_expired" -> "Este código QR ha expirado"
                    "qr_already_processed" -> "Este código QR ya ha sido procesado"
                    "invalid_token" -> "Token inválido o expirado"
                    "forbidden" -> "No tienes permiso para esta acción"
                    "company_not_found" -> "Empresa no encontrada"
                    "company_not_validated" -> "La empresa no está validada y no puede generar códigos QR"
                    else -> message
                }

                QRResult.Error(
                    message = specificMessage,
                    code = code,
                    errorType = errorType,
                    errors = validationErrors
                )
            } else {
                getDefaultError(code)
            }
        } catch (e: Exception) {
            getDefaultError(code)
        }
    }

    private fun getDefaultError(code: Int): QRResult {
        return when (code) {
            400 -> QRResult.Error("Datos inválidos", code)
            401 -> QRResult.Error("Sesión expirada", code)
            403 -> QRResult.Error("No tienes permiso para esta acción", code)
            404 -> QRResult.Error("Recurso no encontrado", code)
            409 -> QRResult.Error("Conflicto: el recurso ya existe o fue procesado", code)
            410 -> QRResult.Error("El recurso ha expirado", code)
            422 -> QRResult.Error("Error de validación", code)
            429 -> QRResult.Error("Demasiados intentos. Intente más tarde", code)
            500 -> QRResult.Error("Error del servidor", code)
            else -> QRResult.Error("Error al procesar la solicitud", code)
        }
    }

    private fun parseException(e: Exception): QRResult {
        val message = when {
            e is java.io.IOException && e.message?.contains("mantenimiento", ignoreCase = true) == true ->
                "El servidor está en mantenimiento. Intente más tarde."
            e is java.net.UnknownHostException ||
            e is java.net.ConnectException ||
            e is java.io.IOException -> "Sin conexión al servidor"
            else -> "Error de conexión"
        }
        return QRResult.Error(message)
    }

    class Factory {
        fun create(): QRRepository {
            val retrofit = RetrofitClient.getInstance()
            val apiService = retrofit.create(ApiService::class.java)
            return QRRepository(apiService)
        }
    }
}
