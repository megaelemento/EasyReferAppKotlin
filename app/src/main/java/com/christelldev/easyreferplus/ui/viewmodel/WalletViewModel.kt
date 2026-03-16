package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.TransferResponse
import com.christelldev.easyreferplus.data.model.WalletStatementItem
import com.christelldev.easyreferplus.data.model.WalletTransferNotification
import com.christelldev.easyreferplus.data.network.AppConfig
import com.christelldev.easyreferplus.data.network.WalletBalanceResult
import com.christelldev.easyreferplus.data.network.WalletPinResult
import com.christelldev.easyreferplus.data.network.WalletRepository
import com.christelldev.easyreferplus.data.network.WalletStatementResult
import com.christelldev.easyreferplus.data.network.WalletTransferResult
import com.christelldev.easyreferplus.data.network.WalletCheckRecipientResult
import com.christelldev.easyreferplus.data.network.WebSocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WalletUiState(
    val availableBalance: Double = 0.0,
    val totalBalance: Double = 0.0,
    // Formulario de transferencia
    val recipientPhone: String = "",
    val recipientName: String = "",
    val amount: String = "",
    val description: String = "",
    val pin: String = "",
    // Estado de cuenta
    val statementItems: List<WalletStatementItem> = emptyList(),
    val statementPage: Int = 1,
    val hasMoreItems: Boolean = false,
    // Estados de carga
    val isLoading: Boolean = false,
    val isTransferring: Boolean = false,
    val isLoadingStatement: Boolean = false,
    // PIN
    val hasPinSet: Boolean = false,
    val pinError: String? = null,
    // Transferencia
    val transferError: String? = null,
    val transferSuccess: TransferResponse? = null,
    // Mensajes generales
    val errorMessage: String? = null,
    val successMessage: String? = null,
    // Notificación WebSocket de transferencia entrante
    val incomingTransfer: WalletTransferNotification? = null,
    // Recipient check
    val isCheckingRecipient: Boolean = false,
    val recipientVerified: Boolean = false,
    val recipientNotRegistered: Boolean = false,
    val recipientCheckError: String? = null,
)

