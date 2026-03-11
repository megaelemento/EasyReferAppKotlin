package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.SessionInfo
import com.christelldev.easyreferplus.data.network.LogoutAllResult
import com.christelldev.easyreferplus.data.network.InvalidateResult
import com.christelldev.easyreferplus.data.network.SessionRepository
import com.christelldev.easyreferplus.data.network.SessionsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SessionsUiState(
    val sessions: List<SessionInfo> = emptyList(),
    val maxSessions: Int = 3,
    val isLoading: Boolean = false,
    val isRevoking: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val requireReLogin: Boolean = false
)

class SessionViewModel(
    private val sessionRepository: SessionRepository,
    private val getAccessToken: () -> String
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionsUiState())
    val uiState: StateFlow<SessionsUiState> = _uiState.asStateFlow()

    private val authorization: String get() = "Bearer ${getAccessToken()}"

    // Bandera para evitar cargas duplicadas
    private var isFirstLoad = true

    init {
        // No cargar aquí, esperar a que la pantalla esté lista
    }

    fun loadSessions() {
        // Evitar cargar si el token está vacío
        val token = getAccessToken()
        if (token.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Sesión no disponible. Por favor, inicia sesión nuevamente."
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            when (val result = sessionRepository.getSessions(authorization)) {
                is SessionsResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        sessions = result.sessions,
                        maxSessions = result.maxSessions,
                        isLoading = false,
                        requireReLogin = false
                    )
                }
                is SessionsResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun invalidateSession(sessionId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRevoking = true, errorMessage = null, successMessage = null)

            when (val result = sessionRepository.invalidateSession(authorization, sessionId)) {
                is InvalidateResult.Success -> {
                    // If the current session was invalidated, user needs to re-login
                    if (result.isCurrentSession) {
                        _uiState.value = _uiState.value.copy(
                            isRevoking = false,
                            successMessage = result.message,
                            requireReLogin = true
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isRevoking = false,
                            successMessage = result.message
                        )
                        // Reload sessions
                        loadSessions()
                    }
                }
                is InvalidateResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isRevoking = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun logoutAllExceptCurrent() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRevoking = true, errorMessage = null, successMessage = null)

            when (val result = sessionRepository.logoutAllExceptCurrent(authorization)) {
                is LogoutAllResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isRevoking = false,
                        successMessage = result.message
                    )
                    // Reload sessions
                    loadSessions()
                }
                is LogoutAllResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isRevoking = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun forceLogoutAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRevoking = true, errorMessage = null, successMessage = null)

            when (val result = sessionRepository.forceLogoutAll(authorization)) {
                is LogoutAllResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isRevoking = false,
                        successMessage = result.message,
                        requireReLogin = true
                    )
                }
                is LogoutAllResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isRevoking = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    class Factory(
        private val sessionRepository: SessionRepository,
        private val getAccessToken: () -> String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SessionViewModel::class.java)) {
                return SessionViewModel(sessionRepository, getAccessToken) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
