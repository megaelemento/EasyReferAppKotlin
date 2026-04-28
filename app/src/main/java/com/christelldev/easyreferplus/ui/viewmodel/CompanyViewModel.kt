package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.RegisterCompanyRequest
import com.christelldev.easyreferplus.data.model.UpdateCompanyRequest
import com.christelldev.easyreferplus.data.model.CompanyValidationError
import com.christelldev.easyreferplus.data.model.UserCompanyResponse
import com.christelldev.easyreferplus.data.model.CategoryInfo
import com.christelldev.easyreferplus.data.model.ServiceInfo
import com.christelldev.easyreferplus.data.network.CompanyRepository
import com.christelldev.easyreferplus.data.network.CompanyResult
import com.christelldev.easyreferplus.data.network.CompanyDetailResult
import com.christelldev.easyreferplus.data.network.UserCompaniesResult
import com.christelldev.easyreferplus.data.network.CompanySearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CompanyUiState(
    val isEditing: Boolean = false,
    val companyName: String = "",
    val companyDescription: String = "",
    val productDescription: String = "",
    val address: String = "",
    val city: String = "",
    val province: String = "",
    val commissionPercentage: String = "",
    val website: String = "",
    val facebookUrl: String = "",
    val instagramUrl: String = "",
    val whatsappNumber: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val companyId: Int? = null,
    val referralCode: String? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
    // Datos para dropdowns
    val provinces: List<String> = emptyList(),
    val cities: List<String> = emptyList(),
    val isLoadingLocations: Boolean = false,
    // Categoría y Servicio
    val categories: List<CategoryInfo> = emptyList(),
    val services: List<ServiceInfo> = emptyList(),
    val selectedCategory: CategoryInfo? = null,
    val selectedService: ServiceInfo? = null,
    val isLoadingCategories: Boolean = false,
    // Empresas del usuario
    val userCompanies: List<UserCompanyResponse> = emptyList(),
    val isLoadingCompanies: Boolean = false,
    // Empresas públicas (validadas)
    val publicCompanies: List<UserCompanyResponse> = emptyList(),
    val isLoadingPublicCompanies: Boolean = false,
    // Logo de empresa
    val logoUrl: String? = null,
    val hasLogo: Boolean = false,
    val isUploadingLogo: Boolean = false,
    val isDeletingLogo: Boolean = false,
    // Coordenadas de ubicación
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    // Category and Service IDs for editing existing company
    val categoryId: Int? = null,
    val serviceId: Int? = null
)

