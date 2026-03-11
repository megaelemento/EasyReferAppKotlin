package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.UserCompanyResponse
import com.christelldev.easyreferplus.data.model.CategoryInfo
import com.christelldev.easyreferplus.data.model.ServiceInfo
import com.christelldev.easyreferplus.data.network.CityResult
import com.christelldev.easyreferplus.data.network.CompanyRepository
import com.christelldev.easyreferplus.data.network.CompanySearchResult
import com.christelldev.easyreferplus.data.network.ProvinceResult
import com.christelldev.easyreferplus.data.network.CategoryResult
import com.christelldev.easyreferplus.data.network.ServiceResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PublicCompaniesUiState(
    val companies: List<UserCompanyResponse> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedProvince: String? = null,
    val selectedCity: String? = null,
    val selectedCategory: CategoryInfo? = null,
    val selectedService: ServiceInfo? = null,
    val provinces: List<String> = emptyList(),
    val cities: List<String> = emptyList(),
    val categories: List<CategoryInfo> = emptyList(),
    val services: List<ServiceInfo> = emptyList(),
    val isLoadingFilters: Boolean = false
)

class PublicCompaniesViewModel(
    private val companyRepository: CompanyRepository,
    private val getAccessToken: () -> String
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublicCompaniesUiState())
    val uiState: StateFlow<PublicCompaniesUiState> = _uiState.asStateFlow()

    private val authorization: String get() = "Bearer ${getAccessToken()}"

    // Eliminado init que cargaba automáticamente - ahora se carga solo cuando se necesita

    fun loadCompanies() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            when (val result = companyRepository.searchCompanies(
                authorization = authorization,
                query = _uiState.value.searchQuery.takeIf { it.isNotBlank() },
                city = _uiState.value.selectedCity,
                province = _uiState.value.selectedProvince,
                categoryId = _uiState.value.selectedCategory?.id,
                serviceId = _uiState.value.selectedService?.id
            )) {
                is CompanySearchResult.Success -> {
                    // Filtrar solo empresas validadas
                    val validatedCompanies = result.companies.filter { it.isValidated }
                    _uiState.value = _uiState.value.copy(
                        companies = validatedCompanies,
                        isLoading = false
                    )
                }
                is CompanySearchResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun loadProvinces() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingFilters = true)
            when (val result = companyRepository.getProvinces()) {
                is ProvinceResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        provinces = result.provinces,
                        isLoadingFilters = false
                    )
                }
                is ProvinceResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoadingFilters = false)
                }
            }
        }
    }

    fun loadCities(province: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingFilters = true)
            when (val result = companyRepository.getCitiesByProvince(province)) {
                is CityResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        cities = result.cities,
                        isLoadingFilters = false
                    )
                }
                is CityResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoadingFilters = false)
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onProvinceSelected(province: String?) {
        _uiState.value = _uiState.value.copy(
            selectedProvince = province,
            selectedCity = null,
            cities = emptyList()
        )
        if (province != null) {
            loadCities(province)
        }
        loadCompanies()
    }

    fun onCitySelected(city: String?) {
        _uiState.value = _uiState.value.copy(selectedCity = city)
        loadCompanies()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingFilters = true)
            when (val result = companyRepository.getCategories()) {
                is CategoryResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        categories = result.categories,
                        isLoadingFilters = false
                    )
                }
                is CategoryResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoadingFilters = false)
                }
            }
        }
    }

    fun loadServices(categoryId: Int? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingFilters = true)
            when (val result = companyRepository.getServices(categoryId)) {
                is ServiceResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        services = result.services,
                        isLoadingFilters = false
                    )
                }
                is ServiceResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoadingFilters = false)
                }
            }
        }
    }

    fun onCategorySelected(category: CategoryInfo?) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            selectedService = null,
            services = emptyList()
        )
        if (category != null) {
            loadServices(category.id)
        }
        loadCompanies()
    }

    fun onServiceSelected(service: ServiceInfo?) {
        _uiState.value = _uiState.value.copy(selectedService = service)
        loadCompanies()
    }

    fun search() {
        loadCompanies()
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            selectedProvince = null,
            selectedCity = null,
            selectedCategory = null,
            selectedService = null,
            cities = emptyList(),
            services = emptyList()
        )
        loadCompanies()
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
            if (modelClass.isAssignableFrom(PublicCompaniesViewModel::class.java)) {
                return PublicCompaniesViewModel(companyRepository, getAccessToken) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
