package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.network.AuthRepository
import com.christelldev.easyreferplus.data.network.PasswordResetError
import com.christelldev.easyreferplus.data.network.PasswordResetRepository
import com.christelldev.easyreferplus.data.network.PasswordResetResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class PasswordResetStep {
    PHONE,
    CODE,
    NEW_PASSWORD,
    COMPLETE
}

data class PasswordResetUiState(
    val phone: String = "",
    val phoneError: String? = null,
    val code: String = "",
    val code1: String = "",
    val code2: String = "",
    val code3: String = "",
    val code4: String = "",
    val codeError: String? = null,
    val newPassword: String = "",
    val newPasswordError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val resetToken: String = "",

    val currentStep: PasswordResetStep = PasswordResetStep.PHONE,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isSuccess: Boolean = false,

    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,

    val waitSeconds: Int? = null,
    val isResendEnabled: Boolean = true,
    val resendAttempts: Int = 0,
    val maxResendAttempts: Int = 3
)

class PasswordResetViewModel(
    private val repository: PasswordResetRepository,
    private val authRepository: AuthRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(PasswordResetUiState())
    val uiState: StateFlow<PasswordResetUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    // Indica si el usuario estaba logueado antes de iniciar el reset
    private var wasLoggedIn: Boolean = false

    init {
        // Verificar si el usuario estaba logueado al iniciar el proceso de reset
        authRepository?.let {
            wasLoggedIn = it.isLoggedIn()
        }
    }

    // ========== PASAO 1: SOLICITAR CÓDIGO ==========

    fun updatePhone(phone: String) {
        val cleanPhone = phone.filter { it.isDigit() }.take(10)
        val phoneError = validatePhone(cleanPhone)
        _uiState.value = _uiState.value.copy(
            phone = cleanPhone,
            phoneError = phoneError
        )
    }

    fun requestPasswordReset() {
        val currentState = _uiState.value
        val phoneError = validatePhone(currentState.phone)

        if (phoneError != null) {
            _uiState.value = currentState.copy(phoneError = phoneError)
            return
        }

        val phoneWithPrefix = "+593${currentState.phone.removePrefix("0")}"

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, errorMessage = null)

            when (val result = repository.requestPasswordReset(phoneWithPrefix)) {
                is PasswordResetResult.RequestSuccess -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentStep = PasswordResetStep.CODE,
                        resendAttempts = 0,
                        isResendEnabled = true,
                        waitSeconds = null,
                        successMessage = "Código enviado a tu teléfono"
                    )
                }
                is PasswordResetResult.Error -> {
                    val errorMessage = getRequestErrorMessage(result.error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    )
                }
                is PasswordResetResult.RateLimitError -> {
                    val message = result.message ?: "Demasiadas solicitudes"
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        waitSeconds = result.waitSeconds,
                        isResendEnabled = false,
                        errorMessage = message
                    )
                    startCountdown(result.waitSeconds)
                }
                else -> {}
            }
        }
    }

    // ========== PASAO 2: VERIFICAR CÓDIGO ==========

    fun updateCode(digit: Int, value: String) {
        val cleanValue = value.filter { it.isDigit() }.take(1)
        val newCode1 = if (digit == 1) cleanValue else _uiState.value.code1
        val newCode2 = if (digit == 2) cleanValue else _uiState.value.code2
        val newCode3 = if (digit == 3) cleanValue else _uiState.value.code3
        val newCode4 = if (digit == 4) cleanValue else _uiState.value.code4

        val code = "$newCode1$newCode2$newCode3$newCode4"

        _uiState.value = _uiState.value.copy(
            code1 = newCode1,
            code2 = newCode2,
            code3 = newCode3,
            code4 = newCode4,
            code = code,
            codeError = null
        )
    }

    fun verifyResetCode() {
        val currentState = _uiState.value

        if (currentState.code.length < 4) {
            _uiState.value = currentState.copy(codeError = "Ingresa los 4 dígitos")
            return
        }

        val phoneWithPrefix = "+593${currentState.phone.removePrefix("0")}"

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, errorMessage = null)

            when (val result = repository.verifyResetCode(phoneWithPrefix, currentState.code)) {
                is PasswordResetResult.VerifySuccess -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentStep = PasswordResetStep.NEW_PASSWORD,
                        resetToken = result.resetToken,
                        successMessage = "Código verificado"
                    )
                }
                is PasswordResetResult.Error -> {
                    val errorMessage = getVerifyErrorMessage(result.error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorMessage,
                        code = "",
                        code1 = "",
                        code2 = "",
                        code3 = "",
                        code4 = ""
                    )
                }
                else -> {}
            }
        }
    }

    fun resendCode() {
        val currentState = _uiState.value

        // Verificar máximo de intentos
        if (currentState.resendAttempts >= currentState.maxResendAttempts) {
            _uiState.value = currentState.copy(
                errorMessage = "Has excedido el número de reenvíos. Debes iniciar el proceso nuevamente."
            )
            return
        }

        if (!currentState.isResendEnabled) {
            return
        }

        val phoneWithPrefix = "+593${currentState.phone.removePrefix("0")}"

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, errorMessage = null)

            when (val result = repository.resendResetCode(phoneWithPrefix)) {
                is PasswordResetResult.ResendSuccess -> {
                    val newAttempts = currentState.resendAttempts + 1
                    val remaining = currentState.maxResendAttempts - newAttempts
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isResendEnabled = false,
                        waitSeconds = result.waitTime,
                        resendAttempts = newAttempts,
                        successMessage = if (remaining > 0) "Código reenviado. Te quedan $remaining intentos." else "Código reenviado. Último intento."
                    )
                    startCountdown(result.waitTime)
                }
                is PasswordResetResult.Error -> {
                    val errorMessage = getResendErrorMessage(result.error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    )
                }
                is PasswordResetResult.RateLimitError -> {
                    val message = result.message ?: "Demasiadas solicitudes"
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isResendEnabled = false,
                        waitSeconds = result.waitSeconds,
                        errorMessage = message
                    )
                    startCountdown(result.waitSeconds)
                }
                else -> {}
            }
        }
    }

    // ========== PASAO 3: NUEVA CONTRASEÑA ==========

    fun updateNewPassword(password: String) {
        val passwordError = validatePassword(password)
        _uiState.value = _uiState.value.copy(
            newPassword = password,
            newPasswordError = passwordError
        )
        // Validar confirmación cuando cambia la contraseña
        if (_uiState.value.confirmPassword.isNotEmpty()) {
            val confirmError = validateConfirmPassword(password, _uiState.value.confirmPassword)
            _uiState.value = _uiState.value.copy(confirmPasswordError = confirmError)
        }
    }

    fun updateConfirmPassword(confirmPassword: String) {
        val confirmError = validateConfirmPassword(_uiState.value.newPassword, confirmPassword)
        _uiState.value = _uiState.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = confirmError
        )
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(passwordVisible = !_uiState.value.passwordVisible)
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.value = _uiState.value.copy(confirmPasswordVisible = !_uiState.value.confirmPasswordVisible)
    }

    fun resetPassword() {
        val currentState = _uiState.value

        // Validaciones locales
        val passwordError = validatePassword(currentState.newPassword)
        val confirmError = validateConfirmPassword(currentState.newPassword, currentState.confirmPassword)

        if (passwordError != null || confirmError != null) {
            _uiState.value = currentState.copy(
                newPasswordError = passwordError,
                confirmPasswordError = confirmError
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, errorMessage = null)

            when (val result = repository.resetPassword(
                currentState.resetToken,
                currentState.newPassword,
                currentState.confirmPassword
            )) {
                is PasswordResetResult.ResetSuccess -> {
                    // Si el usuario estaba logueado, cerrar sesión (invalidar tokens)
                    // Esto es necesario porque la contraseña cambió y los tokens anteriores ya no son válidos
                    if (wasLoggedIn) {
                        authRepository?.clearAllData()
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentStep = PasswordResetStep.COMPLETE,
                        isSuccess = true,
                        successMessage = "Contraseña actualizada exitosamente"
                    )
                }
                is PasswordResetResult.Error -> {
                    val errorMessage = getResetErrorMessage(result.error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    )
                }
                else -> {}
            }
        }
    }

    // ========== NAVEGACIÓN ==========

    fun goToPhoneStep() {
        countdownJob?.cancel()
        _uiState.value = PasswordResetUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    // ========== TIMER ==========

    private fun startCountdown(seconds: Int) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1000)
                remaining--
                _uiState.value = _uiState.value.copy(
                    waitSeconds = remaining,
                    isResendEnabled = remaining == 0
                )
            }
            _uiState.value = _uiState.value.copy(
                waitSeconds = null,
                isResendEnabled = true
            )
        }
    }

    // ========== VALIDACIONES ==========

    private fun validatePhone(phone: String): String? {
        return when {
            phone.isEmpty() -> "El teléfono es obligatorio"
            phone.length < 10 -> "Debe tener exactamente 10 dígitos"
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "La contraseña es obligatoria"
            password.length < 8 -> "Mínimo 8 caracteres"
            !password.any { it.isUpperCase() } -> "Debe tener al menos una mayúscula"
            !password.any { it.isLowerCase() } -> "Debe tener al menos una minúscula"
            !password.any { it.isDigit() } -> "Debe tener al menos un número"
            else -> null
        }
    }

    private fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isBlank() -> "Confirme la contraseña"
            password != confirmPassword -> "Las contraseñas no coinciden"
            else -> null
        }
    }

    // ========== MENSAJES DE ERROR ==========

    private fun getRequestErrorMessage(error: PasswordResetError): String {
        return when (error) {
            is PasswordResetError.UserNotFound -> "No existe una cuenta asociada a este número de teléfono"
            is PasswordResetError.AccountDisabled -> "Cuenta desactivada. Contacte al administrador"
            is PasswordResetError.PhoneNotVerified -> "Debe verificar su número de teléfono antes de restablecer la contraseña"
            is PasswordResetError.DailyLimitExceeded -> "Has alcanzado el límite diario de solicitudes. Intenta mañana."
            is PasswordResetError.ServerUnavailable -> "En mantenimiento, intente más tarde"
            is PasswordResetError.Unknown -> error.message ?: "Error al solicitar código"
            else -> "Error al solicitar código"
        }
    }

    private fun getVerifyErrorMessage(error: PasswordResetError): String {
        return when (error) {
            is PasswordResetError.InvalidCode -> {
                if (error.message?.contains("Intentos restantes") == true) {
                    error.message!!
                } else {
                    "Código incorrecto"
                }
            }
            is PasswordResetError.TooManyAttempts -> error.message ?: "Demasiados intentos. Solicita un nuevo código."
            is PasswordResetError.ServerUnavailable -> "En mantenimiento, intente más tarde"
            is PasswordResetError.Unknown -> error.message ?: "Error al verificar código"
            else -> "Error al verificar código"
        }
    }

    private fun getResendErrorMessage(error: PasswordResetError): String {
        return when (error) {
            is PasswordResetError.DailyLimitExceeded -> "Has alcanzado el límite diario de solicitudes. Intenta mañana."
            is PasswordResetError.ServerUnavailable -> "En mantenimiento, intente más tarde"
            is PasswordResetError.Unknown -> error.message ?: "Error al reenviar código"
            else -> "Error al reenviar código"
        }
    }

    private fun getResetErrorMessage(error: PasswordResetError): String {
        return when (error) {
            is PasswordResetError.InvalidToken -> "El token ha expirado. Debes iniciar el proceso nuevamente."
            is PasswordResetError.WeakPassword -> {
                val errors = error.errors.joinToString(". ")
                "Contraseña débil: $errors"
            }
            is PasswordResetError.SamePassword -> "La nueva contraseña debe ser diferente a la actual"
            is PasswordResetError.ValidationError -> {
                if (error.fieldErrors.isNotEmpty()) {
                    error.fieldErrors.values.firstOrNull() ?: "Error de validación"
                } else {
                    "Error de validación"
                }
            }
            is PasswordResetError.ServerUnavailable -> "En mantenimiento, intente más tarde"
            is PasswordResetError.Unknown -> error.message ?: "Error al cambiar contraseña"
            else -> "Error al cambiar contraseña"
        }
    }

    class Factory(
        private val repository: PasswordResetRepository,
        private val authRepository: AuthRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PasswordResetViewModel::class.java)) {
                return PasswordResetViewModel(repository, authRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
