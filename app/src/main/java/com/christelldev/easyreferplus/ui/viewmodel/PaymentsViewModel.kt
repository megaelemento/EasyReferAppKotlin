package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.CompanyPayment
import com.christelldev.easyreferplus.data.network.CompanyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PaymentsUiState(
    val companyName: String = "",
    val companyId: Int = 0,
    val pendingAmount: Double = 0.0,
    val payments: List<CompanyPayment> = emptyList(),
    val isLoading: Boolean = false,
    val isRegistering: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class PaymentsViewModel(
    private val repository: CompanyRepository,
    private val getAccessToken: () -> String
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentsUiState())
    val uiState: StateFlow<PaymentsUiState> = _uiState.asStateFlow()

    private val authorization: String get() = "Bearer ${getAccessToken()}"

    fun initialize(companyId: Int, companyName: String, pendingAmount: Double) {
        _uiState.value = _uiState.value.copy(
            companyId = companyId,
            companyName = companyName,
            pendingAmount = pendingAmount
        )
        loadPayments()
    }

    fun loadPayments() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val companyId = _uiState.value.companyId
            if (companyId == 0) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No hay empresa seleccionada"
                )
                return@launch
            }

            when (val result = repository.getPaymentHistory(authorization)) {
                is CompanyRepository.PaymentResult.HistorySuccess -> {
                    _uiState.value = _uiState.value.copy(
                        payments = result.payments,
                        pendingAmount = result.pendingCommissionsAmount ?: 0.0,
                        companyName = result.companyName ?: "",
                        isLoading = false
                    )
                }
                is CompanyRepository.PaymentResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error desconocido"
                    )
                }
            }
        }
    }

    fun registerPayment(
        documentNumber: String,
        bankName: String,
        amount: Double,
        notes: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRegistering = true, errorMessage = null)

            when (val result = repository.registerPayment(
                authorization = authorization,
                documentNumber = documentNumber,
                bankName = bankName,
                amount = amount,
                notes = notes
            )) {
                is CompanyRepository.PaymentResult.RegisterSuccess -> {
                    _uiState.value = _uiState.value.copy(
                        isRegistering = false,
                        successMessage = result.message
                    )
                    // Recargar pagos
                    loadPayments()
                }
                is CompanyRepository.PaymentResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isRegistering = false,
                        errorMessage = result.message
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        isRegistering = false,
                        errorMessage = "Error desconocido"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    class Factory(
        private val repository: CompanyRepository,
        private val getAccessToken: () -> String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PaymentsViewModel(repository, getAccessToken) as T
        }
    }
}
