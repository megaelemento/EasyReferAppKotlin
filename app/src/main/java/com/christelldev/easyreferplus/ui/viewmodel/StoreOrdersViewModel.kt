package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.StoreOrderSummary
import com.christelldev.easyreferplus.data.network.AppConfig
import com.christelldev.easyreferplus.data.network.OrderRepository
import com.christelldev.easyreferplus.data.network.SimpleWebSocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class StoreOrdersState {
    object Idle : StoreOrdersState()
    object Loading : StoreOrdersState()
    data class Success(val orders: List<StoreOrderSummary>, val total: Int) : StoreOrdersState()
    data class Error(val message: String) : StoreOrdersState()
}

class StoreOrdersViewModel(
    private val repository: OrderRepository,
    private val getToken: () -> String
) : ViewModel() {

    private val _state = MutableStateFlow<StoreOrdersState>(StoreOrdersState.Idle)
    val state: StateFlow<StoreOrdersState> = _state.asStateFlow()

    private val _wsConnected = MutableStateFlow(false)
    val wsConnected: StateFlow<Boolean> = _wsConnected.asStateFlow()

    private val _newOrderAlert = MutableStateFlow(false)
    val newOrderAlert: StateFlow<Boolean> = _newOrderAlert.asStateFlow()

    private var wsManager: SimpleWebSocketManager? = null

    fun load(statusFilter: String? = null) {
        viewModelScope.launch {
            _state.value = StoreOrdersState.Loading
            val token = "Bearer ${getToken()}"
            when (val r = repository.getMyReceivedOrders(token, status = statusFilter)) {
                is OrderRepository.Result.Success -> _state.value =
                    StoreOrdersState.Success(r.data.orders, r.data.total)
                is OrderRepository.Result.Error -> _state.value =
                    StoreOrdersState.Error(r.message)
            }
        }
    }

    fun clearNewOrderAlert() { _newOrderAlert.value = false }

    fun markOrderReady(orderId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val token = "Bearer ${getToken()}"
            when (val r = repository.markOrderReady(token, orderId)) {
                is OrderRepository.Result.Success -> onSuccess()
                is OrderRepository.Result.Error -> onError(r.message)
            }
        }
    }

    fun initWebSocketManager(context: android.content.Context) {
        if (wsManager == null) {
            wsManager = SimpleWebSocketManager(context, AppConfig.BASE_URL, getToken)
            viewModelScope.launch {
                wsManager?.companySalesData?.collectLatest { data ->
                    data?.let { handleNewSale(it) }
                }
            }
            viewModelScope.launch {
                wsManager?.connectionState?.collectLatest { state ->
                    _wsConnected.value = state is SimpleWebSocketManager.ConnectionState.Connected
                }
            }
        }
    }

    fun connectWebSocket() { wsManager?.subscribe(SimpleWebSocketManager.CHANNEL_COMPANY_SALES) }
    fun disconnectWebSocket() {
        wsManager?.unsubscribe(SimpleWebSocketManager.CHANNEL_COMPANY_SALES)
        _wsConnected.value = false
    }

    override fun onCleared() {
        super.onCleared()
        wsManager?.shutdown()
        wsManager = null
    }

    private fun handleNewSale(data: Map<String, Any>) {
        val orderId = (data["order_id"] as? Double)?.toInt() ?: return
        val newStatus = data["status"] as? String ?: return

        val current = _state.value
        if (current is StoreOrdersState.Success) {
            val exists = current.orders.any { it.id == orderId }
            if (exists) {
                _state.value = StoreOrdersState.Success(
                    current.orders.map { if (it.id == orderId) it.copy(status = newStatus) else it },
                    current.total
                )
            } else {
                // Pedido nuevo: recargar lista y alertar
                _newOrderAlert.value = true
                load()
            }
        } else {
            _newOrderAlert.value = true
            load()
        }
    }

    class Factory(
        private val repository: OrderRepository,
        private val getToken: () -> String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            StoreOrdersViewModel(repository, getToken) as T
    }
}
