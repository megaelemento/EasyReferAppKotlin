package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.BankAccountResponse
import com.christelldev.easyreferplus.data.model.WithdrawalRequestResponse
import com.christelldev.easyreferplus.data.network.WithdrawalRepository
import com.christelldev.easyreferplus.data.network.WithdrawalBalanceResult
import com.christelldev.easyreferplus.data.network.BankAccountsResult
import com.christelldev.easyreferplus.data.network.CreateBankAccountResult
import com.christelldev.easyreferplus.data.network.WithdrawalRequestResult
import com.christelldev.easyreferplus.data.network.WithdrawalHistoryResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WithdrawalUiState(
    // Balance
    val availableBalance: Double = 0.0,
    val totalEarned: Double = 0.0,
    val totalWithdrawn: Double = 0.0,
    val pendingWithdrawal: Double = 0.0,
    val minimumWithdrawalAmount: Double = 0.0,
    val canRequestWithdrawal: Boolean = false,

    // Cuentas bancarias
    val bankAccounts: List<BankAccountResponse> = emptyList(),
    val selectedAccountId: Int? = null,

    // Historial de retiros
    val withdrawalRequests: List<WithdrawalRequestResponse> = emptyList(),
    val totalRequests: Int = 0,
    val currentPage: Int = 1,
    val totalPages: Int = 1,

    // Estado de la solicitud
    val isLoading: Boolean = false,
    val isCreatingAccount: Boolean = false,
    val isRequestingWithdrawal: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class WithdrawalViewModel(
    private val repository: WithdrawalRepository,
    private val getAccessToken: () -> String
) : ViewModel() {

    private val _uiState = MutableStateFlow(WithdrawalUiState())
    val uiState: StateFlow<WithdrawalUiState> = _uiState.asStateFlow()

    private val authorization: String get() = "Bearer ${getAccessToken()}"

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Cargar balance
            when (val result = repository.getCommissionBalance(authorization)) {
                is WithdrawalBalanceResult.Success -> {
                    val balance = result.balance
                    _uiState.value = _uiState.value.copy(
                        availableBalance = balance.availableForWithdrawal,
                        totalEarned = balance.totalBalance,
                        totalWithdrawn = balance.totalWithdrawn,
                        pendingWithdrawal = balance.pendingWithdrawal,
                        minimumWithdrawalAmount = balance.minimumWithdrawalAmount,
                        canRequestWithdrawal = balance.canRequestWithdrawal
                    )
                }
                is WithdrawalBalanceResult.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
            }

            // Cargar cuentas bancarias
            when (val result = repository.getMyBankAccounts(authorization)) {
                is BankAccountsResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        bankAccounts = result.accounts,
                        selectedAccountId = result.defaultAccountId
                    )
                }
                is BankAccountsResult.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
            }

            // Cargar historial
            loadWithdrawalHistory()

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun loadWithdrawalHistory() {
        viewModelScope.launch {
            when (val result = repository.getMyWithdrawalRequests(authorization)) {
                is WithdrawalHistoryResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        withdrawalRequests = result.requests,
                        totalRequests = result.total,
                        currentPage = result.page,
                        totalPages = result.totalPages
                    )
                }
                is WithdrawalHistoryResult.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
            }
        }
    }

    fun selectAccount(accountId: Int) {
        _uiState.value = _uiState.value.copy(selectedAccountId = accountId)
    }

    fun createBankAccount(
        bankName: String,
        accountType: String,
        accountNumber: String,
        accountHolderName: String,
        accountHolderDocument: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingAccount = true, errorMessage = null, successMessage = null)

            when (val result = repository.createBankAccount(
                authorization = authorization,
                bankName = bankName,
                accountType = accountType,
                accountNumber = accountNumber,
                accountHolderName = accountHolderName,
                accountHolderDocument = accountHolderDocument
            )) {
                is CreateBankAccountResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isCreatingAccount = false,
                        successMessage = "Cuenta bancaria creada exitosamente"
                    )
                    // Recargar cuentas bancarias
                    loadData()
                }
                is CreateBankAccountResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isCreatingAccount = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun requestWithdrawal(amount: Double) {
        val accountId = _uiState.value.selectedAccountId
        if (accountId == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Selecciona una cuenta bancaria")
            return
        }

        if (amount < _uiState.value.minimumWithdrawalAmount) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "El monto mínimo de retiro es: \$${String.format("%.2f", _uiState.value.minimumWithdrawalAmount)}"
            )
            return
        }

        if (amount > _uiState.value.availableBalance) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "No tienes suficiente saldo disponible"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRequestingWithdrawal = true, errorMessage = null, successMessage = null)

            when (val result = repository.createWithdrawalRequest(
                authorization = authorization,
                amount = amount,
                bankAccountId = accountId
            )) {
                is WithdrawalRequestResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isRequestingWithdrawal = false,
                        successMessage = result.message
                    )
                    // Recargar datos
                    loadData()
                }
                is WithdrawalRequestResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isRequestingWithdrawal = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }

    // Factory para ViewModel
    class Factory(
        private val repository: WithdrawalRepository,
        private val getAccessToken: () -> String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WithdrawalViewModel::class.java)) {
                return WithdrawalViewModel(repository, getAccessToken) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
