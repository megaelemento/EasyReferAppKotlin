package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.CapturePayPalOrderResponse
import com.christelldev.easyreferplus.data.model.CreatePayPalOrderResponse
import com.christelldev.easyreferplus.data.network.PayPalRepository
import com.christelldev.easyreferplus.data.network.PayPalResult
import com.paypal.checkout.approve.OnApprove
import com.paypal.checkout.cancel.OnCancel
import com.paypal.checkout.createorder.CreateOrder
import com.paypal.checkout.error.OnError
import android.util.Log
import com.christelldev.easyreferplus.util.AppLockManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PayPalUiState {
    object Idle : PayPalUiState()
    object Loading : PayPalUiState()
    data class Success(val response: CapturePayPalOrderResponse) : PayPalUiState()
    data class Error(val message: String) : PayPalUiState()
    data class OrderCreated(val response: CreatePayPalOrderResponse) : PayPalUiState()
}

class PayPalViewModel(
    private val repository: PayPalRepository,
    private val getAccessToken: () -> String
) : ViewModel() {

    private val _uiState = MutableStateFlow<PayPalUiState>(PayPalUiState.Idle)
    val uiState: StateFlow<PayPalUiState> = _uiState.asStateFlow()

    private val authorization: String get() = "Bearer ${getAccessToken()}"

    /** Guardados para registrar el pago pendiente al crear la orden PayPal */
    private var pendingOrderId: Int? = null
    private var pendingNeedsDelivery: Boolean = false
    private var pendingCompanyId: Int? = null

    /**
     * Configura el contexto del pago para poder registrarlo como pendiente
     * y suprimir el lock screen mientras se está en PayPal.
     */
    fun setPaymentContext(orderId: Int?, needsDelivery: Boolean, companyId: Int?) {
        this.pendingOrderId = orderId
        this.pendingNeedsDelivery = needsDelivery
        this.pendingCompanyId = companyId
    }

    fun startPayPalCheckout(amount: Double, orderId: Int? = null, notes: String? = null) {
        viewModelScope.launch {
            Log.d("PayPalFlow", "startPayPalCheckout - amount: $amount, orderId: $orderId")
            _uiState.value = PayPalUiState.Loading
            when (val result = repository.createOrder(authorization, amount, orderId, notes)) {
                is PayPalResult.Success -> {
                    Log.d("PayPalFlow", "PayPal Order Pre-created: ${result.data.paypalOrderId}")
                    _uiState.value = PayPalUiState.OrderCreated(result.data)
                    // Registra el pago pendiente para suprimir el lock screen
                    val oid = orderId ?: pendingOrderId
                    if (oid != null) {
                        AppLockManager.startPayment(
                            AppLockManager.PendingPayment(
                                orderId = oid,
                                paypalOrderId = result.data.paypalOrderId,
                                needsDelivery = pendingNeedsDelivery,
                                companyId = pendingCompanyId,
                                amount = amount
                            )
                        )
                    }
                }
                is PayPalResult.Error -> {
                    Log.e("PayPalFlow", "Error pre-creating PayPal order: ${result.message}")
                    _uiState.value = PayPalUiState.Error(result.message)
                    AppLockManager.clearPaymentInProgress()
                }
            }
        }
    }

    fun captureOrder(paypalOrderId: String, notes: String? = null) {
        viewModelScope.launch {
            Log.d("PayPalFlow", "captureOrder - paypalOrderId: $paypalOrderId")
            _uiState.value = PayPalUiState.Loading
            when (val result = repository.captureOrder(authorization, paypalOrderId, notes)) {
                is PayPalResult.Success -> {
                    Log.d("PayPalFlow", "PayPal Capture Success: ${result.data.status}")
                    _uiState.value = PayPalUiState.Success(result.data)
                    AppLockManager.clearPaymentInProgress()
                }
                is PayPalResult.Error -> {
                    Log.e("PayPalFlow", "Error capturing PayPal order: ${result.message}")
                    _uiState.value = PayPalUiState.Error(result.message)
                    AppLockManager.clearPaymentInProgress()
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = PayPalUiState.Idle
    }

    // Callbacks para PayPal SDK
    val onCreateOrderCallback = CreateOrder { createOrderActions ->
        val currentState = _uiState.value
        if (currentState is PayPalUiState.OrderCreated) {
            createOrderActions.set(currentState.response.paypalOrderId)
        } else {
            // Si el estado no es OrderCreated, no podemos proceder con el SDK
            _uiState.value = PayPalUiState.Error("La orden de PayPal no ha sido pre-creada")
        }
    }

    val onApproveCallback = OnApprove { approval ->
        approval.data.orderId?.let { orderId ->
            captureOrder(orderId)
        } ?: run {
            _uiState.value = PayPalUiState.Error("No se recibió ID de orden de PayPal")
        }
    }

    val onCancelCallback = OnCancel {
        _uiState.value = PayPalUiState.Idle
        AppLockManager.clearPaymentInProgress()
    }

    val onErrorCallback = OnError { errorInfo ->
        _uiState.value = PayPalUiState.Error(errorInfo.reason)
        AppLockManager.clearPaymentInProgress()
    }

    class Factory(
        private val repository: PayPalRepository,
        private val getAccessToken: () -> String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PayPalViewModel::class.java)) {
                return PayPalViewModel(repository, getAccessToken) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
