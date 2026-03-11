package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.AdminWithdrawalActionRequest
import com.christelldev.easyreferplus.data.model.AdminWithdrawalResponse
import com.christelldev.easyreferplus.data.model.AdminWithdrawalsListResponse
import com.christelldev.easyreferplus.data.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminWithdrawalsUiState(
    val withdrawals: List<AdminWithdrawalResponse> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val selectedFilter: String? = null, // "pending", "approved", "rejected", "postponed"
    val total: Int = 0,
    val pendingCount: Int = 0,
    val approvedCount: Int = 0,
    val rejectedCount: Int = 0,
    val postponedCount: Int = 0,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val isProcessingAction: Boolean = false
)

class AdminWithdrawalsViewModel(
    private val apiService: ApiService,
    private val getAccessToken: () -> String
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminWithdrawalsUiState())
    val uiState: StateFlow<AdminWithdrawalsUiState> = _uiState.asStateFlow()

    private val authorization: String get() = "Bearer ${getAccessToken()}"

    // Eliminado init que cargaba automáticamente - ahora se carga solo cuando se necesita

    fun loadWithdrawals(page: Int = 1, status: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val response = apiService.getAdminWithdrawals(
                    authorization = authorization,
                    status = status,
                    page = page
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        _uiState.value = _uiState.value.copy(
                            withdrawals = body.withdrawals,
                            total = body.total,
                            pendingCount = body.pendingCount,
                            approvedCount = body.approvedCount,
                            rejectedCount = body.rejectedCount,
                            postponedCount = body.postponedCount,
                            currentPage = body.page,
                            totalPages = body.totalPages,
                            selectedFilter = status,
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Error al cargar solicitudes"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error de conexión: ${e.message}"
                )
            }
        }
    }

    fun approveWithdrawal(withdrawalId: Int, notes: String?, transactionReference: String?) {
        performAction(withdrawalId, "approved", notes, transactionReference)
    }

    fun rejectWithdrawal(withdrawalId: Int, notes: String?) {
        performAction(withdrawalId, "rejected", notes, null)
    }

    fun postponeWithdrawal(withdrawalId: Int, notes: String?) {
        performAction(withdrawalId, "postponed", notes, null)
    }

    private fun performAction(withdrawalId: Int, status: String, notes: String?, transactionReference: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessingAction = true, errorMessage = null, successMessage = null)

            try {
                val request = AdminWithdrawalActionRequest(
                    status = status,
                    reviewNotes = notes,
                    rejectionReason = if (status == "rejected") notes else null,
                    transactionReference = if (status == "approved") transactionReference else null,
                    processingNotes = notes
                )

                val response = apiService.reviewWithdrawal(authorization, withdrawalId, request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        _uiState.value = _uiState.value.copy(
                            isProcessingAction = false,
                            successMessage = body.message
                        )
                        // Recargar la lista
                        loadWithdrawals(status = _uiState.value.selectedFilter)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isProcessingAction = false,
                            errorMessage = body?.message ?: "Error al procesar"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isProcessingAction = false,
                        errorMessage = "Error: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessingAction = false,
                    errorMessage = "Error de conexión: ${e.message}"
                )
            }
        }
    }

    fun setFilter(status: String?) {
        _uiState.value = _uiState.value.copy(selectedFilter = status)
        loadWithdrawals(status = status)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    class Factory(
        private val apiService: ApiService,
        private val getAccessToken: () -> String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AdminWithdrawalsViewModel::class.java)) {
                return AdminWithdrawalsViewModel(apiService, getAccessToken) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
