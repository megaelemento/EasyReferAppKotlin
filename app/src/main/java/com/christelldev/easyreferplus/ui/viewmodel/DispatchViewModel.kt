package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.ActiveDispatch
import com.christelldev.easyreferplus.data.model.DispatchDetail
import com.christelldev.easyreferplus.data.network.DispatchListResult
import com.christelldev.easyreferplus.data.network.DispatchDetailResult
import com.christelldev.easyreferplus.data.network.DispatchRepository
import com.christelldev.easyreferplus.data.network.SimpleResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DispatchUiState(
    val dispatches: List<ActiveDispatch> = emptyList(),
    val selectedDetail: DispatchDetail? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class DispatchViewModel(
    private val repository: DispatchRepository,
    private val getAccessToken: () -> String
) : ViewModel() {

    private val _uiState = MutableStateFlow(DispatchUiState())
    val uiState: StateFlow<DispatchUiState> = _uiState.asStateFlow()

    private val authorization: String get() = "Bearer ${getAccessToken()}"

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    fun loadActiveDispatches() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.getActiveDispatches(authorization)) {
                is DispatchListResult.Success -> {
                    _uiState.value = _uiState.value.copy(dispatches = result.dispatches, isLoading = false)
                }
                is DispatchListResult.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message, isLoading = false)
                }
            }
        }
    }

    fun loadDispatchDetail(orderId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.getDispatchDetail(authorization, orderId)) {
                is DispatchDetailResult.Success -> {
                    _uiState.value = _uiState.value.copy(selectedDetail = result.detail, isLoading = false)
                }
                is DispatchDetailResult.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message, isLoading = false)
                }
            }
        }
    }

    fun markOrderReadyPickup(orderId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.markOrderReadyPickup(authorization, orderId)) {
                is SimpleResponse.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = result.message,
                        isLoading = false
                    )
                    // Recargar detalle para ver el cambio de estado
                    loadDispatchDetail(orderId)
                }
                is SimpleResponse.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message, isLoading = false)
                }
            }
        }
    }

    fun markOrderDispatched(orderId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.markOrderDispatched(authorization, orderId)) {
                is SimpleResponse.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = result.message,
                        isLoading = false
                    )
                    // Recargar detalle
                    loadDispatchDetail(orderId)
                }
                is SimpleResponse.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message, isLoading = false)
                }
            }
        }
    }

    class Factory(
        private val repository: DispatchRepository,
        private val getAccessToken: () -> String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DispatchViewModel(repository, getAccessToken) as T
        }
    }
}
