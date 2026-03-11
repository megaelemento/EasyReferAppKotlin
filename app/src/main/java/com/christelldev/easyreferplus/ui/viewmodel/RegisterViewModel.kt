package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.network.RegisterError
import com.christelldev.easyreferplus.data.network.RegisterRepository
import com.christelldev.easyreferplus.data.network.RegisterResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class RegisterStep {
    PHONE,
    CODE,
    COMPLETE
}

data class RegisterUiState(
    // Teléfono
    val phone: String = "",
    val phoneError: String? = null,

    // Código SMS
    val code: String = "",
    val code1: String = "",
    val code2: String = "",
    val code3: String = "",
    val code4: String = "",
    val codeError: String? = null,
    val remainingAttempts: Int? = null,
    val verificationToken: String = "",

    // Datos de registro
    val nombres: String = "",
    val nombresError: String? = null,
    val apellidos: String = "",
    val apellidosError: String? = null,
    val cedulaRuc: String = "",
    val cedulaRucError: String? = null,
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val referralCode: String = "",
    val referralCodeError: String? = null,
    val isAdult: Boolean = false,
    val acceptsPrivacyPolicy: Boolean = false,
    val acceptsPrivacyPolicyError: String? = null,

    // UI State
    val currentStep: RegisterStep = RegisterStep.PHONE,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val waitSeconds: Int? = null,
    val isResendEnabled: Boolean = true,

    // Intentos de reenvío (máximo 3)
    val resendAttempts: Int = 0,
    val maxResendAttempts: Int = 3,

    // Política de privacidad
    val showPrivacyPolicyDialog: Boolean = false,
    val privacyPolicyTitle: String = "",
    val privacyPolicyContent: String = "",
    val isLoadingPrivacyPolicy: Boolean = false
)

