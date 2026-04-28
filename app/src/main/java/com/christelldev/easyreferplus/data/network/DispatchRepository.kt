package com.christelldev.easyreferplus.data.network

import com.christelldev.easyreferplus.data.model.ActiveDispatchesResponse
import com.christelldev.easyreferplus.data.model.DispatchDetail
import retrofit2.Response

sealed class DispatchListResult {
    data class Success(val dispatches: List<com.christelldev.easyreferplus.data.model.ActiveDispatch>) : DispatchListResult()
    data class Error(val message: String) : DispatchListResult()
}

sealed class DispatchDetailResult {
    data class Success(val detail: DispatchDetail) : DispatchDetailResult()
    data class Error(val message: String) : DispatchDetailResult()
}

class DispatchRepository(private val apiService: ApiService) {
    suspend fun getActiveDispatches(authorization: String): DispatchListResult {
        return try {
            val response = apiService.getActiveDispatches(authorization)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) DispatchListResult.Success(body.dispatches)
                else DispatchListResult.Error("Error al cargar despachos")
            } else {
                DispatchListResult.Error("Error del servidor: ${response.code()}")
            }
        } catch (e: Exception) {
            DispatchListResult.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun getDispatchDetail(authorization: String, orderId: Int): DispatchDetailResult {
        return try {
            val response = apiService.getDispatchDetail(authorization, orderId)
            if (response.isSuccessful && response.body() != null) {
                DispatchDetailResult.Success(response.body()!!)
            } else {
                DispatchDetailResult.Error("Error al obtener detalle: ${response.code()}")
            }
        } catch (e: Exception) {
            DispatchDetailResult.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun markOrderReadyPickup(authorization: String, orderId: Int): SimpleResponse {
        return try {
            val response = apiService.markOrderReadyPickup(authorization, orderId)
            if (response.isSuccessful && response.body()?.success == true) {
                SimpleResponse.Success(response.body()?.message ?: "Estado actualizado")
            } else {
                SimpleResponse.Error("Error al actualizar: ${response.code()}")
            }
        } catch (e: Exception) {
            SimpleResponse.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun markOrderDispatched(authorization: String, orderId: Int): SimpleResponse {
        return try {
            val response = apiService.markOrderDispatched(authorization, orderId)
            if (response.isSuccessful && response.body()?.success == true) {
                SimpleResponse.Success(response.body()?.message ?: "Estado actualizado")
            } else {
                SimpleResponse.Error("Error al actualizar: ${response.code()}")
            }
        } catch (e: Exception) {
            SimpleResponse.Error("Error de conexión: ${e.message}")
        }
    }
}

sealed class SimpleResponse {
    data class Success(val message: String) : SimpleResponse()
    data class Error(val message: String) : SimpleResponse()
}
