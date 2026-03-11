package com.christelldev.easyreferplus.data.network

import com.christelldev.easyreferplus.data.model.ReferralTreeResponse
import com.christelldev.easyreferplus.data.model.SearchReferralResponse

sealed class ReferralResult {
    data class GetReferralsSuccess(val response: ReferralTreeResponse) : ReferralResult()
    data class SearchSuccess(val response: SearchReferralResponse) : ReferralResult()
    data class Error(val message: String, val code: Int? = null) : ReferralResult()
}

class ReferralRepository(
    private val apiService: ApiService
) {
    suspend fun getMyReferrals(authorization: String): ReferralResult {
        return try {
            val response = apiService.getMyReferrals(authorization)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.success) {
                        ReferralResult.GetReferralsSuccess(body)
                    } else {
                        ReferralResult.Error("Error al obtener referidos")
                    }
                } ?: ReferralResult.Error("Respuesta vacía")
            } else {
                when (response.code()) {
                    401 -> ReferralResult.Error("Sesión expirada", 401)
                    500 -> ReferralResult.Error("Error del servidor", 500)
                    else -> ReferralResult.Error("Error al obtener referidos")
                }
            }
        } catch (e: Exception) {
            val message = when {
                e is java.io.IOException && e.message?.contains("mantenimiento", ignoreCase = true) == true ->
                    "El servidor está en mantenimiento. Intente más tarde."
                e is java.net.UnknownHostException ||
                e is java.net.ConnectException ||
                e is java.io.IOException -> "Sin conexión al servidor"
                else -> "Error de conexión"
            }
            ReferralResult.Error(message)
        }
    }

    suspend fun searchReferral(authorization: String, code: String): ReferralResult {
        return try {
            val response = apiService.searchReferral(authorization, code)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    ReferralResult.SearchSuccess(body)
                } ?: ReferralResult.Error("Respuesta vacía")
            } else {
                when (response.code()) {
                    400 -> ReferralResult.Error("Código inválido", 400)
                    401 -> ReferralResult.Error("Sesión expirada", 401)
                    500 -> ReferralResult.Error("Error del servidor", 500)
                    else -> ReferralResult.Error("Error en la búsqueda")
                }
            }
        } catch (e: Exception) {
            val message = when {
                e is java.io.IOException && e.message?.contains("mantenimiento", ignoreCase = true) == true ->
                    "El servidor está en mantenimiento. Intente más tarde."
                e is java.net.UnknownHostException ||
                e is java.net.ConnectException ||
                e is java.io.IOException -> "Sin conexión al servidor"
                else -> "Error de conexión"
            }
            ReferralResult.Error(message)
        }
    }

    class Factory {
        fun create(): ReferralRepository {
            val retrofit = RetrofitClient.getInstance()
            val apiService = retrofit.create(ApiService::class.java)
            return ReferralRepository(apiService)
        }
    }
}
