package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.LoginRequest
import com.christelldev.easyreferplus.data.network.AuthError
import com.christelldev.easyreferplus.data.network.AuthRepository
import com.christelldev.easyreferplus.data.network.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val phone: String = "",
    val password: String = "",
    val phoneError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val isPasswordVisible: Boolean = false
)

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updatePhone(phone: String) {
        val cleanPhone = phone.filter { it.isDigit() }.take(10)
        val phoneError = validatePhone(cleanPhone)
        _uiState.value = _uiState.value.copy(
            phone = cleanPhone,
            phoneError = phoneError
        )
    }

    fun updatePassword(password: String) {
        val passwordError = validatePassword(password)
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = passwordError
        )
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isPasswordVisible = !_uiState.value.isPasswordVisible
        )
    }

    fun login() {
        val currentState = _uiState.value
        val cleanPhone = currentState.phone.filter { it.isDigit() }

        val phoneError = validatePhone(cleanPhone)
        val passwordError = validatePassword(currentState.password)

        if (phoneError != null || passwordError != null) {
            _uiState.value = currentState.copy(
                phoneError = phoneError,
                passwordError = passwordError
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val phoneWithoutZero = cleanPhone.removePrefix("0")
            val phoneWithPrefix = "+593$phoneWithoutZero"

            val request = LoginRequest(
                phone = phoneWithPrefix,
                password = currentState.password
            )

            when (val result = authRepository.login(request)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
                is AuthResult.Error -> {
                    val errorMessage = getErrorMessage(result.authError)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    )
                }
            }
        }
    }

    private fun getErrorMessage(error: AuthError): String {
        return when (error) {
            is AuthError.InvalidCredentials -> "Usuario o contraseña incorrectos"
            is AuthError.InvalidPhone -> "El número de teléfono no existe"
            is AuthError.AccountBlocked -> "Tu cuenta ha sido bloqueada"
            is AuthError.TooManyAttempts -> "Demasiados intentos. Bloqueado temporalmente"
            is AuthError.ServerUnavailable -> "Sin conexión al servidor. Verifique su red WiFi"
            is AuthError.Unknown -> error.message
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun validatePhone(phone: String): String? {
        return when {
            phone.isEmpty() -> "El teléfono es obligatorio"
            phone.length < 10 -> "Debe tener exactamente 10 dígitos"
            phone.length > 10 -> "Debe tener exactamente 10 dígitos"
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isEmpty() -> "La contraseña es obligatoria"
            password.length < 6 -> "Mínimo 6 caracteres"
            else -> null
        }
    }

    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                return LoginViewModel(authRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