class CompanyViewModel(
    private val repository: CompanyRepository,
    private val getAccessToken: () -> String
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompanyUiState())
    val uiState: StateFlow<CompanyUiState> = _uiState.asStateFlow()

    private val authorization: String get() = "Bearer ${getAccessToken()}"

    init {
        // No cargar aquí, esperar a que la pantalla esté lista
    }

    fun initialize() {
        loadProvinces()
        loadCategories()
        loadMyCompany()
    }

    fun loadMyCompany() {
        // Verificar que el token esté disponible
        val token = getAccessToken()
        if (token.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Sesión no disponible"
            )
            return
        }
        viewModelScope.launch {
            // Limpiar campos de redes sociales antes de cargar nuevos datos para evitar valores residuales
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                website = "",
                facebookUrl = "",
                instagramUrl = "",
                whatsappNumber = ""
            )

            when (val result = repository.getMyCompany(authorization)) {
                is com.christelldev.easyreferplus.data.network.UserCompaniesResult.Success -> {
                    val company = result.companies.firstOrNull()
                    if (company != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSuccess = false, // Reset success state when loading
                            successMessage = null, // Reset success message
                            companyId = company.id,
                            companyName = company.companyName ?: "",
                            companyDescription = company.companyDescription ?: "",
                            productDescription = company.productDescription ?: "",
                            address = company.address ?: "",
                            city = company.city ?: "",
                            province = company.province ?: "",
                            commissionPercentage = company.commissionPercentage ?: "10",
                            website = company.website ?: "",
                            facebookUrl = company.facebookUrl ?: "",
                            instagramUrl = company.instagramUrl ?: "",
                            whatsappNumber = company.whatsappNumber ?: "",
                            latitude = company.latitude ?: 0.0,
                            longitude = company.longitude ?: 0.0,
                            logoUrl = company.logoUrl,
                            hasLogo = company.hasLogo ?: false,
                            isEditing = true,
                            categoryId = company.categoryId,
                            serviceId = company.serviceId
                        )
                        // Restore category and service selection after loading
                        restoreCategoryAndServiceSelection()
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = false, successMessage = null)
                    }
                }
                is com.christelldev.easyreferplus.data.network.UserCompaniesResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun loadProvinces() {
        // Las provincias no requieren token
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingLocations = true)
            when (val result = repository.getProvinces()) {
                is com.christelldev.easyreferplus.data.network.ProvinceResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        provinces = result.provinces,
                        isLoadingLocations = false
                    )
                }
                is com.christelldev.easyreferplus.data.network.ProvinceResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoadingLocations = false)
                }
            }
        }
    }

    fun loadCitiesByProvince(province: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingLocations = true, cities = emptyList())
            when (val result = repository.getCitiesByProvince(province)) {
                is com.christelldev.easyreferplus.data.network.CityResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        cities = result.cities,
                        isLoadingLocations = false
                    )
                }
                is com.christelldev.easyreferplus.data.network.CityResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoadingLocations = false)
                }
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingCategories = true)
            when (val result = repository.getCategories()) {
                is com.christelldev.easyreferplus.data.network.CategoryResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        categories = result.categories,
                        isLoadingCategories = false
                    )
                }
                is com.christelldev.easyreferplus.data.network.CategoryResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoadingCategories = false)
                }
            }
        }
    }

    fun loadServices(categoryId: Int? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingCategories = true)
            when (val result = repository.getServices(categoryId)) {
                is com.christelldev.easyreferplus.data.network.ServiceResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        services = result.services,
                        isLoadingCategories = false
                    )
                }
                is com.christelldev.easyreferplus.data.network.ServiceResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoadingCategories = false)
                }
            }
        }
    }

    fun selectCategory(category: CategoryInfo?) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            selectedService = null,
            services = emptyList()
        )
        if (category != null) {
            loadServices(category.id)
        }
    }

    fun selectService(service: ServiceInfo?) {
        _uiState.value = _uiState.value.copy(selectedService = service)
    }

    fun updateLocation(lat: Double, lng: Double) {
        _uiState.value = _uiState.value.copy(
            latitude = lat,
            longitude = lng
        )
    }

    fun restoreCategoryAndServiceSelection() {
        val state = _uiState.value
        val companyCategoryId = state.categoryId
        val companyServiceId = state.serviceId

        if (companyCategoryId == null) return

        // Wait for categories to be loaded if not yet
        if (state.categories.isEmpty()) {
            viewModelScope.launch {
                // Wait for categories to load
                kotlinx.coroutines.delay(500)
                restoreCategoryAndServiceSelection()
            }
            return
        }

        val category = state.categories.find { it.id == companyCategoryId }
        if (category != null) {
            _uiState.value = _uiState.value.copy(selectedCategory = category)
            // Load services for this category and then select the service
            viewModelScope.launch {
                loadServices(category.id)
                // After loading services, select the service
                kotlinx.coroutines.delay(200) // Small delay to ensure services are loaded
                val service = _uiState.value.services.find { it.id == companyServiceId }
                _uiState.value = _uiState.value.copy(selectedService = service)
            }
        }
    }

    fun updateCompanyName(value: String) {
        _uiState.value = _uiState.value.copy(
            companyName = value,
            fieldErrors = _uiState.value.fieldErrors - "company_name"
        )
    }

    fun updateCompanyDescription(value: String) {
        _uiState.value = _uiState.value.copy(
            companyDescription = value,
            fieldErrors = _uiState.value.fieldErrors - "company_description"
        )
    }

    fun updateProductDescription(value: String) {
        _uiState.value = _uiState.value.copy(
            productDescription = value,
            fieldErrors = _uiState.value.fieldErrors - "product_description"
        )
    }

    fun updateAddress(value: String) {
        _uiState.value = _uiState.value.copy(
            address = value,
            fieldErrors = _uiState.value.fieldErrors - "address"
        )
    }

    fun updateCity(value: String) {
        _uiState.value = _uiState.value.copy(
            city = value,
            fieldErrors = _uiState.value.fieldErrors - "city"
        )
    }

    fun updateProvince(value: String) {
        _uiState.value = _uiState.value.copy(
            province = value,
            city = "", // Reset city when province changes
            fieldErrors = _uiState.value.fieldErrors - "province"
        )
        if (value.isNotBlank()) {
            loadCitiesByProvince(value)
        }
    }

    fun updateCommissionPercentage(value: String) {
        _uiState.value = _uiState.value.copy(
            commissionPercentage = value.filter { it.isDigit() || it == '.' },
            fieldErrors = _uiState.value.fieldErrors - "commission_percentage"
        )
    }

    fun updateWebsite(value: String) {
        _uiState.value = _uiState.value.copy(website = value)
    }

    fun updateFacebookUrl(value: String) {
        _uiState.value = _uiState.value.copy(facebookUrl = value)
    }

    fun updateInstagramUrl(value: String) {
        _uiState.value = _uiState.value.copy(instagramUrl = value)
    }

    fun updateWhatsappNumber(value: String) {
        // Solo permitir dígitos, pero guardar con prefijo 593
        val digits = value.filter { it.isDigit() }
        val formatted = when {
            digits.startsWith("593") -> "+$digits"
            digits.startsWith("0") && digits.length > 1 -> "593${digits.substring(1)}"
            digits.isNotEmpty() && !digits.startsWith("593") -> "593$digits"
            else -> digits
        }
        _uiState.value = _uiState.value.copy(
            whatsappNumber = formatted,
            fieldErrors = _uiState.value.fieldErrors - "whatsapp_number"
        )
    }

    private fun validate(): Boolean {
        val state = _uiState.value
        val errors = mutableMapOf<String, String>()

        if (state.companyName.isBlank()) {
            errors["company_name"] = "El nombre de la empresa es obligatorio"
        } else if (state.companyName.length > 200) {
            errors["company_name"] = "Máximo 200 caracteres"
        }

        if (state.companyDescription.isBlank()) {
            errors["company_description"] = "La descripción de la empresa es obligatoria"
        } else if (state.companyDescription.length > 500) {
            errors["company_description"] = "Máximo 500 caracteres"
        }

        if (state.productDescription.isBlank()) {
            errors["product_description"] = "La descripción del producto es obligatoria"
        } else if (state.productDescription.length < 10) {
            errors["product_description"] = "Mínimo 10 caracteres"
        } else if (state.productDescription.length > 500) {
            errors["product_description"] = "Máximo 500 caracteres"
        }

        if (state.address.isBlank()) {
            errors["address"] = "La dirección es obligatoria"
        } else if (state.address.length > 500) {
            errors["address"] = "Máximo 500 caracteres"
        }

        if (state.city.isBlank()) {
            errors["city"] = "La ciudad es obligatoria"
        }

        if (state.province.isBlank()) {
            errors["province"] = "La provincia es obligatoria"
        }

        if (state.commissionPercentage.isBlank()) {
            errors["commission_percentage"] = "El porcentaje de comisión es obligatorio"
        } else {
            val commission = state.commissionPercentage.toDoubleOrNull()
            if (commission == null || commission < 1 || commission > 50) {
                errors["commission_percentage"] = "Debe estar entre 1% y 50%"
            }
        }

        _uiState.value = _uiState.value.copy(fieldErrors = errors)
        return errors.isEmpty()
    }

    fun registerCompany() {
        val token = getAccessToken()
        if (token.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Sesión no disponible. Por favor, inicia sesión nuevamente."
            )
            return
        }

        if (!validate()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Por favor complete todos los campos obligatorios"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val state = _uiState.value
            val request = RegisterCompanyRequest(
                companyName = state.companyName.trim(),
                companyDescription = state.companyDescription.trim(),
                productDescription = state.productDescription.trim(),
                address = state.address.trim(),
                city = state.city.trim(),
                province = state.province.trim(),
                latitude = state.latitude,
                longitude = state.longitude,
                commissionPercentage = state.commissionPercentage.toDoubleOrNull() ?: 10.0,
                website = state.website.trim().takeIf { it.isNotBlank() },
                facebookUrl = state.facebookUrl.trim().takeIf { it.isNotBlank() },
                instagramUrl = state.instagramUrl.trim().takeIf { it.isNotBlank() },
                whatsappNumber = state.whatsappNumber.trim().takeIf { it.isNotBlank() },
                initialProducts = null,
                categoryId = state.selectedCategory?.id,
                serviceId = state.selectedService?.id
            )

            when (val result = repository.registerCompany(authorization, request)) {
                is CompanyResult.RegisterSuccess -> {
                    val response = result.response
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        companyId = response.companyId,
                        successMessage = response.message
                    )
                }
                is CompanyResult.UpdateSuccess -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        successMessage = result.message
                    )
                }
                is CompanyResult.Error -> {
                    // Convertir errores de validación a mapa
                    val fieldErrorMap = result.errors?.associate { it.field to it.message } ?: emptyMap()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        fieldErrors = fieldErrorMap
                    )
                }
            }
        }
    }

    fun updateCompany() {
        val token = getAccessToken()
        if (token.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Sesión no disponible. Por favor, inicia sesión nuevamente."
            )
            return
        }

        val companyId = _uiState.value.companyId
        if (companyId == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "ID de empresa no encontrado")
            return
        }

        if (!validate()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Por favor complete todos los campos obligatorios"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val state = _uiState.value
            val request = UpdateCompanyRequest(
                companyName = state.companyName.trim(),
                companyDescription = state.companyDescription.trim(),
                productDescription = state.productDescription.trim(),
                address = state.address.trim(),
                city = state.city.trim(),
                province = state.province.trim(),
                latitude = state.latitude,
                longitude = state.longitude,
                commissionPercentage = state.commissionPercentage.toDoubleOrNull() ?: 10.0,
                website = state.website.trim().takeIf { it.isNotBlank() },
                facebookUrl = state.facebookUrl.trim().takeIf { it.isNotBlank() },
                instagramUrl = state.instagramUrl.trim().takeIf { it.isNotBlank() },
                whatsappNumber = state.whatsappNumber.trim().takeIf { it.isNotBlank() },
                initialProducts = null,
                categoryId = state.selectedCategory?.id,
                serviceId = state.selectedService?.id
            )

            when (val result = repository.updateCompany(authorization, request)) {
                is CompanyResult.UpdateSuccess -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        successMessage = result.message
                    )
                }
                is CompanyResult.RegisterSuccess -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        successMessage = result.response.message
                    )
                }
                is CompanyResult.Error -> {
                    val fieldErrorMap = result.errors?.associate { it.field to it.message } ?: emptyMap()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        fieldErrors = fieldErrorMap
                    )
                }
            }
        }
    }

    fun loadCompanyForEdit(companyId: Int) {
        val token = getAccessToken()
        if (token.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Sesión no disponible"
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            when (val result = repository.getCompanyById(authorization, companyId)) {
                is CompanyDetailResult.Success -> {
                    val company = result.company
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        companyId = company.id,
                        companyName = company.companyName ?: "",
                        companyDescription = company.companyDescription ?: "",
                        productDescription = company.productDescription ?: "",
                        address = company.address ?: "",
                        city = company.city ?: "",
                        province = company.province ?: "",
                        commissionPercentage = company.commissionPercentage?.toString() ?: "10",
                        website = company.website ?: "",
                        facebookUrl = company.facebookUrl ?: "",
                        instagramUrl = company.instagramUrl ?: "",
                        whatsappNumber = company.whatsappNumber ?: "",
                        logoUrl = company.logoUrl,
                        hasLogo = company.hasLogo ?: false
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

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            isSuccess = false
        )
    }

    fun loadUserCompanies() {
        val token = getAccessToken()
        if (token.isBlank()) {
            _uiState.value = _uiState.value.copy(
                userCompanies = emptyList(),
                isLoadingCompanies = false
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingCompanies = true)
            when (val result = repository.getMyCompany(authorization)) {
                is UserCompaniesResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        userCompanies = result.companies,
                        isLoadingCompanies = false
                    )
                }
                is UserCompaniesResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        userCompanies = emptyList(),
                        isLoadingCompanies = false
                    )
                }
            }
        }
    }

    fun loadPublicCompanies() {
        val token = getAccessToken()
        if (token.isBlank()) {
            _uiState.value = _uiState.value.copy(
                publicCompanies = emptyList(),
                isLoadingPublicCompanies = false
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingPublicCompanies = true)
            when (val result = repository.searchCompanies(authorization)) {
                is CompanySearchResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        publicCompanies = result.companies,
                        isLoadingPublicCompanies = false
                    )
                }
                is CompanySearchResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        publicCompanies = emptyList(),
                        isLoadingPublicCompanies = false
                    )
                }
            }
        }
    }

    // ==================== LOGO ====================

    fun uploadLogo(logoFile: java.io.File) {
        val token = getAccessToken()
        if (token.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Sesión no disponible"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploadingLogo = true, errorMessage = null)

            when (val result = repository.uploadLogo("Bearer $token", logoFile)) {
                is com.christelldev.easyreferplus.data.network.LogoResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isUploadingLogo = false,
                        logoUrl = result.logoUrl,
                        hasLogo = true,
                        successMessage = "Logo subido correctamente"
                    )
                }
                is com.christelldev.easyreferplus.data.network.LogoResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isUploadingLogo = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun deleteLogo() {
        val token = getAccessToken()
        if (token.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Sesión no disponible"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeletingLogo = true, errorMessage = null)

            when (val result = repository.deleteLogo("Bearer $token")) {
                is com.christelldev.easyreferplus.data.network.LogoResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isDeletingLogo = false,
                        logoUrl = null,
                        hasLogo = false,
                        successMessage = "Logo eliminado correctamente"
                    )
                }
                is com.christelldev.easyreferplus.data.network.LogoResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isDeletingLogo = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    class Factory(
        private val repository: CompanyRepository,
        private val getAccessToken: () -> String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CompanyViewModel::class.java)) {
                return CompanyViewModel(repository, getAccessToken) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
