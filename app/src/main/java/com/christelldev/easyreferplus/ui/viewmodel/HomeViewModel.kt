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
import com.christelldev.easyreferplus.data.model.FeedProduct
import com.christelldev.easyreferplus.data.model.ProductCategory
import com.christelldev.easyreferplus.data.model.ProductSearchResult
import com.christelldev.easyreferplus.data.network.ApiService
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
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
    val isMotorizado: Boolean = false,
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

    // Search
    val searchQuery: String = "",
    val searchResults: List<ProductSearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val showSearchResults: Boolean = false,
    val searchPage: Int = 1,
    val searchHasMore: Boolean = false,
    val isLoadingMore: Boolean = false,

    // Filters
    val showFilters: Boolean = false,
    val categories: List<ProductCategory> = emptyList(),
    val selectedCategoryId: Int? = null,
    val minPrice: String = "",
    val maxPrice: String = "",
    val sortBy: String = "",

    // Feed
    val recentProducts: List<FeedProduct> = emptyList(),
    val bestsellerProducts: List<FeedProduct> = emptyList(),
    val isLoadingFeed: Boolean = false,
    val recentPage: Int = 1,
    val recentHasMore: Boolean = true,
    val bestsellerPage: Int = 1,
    val bestsellerHasMore: Boolean = true,

    // Loading states
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
) {
    val activeFilterCount: Int
        get() = listOfNotNull(
            selectedCategoryId,
            minPrice.takeIf { it.isNotEmpty() },
            maxPrice.takeIf { it.isNotEmpty() },
            sortBy.takeIf { it.isNotEmpty() }
        ).size

    val hasActiveFilters: Boolean get() = activeFilterCount > 0

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

    private var lastLoadTime: Long = 0L

    fun loadHomeData(forceRefresh: Boolean = false) {
        val now = System.currentTimeMillis()
        val dataExists = _uiState.value.nombres.isNotBlank()
        if (!forceRefresh && dataExists && (now - lastLoadTime < 60_000L)) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = !dataExists, errorMessage = null)
            loadAll()
            lastLoadTime = System.currentTimeMillis()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
        if (_uiState.value.recentProducts.isEmpty()) loadProductFeed()
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
                    isMotorizado = profile?.role?.lowercase() == "motorizado",
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

    private var searchJob: Job? = null

    fun searchProducts(query: String) {
        val state = _uiState.value
        val hasFilters = state.selectedCategoryId != null || state.minPrice.isNotEmpty() ||
                state.maxPrice.isNotEmpty() || state.sortBy.isNotEmpty()
        _uiState.value = state.copy(
            searchQuery = query,
            showSearchResults = query.isNotEmpty() || hasFilters
        )
        searchJob?.cancel()
        if (query.length < 2 && !hasFilters) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList(), isSearching = false)
            return
        }
        triggerSearch()
    }

    fun applyFilters(categoryId: Int?, minPrice: String, maxPrice: String, sortBy: String) {
        val state = _uiState.value
        val hasFilters = categoryId != null || minPrice.isNotEmpty() || maxPrice.isNotEmpty() || sortBy.isNotEmpty()
        _uiState.value = state.copy(
            selectedCategoryId = categoryId,
            minPrice = minPrice,
            maxPrice = maxPrice,
            sortBy = sortBy,
            showSearchResults = state.searchQuery.isNotEmpty() || hasFilters
        )
        triggerSearch()
    }

    fun toggleFilters() {
        val state = _uiState.value
        _uiState.value = state.copy(showFilters = !state.showFilters)
        if (state.categories.isEmpty()) loadCategories()
    }

    fun clearFilters() {
        val state = _uiState.value
        _uiState.value = state.copy(
            selectedCategoryId = null,
            minPrice = "",
            maxPrice = "",
            sortBy = "",
            showSearchResults = state.searchQuery.isNotEmpty()
        )
        triggerSearch()
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                val response = apiService.getProductCategories(authorization)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(categories = response.body() ?: emptyList())
                }
            } catch (e: Exception) { /* silencioso */ }
        }
    }

    private fun triggerSearch(resetPagination: Boolean = true) {
        val state = _uiState.value
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (resetPagination) delay(300)
            val page = if (resetPagination) 1 else state.searchPage + 1
            _uiState.value = _uiState.value.copy(
                isSearching = resetPagination,
                isLoadingMore = !resetPagination
            )
            try {
                val response = apiService.searchProducts(
                    authorization,
                    query = state.searchQuery.takeIf { it.length >= 2 },
                    categoryId = state.selectedCategoryId,
                    minPrice = state.minPrice.toDoubleOrNull(),
                    maxPrice = state.maxPrice.toDoubleOrNull(),
                    sortBy = state.sortBy.takeIf { it.isNotEmpty() },
                    page = page,
                    perPage = 20
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    val body = response.body()!!
                    val newResults = if (resetPagination) body.products
                                     else state.searchResults + body.products
                    _uiState.value = _uiState.value.copy(
                        searchResults = newResults,
                        searchPage = page,
                        searchHasMore = body.products.size >= 20,
                        isSearching = false,
                        isLoadingMore = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isSearching = false, isLoadingMore = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSearching = false, isLoadingMore = false)
            }
        }
    }

    fun loadMoreSearchResults() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.searchHasMore || state.isSearching) return
        triggerSearch(resetPagination = false)
    }

    fun loadProductFeed() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.isLoadingFeed) return@launch
            _uiState.value = state.copy(isLoadingFeed = true)
            val auth = authorization
            try {
                val recentJob = async { fetchFeedSection(auth, "recent", 1) }
                val bestsellerJob = async { fetchFeedSection(auth, "bestseller", 1) }
                val recent = recentJob.await()
                val bestseller = bestsellerJob.await()
                _uiState.value = _uiState.value.copy(
                    recentProducts = recent?.products ?: emptyList(),
                    recentPage = 1,
                    recentHasMore = recent?.hasMore ?: false,
                    bestsellerProducts = bestseller?.products ?: emptyList(),
                    bestsellerPage = 1,
                    bestsellerHasMore = bestseller?.hasMore ?: false,
                    isLoadingFeed = false
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingFeed = false)
            }
        }
    }

    fun loadMoreRecent() {
        val state = _uiState.value
        if (state.isLoadingFeed || !state.recentHasMore) return
        viewModelScope.launch {
            _uiState.value = state.copy(isLoadingFeed = true)
            val result = fetchFeedSection(authorization, "recent", state.recentPage + 1)
            if (result != null) {
                _uiState.value = _uiState.value.copy(
                    recentProducts = state.recentProducts + result.products,
                    recentPage = state.recentPage + 1,
                    recentHasMore = result.hasMore,
                    isLoadingFeed = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoadingFeed = false)
            }
        }
    }

    fun loadMoreBestseller() {
        val state = _uiState.value
        if (state.isLoadingFeed || !state.bestsellerHasMore) return
        viewModelScope.launch {
            _uiState.value = state.copy(isLoadingFeed = true)
            val result = fetchFeedSection(authorization, "bestseller", state.bestsellerPage + 1)
            if (result != null) {
                _uiState.value = _uiState.value.copy(
                    bestsellerProducts = state.bestsellerProducts + result.products,
                    bestsellerPage = state.bestsellerPage + 1,
                    bestsellerHasMore = result.hasMore,
                    isLoadingFeed = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoadingFeed = false)
            }
        }
    }

    private suspend fun fetchFeedSection(auth: String, section: String, page: Int) =
        try {
            val r = apiService.getProductFeed(auth, section, page, 20)
            if (r.isSuccessful) r.body() else null
        } catch (_: Exception) { null }

    fun clearSearch() {
        searchJob?.cancel()
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            searchResults = emptyList(),
            isSearching = false,
            showSearchResults = _uiState.value.hasActiveFilters,
            showFilters = false,
            selectedCategoryId = null,
            minPrice = "",
            maxPrice = "",
            sortBy = "",
            searchPage = 1,
            searchHasMore = false,
            isLoadingMore = false
        )
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
