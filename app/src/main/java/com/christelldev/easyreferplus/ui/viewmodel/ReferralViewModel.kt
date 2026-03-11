package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.ReferralTree
import com.christelldev.easyreferplus.data.model.ReferralTotals
import com.christelldev.easyreferplus.data.network.ReferralRepository
import com.christelldev.easyreferplus.data.network.ReferralResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReferralUiState(
    val userReferralCode: String = "",
    val level1Count: Int = 0,
    val level2Count: Int = 0,
    val level3Count: Int = 0,
    val totalCount: Int = 0,
    val level1Codes: List<String> = emptyList(),
    val level2Codes: List<String> = emptyList(),
    val level3Codes: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchCode: String = "",
    val searchResult: SearchResult? = null,
    val isSearching: Boolean = false
)

data class SearchResult(
    val code: String,
    val found: Boolean,
    val level: Int?
)

class ReferralViewModel(
    private val repository: ReferralRepository,
    private val getAccessToken: () -> String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReferralUiState())
    val uiState: StateFlow<ReferralUiState> = _uiState.asStateFlow()

    // Obtener token dinámicamente cuando se necesita
    private val authorization: String get() = "Bearer ${getAccessToken()}"

    // Eliminado init que cargaba automáticamente - ahora se carga solo cuando se necesita

    fun loadReferrals() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            when (val result = repository.getMyReferrals(authorization)) {
                is ReferralResult.GetReferralsSuccess -> {
                    val response = result.response
                    val tree = response.referralTree
                    val totals = response.totals

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        userReferralCode = response.userReferralCode,
                        level1Count = totals.level1,
                        level2Count = totals.level2,
                        level3Count = totals.level3,
                        totalCount = totals.total,
                        level1Codes = tree.level1,
                        level2Codes = tree.level2,
                        level3Codes = tree.level3
                    )
                }
                is ReferralResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun updateSearchCode(code: String) {
        _uiState.value = _uiState.value.copy(
            searchCode = code.uppercase().filter { it.isLetterOrDigit() },
            searchResult = null
        )
    }

    fun searchReferral() {
        val code = _uiState.value.searchCode
        if (code.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Ingrese un código de referido"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, errorMessage = null)

            when (val result = repository.searchReferral(authorization, code)) {
                is ReferralResult.SearchSuccess -> {
                    val response = result.response
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        searchResult = SearchResult(
                            code = response.searchCode,
                            found = response.found,
                            level = response.level
                        )
                    )
                }
                is ReferralResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        errorMessage = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSearchResult() {
        _uiState.value = _uiState.value.copy(searchResult = null)
    }

    class Factory(
        private val repository: ReferralRepository,
        private val getAccessToken: () -> String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReferralViewModel::class.java)) {
                return ReferralViewModel(repository, getAccessToken) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
