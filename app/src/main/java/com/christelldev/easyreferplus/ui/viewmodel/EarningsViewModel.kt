package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.CompanyEarnings
import com.christelldev.easyreferplus.data.model.CommissionResponse
import com.christelldev.easyreferplus.data.model.EarningsByLevel
import com.christelldev.easyreferplus.data.network.EarningsRepository
import com.christelldev.easyreferplus.data.network.EarningsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EarningsUiState(
    // Resumen general
    val totalEarned: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalPending: Double = 0.0,
    val totalCommissions: Int = 0,

    // Detalles
    val pendingCount: Int = 0,
    val paidCount: Int = 0,
    val scheduledCount: Int = 0,

    // Ganancias por nivel
    val earningsByLevel: EarningsByLevel? = null,

    // Empresas top
    val topCompanies: List<CompanyEarnings> = emptyList(),

    // Lista de comisiones
    val commissions: List<CommissionResponse> = emptyList(),
    val isLoadingCommissions: Boolean = false,
    val commissionsPage: Int = 1,
    val hasMoreCommissions: Boolean = true,
    val selectedFilter: String = "all", // all, pending, paid

    // Estado
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEmpty: Boolean = false
)

class EarningsViewModel(
    private val repository: EarningsRepository,
    private val getAccessToken: () -> String
) : ViewModel() {

    private val _uiState = MutableStateFlow(EarningsUiState())
    val uiState: StateFlow<EarningsUiState> = _uiState.asStateFlow()

    private val authorization: String get() = "Bearer ${getAccessToken()}"

    fun loadEarnings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            when (val result = repository.getMyEarningsSummary(authorization)) {
                is EarningsResult.Success -> {
                    val response = result.response
                    _uiState.value = _uiState.value.copy(
                        totalEarned = response.totalEarned,
                        totalPaid = response.totalPaid,
                        totalPending = response.totalPending,
                        totalCommissions = response.totalCommissions,
                        pendingCount = response.pendingCount,
                        paidCount = response.paidCount,
                        scheduledCount = response.scheduledCount,
                        earningsByLevel = response.earningsByLevel,
                        topCompanies = response.topCompanies ?: emptyList(),
                        isLoading = false,
                        isEmpty = response.totalCommissions == 0
                    )
                }
                is EarningsResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun refresh() {
        loadEarnings()
    }

    /**
     * Cargar lista de comisiones
     */
    fun loadCommissions(page: Int = 1, filter: String = "all") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingCommissions = true,
                commissionsPage = page,
                selectedFilter = filter
            )

            val paymentStatus = when (filter) {
                "pending" -> "pending"
                "paid" -> "paid"
                else -> null
            }

            when (val result = repository.getMyCommissions(
                authorization = authorization,
                page = page,
                perPage = 20,
                paymentStatus = paymentStatus
            )) {
                is com.christelldev.easyreferplus.data.network.CommissionsResult.Success -> {
                    val newCommissions = result.commissions
                    val allCommissions = if (page == 1) {
                        newCommissions
                    } else {
                        _uiState.value.commissions + newCommissions
                    }

                    _uiState.value = _uiState.value.copy(
                        commissions = allCommissions,
                        isLoadingCommissions = false,
                        hasMoreCommissions = newCommissions.size >= 20
                    )
                }
                is com.christelldev.easyreferplus.data.network.CommissionsResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingCommissions = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    /**
     * Cargar más comisiones (paginación)
     */
    fun loadMoreCommissions() {
        if (!_uiState.value.isLoadingCommissions && _uiState.value.hasMoreCommissions) {
            loadCommissions(
                page = _uiState.value.commissionsPage + 1,
                filter = _uiState.value.selectedFilter
            )
        }
    }

    /**
     * Cambiar filtro de comisiones
     */
    fun setFilter(filter: String) {
        _uiState.value = _uiState.value.copy(commissions = emptyList())
        loadCommissions(page = 1, filter = filter)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    // Factory para ViewModel
    class Factory(
        private val repository: EarningsRepository,
        private val getAccessToken: () -> String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EarningsViewModel::class.java)) {
                return EarningsViewModel(repository, getAccessToken) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
