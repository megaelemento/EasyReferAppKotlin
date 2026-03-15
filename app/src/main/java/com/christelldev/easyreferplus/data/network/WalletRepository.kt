package com.christelldev.easyreferplus.data.network

import com.christelldev.easyreferplus.data.model.ChangePinRequest
import com.christelldev.easyreferplus.data.model.SetPinRequest
import com.christelldev.easyreferplus.data.model.TransferRequest
import com.christelldev.easyreferplus.data.model.TransferResponse
import com.christelldev.easyreferplus.data.model.WalletBalanceResponse
import com.christelldev.easyreferplus.data.model.WalletStatementItem
import com.christelldev.easyreferplus.data.model.WalletStatementResponse
import com.google.gson.Gson
import java.util.UUID

// =====================================================
// RESULTADOS
// =====================================================

sealed class WalletBalanceResult {
    data class Success(val balance: WalletBalanceResponse) : WalletBalanceResult()
    data class Error(val message: String) : WalletBalanceResult()
}

sealed class WalletPinResult {
    data object Success : WalletPinResult()
    data class Error(val message: String) : WalletPinResult()
}

sealed class WalletTransferResult {
    data class Success(val transfer: TransferResponse) : WalletTransferResult()
    data class Error(val message: String) : WalletTransferResult()
}

sealed class WalletStatementResult {
    data class Success(val statement: WalletStatementResponse) : WalletStatementResult()
    data class Error(val message: String) : WalletStatementResult()
}

sealed class WalletTransferDetailResult {
    data class Success(val item: WalletStatementItem) : WalletTransferDetailResult()
    data class Error(val message: String) : WalletTransferDetailResult()
}

sealed class WalletCheckRecipientResult {
    data class Success(val registered: Boolean, val recipientName: String? = null) : WalletCheckRecipientResult()
    data class Error(val message: String) : WalletCheckRecipientResult()
}

// =====================================================
// REPOSITORIO DE BILLETERA DIGITAL
// =====================================================

