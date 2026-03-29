package com.christelldev.easyreferplus.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.AdminDriverLocation
import com.christelldev.easyreferplus.data.model.CompanyOrderSummary
import com.christelldev.easyreferplus.data.network.AdminDriverSocket
import com.christelldev.easyreferplus.data.network.ApiService
import com.christelldev.easyreferplus.data.network.DriverRepository
import com.christelldev.easyreferplus.data.network.DriverResult
import com.christelldev.easyreferplus.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminDeliveryViewModel(
    private val repository: DriverRepository
) : ViewModel() {
    private val _driverLocations = MutableStateFlow<List<AdminDriverLocation>>(emptyList())
    val driverLocations: StateFlow<List<AdminDriverLocation>> = _driverLocations

    private val _companyOrders = MutableStateFlow<List<CompanyOrderSummary>>(emptyList())
    val companyOrders: StateFlow<List<CompanyOrderSummary>> = _companyOrders

    private val _ordersLoading = MutableStateFlow(false)
    val ordersLoading: StateFlow<Boolean> = _ordersLoading

    private val _ordersTotal = MutableStateFlow(0)
    val ordersTotal: StateFlow<Int> = _ordersTotal

    private var socket: AdminDriverSocket? = null

    fun loadDriverLocations() {
        viewModelScope.launch {
            when (val r = repository.getAdminDriverLocations()) {
                is DriverResult.Success -> _driverLocations.value = r.data
                is DriverResult.Error -> Unit
            }
        }
    }

    fun loadCompanyOrders(page: Int = 1, status: String? = null) {
        viewModelScope.launch {
            _ordersLoading.value = true
            when (val r = repository.getCompanyOrders(page, status)) {
                is DriverResult.Success -> {
                    if (page == 1) _companyOrders.value = r.data.orders
                    else _companyOrders.value = _companyOrders.value + r.data.orders
                    _ordersTotal.value = r.data.total
                }
                is DriverResult.Error -> Unit
            }
            _ordersLoading.value = false
        }
    }

    fun startLiveDriverUpdates(token: String) {
        if (socket != null) return
        socket = AdminDriverSocket(token)
        socket?.connect()
        viewModelScope.launch {
            socket?.events?.collect { event ->
                when (event) {
                    is AdminDriverSocket.Event.LocationUpdate -> {
                        _driverLocations.value = _driverLocations.value.map { driver ->
                            if (driver.driverId == event.driverId)
                                driver.copy(lat = event.lat, lng = event.lng)
                            else driver
                        }
                    }
                    is AdminDriverSocket.Event.DutyChange -> {
                        val updated = _driverLocations.value.map { driver ->
                            if (driver.driverId == event.driverId)
                                driver.copy(isOnDuty = event.isOnDuty)
                            else driver
                        }
                        // Conductor no estaba en la lista y acaba de conectarse → recargar con datos completos
                        if (event.isOnDuty && updated.none { it.driverId == event.driverId }) {
                            loadDriverLocations()
                        } else {
                            _driverLocations.value = updated
                        }
                    }
                }
            }
        }
    }

    fun stopLiveDriverUpdates() {
        socket?.disconnect()
        socket = null
    }

    override fun onCleared() {
        super.onCleared()
        socket?.disconnect()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val apiService = RetrofitClient.getInstance().create(ApiService::class.java)
            val sharedPrefs = context.getSharedPreferences("EasyReferPrefs", Context.MODE_PRIVATE)
            val repo = DriverRepository(apiService) { sharedPrefs.getString("access_token", "") ?: "" }
            return AdminDeliveryViewModel(repo) as T
        }
    }
}
