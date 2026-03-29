package com.christelldev.easyreferplus.data.network

import com.christelldev.easyreferplus.data.model.*

class AddressRepository(private val apiService: ApiService) {

    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val message: String) : Result<Nothing>()
    }

    suspend fun getSavedAddresses(authorization: String): Result<List<SavedAddress>> {
        return try {
            val response = apiService.getSavedAddresses(authorization)
            if (response.isSuccessful) Result.Success(response.body() ?: emptyList())
            else Result.Error("Error al cargar direcciones (${response.code()})")
        } catch (e: Exception) {
            Result.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun createAddress(authorization: String, request: CreateAddressRequest): Result<SavedAddress> {
        return try {
            val response = apiService.createSavedAddress(authorization, request)
            if (response.isSuccessful) Result.Success(response.body()!!)
            else {
                val errorBody = response.errorBody()?.string() ?: ""
                val msg = Regex("\"detail\":\"([^\"]+)\"").find(errorBody)?.groupValues?.get(1)
                Result.Error(msg ?: "Error al guardar dirección (${response.code()})")
            }
        } catch (e: Exception) {
            Result.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun updateAddress(authorization: String, addressId: Int, request: UpdateAddressRequest): Result<SavedAddress> {
        return try {
            val response = apiService.updateSavedAddress(authorization, addressId, request)
            if (response.isSuccessful) Result.Success(response.body()!!)
            else Result.Error("Error al actualizar dirección (${response.code()})")
        } catch (e: Exception) {
            Result.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun deleteAddress(authorization: String, addressId: Int): Result<Boolean> {
        return try {
            val response = apiService.deleteSavedAddress(authorization, addressId)
            if (response.isSuccessful) Result.Success(true)
            else Result.Error("Error al eliminar dirección (${response.code()})")
        } catch (e: Exception) {
            Result.Error("Error de conexión: ${e.message}")
        }
    }
}
