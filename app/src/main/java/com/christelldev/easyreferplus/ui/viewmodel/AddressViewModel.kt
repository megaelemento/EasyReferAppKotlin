package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.*
import com.christelldev.easyreferplus.data.network.AddressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AddressListState {
    object Idle : AddressListState()
    object Loading : AddressListState()
    data class Success(val addresses: List<SavedAddress>) : AddressListState()
    data class Error(val message: String) : AddressListState()
}

sealed class AddressSaveState {
    object Idle : AddressSaveState()
    object Saving : AddressSaveState()
    data class Success(val address: SavedAddress) : AddressSaveState()
    data class Error(val message: String) : AddressSaveState()
}

class AddressViewModel(
    private val repository: AddressRepository,
    private val getToken: () -> String
) : ViewModel() {

    private val _listState = MutableStateFlow<AddressListState>(AddressListState.Idle)
    val listState: StateFlow<AddressListState> = _listState.asStateFlow()

    private val _saveState = MutableStateFlow<AddressSaveState>(AddressSaveState.Idle)
    val saveState: StateFlow<AddressSaveState> = _saveState.asStateFlow()

    private val _addresses = MutableStateFlow<List<SavedAddress>>(emptyList())
    val addresses: StateFlow<List<SavedAddress>> = _addresses.asStateFlow()

    fun loadAddresses() {
        viewModelScope.launch {
            _listState.value = AddressListState.Loading
            val token = "Bearer ${getToken()}"
            when (val result = repository.getSavedAddresses(token)) {
                is AddressRepository.Result.Success -> {
                    _addresses.value = result.data
                    _listState.value = AddressListState.Success(result.data)
                }
                is AddressRepository.Result.Error -> {
                    _listState.value = AddressListState.Error(result.message)
                }
            }
        }
    }

    fun createAddress(label: String, address: String, lat: Double, lng: Double, isDefault: Boolean = false) {
        viewModelScope.launch {
            _saveState.value = AddressSaveState.Saving
            val token = "Bearer ${getToken()}"
            val request = CreateAddressRequest(label, address, lat, lng, isDefault)
            when (val result = repository.createAddress(token, request)) {
                is AddressRepository.Result.Success -> {
                    _saveState.value = AddressSaveState.Success(result.data)
                    loadAddresses()
                }
                is AddressRepository.Result.Error -> {
                    _saveState.value = AddressSaveState.Error(result.message)
                }
            }
        }
    }

    fun updateAddress(addressId: Int, label: String? = null, address: String? = null,
                      lat: Double? = null, lng: Double? = null, isDefault: Boolean? = null) {
        viewModelScope.launch {
            _saveState.value = AddressSaveState.Saving
            val token = "Bearer ${getToken()}"
            val request = UpdateAddressRequest(label, address, lat, lng, isDefault)
            when (val result = repository.updateAddress(token, addressId, request)) {
                is AddressRepository.Result.Success -> {
                    _saveState.value = AddressSaveState.Success(result.data)
                    loadAddresses()
                }
                is AddressRepository.Result.Error -> {
                    _saveState.value = AddressSaveState.Error(result.message)
                }
            }
        }
    }

    fun deleteAddress(addressId: Int) {
        viewModelScope.launch {
            val token = "Bearer ${getToken()}"
            when (repository.deleteAddress(token, addressId)) {
                is AddressRepository.Result.Success -> loadAddresses()
                is AddressRepository.Result.Error -> { /* silently fail, list stays */ }
            }
        }
    }

    fun resetSaveState() {
        _saveState.value = AddressSaveState.Idle
    }

    class Factory(
        private val repository: AddressRepository,
        private val getToken: () -> String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddressViewModel(repository, getToken) as T
        }
    }
}
