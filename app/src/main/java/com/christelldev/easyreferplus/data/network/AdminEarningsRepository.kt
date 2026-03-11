package com.christelldev.easyreferplus.data.network

import com.christelldev.easyreferplus.data.model.AdminEarningsResponse
import com.christelldev.easyreferplus.data.model.AdminUserEarningsResponse
import retrofit2.Response

// Results for admin earnings operations
sealed class AdminEarningsResult {
    data class Success(val response: AdminEarningsResponse) : AdminEarningsResult()
    data class Error(val message: String) : AdminEarningsResult()
}

sealed class AdminUserEarningsResult {
    data class Success(val response: AdminUserEarningsResponse) : AdminUserEarningsResult()
    data class Error(val message: String) : AdminUserEarningsResult()
}

class AdminEarningsRepository(private val apiService: ApiService) {

    suspend fun getEarnings(
        authorization: String,
        search: String? = null,
        filter: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null,
        page: Int = 1
    ): AdminEarningsResult {
        return try {
            val response = apiService.getAdminEarnings(
                authorization = authorization,
                search = search,
                filter = filter,
                dateFrom = dateFrom,
                dateTo = dateTo,
                page = page
            )

            if (response.isSuccessful) {
                response.body()?.let { earningsResponse ->
                    if (earningsResponse.success) {
                        AdminEarningsResult.Success(earningsResponse)
                    } else {
                        AdminEarningsResult.Error("Error al obtener ganancias")
                    }
                } ?: AdminEarningsResult.Error("Respuesta vacía")
            } else {
                AdminEarningsResult.Error(parseError(response.code()))
            }
        } catch (e: Exception) {
            val message = when {
                e is java.io.IOException && e.message?.contains("mantenimiento", ignoreCase = true) == true ->
                    "El servidor está en mantenimiento. Intente más tarde."
                else -> e.message ?: "Error desconocido"
            }
            AdminEarningsResult.Error(message)
        }
    }

    suspend fun getUserEarnings(
        authorization: String,
        userId: Int,
        page: Int = 1
    ): AdminUserEarningsResult {
        return try {
            val response = apiService.getAdminUserEarnings(
                authorization = authorization,
                userId = userId,
                page = page
            )

            if (response.isSuccessful) {
                response.body()?.let { userEarningsResponse ->
                    if (userEarningsResponse.success) {
                        AdminUserEarningsResult.Success(userEarningsResponse)
                    } else {
                        AdminUserEarningsResult.Error("Error al obtener ganancias del usuario")
                    }
                } ?: AdminUserEarningsResult.Error("Respuesta vacía")
            } else {
                AdminUserEarningsResult.Error(parseError(response.code()))
            }
        } catch (e: Exception) {
            AdminUserEarningsResult.Error(e.message ?: "Error desconocido")
        }
    }

    private fun parseError(code: Int): String {
        return when (code) {
            401 -> "No autorizado"
            403 -> "No tienes permisos para acceder a esta sección"
            404 -> "Recurso no encontrado"
            500 -> "Error del servidor"
            else -> "Error: $code"
        }
    }

    companion object {
        fun Factory(): AdminEarningsRepository {
            val apiService = RetrofitClient.getInstance().create(ApiService::class.java)
            return AdminEarningsRepository(apiService)
        }
    }
}
