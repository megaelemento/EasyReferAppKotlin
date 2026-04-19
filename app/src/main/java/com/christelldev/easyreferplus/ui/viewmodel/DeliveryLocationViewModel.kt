package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.SaveLocationRequest
import com.christelldev.easyreferplus.data.model.UserSavedLocation
import com.christelldev.easyreferplus.data.network.DeliveryLocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DeliveryLocationViewModel(
    private val repository: DeliveryLocationRepository,
    private val getToken: () -> String?
) : ViewModel() {

    private val _locations = MutableStateFlow<List<UserSavedLocation>>(emptyList())
    val locations: StateFlow<List<UserSavedLocation>> = _locations.asStateFlow()

    val favorites: StateFlow<List<UserSavedLocation>> = _locations
        .map { it.filter { loc -> loc.isFavorite } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val history: StateFlow<List<UserSavedLocation>> = _locations
        .map { it.filter { loc -> !loc.isFavorite }.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadLocations()
    }

    fun loadLocations() {
        viewModelScope.launch {
            _isLoading.value = true
            val token = getToken() ?: run { _isLoading.value = false; return@launch }
            val result = repository.getLocations("Bearer $token")
            if (result is DeliveryLocationRepository.Result.Success) {
                _locations.value = result.data
            }
            _isLoading.value = false
        }
    }

    fun saveLocation(address: String, lat: Double, lng: Double, alias: String? = null) {
        viewModelScope.launch {
            val token = getToken() ?: return@launch
            repository.saveLocation("Bearer $token", SaveLocationRequest(alias, address, lat, lng))
            loadLocations()
        }
    }

    fun toggleFavorite(id: Int, currentIsFavorite: Boolean) {
        // Optimistic update
        _locations.value = _locations.value.map {
            if (it.id == id) it.copy(isFavorite = !currentIsFavorite) else it
        }
        viewModelScope.launch {
            val token = getToken() ?: return@launch
            val result = repository.toggleFavorite("Bearer $token", id, !currentIsFavorite)
            if (result is DeliveryLocationRepository.Result.Error) {
                // Revert on failure
                _locations.value = _locations.value.map {
                    if (it.id == id) it.copy(isFavorite = currentIsFavorite) else it
                }
            }
        }
    }

    fun deleteLocation(id: Int) {
        viewModelScope.launch {
            val token = getToken() ?: return@launch
            val result = repository.deleteLocation("Bearer $token", id)
            if (result is DeliveryLocationRepository.Result.Success) {
                _locations.value = _locations.value.filter { it.id != id }
            }
        }
    }
}
