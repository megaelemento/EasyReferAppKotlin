package com.christelldev.easyreferplus.data.network

import com.christelldev.easyreferplus.data.model.CommissionBalanceResponse
import com.christelldev.easyreferplus.data.model.BankAccountListResponse
import com.christelldev.easyreferplus.data.model.CreateBankAccountRequest
import com.christelldev.easyreferplus.data.model.WithdrawalSuccessResponse
import com.christelldev.easyreferplus.data.model.CreateWithdrawalRequest
import com.christelldev.easyreferplus.data.model.WithdrawalRequestListResponse
import com.google.gson.Gson

// =====================================================
// RESULTADOS
// =====================================================

sealed class WithdrawalBalanceResult {
    data class Success(val balance: CommissionBalanceResponse) : WithdrawalBalanceResult()
    data class Error(val message: String) : WithdrawalBalanceResult()
}

sealed class BankAccountsResult {
    data class Success(
        val accounts: List<com.christelldev.easyreferplus.data.model.BankAccountResponse>,
        val defaultAccountId: Int?
    ) : BankAccountsResult()
    data class Error(val message: String) : BankAccountsResult()
}

sealed class CreateBankAccountResult {
    data class Success(val message: String, val accountId: Int?) : CreateBankAccountResult()
    data class Error(val message: String) : CreateBankAccountResult()
}

sealed class WithdrawalRequestResult {
    data class Success(
        val message: String,
        val requestId: Int?,
        val estimatedProcessingTime: String?
    ) : WithdrawalRequestResult()
    data class Error(val message: String) : WithdrawalRequestResult()
}

sealed class WithdrawalHistoryResult {
    data class Success(
        val requests: List<com.christelldev.easyreferplus.data.model.WithdrawalRequestResponse>,
        val total: Int,
        val page: Int,
        val totalPages: Int
    ) : WithdrawalHistoryResult()
    data class Error(val message: String) : WithdrawalHistoryResult()
}

sealed class CancelWithdrawalResult {
    data class Success(val message: String) : CancelWithdrawalResult()
    data class Error(val message: String) : CancelWithdrawalResult()
}

// =====================================================
// REPOSITORIO DE RETIROS
// =====================================================

