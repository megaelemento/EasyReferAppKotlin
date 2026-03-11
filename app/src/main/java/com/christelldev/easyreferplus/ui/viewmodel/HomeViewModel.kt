package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.BalanceResponse
import com.christelldev.easyreferplus.data.model.ReferralTreeResponse
import com.christelldev.easyreferplus.data.model.UserBalance
import com.christelldev.easyreferplus.data.model.UserProfile
import com.christelldev.easyreferplus.data.model.PaymentAccessResponse
import com.christelldev.easyreferplus.data.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    // User data
    val nombres: String = "",
    val apellidos: String = "",
    val email: String = "",
    val phone: String = "",
    val referralCode: String = "",
    val isVerified: Boolean = false,
    val hasCompany: Boolean = false,
    val empresaNombre: String? = null,
    val empresaStatus: String? = null,
    val empresaActiva: Boolean? = false,
    val isAdmin: Boolean = false,
    val selfieUrl: String? = null,

    // Payment access
    val canAccessPayments: Boolean = false,
    val paymentCompanyId: Int? = null,
    val paymentCompanyName: String? = null,
    val paymentCompanyStatus: String? = null,
    val paymentAccessMessage: String? = null,
    val pendingPaymentAmount: Double = 0.0,

    // Stats
    val balance: UserBalance? = null,
    val totalReferrals: Int = 0,
    val level1Referrals: Int = 0,
    val level2Referrals: Int = 0,
    val level3Referrals: Int = 0,

    // Ganancias y Retiros - mostrar solo cuando hay actividad
    val hasEarnings: Boolean = false,
    val hasPendingWithdrawals: Boolean = false,

    // Loading states
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
) {
    // Helper to check if company is validated and active
    val canGenerateQR: Boolean
        get() = hasCompany && (empresaActiva == true || empresaStatus?.lowercase() == "validated")

    // Helper para pagos: mostrar botón si tiene empresa validada Y activa
    // El endpoint de payment access verificará si hay pagos pendientes
    val hasValidatedCompany: Boolean
        get() = hasCompany && (empresaActiva == true || empresaStatus?.lowercase() == "validated")

    // Ganancias: mostrar si tiene balance positivo o ganancias pendientes
    val hasEarningsAvailable: Boolean
        get() = (balance?.availableBalance ?: 0.0) > 0 || 
                 (balance?.pendingBalance ?: 0.0) > 0 ||
                 (balance?.totalEarned ?: 0.0) > 0

    // Retiros: mostrar si tiene balance disponible
    val hasWithdrawalsAvailable: Boolean
        get() = (balance?.availableBalance ?: 0.0) > 0 ||
                 (balance?.totalWithdrawn ?: 0.0) > 0
}

class HomeViewModel(
    private val apiService: ApiService,
    private val getAccessToken: () -> String
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val authorization: String get() = "Bearer ${getAccessToken()}"

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                // Cargar todo en paralelo para mayor velocidad
                kotlinx.coroutines.coroutineScope {
                    async { loadProfile() }
                    async { loadBalance() }
                    async { loadReferrals() }
                    async { checkPaymentAccess() }
                }

                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error al cargar datos"
                )
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)

            try {
                // Cargar todo en paralelo
                kotlinx.coroutines.coroutineScope {
                    async { loadProfile() }
                    async { loadBalance() }
                    async { loadReferrals() }
                    async { checkPaymentAccess() }
                }

                _uiState.value = _uiState.value.copy(isRefreshing = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    errorMessage = e.message
                )
            }
        }
    }

    private suspend fun loadProfile() {
        try {
            val response = apiService.getProfile(authorization)
            if (response.isSuccessful) {
                response.body()?.let { profile ->
                    _uiState.value = _uiState.value.copy(
                        nombres = profile.nombres,
                        apellidos = profile.apellidos,
                        email = profile.email,
                        phone = profile.phone ?: "",
                        referralCode = profile.referralCode ?: "",
                        isVerified = profile.isVerified,
                        hasCompany = profile.hasCompany,
                        empresaNombre = profile.empresaNombre,
                        empresaStatus = profile.empresaStatus,
                        empresaActiva = profile.empresaActiva,
                        isAdmin = profile.isAdmin,
                        selfieUrl = profile.selfieUrl
                    )
                }
            }
        } catch (e: Exception) {
            // Silently handle profile errors
        }
    }

    private suspend fun checkPaymentAccess() {
        try {
            val response = apiService.checkPaymentAccess(authorization)
            if (response.isSuccessful) {
                response.body()?.let { access ->
                    _uiState.value = _uiState.value.copy(
                        canAccessPayments = access.canAccess,
                        paymentCompanyId = access.companyId,
                        paymentCompanyName = access.companyName,
                        paymentCompanyStatus = access.companyStatus,
                        paymentAccessMessage = access.message,
                        pendingPaymentAmount = access.pendingAmount ?: 0.0
                    )
                }
            }
        } catch (e: Exception) {
            // Silently handle payment access errors
        }
    }

    private suspend fun loadBalance() {
        try {
            val response = apiService.getBalance(authorization)
            if (response.isSuccessful) {
                val balanceResponse: com.christelldev.easyreferplus.data.model.BalanceResponse? = response.body()
                balanceResponse?.let {
                    if (it.success) {
                        _uiState.value = _uiState.value.copy(
                            balance = it.balance
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Silently handle balance errors
        }
    }

    private suspend fun loadReferrals() {
        try {
            val response = apiService.getMyReferrals(authorization)
            if (response.isSuccessful) {
                val referralResponse: com.christelldev.easyreferplus.data.model.ReferralTreeResponse? = response.body()
                referralResponse?.let {
                    if (it.success) {
                        val totals = it.totals
                        _uiState.value = _uiState.value.copy(
                            totalReferrals = totals.total,
                            level1Referrals = totals.level1,
                            level2Referrals = totals.level2,
                            level3Referrals = totals.level3
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Silently handle referral errors
        }
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
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(apiService, getAccessToken) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
