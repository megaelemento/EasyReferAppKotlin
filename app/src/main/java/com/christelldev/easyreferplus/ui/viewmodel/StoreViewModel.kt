package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.network.CompanyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StoreUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isToggling: Boolean = false,
    val storeEnabled: Boolean = false,
    val storeSlug: String = "",
    val templateId: Int = 1,
    val primaryColor: String = "#6366f1",
    val secondaryColor: String = "#f59e0b",
    val tagline: String = "",
    val font: String = "inter",
    val storeUrl: String? = null,
    val productsCount: Int = 0,
    val isValidated: Boolean = false,
    val slugCheckResult: SlugCheckResult = SlugCheckResult.Idle,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

sealed class SlugCheckResult {
    data object Idle : SlugCheckResult()
    data object Checking : SlugCheckResult()
    data object Available : SlugCheckResult()
    data class Taken(val reason: String) : SlugCheckResult()
}

class StoreViewModel(
    private val repository: CompanyRepository,
    private val getToken: () -> String
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreUiState())
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    private var slugCheckJob: Job? = null

    fun loadStore() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val token = "Bearer ${getToken()}"
            val result = repository.getMyStore(token)
            result.onSuccess { store ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    storeEnabled = store.storeEnabled,
                    storeSlug = store.storeSlug ?: "",
                    templateId = store.storeTemplateId,
                    primaryColor = store.storePrimaryColor,
                    secondaryColor = store.storeSecondaryColor,
                    tagline = store.storeTagline ?: "",
                    font = store.storeFont,
                    storeUrl = store.storeUrl,
                    productsCount = store.productsCount,
                    isValidated = store.isValidated
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = it.message
                )
            }
        }
    }

    fun onSlugChange(slug: String) {
        val cleaned = slug.lowercase().replace(" ", "-").take(50)
        _uiState.value = _uiState.value.copy(storeSlug = cleaned, slugCheckResult = SlugCheckResult.Idle)
        slugCheckJob?.cancel()
        if (cleaned.length >= 3) {
            slugCheckJob = viewModelScope.launch {
                delay(600)
                _uiState.value = _uiState.value.copy(slugCheckResult = SlugCheckResult.Checking)
                val result = repository.checkSlug(cleaned)
                _uiState.value = _uiState.value.copy(
                    slugCheckResult = when {
                        result == null -> SlugCheckResult.Idle
                        result.available -> SlugCheckResult.Available
                        else -> SlugCheckResult.Taken(result.reason ?: "No disponible")
                    }
                )
            }
        }
    }

    fun onTemplateChange(id: Int) { _uiState.value = _uiState.value.copy(templateId = id) }
    fun onPrimaryColorChange(color: String) { _uiState.value = _uiState.value.copy(primaryColor = color) }
    fun onSecondaryColorChange(color: String) { _uiState.value = _uiState.value.copy(secondaryColor = color) }
    fun onTaglineChange(text: String) { _uiState.value = _uiState.value.copy(tagline = text.take(300)) }
    fun onFontChange(font: String) { _uiState.value = _uiState.value.copy(font = font) }

    fun saveStore() {
        val state = _uiState.value
        if (state.storeSlug.length < 3) {
            _uiState.value = state.copy(errorMessage = "El nombre de la tienda debe tener al menos 3 caracteres.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            val token = "Bearer ${getToken()}"
            val result = repository.setupStore(
                authorization = token,
                slug = state.storeSlug,
                templateId = state.templateId,
                primaryColor = state.primaryColor,
                secondaryColor = state.secondaryColor,
                tagline = state.tagline,
                font = state.font
            )
            result.onSuccess { resp ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    storeEnabled = resp.storeEnabled,
                    storeUrl = resp.storeUrl,
                    successMessage = resp.message.ifEmpty { "¡Tienda configurada y activada!" }
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = it.message)
            }
        }
    }

    fun toggleStore() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isToggling = true)
            val token = "Bearer ${getToken()}"
            val result = repository.toggleStore(token)
            result.onSuccess { resp ->
                _uiState.value = _uiState.value.copy(
                    isToggling = false,
                    storeEnabled = resp.storeEnabled,
                    successMessage = resp.message
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(isToggling = false, errorMessage = it.message)
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }

    class Factory(
        private val repository: CompanyRepository,
        private val getToken: () -> String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            StoreViewModel(repository, getToken) as T
    }
}
