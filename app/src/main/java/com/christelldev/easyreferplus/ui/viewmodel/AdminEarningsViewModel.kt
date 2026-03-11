package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.AdminEarning
import com.christelldev.easyreferplus.data.model.AdminEarningsStats
import com.christelldev.easyreferplus.data.model.AdminUserEarningsStats
import com.christelldev.easyreferplus.data.model.AdminUserInfo
import com.christelldev.easyreferplus.data.model.AdminUserEarning
import com.christelldev.easyreferplus.data.network.AdminEarningsResult
import com.christelldev.easyreferplus.data.network.AdminEarningsRepository
import com.christelldev.easyreferplus.data.network.AdminUserEarningsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Filter options for earnings
enum class EarningsFilter(val value: String, val label: String) {
    ALL("all", "Todas"),
    PENDING("pending", "Pendientes"),
    PAID("paid", "Pagadas"),
    LEVEL_1("level_1", "Nivel 1"),
    LEVEL_2("level_2", "Nivel 2"),
    LEVEL_3("level_3", "Nivel 3"),
    ADMIN_ORPHAN("admin_orphan", "Admin Huérfanos")
}

data class AdminEarningsUiState(
    // Main earnings list
    val earnings: List<AdminEarning> = emptyList(),
    val stats: AdminEarningsStats? = null,

    // User earnings detail
    val selectedUserId: Int? = null,
    val selectedUserInfo: AdminUserInfo? = null,
    val userEarnings: List<AdminUserEarning> = emptyList(),
    val userStats: AdminUserEarningsStats? = null,

    // Filters
    val searchQuery: String = "",
    val selectedFilter: EarningsFilter = EarningsFilter.ALL,
    val dateFrom: String? = null,
    val dateTo: String? = null,

    // Pagination
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val total: Int = 0,

    // Loading states
    val isLoading: Boolean = false,
    val isLoadingUserEarnings: Boolean = false,

    // Messages
    val errorMessage: String? = null,
    val isViewingUserDetail: Boolean = false
)

class AdminEarningsViewModel(
    private val repository: AdminEarningsRepository,
    private val getAccessToken: () -> String
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminEarningsUiState())
    val uiState: StateFlow<AdminEarningsUiState> = _uiState.asStateFlow()

    private val authorization: String get() = "Bearer ${getAccessToken()}"

    // Eliminado init que cargaba automáticamente - ahora se carga solo cuando se necesita

    fun loadEarnings(page: Int = 1) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val result = repository.getEarnings(
                authorization = authorization,
                search = _uiState.value.searchQuery.takeIf { it.isNotBlank() },
                filter = _uiState.value.selectedFilter.value.takeIf { it != EarningsFilter.ALL.value },
                dateFrom = _uiState.value.dateFrom,
                dateTo = _uiState.value.dateTo,
                page = page
            )

            when (result) {
                is AdminEarningsResult.Success -> {
                    val pagination = result.response.pagination
                    _uiState.value = _uiState.value.copy(
                        earnings = result.response.data ?: emptyList(),
                        stats = result.response.stats,
                        currentPage = pagination?.page ?: 1,
                        totalPages = pagination?.totalPages ?: 1,
                        total = pagination?.total ?: 0,
                        isLoading = false
                    )
                }
                is AdminEarningsResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun updateFilter(filter: EarningsFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        loadEarnings()
    }

    fun updateDateRange(from: String?, to: String?) {
        _uiState.value = _uiState.value.copy(
            dateFrom = from,
            dateTo = to
        )
        loadEarnings()
    }

    fun search() {
        loadEarnings()
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            selectedFilter = EarningsFilter.ALL,
            dateFrom = null,
            dateTo = null
        )
        loadEarnings()
    }

    fun viewUserEarnings(userId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingUserEarnings = true,
                selectedUserId = userId,
                isViewingUserDetail = true,
                errorMessage = null
            )

            val result = repository.getUserEarnings(
                authorization = authorization,
                userId = userId
            )

            when (result) {
                is AdminUserEarningsResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        selectedUserInfo = result.response.user,
                        userEarnings = result.response.data ?: emptyList(),
                        userStats = result.response.stats,
                        isLoadingUserEarnings = false
                    )
                }
                is AdminUserEarningsResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingUserEarnings = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun closeUserDetail() {
        _uiState.value = _uiState.value.copy(
            isViewingUserDetail = false,
            selectedUserId = null,
            selectedUserInfo = null,
            userEarnings = emptyList(),
            userStats = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    class Factory(
        private val repository: AdminEarningsRepository,
        private val getAccessToken: () -> String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AdminEarningsViewModel::class.java)) {
                return AdminEarningsViewModel(repository, getAccessToken) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
