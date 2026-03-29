package com.christelldev.easyreferplus.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.*
import com.christelldev.easyreferplus.data.network.ApiService
import com.christelldev.easyreferplus.data.network.AuthRepository
import com.christelldev.easyreferplus.data.network.DriverLiveSocket
import com.christelldev.easyreferplus.data.network.DriverRepository
import com.christelldev.easyreferplus.data.network.DriverResult
import com.christelldev.easyreferplus.data.network.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class DriverUiState {
    object Idle : DriverUiState()
    object Loading : DriverUiState()
    data class Error(val message: String) : DriverUiState()
}

class DriverViewModel(
    private val repository: DriverRepository,
    private val authRepo: AuthRepository? = null,
    private val prefs: SharedPreferences? = null,
) : ViewModel() {
    private val _profile = MutableStateFlow<DriverProfile?>(null)
    val profile: StateFlow<DriverProfile?> = _profile

    private val _availableOrders = MutableStateFlow<List<AvailableOrder>>(emptyList())
    val availableOrders: StateFlow<List<AvailableOrder>> = _availableOrders

    // IDs rechazados en esta sesión de turno — se excluyen del poll hasta que se reinicie
    private val rejectedOrderIds = mutableSetOf<Int>()

    private val _activeOrder = MutableStateFlow<ActiveDriverOrder?>(null)
    val activeOrder: StateFlow<ActiveDriverOrder?> = _activeOrder

    private val _invitations = MutableStateFlow<List<DriverInvitation>>(emptyList())
    val invitations: StateFlow<List<DriverInvitation>> = _invitations

    val pendingInvitationsCount: StateFlow<Int> = _invitations
        .map { list -> list.count { it.status == "pending" } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val _state = MutableStateFlow<DriverUiState>(DriverUiState.Idle)
    val state: StateFlow<DriverUiState> = _state

    private val _keepScreenOn = MutableStateFlow(prefs?.getBoolean("keepScreenOn", false) ?: false)
    val keepScreenOn: StateFlow<Boolean> = _keepScreenOn

    // Emite cada vez que llega un pedido nuevo para que la UI dispare la alerta de sonido
    private val _newOrderAlert = MutableStateFlow(0)
    val newOrderAlert: StateFlow<Int> = _newOrderAlert

    private var pollingJob: Job? = null

    fun setKeepScreenOn(enabled: Boolean) {
        _keepScreenOn.value = enabled
        prefs?.edit()?.putBoolean("keepScreenOn", enabled)?.apply()
    }

    private var liveSocket: DriverLiveSocket? = null

    fun loadAll() {
        loadProfile()
        loadAvailableOrders()
        loadActiveOrder()
        loadInvitations()
    }

    fun loadProfile() {
        viewModelScope.launch {
            when (val r = repository.getMyProfile()) {
                is DriverResult.Success -> _profile.value = r.data
                is DriverResult.Error -> Unit
            }
        }
    }

    fun loadAvailableOrders() {
        viewModelScope.launch {
            val prevIds = _availableOrders.value.map { it.id }.toSet()
            when (val r = repository.getAvailableOrders()) {
                is DriverResult.Success -> {
                    val filtered = r.data.filter { it.id !in rejectedOrderIds }
                    val newIds = filtered.map { it.id }.toSet()
                    val hasNew = (newIds - prevIds).isNotEmpty()
                    _availableOrders.value = filtered
                    if (hasNew && prevIds.isNotEmpty()) _newOrderAlert.value++
                }
                is DriverResult.Error -> Unit
            }
        }
    }

    fun rejectOrder(orderId: Int) {
        rejectedOrderIds.add(orderId)
        _availableOrders.value = _availableOrders.value.filter { it.id != orderId }
        viewModelScope.launch {
            repository.rejectOrder(orderId)
        }
    }

    fun startOrderPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(8_000)  // 8s < 10s timeout del servidor
                if (_profile.value?.isOnDuty == true) loadAvailableOrders()
            }
        }
    }

    fun stopOrderPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun loadActiveOrder() {
        viewModelScope.launch {
            when (val r = repository.getActiveOrder()) {
                is DriverResult.Success -> _activeOrder.value = r.data.activeOrder
                is DriverResult.Error -> Unit
            }
        }
    }

    fun loadInvitations() {
        viewModelScope.launch {
            _state.value = DriverUiState.Loading
            when (val r = repository.getMyInvitations()) {
                is DriverResult.Success -> _invitations.value = r.data
                is DriverResult.Error -> Unit
            }
            _state.value = DriverUiState.Idle
        }
    }

    fun toggleOnDuty(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _state.value = DriverUiState.Loading
            when (val r = repository.toggleOnDuty()) {
                is DriverResult.Success -> {
                    _profile.value = _profile.value?.copy(isOnDuty = r.data.is_on_duty)
                    liveSocket?.sendDutyChange(r.data.is_on_duty)
                    if (!r.data.is_on_duty) rejectedOrderIds.clear()
                    onResult(true, "")
                }
                is DriverResult.Error -> onResult(false, r.message)
            }
            _state.value = DriverUiState.Idle
        }
    }

    fun updateIdleLocation(lat: Double, lng: Double) {
        viewModelScope.launch {
            repository.updateMyLocation(lat, lng)
            liveSocket?.sendLocation(lat, lng)
        }
    }

    fun acceptOrder(id: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (val r = repository.acceptOrder(id)) {
                is DriverResult.Success -> onSuccess()
                is DriverResult.Error -> onError(r.message)
            }
        }
    }

    fun confirmPickup(id: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (val r = repository.confirmPickup(id)) {
                is DriverResult.Success -> onSuccess()
                is DriverResult.Error -> onError(r.message)
            }
        }
    }

    fun confirmDelivery(id: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (val r = repository.confirmDelivery(id)) {
                is DriverResult.Success -> onSuccess()
                is DriverResult.Error -> onError(r.message)
            }
        }
    }

    fun acceptInvitation(id: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (val r = repository.acceptInvitation(id)) {
                is DriverResult.Success -> onSuccess()
                is DriverResult.Error -> onError(r.message)
            }
        }
    }

    fun rejectInvitation(id: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (val r = repository.rejectInvitation(id)) {
                is DriverResult.Success -> onSuccess()
                is DriverResult.Error -> onError(r.message)
            }
        }
    }

    // ─── Phase 4: New driver features ─────────────────────────────────────

    private val _earningsToday = MutableStateFlow<DriverEarningsTodayResponse?>(null)
    val earningsToday: StateFlow<DriverEarningsTodayResponse?> = _earningsToday

    fun loadEarningsToday() {
        viewModelScope.launch {
            when (val r = repository.getEarningsToday()) {
                is DriverResult.Success -> _earningsToday.value = r.data
                is DriverResult.Error -> Unit
            }
        }
    }

    fun uploadDeliveryPhoto(orderId: Int, photoPart: okhttp3.MultipartBody.Part, onSuccess: (String?) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (val r = repository.uploadDeliveryPhoto(orderId, photoPart)) {
                is DriverResult.Success -> onSuccess(r.data.photoUrl)
                is DriverResult.Error -> onError(r.message)
            }
        }
    }

    fun arrivedAtPickup(orderId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (val r = repository.arrivedAtPickup(orderId)) {
                is DriverResult.Success -> onSuccess()
                is DriverResult.Error -> onError(r.message)
            }
        }
    }

    fun arrivedAtDropoff(orderId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (val r = repository.arrivedAtDropoff(orderId)) {
                is DriverResult.Success -> onSuccess()
                is DriverResult.Error -> onError(r.message)
            }
        }
    }

    fun startLiveSocket() {
        if (liveSocket != null) return
        val token = authRepo?.getAccessToken() ?: return
        liveSocket = DriverLiveSocket(token)
        liveSocket?.connect()
    }

    fun stopLiveSocket() {
        liveSocket?.disconnect()
        liveSocket = null
    }

    override fun onCleared() {
        super.onCleared()
        liveSocket?.disconnect()
        stopOrderPolling()
    }

    class Factory(
        private val repository: DriverRepository,
        private val authRepository: AuthRepository,
        private val sharedPrefs: SharedPreferences? = null,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DriverViewModel(repository, authRepository, sharedPrefs) as T

        companion object {
            fun fromContext(context: Context): Factory {
                val apiService = RetrofitClient.getInstance().create(ApiService::class.java)
                val sharedPrefs = context.getSharedPreferences("EasyReferPrefs", Context.MODE_PRIVATE)
                val authRepo = AuthRepository(apiService, sharedPrefs)
                val driverRepo = DriverRepository(apiService) { sharedPrefs.getString("access_token", "") ?: "" }
                return Factory(driverRepo, authRepo, sharedPrefs)
            }
        }
    }
}
