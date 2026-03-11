package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.TransactionReceipt
import com.christelldev.easyreferplus.data.network.QRRepository
import com.christelldev.easyreferplus.data.network.TransactionDetailResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TransactionDetailUiState(
    val receipt: TransactionReceipt? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class TransactionDetailViewModel(
    private val qrRepository: QRRepository,
    private val getAccessToken: () -> String
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionDetailUiState())
    val uiState: StateFlow<TransactionDetailUiState> = _uiState.asStateFlow()

    private val authorization: String get() = "Bearer ${getAccessToken()}"

    fun loadTransactionDetail(transactionId: String) {
        val token = getAccessToken()
        if (token.isBlank()) {
            _uiState.value = TransactionDetailUiState(
                isLoading = false,
                errorMessage = "Sesión no disponible"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = TransactionDetailUiState(isLoading = true)

            when (val result = qrRepository.getTransactionDetail(authorization, transactionId)) {
                is TransactionDetailResult.Success -> {
                    _uiState.value = TransactionDetailUiState(
                        receipt = result.receipt,
                        isLoading = false
                    )
                }
                is TransactionDetailResult.Error -> {
                    _uiState.value = TransactionDetailUiState(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    class Factory(
        private val qrRepository: QRRepository,
        private val getAccessToken: () -> String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TransactionDetailViewModel::class.java)) {
                return TransactionDetailViewModel(qrRepository, getAccessToken) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