class WithdrawalRepository(
    private val apiService: ApiService
) {
    private val gson = Gson()

    /**
     * Obtener balance de comisiones (incluye monto mínimo)
     */
    suspend fun getCommissionBalance(authorization: String): WithdrawalBalanceResult {
        return try {
            val response = apiService.getCommissionBalance(authorization)
            if (response.isSuccessful) {
                response.body()?.let { balance ->
                    WithdrawalBalanceResult.Success(balance)
                } ?: WithdrawalBalanceResult.Error("Respuesta vacía")
            } else {
                val errorMessage = parseError(response.code(), response.errorBody()?.string())
                WithdrawalBalanceResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            val message = when {
                e is java.io.IOException && e.message?.contains("mantenimiento", ignoreCase = true) == true ->
                    "El servidor está en mantenimiento. Intente más tarde."
                e is java.io.IOException ->
                    "Sin conexión al servidor"
                else -> e.message ?: "Error desconocido"
            }
            WithdrawalBalanceResult.Error(message)
        }
    }

    /**
     * Obtener mis cuentas bancarias
     */
    suspend fun getMyBankAccounts(authorization: String): BankAccountsResult {
        return try {
            val response = apiService.getMyBankAccounts(authorization)
            if (response.isSuccessful) {
                response.body()?.let { accountsResponse ->
                    if (accountsResponse.success) {
                        BankAccountsResult.Success(
                            accounts = accountsResponse.accounts,
                            defaultAccountId = accountsResponse.defaultAccountId
                        )
                    } else {
                        BankAccountsResult.Error("Error al obtener cuentas")
                    }
                } ?: BankAccountsResult.Error("Respuesta vacía")
            } else {
                val errorMessage = parseError(response.code(), response.errorBody()?.string())
                BankAccountsResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            val message = when {
                e is java.io.IOException && e.message?.contains("mantenimiento", ignoreCase = true) == true ->
                    "El servidor está en mantenimiento. Intente más tarde."
                e is java.io.IOException ->
                    "Sin conexión al servidor"
                else -> e.message ?: "Error desconocido"
            }
            BankAccountsResult.Error(message)
        }
    }

    /**
     * Crear cuenta bancaria
     */
    suspend fun createBankAccount(
        authorization: String,
        bankName: String,
        accountType: String,
        accountNumber: String,
        accountHolderName: String,
        accountHolderDocument: String
    ): CreateBankAccountResult {
        return try {
            val request = CreateBankAccountRequest(
                bankName = bankName,
                accountType = accountType,
                accountNumber = accountNumber,
                accountHolderName = accountHolderName,
                accountHolderDocument = accountHolderDocument,
                isDefault = true
            )
            val response = apiService.createBankAccount(authorization, request)
            if (response.isSuccessful) {
                response.body()?.let { successResponse ->
                    if (successResponse.success) {
                        CreateBankAccountResult.Success(
                            message = successResponse.message,
                            accountId = successResponse.data?.accountId
                        )
                    } else {
                        CreateBankAccountResult.Error(successResponse.message)
                    }
                } ?: CreateBankAccountResult.Error("Respuesta vacía")
            } else {
                val errorMessage = parseError(response.code(), response.errorBody()?.string())
                CreateBankAccountResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            val message = when {
                e is java.io.IOException && e.message?.contains("mantenimiento", ignoreCase = true) == true ->
                    "El servidor está en mantenimiento. Intente más tarde."
                e is java.io.IOException ->
                    "Sin conexión al servidor"
                else -> e.message ?: "Error desconocido"
            }
            CreateBankAccountResult.Error(message)
        }
    }

    /**
     * Crear solicitud de retiro
     */
    suspend fun createWithdrawalRequest(
        authorization: String,
        amount: Double,
        bankAccountId: Int
    ): WithdrawalRequestResult {
        return try {
            val request = CreateWithdrawalRequest(
                requestedAmount = amount,
                bankAccountId = bankAccountId
            )
            val response = apiService.createWithdrawalRequest(authorization, request)
            if (response.isSuccessful) {
                response.body()?.let { successResponse ->
                    if (successResponse.success) {
                        WithdrawalRequestResult.Success(
                            message = successResponse.message,
                            requestId = successResponse.data?.requestId,
                            estimatedProcessingTime = successResponse.data?.estimatedProcessingTime
                        )
                    } else {
                        WithdrawalRequestResult.Error(successResponse.message)
                    }
                } ?: WithdrawalRequestResult.Error("Respuesta vacía")
            } else {
                val errorMessage = parseError(response.code(), response.errorBody()?.string())
                WithdrawalRequestResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            val message = when {
                e is java.io.IOException && e.message?.contains("mantenimiento", ignoreCase = true) == true ->
                    "El servidor está en mantenimiento. Intente más tarde."
                e is java.io.IOException ->
                    "Sin conexión al servidor"
                else -> e.message ?: "Error desconocido"
            }
            WithdrawalRequestResult.Error(message)
        }
    }

    /**
     * Obtener historial de solicitudes de retiro
     */
    suspend fun getMyWithdrawalRequests(
        authorization: String,
        page: Int = 1,
        perPage: Int = 20
    ): WithdrawalHistoryResult {
        return try {
            val response = apiService.getMyWithdrawalRequests(authorization, page, perPage)
            if (response.isSuccessful) {
                response.body()?.let { historyResponse ->
                    if (historyResponse.success) {
                        WithdrawalHistoryResult.Success(
                            requests = historyResponse.requests,
                            total = historyResponse.total,
                            page = historyResponse.page,
                            totalPages = historyResponse.totalPages
                        )
                    } else {
                        WithdrawalHistoryResult.Error("Error al obtener historial")
                    }
                } ?: WithdrawalHistoryResult.Error("Respuesta vacía")
            } else {
                val errorMessage = parseError(response.code(), response.errorBody()?.string())
                WithdrawalHistoryResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            val message = when {
                e is java.io.IOException && e.message?.contains("mantenimiento", ignoreCase = true) == true ->
                    "El servidor está en mantenimiento. Intente más tarde."
                e is java.io.IOException ->
                    "Sin conexión al servidor"
                else -> e.message ?: "Error desconocido"
            }
            WithdrawalHistoryResult.Error(message)
        }
    }

    /**
     * Cancelar solicitud de retiro
     */
    suspend fun cancelWithdrawalRequest(
        authorization: String,
        requestId: Int
    ): CancelWithdrawalResult {
        return try {
            val response = apiService.cancelWithdrawalRequest(authorization, requestId)
            if (response.isSuccessful) {
                response.body()?.let { successResponse ->
                    if (successResponse.success) {
                        CancelWithdrawalResult.Success(successResponse.message)
                    } else {
                        CancelWithdrawalResult.Error(successResponse.message)
                    }
                } ?: CancelWithdrawalResult.Error("Respuesta vacía")
            } else {
                val errorMessage = parseError(response.code(), response.errorBody()?.string())
                CancelWithdrawalResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            val message = when {
                e is java.io.IOException && e.message?.contains("mantenimiento", ignoreCase = true) == true ->
                    "El servidor está en mantenimiento. Intente más tarde."
                e is java.io.IOException ->
                    "Sin conexión al servidor"
                else -> e.message ?: "Error desconocido"
            }
            CancelWithdrawalResult.Error(message)
        }
    }

    private fun parseError(code: Int, errorBody: String?): String {
        return try {
            if (errorBody != null) {
                val errorJson = gson.fromJson(errorBody, Map::class.java)
                (errorJson["detail"] as? String) ?: "Error $code"
            } else {
                "Error $code"
            }
        } catch (e: Exception) {
            "Error $code"
        }
    }

    companion object {
        fun Factory(): WithdrawalRepository {
            val retrofit = RetrofitClient.getInstance()
            val apiService = retrofit.create(ApiService::class.java)
            return WithdrawalRepository(apiService)
        }
    }
}