class WalletViewModel(
    private val repository: WalletRepository,
    private val getAccessToken: () -> String
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    // Preserve active date filter for pagination
    private var activeStartDate: String? = null
    private var activeEndDate: String? = null

    // WebSocket propio del wallet
    private var wsManager: WebSocketManager? = null

    fun connectWebSocket() {
        // Si ya hay un wsManager activo (conectado o conectando), no hacer nada
        if (wsManager != null && (wsManager!!.isConnected() || wsManager!!.isConnecting())) return
        // Si existe pero está muerto, limpiar
        wsManager?.disconnect()
        wsManager = WebSocketManager(AppConfig.WS_URL, getAccessToken)
        wsManager!!.connect()
        viewModelScope.launch {
            wsManager!!.walletTransferFlow.collect { notification ->
                onWalletNotificationReceived(notification)
                loadBalance()
                loadStatement(refresh = true)
            }
        }
    }

    fun disconnectWebSocket() {
        wsManager?.disconnect()
        wsManager = null
    }

    override fun onCleared() {
        super.onCleared()
        wsManager?.disconnect()
    }

    // --------------------------------------------------
    // Carga de saldo
    // --------------------------------------------------

    fun loadBalance() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.getBalance()) {
                is WalletBalanceResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        availableBalance = result.balance.availableBalance,
                        totalBalance = result.balance.totalBalance
                    )
                }
                is WalletBalanceResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    // --------------------------------------------------
    // Gestión de PIN
    // --------------------------------------------------

    fun setPin(pin: String) {
        if (pin.isBlank()) {
            _uiState.value = _uiState.value.copy(pinError = "El PIN no puede estar vacío")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, pinError = null)
            when (val result = repository.setPin(pin)) {
                is WalletPinResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        hasPinSet = true,
                        successMessage = "PIN establecido correctamente"
                    )
                }
                is WalletPinResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pinError = result.message
                    )
                }
            }
        }
    }

    fun changePin(currentPin: String, newPin: String) {
        if (currentPin.isBlank()) {
            _uiState.value = _uiState.value.copy(pinError = "Ingresa tu PIN actual")
            return
        }
        if (newPin.isBlank()) {
            _uiState.value = _uiState.value.copy(pinError = "El nuevo PIN no puede estar vacío")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, pinError = null)
            when (val result = repository.changePin(currentPin, newPin)) {
                is WalletPinResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "PIN actualizado correctamente"
                    )
                }
                is WalletPinResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pinError = result.message
                    )
                }
            }
        }
    }

    // --------------------------------------------------
    // Campos del formulario de transferencia
    // --------------------------------------------------

    fun updateRecipientPhone(phone: String) {
        _uiState.value = _uiState.value.copy(
            recipientPhone = phone,
            recipientName = "",
            transferError = null
        )
    }

    fun updateAmount(amount: String) {
        _uiState.value = _uiState.value.copy(
            amount = amount,
            transferError = null
        )
    }

    fun updateDescription(desc: String) {
        _uiState.value = _uiState.value.copy(description = desc)
    }

    fun updatePin(pin: String) {
        _uiState.value = _uiState.value.copy(
            pin = pin,
            transferError = null
        )
    }

    // --------------------------------------------------
    // Ejecutar transferencia
    // --------------------------------------------------

    fun executeTransfer() {
        val state = _uiState.value

        val recipientPhone = state.recipientPhone.trim()
        val amountText = state.amount.trim()
        val pin = state.pin.trim()

        if (recipientPhone.isBlank()) {
            _uiState.value = _uiState.value.copy(
                transferError = "Ingresa el número de teléfono del destinatario"
            )
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            _uiState.value = _uiState.value.copy(
                transferError = "Ingresa un monto válido"
            )
            return
        }

        if (amount > state.availableBalance) {
            _uiState.value = _uiState.value.copy(
                transferError = "Saldo insuficiente. Tu saldo disponible es \$${String.format("%.2f", state.availableBalance)}"
            )
            return
        }

        if (pin.isBlank()) {
            _uiState.value = _uiState.value.copy(
                transferError = "Ingresa tu PIN para confirmar la transferencia"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isTransferring = true,
                transferError = null,
                transferSuccess = null
            )
            when (val result = repository.transfer(
                recipientPhone = recipientPhone,
                amount = amount,
                description = state.description.trim().ifBlank { null },
                pin = pin
            )) {
                is WalletTransferResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isTransferring = false,
                        transferSuccess = result.transfer,
                        availableBalance = result.transfer.senderBalanceAfter,
                        recipientPhone = "",
                        amount = "",
                        description = "",
                        pin = ""
                    )
                    // Recargar saldo y estado de cuenta desde el servidor
                    loadBalance()
                    loadStatement(refresh = true)
                }
                is WalletTransferResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isTransferring = false,
                        transferError = result.message
                    )
                }
            }
        }
    }

    fun executeTransferDirect(pin: String) {
        val state = _uiState.value
        val recipientPhone = state.recipientPhone.trim()
        val amount = state.amount.trim().toDoubleOrNull()

        if (recipientPhone.isBlank() || amount == null || amount <= 0.0) {
            _uiState.value = _uiState.value.copy(transferError = "Datos de transferencia inválidos")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isTransferring = true,
                transferError = null,
                transferSuccess = null
            )
            when (val result = repository.transfer(
                recipientPhone = recipientPhone,
                amount = amount,
                description = state.description.trim().ifBlank { null },
                pin = pin
            )) {
                is WalletTransferResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isTransferring = false,
                        transferSuccess = result.transfer,
                        availableBalance = result.transfer.senderBalanceAfter,
                        recipientPhone = "",
                        amount = "",
                        description = "",
                        pin = ""
                    )
                    // Recargar saldo y estado de cuenta desde el servidor
                    loadBalance()
                    loadStatement(refresh = true)
                }
                is WalletTransferResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isTransferring = false,
                        transferError = result.message
                    )
                }
            }
        }
    }

    // --------------------------------------------------
    // Estado de cuenta (historial de movimientos)
    // --------------------------------------------------

    fun loadStatement(refresh: Boolean = false, startDate: String? = null, endDate: String? = null) {
        if (_uiState.value.isLoadingStatement) return

        if (refresh) {
            activeStartDate = startDate
            activeEndDate = endDate
        }

        val page = if (refresh) 1 else _uiState.value.statementPage

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingStatement = true, errorMessage = null)
            when (val result = repository.getStatement(page = page, startDate = activeStartDate, endDate = activeEndDate)) {
                is WalletStatementResult.Success -> {
                    val newItems = if (refresh) {
                        result.statement.items
                    } else {
                        _uiState.value.statementItems + result.statement.items
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoadingStatement = false,
                        statementItems = newItems,
                        statementPage = result.statement.page,
                        hasMoreItems = result.statement.hasMore
                    )
                }
                is WalletStatementResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingStatement = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun loadMoreStatement() {
        if (!_uiState.value.hasMoreItems || _uiState.value.isLoadingStatement) return

        val nextPage = _uiState.value.statementPage + 1
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingStatement = true)
            when (val result = repository.getStatement(page = nextPage, startDate = activeStartDate, endDate = activeEndDate)) {
                is WalletStatementResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingStatement = false,
                        statementItems = _uiState.value.statementItems + result.statement.items,
                        statementPage = result.statement.page,
                        hasMoreItems = result.statement.hasMore
                    )
                }
                is WalletStatementResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingStatement = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    // --------------------------------------------------
    // Limpieza de estado
    // --------------------------------------------------

    fun clearTransferSuccess() {
        _uiState.value = _uiState.value.copy(transferSuccess = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            transferError = null,
            pinError = null
        )
    }

    // --------------------------------------------------
    // WebSocket: notificación de transferencia entrante
    // --------------------------------------------------

    fun onWalletNotificationReceived(notification: WalletTransferNotification) {
        _uiState.value = _uiState.value.copy(
            incomingTransfer = notification,
            availableBalance = notification.newBalance
        )
    }

    fun checkAndSetRecipient(phone: String) {
        _uiState.value = _uiState.value.copy(
            isCheckingRecipient = true,
            recipientVerified = false,
            recipientNotRegistered = false,
            recipientCheckError = null,
            transferError = null
        )
        viewModelScope.launch {
            when (val result = repository.checkRecipient(phone)) {
                is WalletCheckRecipientResult.Success -> {
                    if (result.registered) {
                        _uiState.value = _uiState.value.copy(
                            isCheckingRecipient = false,
                            recipientPhone = phone,
                            recipientName = result.recipientName ?: "",
                            recipientVerified = true
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isCheckingRecipient = false,
                            recipientNotRegistered = true
                        )
                    }
                }
                is WalletCheckRecipientResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isCheckingRecipient = false,
                        recipientCheckError = result.message
                    )
                }
            }
        }
    }

    fun dismissRecipientNotRegistered() {
        _uiState.value = _uiState.value.copy(recipientNotRegistered = false)
    }

    fun clearRecipientVerified() {
        _uiState.value = _uiState.value.copy(recipientVerified = false)
    }

    class Factory(
        private val repository: WalletRepository,
        private val getAccessToken: () -> String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WalletViewModel::class.java)) {
                return WalletViewModel(repository, getAccessToken) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
