package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.BalanceResponse
import com.christelldev.easyreferplus.data.model.ReferralTreeResponse
import com.christelldev.easyreferplus.data.model.UserBalance
import com.christelldev.easyreferplus.data.model.UserProfile
import com.christelldev.easyreferplus.data.model.ProfileResponse
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
            loadAll()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)
            loadAll()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    private suspend fun loadAll() {
        try {
            // Cargar todo en paralelo y acumular en una sola emisión
            val auth = authorization
            coroutineScope {
                val profileDeferred = async { fetchProfile(auth) }
                val balanceDeferred = async { fetchBalance(auth) }
                val referralsDeferred = async { fetchReferrals(auth) }
                val paymentDeferred = async { fetchPaymentAccess(auth) }

                val profile = profileDeferred.await()
                val balance = balanceDeferred.await()
                val referrals = referralsDeferred.await()
                val payment = paymentDeferred.await()

                // Una sola emisión con todos los datos → una sola recomposición
                _uiState.value = _uiState.value.copy(
                    nombres = profile?.nombres ?: _uiState.value.nombres,
                    apellidos = profile?.apellidos ?: _uiState.value.apellidos,
                    email = profile?.email ?: _uiState.value.email,
                    phone = profile?.phone ?: _uiState.value.phone,
                    referralCode = profile?.referralCode ?: _uiState.value.referralCode,
                    isVerified = profile?.isVerified ?: _uiState.value.isVerified,
                    hasCompany = profile?.hasCompany ?: _uiState.value.hasCompany,
                    empresaNombre = profile?.empresaNombre ?: _uiState.value.empresaNombre,
                    empresaStatus = profile?.empresaStatus ?: _uiState.value.empresaStatus,
                    empresaActiva = profile?.empresaActiva ?: _uiState.value.empresaActiva,
                    isAdmin = profile?.isAdmin ?: _uiState.value.isAdmin,
                    selfieUrl = profile?.selfieUrl ?: _uiState.value.selfieUrl,
                    balance = balance ?: _uiState.value.balance,
                    totalReferrals = referrals?.totals?.total ?: _uiState.value.totalReferrals,
                    level1Referrals = referrals?.totals?.level1 ?: _uiState.value.level1Referrals,
                    level2Referrals = referrals?.totals?.level2 ?: _uiState.value.level2Referrals,
                    level3Referrals = referrals?.totals?.level3 ?: _uiState.value.level3Referrals,
                    canAccessPayments = payment?.canAccess ?: _uiState.value.canAccessPayments,
                    paymentCompanyId = payment?.companyId ?: _uiState.value.paymentCompanyId,
                    paymentCompanyName = payment?.companyName ?: _uiState.value.paymentCompanyName,
                    paymentCompanyStatus = payment?.companyStatus ?: _uiState.value.paymentCompanyStatus,
                    paymentAccessMessage = payment?.message ?: _uiState.value.paymentAccessMessage,
                    pendingPaymentAmount = payment?.pendingAmount ?: _uiState.value.pendingPaymentAmount
                )
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Error al cargar datos")
        }
    }

    private suspend fun fetchProfile(auth: String): ProfileResponse? = try {
        val response = apiService.getProfile(auth)
        if (response.isSuccessful) response.body() else null
    } catch (e: Exception) { null }

    private suspend fun fetchPaymentAccess(auth: String): PaymentAccessResponse? = try {
        val response = apiService.checkPaymentAccess(auth)
        if (response.isSuccessful) response.body() else null
    } catch (e: Exception) { null }

    private suspend fun fetchBalance(auth: String): UserBalance? = try {
        val response = apiService.getBalance(auth)
        if (response.isSuccessful) response.body()?.let { if (it.success) it.balance else null } else null
    } catch (e: Exception) { null }

    private suspend fun fetchReferrals(auth: String): ReferralTreeResponse? = try {
        val response = apiService.getMyReferrals(auth)
        if (response.isSuccessful) response.body()?.let { if (it.success) it else null } else null
    } catch (e: Exception) { null }

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
