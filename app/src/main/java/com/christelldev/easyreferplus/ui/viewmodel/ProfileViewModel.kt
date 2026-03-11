package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.UpdateProfileRequest
import com.christelldev.easyreferplus.data.model.UserProfile
import com.christelldev.easyreferplus.data.network.ProfileRepository
import com.christelldev.easyreferplus.data.network.ProfileResult
import com.christelldev.easyreferplus.data.network.PhoneVerificationResult
import com.christelldev.easyreferplus.data.network.SelfieResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val nombres: String = "",
    val apellidos: String = "",
    val email: String = "",
    val phone: String = "",
    val cedulaRuc: String = "",
    val isRuc: Boolean = false,
    val empresaNombre: String? = null,
    val referralCode: String? = null,
    val phoneVerified: Boolean = false,
    val isVerified: Boolean = false,
    val hasCompany: Boolean = false,
    val createdAt: String? = null,
    val selfieUrl: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val pendingPhone: String? = null,
    val isPhoneVerificationPending: Boolean = false,
    // Estados para verificación de teléfono
    val isChangingPhone: Boolean = false,
    val newPhone: String = "",
    val verificationCode: String = "",
    val isSendingCode: Boolean = false,
    val isVerifyingCode: Boolean = false,
    val codeSent: Boolean = false,
    val resendTimer: Int = 0,
    // Estados para selfie
    val isUploadingSelfie: Boolean = false,
    val isDeletingSelfie: Boolean = false,
    val pendingSelfieUri: String? = null
)

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val getAccessToken: () -> String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val authorization: String get() = "Bearer ${getAccessToken()}"

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            when (val result = repository.getProfile(authorization)) {
                is ProfileResult.Success -> {
                    val profile = result.profile
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        nombres = profile.nombres,
                        apellidos = profile.apellidos,
                        email = profile.email,
                        phone = profile.phone ?: "",
                        cedulaRuc = profile.cedulaRuc,
                        isRuc = profile.isRuc,
                        empresaNombre = profile.empresaNombre,
                        referralCode = profile.referralCode,
                        phoneVerified = profile.phoneVerified,
                        isVerified = profile.isVerified,
                        hasCompany = profile.hasCompany,
                        createdAt = profile.createdAt,
                        selfieUrl = profile.selfieUrl
                    )
                }
                is ProfileResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun updateNombres(value: String) {
        _uiState.value = _uiState.value.copy(nombres = value)
    }

    fun updateApellidos(value: String) {
        _uiState.value = _uiState.value.copy(apellidos = value)
    }

    fun updateEmail(value: String) {
        _uiState.value = _uiState.value.copy(email = value)
    }

    fun updatePhone(value: String) {
        _uiState.value = _uiState.value.copy(phone = value)
    }

    fun updateCedulaRuc(value: String) {
        _uiState.value = _uiState.value.copy(cedulaRuc = value)
    }

    // Funciones para cambio de teléfono
    fun startChangePhone() {
        _uiState.value = _uiState.value.copy(
            isChangingPhone = true,
            newPhone = _uiState.value.phone,
            verificationCode = "",
            codeSent = false,
            errorMessage = null
        )
    }

    fun cancelChangePhone() {
        _uiState.value = _uiState.value.copy(
            isChangingPhone = false,
            newPhone = "",
            verificationCode = "",
            codeSent = false
        )
    }

    fun updateNewPhone(value: String) {
        _uiState.value = _uiState.value.copy(newPhone = value)
    }

    fun updateVerificationCode(value: String) {
        _uiState.value = _uiState.value.copy(verificationCode = value)
    }

    fun sendVerificationCode() {
        val phone = _uiState.value.newPhone.trim()
        if (phone.isBlank() || phone.length != 10) {
            _uiState.value = _uiState.value.copy(errorMessage = "Ingrese un teléfono válido de 10 dígitos")
            return
        }

        // Convertir al formato del backend: +593987654321
        val phoneWithoutZero = phone.removePrefix("0")
        val phoneWithPrefix = "+593$phoneWithoutZero"

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSendingCode = true, errorMessage = null)

            when (val result = repository.sendPhoneVerificationCode(phoneWithPrefix)) {
                is PhoneVerificationResult.CodeSent -> {
                    _uiState.value = _uiState.value.copy(
                        isSendingCode = false,
                        codeSent = true,
                        resendTimer = 60
                    )
                    // Iniciar timer para reenvío
                    startResendTimer()
                }
                is PhoneVerificationResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSendingCode = false,
                        errorMessage = result.message
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isSendingCode = false)
                }
            }
        }
    }

    private fun startResendTimer() {
        viewModelScope.launch {
            while (_uiState.value.resendTimer > 0) {
                delay(1000)
                _uiState.value = _uiState.value.copy(resendTimer = _uiState.value.resendTimer - 1)
            }
        }
    }

    fun resendCode() {
        if (_uiState.value.resendTimer > 0) return
        sendVerificationCode()
    }

    fun confirmPhoneChange() {
        val phone = _uiState.value.newPhone.trim()
        val code = _uiState.value.verificationCode.trim()

        if (code.isBlank() || code.length < 4) {
            _uiState.value = _uiState.value.copy(errorMessage = "Ingrese el código de verificación")
            return
        }

        // Convertir al formato del backend: +593987654321
        val phoneWithoutZero = phone.removePrefix("0")
        val phoneWithPrefix = "+593$phoneWithoutZero"

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isVerifyingCode = true, errorMessage = null)

            when (val result = repository.confirmPhoneVerification(phoneWithPrefix, code)) {
                is PhoneVerificationResult.Verified -> {
                    // Actualizar el teléfono en el perfil
                    val updateRequest = UpdateProfileRequest(
                        nombres = _uiState.value.nombres,
                        apellidos = _uiState.value.apellidos,
                        email = _uiState.value.email,
                        cedulaRuc = _uiState.value.cedulaRuc,
                        phone = phoneWithPrefix
                    )

                    when (val updateResult = repository.updateProfile(authorization, updateRequest)) {
                        is ProfileResult.UpdateSuccess -> {
                            _uiState.value = _uiState.value.copy(
                                isVerifyingCode = false,
                                phone = phone,
                                phoneVerified = true,
                                isChangingPhone = false,
                                isSuccess = true,
                                successMessage = "Teléfono actualizado y verificado exitosamente",
                                codeSent = false,
                                newPhone = "",
                                verificationCode = ""
                            )
                        }
                        is ProfileResult.Error -> {
                            // El teléfono ya fue verificado, actualizamos el estado local
                            _uiState.value = _uiState.value.copy(
                                isVerifyingCode = false,
                                phone = phone,
                                phoneVerified = true,
                                isChangingPhone = false,
                                isSuccess = true,
                                successMessage = "Teléfono verificado exitosamente",
                                codeSent = false,
                                newPhone = "",
                                verificationCode = ""
                            )
                        }
                        else -> {
                            _uiState.value = _uiState.value.copy(isVerifyingCode = false)
                        }
                    }
                }
                is PhoneVerificationResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isVerifyingCode = false,
                        errorMessage = result.message
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isVerifyingCode = false)
                }
            }
        }
    }

    fun saveProfile() {
        val state = _uiState.value

        // Validar campos
        if (state.nombres.isBlank() || state.apellidos.isBlank() || state.email.isBlank() || state.cedulaRuc.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Por favor complete todos los campos")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val request = UpdateProfileRequest(
                nombres = state.nombres.trim(),
                apellidos = state.apellidos.trim(),
                email = state.email.trim(),
                cedulaRuc = state.cedulaRuc.trim(),
                phone = state.phone.trim().takeIf { it.isNotBlank() }
            )

            when (val result = repository.updateProfile(authorization, request)) {
                is ProfileResult.UpdateSuccess -> {
                    // Si hay verificación de teléfono pendiente
                    if (result.updatedFields?.contains("phone") == true) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSuccess = true,
                            successMessage = result.message,
                            isPhoneVerificationPending = true,
                            pendingPhone = state.phone.trim()
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSuccess = true,
                            successMessage = result.message
                        )
                    }
                }
                is ProfileResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            isSuccess = false,
            isPhoneVerificationPending = false,
            pendingPhone = null,
            isChangingPhone = false,
            codeSent = false,
            newPhone = "",
            verificationCode = ""
        )
    }

    // Funciones para selfie
    fun setPendingSelfieUri(uriString: String) {
        _uiState.value = _uiState.value.copy(pendingSelfieUri = uriString)
    }

    fun clearPendingSelfieUri() {
        _uiState.value = _uiState.value.copy(pendingSelfieUri = null)
    }

    fun uploadSelfieFromUri(context: android.content.Context, imageUri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploadingSelfie = true, errorMessage = null)

            try {
                // Copiar el contenido de la URI a un archivo temporal
                val tempFile = createTempFileFromUri(context, imageUri)
                if (tempFile == null) {
                    _uiState.value = _uiState.value.copy(
                        isUploadingSelfie = false,
                        errorMessage = "No se pudo procesar la imagen"
                    )
                    return@launch
                }

                when (val result = repository.uploadSelfie(authorization, tempFile.absolutePath)) {
                    is SelfieResult.UploadSuccess -> {
                        // Eliminar archivo temporal
                        tempFile.delete()

                        _uiState.value = _uiState.value.copy(
                            isUploadingSelfie = false,
                            selfieUrl = result.selfieUrl,
                            isSuccess = true,
                            successMessage = result.message
                        )
                    }
                    is SelfieResult.Error -> {
                        tempFile.delete()
                        _uiState.value = _uiState.value.copy(
                            isUploadingSelfie = false,
                            errorMessage = result.message
                        )
                    }
                    else -> {
                        tempFile.delete()
                        _uiState.value = _uiState.value.copy(isUploadingSelfie = false)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUploadingSelfie = false,
                    errorMessage = "Error al procesar imagen: ${e.message}"
                )
            }
        }
    }

    private fun createTempFileFromUri(context: android.content.Context, uri: android.net.Uri): java.io.File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null

            // Verificar que el archivo no esté vacío
            val available = inputStream.available()
            if (available <= 0) {
                inputStream.close()
                return null
            }

            val tempFile = java.io.File.createTempFile("selfie_upload_", ".jpg", context.cacheDir)

            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()

            // Verificar que el archivo copiado tenga contenido
            if (tempFile.length() <= 0) {
                tempFile.delete()
                return null
            }

            tempFile
        } catch (e: Exception) {
            null
        }
    }

    fun deleteSelfie() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeletingSelfie = true, errorMessage = null)

            when (val result = repository.deleteSelfie(authorization)) {
                is SelfieResult.DeleteSuccess -> {
                    _uiState.value = _uiState.value.copy(
                        isDeletingSelfie = false,
                        selfieUrl = null,
                        isSuccess = true,
                        successMessage = result.message
                    )
                }
                is SelfieResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isDeletingSelfie = false,
                        errorMessage = result.message
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isDeletingSelfie = false)
                }
            }
        }
    }

    class Factory(
        private val repository: ProfileRepository,
        private val getAccessToken: () -> String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                return ProfileViewModel(repository, getAccessToken) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
