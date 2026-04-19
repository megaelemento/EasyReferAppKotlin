package com.christelldev.easyreferplus.data.network

import com.christelldev.easyreferplus.data.model.SaveLocationRequest
import com.christelldev.easyreferplus.data.model.UpdateLocationRequest
import com.christelldev.easyreferplus.data.model.UserSavedLocation

class DeliveryLocationRepository(private val apiService: ApiService) {

    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val message: String) : Result<Nothing>()
    }

    suspend fun getLocations(authorization: String): Result<List<UserSavedLocation>> {
        return try {
            val response = apiService.getDeliveryLocations(authorization)
            if (response.isSuccessful) Result.Success(response.body() ?: emptyList())
            else Result.Error("Error al cargar ubicaciones (${response.code()})")
        } catch (e: Exception) {
            Result.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun saveLocation(authorization: String, request: SaveLocationRequest): Result<UserSavedLocation> {
        return try {
            val response = apiService.saveDeliveryLocation(authorization, request)
            if (response.isSuccessful) Result.Success(response.body()!!)
            else Result.Error("Error al guardar ubicación (${response.code()})")
        } catch (e: Exception) {
            Result.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun toggleFavorite(authorization: String, id: Int, isFavorite: Boolean): Result<UserSavedLocation> {
        return try {
            val response = apiService.updateDeliveryLocation(authorization, id, UpdateLocationRequest(isFavorite = isFavorite))
            if (response.isSuccessful) Result.Success(response.body()!!)
            else Result.Error("Error al actualizar (${response.code()})")
        } catch (e: Exception) {
            Result.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun deleteLocation(authorization: String, id: Int): Result<Boolean> {
        return try {
            val response = apiService.deleteDeliveryLocation(authorization, id)
            if (response.isSuccessful) Result.Success(true)
            else Result.Error("Error al eliminar (${response.code()})")
        } catch (e: Exception) {
            Result.Error("Error de conexión: ${e.message}")
        }
    }
}
