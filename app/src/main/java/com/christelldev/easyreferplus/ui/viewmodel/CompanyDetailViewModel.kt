package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.UserCompanyResponse
import com.christelldev.easyreferplus.data.network.CompanyDetailResult
import com.christelldev.easyreferplus.data.network.CompanyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CompanyDetailUiState(
    val company: UserCompanyResponse? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class CompanyDetailViewModel(
    private val companyRepository: CompanyRepository,
    private val getAccessToken: () -> String
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompanyDetailUiState())
    val uiState: StateFlow<CompanyDetailUiState> = _uiState.asStateFlow()

    private val authorization: String get() = "Bearer ${getAccessToken()}"

    fun loadCompany(companyId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            when (val result = companyRepository.getCompanyById(authorization, companyId)) {
                is CompanyDetailResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        company = result.company,
                        isLoading = false
                    )
                }
                is CompanyDetailResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    class Factory(
        private val companyRepository: CompanyRepository,
        private val getAccessToken: () -> String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CompanyDetailViewModel::class.java)) {
                return CompanyDetailViewModel(companyRepository, getAccessToken) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