class WalletRepository(
    private val apiService: ApiService
) {
    private val gson = Gson()

    /**
     * Consultar saldo de la billetera digital
     */
    suspend fun getBalance(): WalletBalanceResult {
        return try {
            val response = apiService.getWalletBalance()
            if (response.isSuccessful) {
                response.body()?.let { balance ->
                    WalletBalanceResult.Success(balance)
                } ?: WalletBalanceResult.Error("Respuesta vacía")
            } else {
                val errorMessage = parseError(response.code(), response.errorBody()?.string())
                WalletBalanceResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            WalletBalanceResult.Error(buildNetworkErrorMessage(e))
        }
    }

    /**
     * Establecer PIN de billetera (primera vez)
     */
    suspend fun setPin(pin: String): WalletPinResult {
        return try {
            val response = apiService.setWalletPin(SetPinRequest(pin = pin))
            if (response.isSuccessful) {
                val body = response.body()
                if (body == null || body.success) {
                    WalletPinResult.Success
                } else {
                    WalletPinResult.Error(body.message ?: "No se pudo establecer el PIN")
                }
            } else {
                val errorMessage = parseError(response.code(), response.errorBody()?.string())
                WalletPinResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            WalletPinResult.Error(buildNetworkErrorMessage(e))
        }
    }

    /**
     * Cambiar PIN de billetera
     */
    suspend fun changePin(currentPin: String, newPin: String): WalletPinResult {
        return try {
            val response = apiService.changeWalletPin(
                ChangePinRequest(currentPin = currentPin, newPin = newPin)
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body == null || body.success) {
                    WalletPinResult.Success
                } else {
                    WalletPinResult.Error(body.message ?: "No se pudo cambiar el PIN")
                }
            } else {
                val errorMessage = parseError(response.code(), response.errorBody()?.string())
                WalletPinResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            WalletPinResult.Error(buildNetworkErrorMessage(e))
        }
    }

    /**
     * Realizar transferencia a otro usuario.
     * El idempotencyKey se genera aquí para garantizar unicidad por intento.
     */
    suspend fun transfer(
        recipientPhone: String,
        amount: Double,
        description: String?,
        pin: String
    ): WalletTransferResult {
        return try {
            val idempotencyKey = UUID.randomUUID().toString()
            val request = TransferRequest(
                recipientPhone = recipientPhone,
                amount = amount,
                description = description,
                idempotencyKey = idempotencyKey,
                pin = pin
            )
            val response = apiService.walletTransfer(request)
            if (response.isSuccessful) {
                response.body()?.let { transferResponse ->
                    WalletTransferResult.Success(transferResponse)
                } ?: WalletTransferResult.Error("Respuesta vacía")
            } else {
                val errorMessage = parseError(response.code(), response.errorBody()?.string())
                WalletTransferResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            WalletTransferResult.Error(buildNetworkErrorMessage(e))
        }
    }

    /**
     * Obtener estado de cuenta con paginación y filtros de fecha opcionales
     */
    suspend fun getStatement(
        page: Int = 1,
        perPage: Int = 20,
        startDate: String? = null,
        endDate: String? = null
    ): WalletStatementResult {
        return try {
            val response = apiService.getWalletStatement(
                page = page,
                perPage = perPage,
                startDate = startDate,
                endDate = endDate
            )
            if (response.isSuccessful) {
                response.body()?.let { statement ->
                    WalletStatementResult.Success(statement)
                } ?: WalletStatementResult.Error("Respuesta vacía")
            } else {
                val errorMessage = parseError(response.code(), response.errorBody()?.string())
                WalletStatementResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            WalletStatementResult.Error(buildNetworkErrorMessage(e))
        }
    }

    /**
     * Obtener el detalle de una transferencia específica
     */
    suspend fun getTransferDetail(id: Int): WalletTransferDetailResult {
        return try {
            val response = apiService.getWalletTransfer(id)
            if (response.isSuccessful) {
                response.body()?.let { item ->
                    WalletTransferDetailResult.Success(item)
                } ?: WalletTransferDetailResult.Error("Respuesta vacía")
            } else {
                val errorMessage = parseError(response.code(), response.errorBody()?.string())
                WalletTransferDetailResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            WalletTransferDetailResult.Error(buildNetworkErrorMessage(e))
        }
    }

    suspend fun checkRecipient(phone: String): WalletCheckRecipientResult {
        return try {
            val response = apiService.checkWalletRecipient(phone)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    WalletCheckRecipientResult.Success(body.registered, body.recipientName)
                } ?: WalletCheckRecipientResult.Error("Respuesta vacía")
            } else {
                WalletCheckRecipientResult.Error(parseError(response.code(), response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            WalletCheckRecipientResult.Error(buildNetworkErrorMessage(e))
        }
    }

    // --------------------------------------------------
    // Helpers privados
    // --------------------------------------------------

    private fun parseError(code: Int, errorBody: String?): String {
        return try {
            if (errorBody != null) {
                val errorJson = gson.fromJson(errorBody, Map::class.java)
                when (code) {
                    403 -> (errorJson["detail"] as? String) ?: "PIN incorrecto o acceso denegado"
                    429 -> "Demasiadas solicitudes. Intente más tarde."
                    else -> (errorJson["detail"] as? String) ?: "Error $code"
                }
            } else {
                when (code) {
                    403 -> "PIN incorrecto o acceso denegado"
                    429 -> "Demasiadas solicitudes. Intente más tarde."
                    else -> "Error $code"
                }
            }
        } catch (e: Exception) {
            "Error $code"
        }
    }

    private fun buildNetworkErrorMessage(e: Exception): String {
        return when {
            e is java.io.IOException && e.message?.contains("mantenimiento", ignoreCase = true) == true ->
                "El servidor está en mantenimiento. Intente más tarde."
            e is java.io.IOException ->
                "Sin conexión al servidor"
            else -> e.message ?: "Error desconocido"
        }
    }

    companion object {
        fun Factory(): WalletRepository {
            val retrofit = RetrofitClient.getInstance()
            val apiService = retrofit.create(ApiService::class.java)
            return WalletRepository(apiService)
        }
    }
}
