package com.christelldev.easyreferplus.data.network

import com.christelldev.easyreferplus.data.model.EarningsSummaryResponse
import com.christelldev.easyreferplus.data.model.AdminEarningsSummaryResponse
import com.christelldev.easyreferplus.data.model.AdminUserEarningsSummaryResponse
import com.christelldev.easyreferplus.data.model.CommissionResponse
import com.google.gson.Gson

// =====================================================
// RESULTADOS PARA USUARIO
// =====================================================

sealed class EarningsResult {
    data class Success(val response: EarningsSummaryResponse) : EarningsResult()
    data class Error(val message: String) : EarningsResult()
}

// Resultados para lista de comisiones
sealed class CommissionsResult {
    data class Success(val commissions: List<CommissionResponse>) : CommissionsResult()
    data class Error(val message: String) : CommissionsResult()
}

// =====================================================
// RESULTADOS PARA ADMIN
// =====================================================

sealed class AdminEarningsSummaryResult {
    data class Success(val response: AdminEarningsSummaryResponse) : AdminEarningsSummaryResult()
    data class Error(val message: String) : AdminEarningsSummaryResult()
}

sealed class AdminUserEarningsSummaryResult {
    data class Success(val response: AdminUserEarningsSummaryResponse) : AdminUserEarningsSummaryResult()
    data class Error(val message: String) : AdminUserEarningsSummaryResult()
}

// =====================================================
// REPOSITORIO DE GANANCIAS
// =====================================================

class EarningsRepository(
    private val apiService: ApiService
) {
    private val gson = Gson()

    /**
     * Obtener resumen de ganancias del usuario autenticado
     */
    suspend fun getMyEarningsSummary(authorization: String): EarningsResult {
        return try {
            val response = apiService.getMyEarningsSummary(authorization)
            if (response.isSuccessful) {
                response.body()?.let { earningsResponse ->
                    if (earningsResponse.success) {
                        EarningsResult.Success(earningsResponse)
                    } else {
                        EarningsResult.Error("Error al obtener ganancias")
                    }
                } ?: EarningsResult.Error("Respuesta vacía")
            } else {
                val errorMessage = parseError(response.code(), response.errorBody()?.string())
                EarningsResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            val message = when {
                e is java.io.IOException && e.message?.contains("mantenimiento", ignoreCase = true) == true ->
                    "El servidor está en mantenimiento. Intente más tarde."
                e is java.io.IOException ->
                    "Sin conexión al servidor"
                else -> e.message ?: "Error desconocido"
            }
            EarningsResult.Error(message)
        }
    }

    /**
     * Obtener lista de comisiones del usuario autenticado
     */
    suspend fun getMyCommissions(
        authorization: String,
        page: Int = 1,
        perPage: Int = 20,
        paymentStatus: String? = null
    ): CommissionsResult {
        return try {
            val response = apiService.getMyCommissions(
                authorization = authorization,
                page = page,
                perPage = perPage,
                paymentStatus = paymentStatus
            )
            if (response.isSuccessful) {
                response.body()?.let { commissions ->
                    CommissionsResult.Success(commissions)
                } ?: CommissionsResult.Error("Respuesta vacía")
            } else {
                val errorMessage = parseError(response.code(), response.errorBody()?.string())
                CommissionsResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            val message = when {
                e is java.io.IOException && e.message?.contains("mantenimiento", ignoreCase = true) == true ->
                    "El servidor está en mantenimiento. Intente más tarde."
                e is java.io.IOException ->
                    "Sin conexión al servidor"
                else -> e.message ?: "Error desconocido"
            }
            CommissionsResult.Error(message)
        }
    }

    /**
     * Obtener resumen general de ganancias (Admin)
     */
    suspend fun getAdminEarningsSummary(authorization: String): AdminEarningsSummaryResult {
        return try {
            val response = apiService.getAdminEarningsSummary(authorization)
            if (response.isSuccessful) {
                response.body()?.let { summaryResponse ->
                    if (summaryResponse.success) {
                        AdminEarningsSummaryResult.Success(summaryResponse)
                    } else {
                        AdminEarningsSummaryResult.Error("Error al obtener resumen de ganancias")
                    }
                } ?: AdminEarningsSummaryResult.Error("Respuesta vacía")
            } else {
                val errorMessage = parseError(response.code(), response.errorBody()?.string())
                AdminEarningsSummaryResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            val message = when {
                e is java.io.IOException && e.message?.contains("mantenimiento", ignoreCase = true) == true ->
                    "El servidor está en mantenimiento. Intente más tarde."
                e is java.io.IOException ->
                    "Sin conexión al servidor"
                else -> e.message ?: "Error desconocido"
            }
            AdminEarningsSummaryResult.Error(message)
        }
    }

    /**
     * Obtener resumen de ganancias de un usuario específico (Admin)
     */
    suspend fun getAdminUserEarningsSummary(authorization: String, userId: Int): AdminUserEarningsSummaryResult {
        return try {
            val response = apiService.getAdminUserEarningsSummary(authorization, userId)
            if (response.isSuccessful) {
                response.body()?.let { userSummaryResponse ->
                    if (userSummaryResponse.success) {
                        AdminUserEarningsSummaryResult.Success(userSummaryResponse)
                    } else {
                        AdminUserEarningsSummaryResult.Error("Error al obtener ganancias del usuario")
                    }
                } ?: AdminUserEarningsSummaryResult.Error("Respuesta vacía")
            } else {
                val errorMessage = parseError(response.code(), response.errorBody()?.string())
                AdminUserEarningsSummaryResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            val message = when {
                e is java.io.IOException && e.message?.contains("mantenimiento", ignoreCase = true) == true ->
                    "El servidor está en mantenimiento. Intente más tarde."
                e is java.io.IOException ->
                    "Sin conexión al servidor"
                else -> e.message ?: "Error desconocido"
            }
            AdminUserEarningsSummaryResult.Error(message)
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

    // Factory para crear instancia
    companion object {
        fun Factory(): EarningsRepository {
            val retrofit = RetrofitClient.getInstance()
            val apiService = retrofit.create(ApiService::class.java)
            return EarningsRepository(apiService)
        }
    }
}
