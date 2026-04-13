package com.christelldev.easyreferplus.data.network

import com.christelldev.easyreferplus.data.model.*

class OrderRepository(private val apiService: ApiService) {

    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val message: String) : Result<Nothing>()
    }

    suspend fun getMyOrders(authorization: String): Result<List<OrderOut>> {
        return try {
            val response = apiService.getMyOrders(authorization)
            if (response.isSuccessful) Result.Success(response.body() ?: emptyList())
            else Result.Error("Error ${response.code()}: ${response.message()}")
        } catch (e: Exception) {
            Result.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun getDeliveryOptions(authorization: String, destLat: Double, destLng: Double): Result<List<DeliveryOption>> {
        return try {
            val response = apiService.getDeliveryOptions(authorization, destLat, destLng)
            if (response.isSuccessful) Result.Success(response.body()?.options ?: emptyList())
            else Result.Error("Error al cargar empresas de delivery")
        } catch (e: Exception) {
            Result.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun createOrder(authorization: String, request: OrderCreateRequest): Result<OrderCreateResponse> {
        return try {
            val response = apiService.createOrder(authorization, request)
            if (response.isSuccessful) Result.Success(response.body()!!)
            else {
                val errorBody = response.errorBody()?.string() ?: ""
                val msg = Regex("\"detail\":\"([^\"]+)\"").find(errorBody)?.groupValues?.get(1)
                Result.Error(msg ?: "Error al crear la orden (${response.code()})")
            }
        } catch (e: Exception) {
            Result.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun simulatePayment(authorization: String, orderId: Int): Result<SimulatePaymentResponse> {
        return try {
            val response = apiService.simulatePayment(authorization, orderId)
            if (response.isSuccessful) Result.Success(response.body()!!)
            else Result.Error("Error al procesar el pago (${response.code()})")
        } catch (e: Exception) {
            Result.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun getMyReceivedOrders(authorization: String, page: Int = 1, status: String? = null): Result<StoreOrdersResponse> {
        return try {
            val response = apiService.getMyReceivedOrders(authorization, page = page, status = status)
            if (response.isSuccessful) Result.Success(response.body()!!)
            else Result.Error("Error ${response.code()}: ${response.message()}")
        } catch (e: Exception) {
            Result.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun cancelOrder(authorization: String, orderId: Int): Result<SimpleSuccessResponse> {
        return try {
            val response = apiService.cancelOrder(authorization, orderId)
            if (response.isSuccessful) Result.Success(response.body()!!)
            else Result.Error("Error ${response.code()}: ${response.message()}")
        } catch (e: Exception) {
            Result.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun acceptStoreOrder(authorization: String, orderId: Int): Result<SimpleSuccessResponse> {
        return try {
            val response = apiService.storeAcceptOrder(authorization, orderId)
            if (response.isSuccessful) Result.Success(response.body()!!)
            else Result.Error("Error ${response.code()}: ${response.message()}")
        } catch (e: Exception) {
            Result.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun markOrderReady(authorization: String, orderId: Int): Result<SimpleSuccessResponse> {
        return try {
            val response = apiService.storeMarkOrderReady(authorization, orderId)
            if (response.isSuccessful) Result.Success(response.body()!!)
            else Result.Error("Error ${response.code()}: ${response.message()}")
        } catch (e: Exception) {
            Result.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun confirmReceipt(authorization: String, orderId: Int): Result<SimpleSuccessResponse> {
        return try {
            val response = apiService.buyerConfirmReceipt(authorization, orderId)
            if (response.isSuccessful) Result.Success(response.body()!!)
            else Result.Error("Error ${response.code()}: ${response.message()}")
        } catch (e: Exception) {
            Result.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun getOrderDetail(authorization: String, orderId: Int): Result<OrderOut> {
        return try {
            val response = apiService.getOrderDetail(authorization, orderId)
            if (response.isSuccessful) Result.Success(response.body()!!)
            else Result.Error("Orden no encontrada")
        } catch (e: Exception) {
            Result.Error("Error de conexión: ${e.message}")
        }
    }

    class Factory {
        fun create(): OrderRepository {
            val retrofit = RetrofitClient.getInstance()
            val apiService = retrofit.create(ApiService::class.java)
            return OrderRepository(apiService)
        }
    }
}
