package com.christelldev.easyreferplus.data.network

import com.christelldev.easyreferplus.data.model.*
import okhttp3.MultipartBody

sealed class DriverResult<out T> {
    data class Success<T>(val data: T) : DriverResult<T>()
    data class Error(val message: String) : DriverResult<Nothing>()
}

class DriverRepository(
    private val apiService: ApiService,
    private val tokenProvider: () -> String
) {
    private fun auth() = "Bearer ${tokenProvider()}"

    suspend fun getMyProfile(): DriverResult<DriverProfile> = try {
        val r = apiService.getDriverProfile(auth())
        if (r.isSuccessful) DriverResult.Success(r.body()!!)
        else DriverResult.Error("Error ${r.code()}: ${r.message()}")
    } catch (e: Exception) { DriverResult.Error(e.message ?: "Error de red") }

    suspend fun getAvailableOrders(): DriverResult<List<AvailableOrder>> = try {
        val r = apiService.getAvailableOrders(auth())
        if (r.isSuccessful) DriverResult.Success(r.body() ?: emptyList())
        else DriverResult.Error("Error ${r.code()}")
    } catch (e: Exception) { DriverResult.Error(e.message ?: "Error de red") }

    suspend fun getActiveOrder(): DriverResult<ActiveOrderResponse> = try {
        val r = apiService.getActiveOrder(auth())
        if (r.isSuccessful) DriverResult.Success(r.body() ?: ActiveOrderResponse(null))
        else DriverResult.Error("Error ${r.code()}")
    } catch (e: Exception) { DriverResult.Error(e.message ?: "Error de red") }

    suspend fun toggleOnDuty(): DriverResult<OnDutyToggleResponse> = try {
        val r = apiService.toggleDriverDuty(auth())
        if (r.isSuccessful) DriverResult.Success(r.body()!!)
        else DriverResult.Error("Error ${r.code()}: ${r.message()}")
    } catch (e: Exception) { DriverResult.Error(e.message ?: "Error de red") }

    suspend fun updateMyLocation(lat: Double, lng: Double): DriverResult<DriverActionResponse> = try {
        val r = apiService.updateDriverLocation(auth(), LocationUpdate(lat, lng))
        if (r.isSuccessful) DriverResult.Success(r.body()!!)
        else DriverResult.Error("Error ${r.code()}")
    } catch (e: Exception) { DriverResult.Error(e.message ?: "Error de red") }

    suspend fun acceptOrder(id: Int): DriverResult<DriverActionResponse> = try {
        val r = apiService.acceptDeliveryOrder(auth(), id)
        if (r.isSuccessful) DriverResult.Success(r.body()!!)
        else DriverResult.Error("Error ${r.code()}: ${r.message()}")
    } catch (e: Exception) { DriverResult.Error(e.message ?: "Error de red") }

    suspend fun rejectOrder(id: Int): DriverResult<DriverActionResponse> = try {
        val r = apiService.rejectOrder(auth(), id)
        if (r.isSuccessful) DriverResult.Success(r.body()!!)
        else DriverResult.Error("Error ${r.code()}: ${r.message()}")
    } catch (e: Exception) { DriverResult.Error(e.message ?: "Error de red") }

    suspend fun confirmPickup(id: Int): DriverResult<DriverActionResponse> = try {
        val r = apiService.confirmOrderPickup(auth(), id)
        if (r.isSuccessful) DriverResult.Success(r.body()!!)
        else DriverResult.Error("Error ${r.code()}: ${r.message()}")
    } catch (e: Exception) { DriverResult.Error(e.message ?: "Error de red") }

    suspend fun confirmDelivery(id: Int): DriverResult<DriverActionResponse> = try {
        val r = apiService.confirmOrderDelivery(auth(), id)
        if (r.isSuccessful) DriverResult.Success(r.body()!!)
        else DriverResult.Error("Error ${r.code()}: ${r.message()}")
    } catch (e: Exception) { DriverResult.Error(e.message ?: "Error de red") }

    suspend fun getMyInvitations(): DriverResult<List<DriverInvitation>> = try {
        val r = apiService.getDriverInvitations(auth())
        if (r.isSuccessful) DriverResult.Success(r.body() ?: emptyList())
        else DriverResult.Error("Error ${r.code()}")
    } catch (e: Exception) { DriverResult.Error(e.message ?: "Error de red") }

    suspend fun acceptInvitation(id: Int): DriverResult<DriverActionResponse> = try {
        val r = apiService.acceptDriverInvitation(auth(), id)
        if (r.isSuccessful) DriverResult.Success(r.body()!!)
        else DriverResult.Error("Error ${r.code()}: ${r.message()}")
    } catch (e: Exception) { DriverResult.Error(e.message ?: "Error de red") }

    suspend fun rejectInvitation(id: Int): DriverResult<DriverActionResponse> = try {
        val r = apiService.rejectDriverInvitation(auth(), id)
        if (r.isSuccessful) DriverResult.Success(r.body()!!)
        else DriverResult.Error("Error ${r.code()}: ${r.message()}")
    } catch (e: Exception) { DriverResult.Error(e.message ?: "Error de red") }

    suspend fun getAdminDriverLocations(): DriverResult<List<com.christelldev.easyreferplus.data.model.AdminDriverLocation>> = try {
        val r = apiService.getAdminDriverLocations(auth())
        if (r.isSuccessful) DriverResult.Success(r.body() ?: emptyList())
        else DriverResult.Error("Error ${r.code()}")
    } catch (e: Exception) { DriverResult.Error(e.message ?: "Error de red") }

    suspend fun getCompanyOrders(page: Int = 1, status: String? = null): DriverResult<com.christelldev.easyreferplus.data.model.CompanyOrdersResponse> = try {
        val r = apiService.getCompanyOrders(auth(), page = page, status = status)
        if (r.isSuccessful) DriverResult.Success(r.body()!!)
        else DriverResult.Error("Error ${r.code()}: ${r.message()}")
    } catch (e: Exception) { DriverResult.Error(e.message ?: "Error de red") }

    suspend fun uploadDeliveryPhoto(orderId: Int, photoPart: MultipartBody.Part): DriverResult<DeliveryPhotoResponse> = try {
        val r = apiService.uploadDeliveryPhoto(auth(), orderId, photoPart)
        if (r.isSuccessful) DriverResult.Success(r.body()!!)
        else DriverResult.Error("Error ${r.code()}: ${r.message()}")
    } catch (e: Exception) { DriverResult.Error(e.message ?: "Error de red") }

    suspend fun arrivedAtPickup(orderId: Int): DriverResult<DriverActionResponse> = try {
        val r = apiService.driverArrivedAtPickup(auth(), orderId)
        if (r.isSuccessful) DriverResult.Success(r.body()!!)
        else DriverResult.Error("Error ${r.code()}: ${r.message()}")
    } catch (e: Exception) { DriverResult.Error(e.message ?: "Error de red") }

    suspend fun arrivedAtDropoff(orderId: Int): DriverResult<DriverActionResponse> = try {
        val r = apiService.driverArrivedAtDropoff(auth(), orderId)
        if (r.isSuccessful) DriverResult.Success(r.body()!!)
        else DriverResult.Error("Error ${r.code()}: ${r.message()}")
    } catch (e: Exception) { DriverResult.Error(e.message ?: "Error de red") }

    suspend fun getEarningsToday(): DriverResult<DriverEarningsTodayResponse> = try {
        val r = apiService.getDriverEarningsToday(auth())
        if (r.isSuccessful) DriverResult.Success(r.body()!!)
        else DriverResult.Error("Error ${r.code()}: ${r.message()}")
    } catch (e: Exception) { DriverResult.Error(e.message ?: "Error de red") }
}
