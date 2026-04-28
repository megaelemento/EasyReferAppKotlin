package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.*
import com.christelldev.easyreferplus.data.network.AppConfig
import com.christelldev.easyreferplus.data.network.OrderRepository
import com.christelldev.easyreferplus.data.network.SimpleWebSocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class OrderListState {
    object Idle : OrderListState()
    object Loading : OrderListState()
    data class Success(val orders: List<OrderOut>) : OrderListState()
    data class Error(val message: String) : OrderListState()
}

sealed class CheckoutFlowState {
    object Idle : CheckoutFlowState()
    object LoadingOptions : CheckoutFlowState()
    data class OptionsLoaded(val options: List<DeliveryOption>) : CheckoutFlowState()
    object Processing : CheckoutFlowState()
    data class Success(val orderId: Int, val status: String, val total: Double, val companyId: Int? = null) : CheckoutFlowState()
    data class Error(val message: String) : CheckoutFlowState()
}

class OrderViewModel(
    private val repository: OrderRepository,
    private val getToken: () -> String
) : ViewModel() {

    private val _ordersState = MutableStateFlow<OrderListState>(OrderListState.Idle)
    val ordersState: StateFlow<OrderListState> = _ordersState.asStateFlow()

    private val _checkoutState = MutableStateFlow<CheckoutFlowState>(CheckoutFlowState.Idle)
    val checkoutState: StateFlow<CheckoutFlowState> = _checkoutState.asStateFlow()

    private val _wsConnected = MutableStateFlow(false)
    val wsConnected: StateFlow<Boolean> = _wsConnected.asStateFlow()

    // Mensajes informativos desde el servidor (ej. "seguimos buscando conductor")
    private val _wsMessage = MutableStateFlow<String?>(null)
    val wsMessage: StateFlow<String?> = _wsMessage.asStateFlow()

    private var wsManager: SimpleWebSocketManager? = null

    fun initWebSocketManager(context: android.content.Context) {
        if (wsManager == null) {
            wsManager = SimpleWebSocketManager(
                context = context,
                baseUrl = AppConfig.BASE_URL,
                getAccessToken = getToken
            )
            viewModelScope.launch {
                wsManager?.ordersData?.collectLatest { data ->
                    data?.let { updateFromWebSocket(it) }
                }
            }
            viewModelScope.launch {
                wsManager?.connectionState?.collectLatest { state ->
                    _wsConnected.value = state is SimpleWebSocketManager.ConnectionState.Connected
                }
            }
        }
    }

    fun connectWebSocket() {
        wsManager?.subscribe(SimpleWebSocketManager.CHANNEL_ORDERS)
    }

    fun disconnectWebSocket() {
        wsManager?.unsubscribe(SimpleWebSocketManager.CHANNEL_ORDERS)
        _wsConnected.value = false
    }

    fun reconnectIfNeeded() {
        val state = wsManager?.connectionState?.value
        if (state == SimpleWebSocketManager.ConnectionState.Disconnected ||
            state is SimpleWebSocketManager.ConnectionState.Error
        ) {
            wsManager?.subscribe(SimpleWebSocketManager.CHANNEL_ORDERS)
        }
    }

    override fun onCleared() {
        super.onCleared()
        wsManager?.shutdown()
        wsManager = null
    }

    private fun updateFromWebSocket(data: Map<String, Any>) {
        val orderId = (data["order_id"] as? Double)?.toInt() ?: return
        val newStatus = data["status"] as? String ?: return
        val current = _ordersState.value as? OrderListState.Success ?: return
        _ordersState.value = OrderListState.Success(
            current.orders.map { order ->
                if (order.id == orderId) order.copy(status = newStatus) else order
            }
        )
        // Surfacea mensaje informativo si el servidor lo envía (ej. conductor rechazó)
        val message = data["message"] as? String
        if (!message.isNullOrBlank()) _wsMessage.value = message
    }

    fun clearWsMessage() { _wsMessage.value = null }

    fun loadMyOrders() {
        viewModelScope.launch {
            _ordersState.value = OrderListState.Loading
            val token = "Bearer ${getToken()}"
            _ordersState.value = when (val r = repository.getMyOrders(token)) {
                is OrderRepository.Result.Success -> OrderListState.Success(r.data)
                is OrderRepository.Result.Error -> OrderListState.Error(r.message)
            }
        }
    }

    private val activeStatuses = setOf(
        "pending_payment", "paid_pending_driver", "driver_assigned",
        "picked_up",
    )

    fun hasActiveOrder(): Boolean {
        val state = _ordersState.value
        if (state is OrderListState.Success) {
            return state.orders.any { it.status in activeStatuses }
        }
        return false
    }

    fun getActiveOrderId(): Int? {
        val state = _ordersState.value
        if (state is OrderListState.Success) {
            return state.orders.firstOrNull { it.status in activeStatuses }?.id
        }
        return null
    }

    fun loadDeliveryOptions(destLat: Double, destLng: Double) {
        viewModelScope.launch {
            _checkoutState.value = CheckoutFlowState.LoadingOptions
            val token = "Bearer ${getToken()}"
            _checkoutState.value = when (val r = repository.getDeliveryOptions(token, destLat, destLng)) {
                is OrderRepository.Result.Success -> CheckoutFlowState.OptionsLoaded(r.data)
                is OrderRepository.Result.Error -> CheckoutFlowState.Error(r.message)
            }
        }
    }

    fun createOrderOnly(
        deliveryRequired: Boolean,
        deliveryCompanyId: Int?,
        dropoffAddress: String?,
        dropoffLat: Double?,
        dropoffLng: Double?,
        observations: String? = null,
        tipAmount: Double? = null
    ) {
        viewModelScope.launch {
            _checkoutState.value = CheckoutFlowState.Processing
            val token = "Bearer ${getToken()}"

            val createResult = repository.createOrder(
                token,
                OrderCreateRequest(
                    deliveryRequired = deliveryRequired,
                    deliveryCompanyId = deliveryCompanyId,
                    dropoffAddress = dropoffAddress,
                    dropoffLat = dropoffLat,
                    dropoffLng = dropoffLng,
                    observations = observations,
                    tipAmount = if (tipAmount != null && tipAmount > 0) tipAmount else null
                )
            )
            when (createResult) {
                is OrderRepository.Result.Success -> {
                    val orderId = createResult.data.id
                    val total = createResult.data.total
                    val companyId = createResult.data.companyId
                    _checkoutState.value = CheckoutFlowState.Success(orderId, "pending_payment", total, companyId)
                }
                is OrderRepository.Result.Error ->
                    _checkoutState.value = CheckoutFlowState.Error(createResult.message)
            }
        }
    }

    fun cancelOrder(orderId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val token = "Bearer ${getToken()}"
            when (val r = repository.cancelOrder(token, orderId)) {
                is OrderRepository.Result.Success -> {
                    val current = _ordersState.value as? OrderListState.Success
                    if (current != null) {
                        _ordersState.value = OrderListState.Success(
                            current.orders.map { if (it.id == orderId) it.copy(status = "cancelled") else it }
                        )
                    }
                    onSuccess()
                }
                is OrderRepository.Result.Error -> onError(r.message)
            }
        }
    }

    fun confirmReceipt(orderId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val token = "Bearer ${getToken()}"
            when (val r = repository.confirmReceipt(token, orderId)) {
                is OrderRepository.Result.Success -> {
                    // Actualizar status localmente
                    val current = _ordersState.value as? OrderListState.Success ?: return@launch
                    _ordersState.value = OrderListState.Success(
                        current.orders.map { if (it.id == orderId) it.copy(status = "completed") else it }
                    )
                    onSuccess()
                }
                is OrderRepository.Result.Error -> onError(r.message)
            }
        }
    }

    fun resetCheckout() {
        _checkoutState.value = CheckoutFlowState.Idle
    }

    class Factory(
        private val repository: OrderRepository,
        private val getToken: () -> String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            OrderViewModel(repository, getToken) as T
    }
}
