package com.christelldev.easyreferplus.data.network

import com.christelldev.easyreferplus.data.model.CapturePayPalOrderRequest
import com.christelldev.easyreferplus.data.model.CapturePayPalOrderResponse
import com.christelldev.easyreferplus.data.model.CreatePayPalOrderRequest
import com.christelldev.easyreferplus.data.model.CreatePayPalOrderResponse

sealed class PayPalResult<out T> {
    data class Success<out T>(val data: T) : PayPalResult<T>()
    data class Error(val message: String) : PayPalResult<Nothing>()
}

class PayPalRepository(private val apiService: ApiService) {

    suspend fun createOrder(authorization: String, amount: Double, orderId: Int? = null, notes: String? = null): PayPalResult<CreatePayPalOrderResponse> {
        return try {
            val response = apiService.createPayPalOrder(authorization, CreatePayPalOrderRequest(amount, orderId, notes))
            if (response.isSuccessful) {
                response.body()?.let {
                    PayPalResult.Success(it)
                } ?: PayPalResult.Error("Respuesta vacía del servidor")
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                val msg = Regex("\"(?:detail|message)\":\"([^\"]+)\"").find(errorBody)?.groupValues?.get(1)
                PayPalResult.Error(msg ?: "Error al crear orden PayPal (${response.code()})")
            }
        } catch (e: Exception) {
            PayPalResult.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun captureOrder(authorization: String, paypalOrderId: String, notes: String? = null): PayPalResult<CapturePayPalOrderResponse> {
        return try {
            val response = apiService.capturePayPalOrder(authorization, CapturePayPalOrderRequest(paypalOrderId, notes))
            if (response.isSuccessful) {
                response.body()?.let {
                    PayPalResult.Success(it)
                } ?: PayPalResult.Error("Respuesta vacía del servidor")
            } else {
                PayPalResult.Error("Error al capturar orden: ${response.code()}")
            }
        } catch (e: Exception) {
            PayPalResult.Error(e.message ?: "Error desconocido")
        }
    }

    class Factory {
        fun create(): PayPalRepository {
            return PayPalRepository(RetrofitClient.getInstance().create(ApiService::class.java))
        }
    }
}
