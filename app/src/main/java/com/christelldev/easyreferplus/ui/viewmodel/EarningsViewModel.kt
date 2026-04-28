package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.CompanyEarnings
import com.christelldev.easyreferplus.data.model.CommissionResponse
import com.christelldev.easyreferplus.data.model.EarningsByLevel
import com.christelldev.easyreferplus.data.network.AppConfig
import com.christelldev.easyreferplus.data.network.EarningsRepository
import com.christelldev.easyreferplus.data.network.EarningsResult
import com.christelldev.easyreferplus.data.network.SimpleWebSocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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

    // Porcentajes de comisión por nivel (desde el backend)
    val level1Percentage: Double = 0.0,
    val level2Percentage: Double = 0.0,
    val level3Percentage: Double = 0.0,

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
            val auth = authorization

            when (val result = repository.getMyEarningsSummary(auth)) {
                is EarningsResult.Success -> {
                    val response = result.response
                    val percentages = response.commissionPercentages
                    _uiState.value = _uiState.value.copy(
                        totalEarned = response.totalEarned,
                        totalPaid = response.totalPaid,
                        totalPending = response.totalPending,
                        totalCommissions = response.totalCommissions,
                        pendingCount = response.pendingCount,
                        paidCount = response.paidCount,
                        scheduledCount = response.scheduledCount,
                        earningsByLevel = response.earningsByLevel,
                        level1Percentage = percentages?.level1 ?: 20.0,
                        level2Percentage = percentages?.level2 ?: 20.0,
                        level3Percentage = percentages?.level3 ?: 20.0,
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
            val auth = authorization

            val paymentStatus = when (filter) {
                "pending" -> "pending"
                "paid" -> "paid"
                else -> null
            }

            when (val result = repository.getMyCommissions(
                authorization = auth,
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

    // ==================== WEBSOCKET ====================
    // WebSocket Manager para tiempo real
    private var wsManager: SimpleWebSocketManager? = null

    /**
     * Inicializar WebSocket Manager con el contexto
     */
    fun initWebSocketManager(context: android.content.Context) {
        if (wsManager == null) {
            wsManager = SimpleWebSocketManager(
                context = context,
                baseUrl = AppConfig.BASE_URL,
                getAccessToken = getAccessToken
            )
            // Observar datos en tiempo real
            viewModelScope.launch {
                wsManager?.earningsData?.collectLatest { data ->
                    data?.let { updateFromWebSocket(it) }
                }
            }
        }
    }

    /**
     * Conectar al WebSocket (llamar cuando entra a la pantalla)
     */
    fun connectWebSocket() {
        wsManager?.subscribe(SimpleWebSocketManager.CHANNEL_EARNINGS)
    }

    /**
     * Desconectar del WebSocket (llamar cuando sale de la pantalla)
     */
    fun disconnectWebSocket() {
        wsManager?.unsubscribe(SimpleWebSocketManager.CHANNEL_EARNINGS)
    }

    fun reconnectIfNeeded() {
        val state = wsManager?.connectionState?.value
        if (state == SimpleWebSocketManager.ConnectionState.Disconnected ||
            state is SimpleWebSocketManager.ConnectionState.Error) {
            wsManager?.subscribe(SimpleWebSocketManager.CHANNEL_EARNINGS)
        }
    }

    override fun onCleared() {
        super.onCleared()
        wsManager?.shutdown()
        wsManager = null
    }

    /**
     * Actualizar UI con datos del WebSocket
     */
    private fun updateFromWebSocket(data: Map<String, Any>) {
        fun double(key: String) = (data[key] as? Double) ?: (_uiState.value.let {
            when (key) {
                "total_earned" -> it.totalEarned
                "total_paid" -> it.totalPaid
                "total_pending" -> it.totalPending
                else -> 0.0
            }
        })
        fun int(key: String) = (data[key] as? Double)?.toInt() ?: (data[key] as? Int)

        val action = data["action"] as? String

        when (action) {
            "initial" -> {
                val byLevel = data["earnings_by_level"]?.let { it as? Map<*, *> }
                val commPercentages = data["commission_percentages"]?.let { it as? Map<*, *> }
                @Suppress("UNCHECKED_CAST")
                val commissions = (data["commissions"] as? List<Map<String, Any>>) ?: emptyList()
                @Suppress("UNCHECKED_CAST")
                val topCompaniesRaw = (data["top_companies"] as? List<Map<String, Any>>) ?: emptyList()

                _uiState.value = _uiState.value.copy(
                    totalEarned = double("total_earned"),
                    totalPaid = double("total_paid"),
                    totalPending = double("total_pending"),
                    totalCommissions = int("total_commissions") ?: _uiState.value.totalCommissions,
                    pendingCount = int("pending_count") ?: _uiState.value.pendingCount,
                    paidCount = int("paid_count") ?: _uiState.value.paidCount,
                    scheduledCount = int("scheduled_count") ?: _uiState.value.scheduledCount,
                    earningsByLevel = if (byLevel != null) com.christelldev.easyreferplus.data.model.EarningsByLevel(
                        level1 = (byLevel["level1_earned"] as? Double) ?: (byLevel["level_1"] as? Double) ?: 0.0,
                        level2 = (byLevel["level2_earned"] as? Double) ?: (byLevel["level_2"] as? Double) ?: 0.0,
                        level3 = (byLevel["level3_earned"] as? Double) ?: (byLevel["level_3"] as? Double) ?: 0.0,
                    ) else com.christelldev.easyreferplus.data.model.EarningsByLevel(
                        level1 = (data["level1_earned"] as? Double) ?: _uiState.value.earningsByLevel?.level1 ?: 0.0,
                        level2 = (data["level2_earned"] as? Double) ?: _uiState.value.earningsByLevel?.level2 ?: 0.0,
                        level3 = (data["level3_earned"] as? Double) ?: _uiState.value.earningsByLevel?.level3 ?: 0.0,
                    ),
                    level1Percentage = (data["level1_percentage"] as? Double) ?: (commPercentages?.get("level_1") as? Double) ?: _uiState.value.level1Percentage,
                    level2Percentage = (data["level2_percentage"] as? Double) ?: (commPercentages?.get("level_2") as? Double) ?: _uiState.value.level2Percentage,
                    level3Percentage = (data["level3_percentage"] as? Double) ?: (commPercentages?.get("level_3") as? Double) ?: _uiState.value.level3Percentage,
                    topCompanies = topCompaniesRaw.map { c ->
                        com.christelldev.easyreferplus.data.model.CompanyEarnings(
                            companyName = (c["company_name"] as? String) ?: "",
                            totalEarned = (c["total_earned"] as? Double) ?: 0.0
                        )
                    },
                    isLoading = false,
                    isEmpty = (int("total_commissions") ?: 0) == 0
                )
            }
            "new_commission" -> {
                val newTotalPending = _uiState.value.totalPending + ((data["commission"] as? Map<*, *>)?.get("amount") as? Double ?: 0.0)
                _uiState.value = _uiState.value.copy(
                    totalPending = newTotalPending,
                    totalEarned = _uiState.value.totalEarned + ((data["commission"] as? Map<*, *>)?.get("amount") as? Double ?: 0.0),
                    totalCommissions = _uiState.value.totalCommissions + 1,
                    pendingCount = _uiState.value.pendingCount + 1
                )
            }
            "commission_paid" -> {
                val amount = (data["commission"] as? Map<*, *>)?.get("amount") as? Double ?: 0.0
                _uiState.value = _uiState.value.copy(
                    totalPaid = _uiState.value.totalPaid + amount,
                    totalPending = (_uiState.value.totalPending - amount).coerceAtLeast(0.0),
                    paidCount = _uiState.value.paidCount + 1,
                    pendingCount = (_uiState.value.pendingCount - 1).coerceAtLeast(0)
                )
            }
            else -> {
                // Fallback: leer campos planos (compatibilidad)
                val totalEarned = data["total_earned"] as? Double
                val totalPaid = data["total_paid"] as? Double
                val totalPending = data["total_pending"] as? Double
                val totalCommissions = (data["total_commissions"] as? Double)?.toInt() ?: data["total_commissions"] as? Int
                _uiState.value = _uiState.value.copy(
                    totalEarned = totalEarned ?: _uiState.value.totalEarned,
                    totalPaid = totalPaid ?: _uiState.value.totalPaid,
                    totalPending = totalPending ?: _uiState.value.totalPending,
                    totalCommissions = totalCommissions ?: _uiState.value.totalCommissions
                )
            }
        }
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