class RegisterViewModel(
    private val registerRepository: RegisterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    // ========== TELÉFONO ==========

    fun updatePhone(phone: String) {
        val cleanPhone = phone.filter { it.isDigit() }.take(10)
        val phoneError = validatePhone(cleanPhone)
        _uiState.value = _uiState.value.copy(
            phone = cleanPhone,
            phoneError = phoneError
        )
    }

    fun verifyPhone() {
        val currentState = _uiState.value
        val cleanPhone = currentState.phone.filter { it.isDigit() }
        val phoneError = validatePhone(cleanPhone)

        if (phoneError != null) {
            _uiState.value = currentState.copy(phoneError = phoneError)
            return
        }

        val phoneWithoutZero = cleanPhone.removePrefix("0")
        val phoneWithPrefix = "+593$phoneWithoutZero"

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            when (val result = registerRepository.verifyPhone(phoneWithPrefix)) {
                is RegisterResult.VerifySuccess -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentStep = RegisterStep.CODE,
                        isResendEnabled = true,
                        waitSeconds = null
                    )
                }
                is RegisterResult.Error -> {
                    val errorMessage = getVerifyErrorMessage(result.registerError)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    )
                }
                is RegisterResult.RateLimitError -> {
                    val message = result.message ?: "Has alcanzado el límite de solicitudes. Intenta de nuevo más tarde."
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

    // ========== CÓDIGO SMS ==========

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
            codeError = null,
            remainingAttempts = null
        )

        // Auto-submit cuando se ingresa el 4to dígito
        if (code.length == 4) {
            confirmCode()
        }
    }

    fun confirmCode() {
        val currentState = _uiState.value
        val cleanCode = currentState.code

        if (cleanCode.length < 4) {
            _uiState.value = currentState.copy(codeError = "Ingresa los 4 dígitos")
            return
        }

        val phoneWithoutZero = currentState.phone.removePrefix("0")
        val phoneWithPrefix = "+593$phoneWithoutZero"

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            when (val result = registerRepository.confirmCode(phoneWithPrefix, cleanCode)) {
                is RegisterResult.ConfirmSuccess -> {
                    countdownJob?.cancel()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        verificationToken = result.verificationToken,
                        currentStep = RegisterStep.COMPLETE,
                        resendAttempts = 0,
                        isResendEnabled = true,
                        waitSeconds = null
                    )
                }
                is RegisterResult.Error -> {
                    val errorMessage = getConfirmErrorMessage(result.registerError, result.remainingAttempts)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorMessage,
                        remainingAttempts = result.remainingAttempts
                    )
                    // Limpiar código si error
                    if (result.registerError is RegisterError.MaxAttemptsExceeded) {
                        clearCode()
                    }
                }
                is RegisterResult.RateLimitError -> {
                    val message = result.message ?: "Demasiados intentos. Espera ${result.waitSeconds} segundos."
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        waitSeconds = result.waitSeconds,
                        isResendEnabled = false,
                        errorMessage = message
                    )
                    clearCode()
                    startCountdown(result.waitSeconds)
                }
                else -> {}
            }
        }
    }

    fun resendCode() {
        val currentState = _uiState.value

        // Verificar si ya alcanzó el máximo de intentos
        if (currentState.resendAttempts >= currentState.maxResendAttempts) {
            _uiState.value = currentState.copy(
                errorMessage = "Has excedido el número de reenvíos. Debes reiniciar el proceso de registro."
            )
            return
        }

        // Verificar si está en cooldown
        if (!currentState.isResendEnabled) {
            return
        }

        _uiState.value = currentState.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            when (val result = registerRepository.resendCode(currentState.phone)) {
                is RegisterResult.ResendSuccess -> {
                    val newAttempts = currentState.resendAttempts + 1
                    val remainingAttempts = currentState.maxResendAttempts - newAttempts
                    val message = if (remainingAttempts > 0) {
                        "Código reenviado. Te quedan $remainingAttempts intentos más."
                    } else {
                        "Código reenviado. Este es tu último intento."
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isResendEnabled = false,
                        waitSeconds = 60,
                        resendAttempts = newAttempts,
                        errorMessage = message
                    )
                }
                is RegisterResult.Error -> {
                    val errorMessage = getResendErrorMessage(result.registerError)
                    val isMaxAttempts = result.registerError is RegisterError.MaxResendAttemptsExceeded

                    if (isMaxAttempts) {
                        // Si excedió los intentos, reiniciar todo
                        goToPhoneStepWithMessage("Has excedido el número de reenvíos. Debes iniciar el registro nuevamente.")
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = errorMessage
                        )
                    }
                }
                is RegisterResult.RateLimitError -> {
                    val message = result.message ?: "Debes esperar ${result.waitSeconds} segundos antes de reenviar."
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isResendEnabled = false,
                        waitSeconds = result.waitSeconds,
                        errorMessage = message
                    )
                    startCountdown(result.waitSeconds)
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al reenviar código"
                    )
                }
            }
        }
    }

    private fun getResendErrorMessage(error: RegisterError): String {
        return when (error) {
            is RegisterError.MaxResendAttemptsExceeded -> "Has excedido el número de reenvíos. Debes reiniciar el proceso de registro."
            is RegisterError.GlobalRateLimitExceeded -> "Debes esperar antes de solicitar otro código de verificación"
            is RegisterError.GlobalVerificationLimitExceeded -> "Has alcanzado el límite de solicitudes de verificación. Intenta de nuevo más tarde."
            is RegisterError.RateLimited -> "Demasiadas solicitudes. Intente más tarde."
            else -> "Error al reenviar código"
        }
    }

    fun clearCode() {
        _uiState.value = _uiState.value.copy(
            code = "",
            code1 = "",
            code2 = "",
            code3 = "",
            code4 = "",
            codeError = null
        )
    }

    // ========== COMPLETAR REGISTRO ==========

    fun updateNombres(nombres: String) {
        val nombresError = validateNombres(nombres)
        _uiState.value = _uiState.value.copy(
            nombres = nombres,
            nombresError = nombresError
        )
    }

    fun updateApellidos(value: String) {
        val cleanValue = value.filter { !it.isDigit() }.take(50)
        val error = validateApellidos(cleanValue)
        _uiState.value = _uiState.value.copy(
            apellidos = cleanValue,
            apellidosError = error
        )
    }

    fun updateCedulaRuc(cedulaRuc: String) {
        val cleanCedula = cedulaRuc.filter { it.isDigit() }.take(13)
        val cedulaError = validateCedulaRuc(cleanCedula)
        _uiState.value = _uiState.value.copy(
            cedulaRuc = cleanCedula,
            cedulaRucError = cedulaError
        )
    }

    fun updateEmail(email: String) {
        val emailError = validateEmail(email)
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = emailError
        )
    }

    fun updatePassword(password: String) {
        val passwordError = validatePassword(password)
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = passwordError
        )
        if (_uiState.value.confirmPassword.isNotEmpty()) {
            val confirmError = validateConfirmPassword(password, _uiState.value.confirmPassword)
            _uiState.value = _uiState.value.copy(confirmPasswordError = confirmError)
        }
    }

    fun updateConfirmPassword(confirmPassword: String) {
        val confirmError = validateConfirmPassword(_uiState.value.password, confirmPassword)
        _uiState.value = _uiState.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = confirmError
        )
    }

    fun updateReferralCode(referralCode: String) {
        _uiState.value = _uiState.value.copy(referralCode = referralCode)
    }

    fun toggleIsAdult() {
        _uiState.value = _uiState.value.copy(isAdult = !_uiState.value.isAdult)
    }

    fun onPrivacyPolicyCheckClick() {
        // Al hacer clic en el checkbox, abrir el diálogo de políticas
        loadPrivacyPolicy()
    }

    fun acceptPrivacyPolicy() {
        // El usuario acepta la política desde el diálogo
        _uiState.value = _uiState.value.copy(
            acceptsPrivacyPolicy = true,
            acceptsPrivacyPolicyError = null,
            showPrivacyPolicyDialog = false
        )
    }

    fun dismissPrivacyPolicyDialog() {
        _uiState.value = _uiState.value.copy(
            showPrivacyPolicyDialog = false
        )
    }

    private fun loadPrivacyPolicy() {
        // Ya tiene la política cargada, mostrar diálogo
        if (_uiState.value.privacyPolicyTitle.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                showPrivacyPolicyDialog = true
            )
            return
        }

        // Cargar la política
        _uiState.value = _uiState.value.copy(
            isLoadingPrivacyPolicy = true,
            showPrivacyPolicyDialog = true
        )

        viewModelScope.launch {
            when (val result = registerRepository.getPrivacyPolicy()) {
                is RegisterRepository.PrivacyPolicyResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingPrivacyPolicy = false,
                        privacyPolicyTitle = result.title,
                        privacyPolicyContent = result.content
                    )
                }
                is RegisterRepository.PrivacyPolicyResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingPrivacyPolicy = false,
                        showPrivacyPolicyDialog = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(passwordVisible = !_uiState.value.passwordVisible)
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.value = _uiState.value.copy(confirmPasswordVisible = !_uiState.value.confirmPasswordVisible)
    }

    fun completeRegistration() {
        val currentState = _uiState.value

        // Limpiar errores de intentos anteriores
        _uiState.value = currentState.copy(
            errorMessage = null,
            nombresError = null,
            apellidosError = null,
            cedulaRucError = null,
            emailError = null,
            passwordError = null,
            confirmPasswordError = null,
            referralCodeError = null,
            acceptsPrivacyPolicyError = null
        )

        val nombresError = validateNombres(currentState.nombres)
        val apellidosError = validateApellidos(currentState.apellidos)
        val cedulaError = validateCedulaRuc(currentState.cedulaRuc)
        val emailError = validateEmail(currentState.email)
        val passwordError = validatePassword(currentState.password)
        val confirmError = validateConfirmPassword(currentState.password, currentState.confirmPassword)
        val privacyError = if (currentState.acceptsPrivacyPolicy) null else "Debe aceptar la política de privacidad"

        if (nombresError != null || apellidosError != null || cedulaError != null ||
            emailError != null || passwordError != null || confirmError != null || privacyError != null
        ) {
            _uiState.value = currentState.copy(
                nombresError = nombresError,
                apellidosError = apellidosError,
                cedulaRucError = cedulaError,
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmError,
                acceptsPrivacyPolicyError = privacyError
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val referralCode = currentState.referralCode.takeIf { it.isNotBlank() }

            when (val result = registerRepository.completeRegistration(
                verificationToken = currentState.verificationToken,
                referralCode = referralCode,
                cedulaRuc = currentState.cedulaRuc,
                nombres = currentState.nombres.trim().uppercase(),
                apellidos = currentState.apellidos.trim().uppercase(),
                email = currentState.email.trim().lowercase(),
                password = currentState.password,
                confirmPassword = currentState.confirmPassword,
                isAdult = currentState.isAdult,
                acceptsPrivacyPolicy = currentState.acceptsPrivacyPolicy
            )) {
                is RegisterResult.RegistrationSuccess -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
                is RegisterResult.Error -> {
                    // Si el token expiró, volver al paso de teléfono y mostrar mensaje
                    if (result.registerError is RegisterError.InvalidToken) {
                        val errorMsg = "La sesión expiró. Por favor, inicia el registro nuevamente."
                        goToPhoneStepWithMessage(errorMsg)
                        return@launch
                    }

                    // Si hay errores de validación específicos del backend, mostrarlos en los campos
                    val fieldErrors = if (result.registerError is RegisterError.ValidationError) {
                        (result.registerError as RegisterError.ValidationError).fieldErrors
                    } else {
                        emptyMap()
                    }

                    // Si hay un error general, usarlo como mensaje principal
                    val errorMessage = fieldErrors["general"] ?: getRegistrationErrorMessage(result.registerError)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorMessage,
                        nombresError = fieldErrors["nombres"],
                        apellidosError = fieldErrors["apellidos"],
                        cedulaRucError = fieldErrors["cedula_ruc"] ?: fieldErrors["general"],
                        emailError = fieldErrors["email"],
                        passwordError = fieldErrors["password"],
                        confirmPasswordError = fieldErrors["confirm_password"],
                        referralCodeError = fieldErrors["referral_code"]
                    )
                }
                else -> {}
            }
        }
    }

    // ========== NAVEGACIÓN ==========

    fun goToCodeStep() {
        countdownJob?.cancel()
        _uiState.value = _uiState.value.copy(
            currentStep = RegisterStep.CODE,
            errorMessage = null,
            waitSeconds = null,
            isResendEnabled = true
        )
    }

    fun goToPhoneStep() {
        // Limpiar todos los datos de registro cuando el token expira
        _uiState.value = _uiState.value.copy(
            currentStep = RegisterStep.PHONE,
            phone = "",
            phoneError = null,
            code = "",
            code1 = "",
            code2 = "",
            code3 = "",
            code4 = "",
            codeError = null,
            verificationToken = "",
            nombres = "",
            nombresError = null,
            apellidos = "",
            apellidosError = null,
            cedulaRuc = "",
            cedulaRucError = null,
            email = "",
            emailError = null,
            password = "",
            passwordError = null,
            confirmPassword = "",
            confirmPasswordError = null,
            referralCode = "",
            referralCodeError = null,
            isAdult = false,
            acceptsPrivacyPolicy = false,
            acceptsPrivacyPolicyError = null,
            isLoading = false,
            isSuccess = false,
            errorMessage = null,
            resendAttempts = 0,
            isResendEnabled = true,
            waitSeconds = null
        )
    }

    fun goToPhoneStepWithMessage(message: String) {
        // Limpiar todos los datos de registro cuando el token expira
        _uiState.value = _uiState.value.copy(
            currentStep = RegisterStep.PHONE,
            phone = "",
            phoneError = null,
            code = "",
            code1 = "",
            code2 = "",
            code3 = "",
            code4 = "",
            codeError = null,
            verificationToken = "",
            nombres = "",
            nombresError = null,
            apellidos = "",
            apellidosError = null,
            cedulaRuc = "",
            cedulaRucError = null,
            email = "",
            emailError = null,
            password = "",
            passwordError = null,
            confirmPassword = "",
            confirmPasswordError = null,
            referralCode = "",
            referralCodeError = null,
            isAdult = false,
            acceptsPrivacyPolicy = false,
            acceptsPrivacyPolicyError = null,
            isLoading = false,
            isSuccess = false,
            errorMessage = message,
            resendAttempts = 0,
            isResendEnabled = true,
            waitSeconds = null
        )
    }

    // ========== VALIDACIONES ==========

    private fun validatePhone(phone: String): String? {
        return when {
            phone.isEmpty() -> "El teléfono es obligatorio"
            phone.length < 10 -> "Debe tener exactamente 10 dígitos"
            phone.length > 10 -> "Debe tener exactamente 10 dígitos"
            else -> null
        }
    }

    private fun validateNombres(nombres: String): String? {
        return when {
            nombres.isBlank() -> "Los nombres son obligatorios"
            else -> null
        }
    }

    private fun validateApellidos(apellidos: String): String? {
        return when {
            apellidos.isBlank() -> "Los apellidos son obligatorios"
            else -> null
        }
    }

    private fun validateCedulaRuc(cedulaRuc: String): String? {
        // Validación básica - la validación completa de cédula/RUC la hace el backend
        return when {
            cedulaRuc.isBlank() -> "La cédula/RUC es obligatorio"
            cedulaRuc.length < 10 -> "Mínimo 10 dígitos"
            cedulaRuc.length > 13 -> "Máximo 13 dígitos"
            else -> null
        }
    }

    private fun validateEmail(email: String): String? {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return when {
            email.isBlank() -> "El email es obligatorio"
            !email.matches(emailRegex) -> "Formato de email inválido"
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

    private fun getVerifyErrorMessage(error: RegisterError): String {
        return when (error) {
            is RegisterError.PhoneAlreadyExists -> "El teléfono ya está registrado"
            is RegisterError.RateLimited -> "Demasiadas solicitudes. Intente más tarde."
            is RegisterError.GlobalRateLimitExceeded -> "Debes esperar antes de solicitar otro código de verificación"
            is RegisterError.GlobalVerificationLimitExceeded -> "Has alcanzado el límite de solicitudes de verificación. Intenta de nuevo más tarde."
            is RegisterError.ServerUnavailable -> "En mantenimiento, intente más tarde"
            else -> "Error al verificar el teléfono"
        }
    }

    private fun getConfirmErrorMessage(error: RegisterError, remainingAttempts: Int?): String {
        return when (error) {
            is RegisterError.InvalidCode -> {
                if (remainingAttempts != null) {
                    "Código incorrecto. Intentos restantes: $remainingAttempts"
                } else {
                    "Código incorrecto"
                }
            }
            is RegisterError.MaxAttemptsExceeded -> "Demasiados intentos fallidos. Solicita un nuevo código."
            is RegisterError.RateLimited -> "Demasiadas solicitudes. Intente más tarde."
            is RegisterError.GlobalRateLimitExceeded -> "Debes esperar antes de solicitar otro código de verificación"
            is RegisterError.GlobalVerificationLimitExceeded -> "Has alcanzado el límite de solicitudes de verificación. Intenta de nuevo más tarde."
            is RegisterError.ServerUnavailable -> "En mantenimiento, intente más tarde"
            else -> "Error al confirmar el código"
        }
    }

    private fun getRegistrationErrorMessage(error: RegisterError): String {
        return when (error) {
            is RegisterError.InvalidToken -> "Token de verificación inválido. Volver al inicio."
            is RegisterError.ValidationError -> {
                if (error.fieldErrors.isEmpty()) {
                    "Hay errores en los datos. Verifique los campos."
                } else {
                    "Verifique los campos marcados con error."
                }
            }
            is RegisterError.ServerUnavailable -> "En mantenimiento, intente más tarde"
            else -> "Error al completar el registro"
        }
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
            // Cuando termina el countdown, habilitar el botón de reenvío
            _uiState.value = _uiState.value.copy(
                waitSeconds = null,
                isResendEnabled = true
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    class Factory(private val registerRepository: RegisterRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                return RegisterViewModel(registerRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
