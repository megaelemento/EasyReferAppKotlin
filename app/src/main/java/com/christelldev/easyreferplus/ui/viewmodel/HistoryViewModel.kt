package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.ProcessReceiptResponse
import com.christelldev.easyreferplus.data.model.TransactionHistoryItem
import com.christelldev.easyreferplus.data.network.ApiService
import com.christelldev.easyreferplus.data.network.HistoryResult
import com.christelldev.easyreferplus.data.network.QRRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    val transactions: List<TransactionHistoryItem> = emptyList(),
    val currentTab: HistoryTab = HistoryTab.ALL,
    val totalSales: Int = 0,
    val totalPurchases: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

enum class HistoryTab {
    ALL, SALES, PURCHASES
}

class HistoryViewModel(
    private val qrRepository: QRRepository,
    private val getAccessToken: () -> String
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val authorization: String get() = "Bearer ${getAccessToken()}"

    init {
        // No cargar aquí, esperar a que la pantalla esté lista
    }

    fun initialize() {
        loadHistory()
    }

    fun loadHistory() {
        // Verificar que el token esté disponible
        val token = getAccessToken()
        if (token.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Sesión no disponible"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val type = when (_uiState.value.currentTab) {
                HistoryTab.ALL -> null
                HistoryTab.SALES -> "sales"
                HistoryTab.PURCHASES -> "purchases"
            }

            when (val result = qrRepository.getHistory(authorization, type)) {
                is HistoryResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        transactions = result.data,
                        totalSales = result.stats?.totalSales ?: 0,
                        totalPurchases = result.stats?.totalPurchases ?: 0,
                        isLoading = false
                    )
                }
                is HistoryResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun setTab(tab: HistoryTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
        loadHistory()
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
            if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                return HistoryViewModel(qrRepository, getAccessToken) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
