package com.christelldev.easyreferplus.data.network

import com.christelldev.easyreferplus.data.model.CompleteRegistrationRequest
import com.christelldev.easyreferplus.data.model.CompleteRegistrationResponse
import com.christelldev.easyreferplus.data.model.ConfirmCodeRequest
import com.christelldev.easyreferplus.data.model.ConfirmCodeResponse
import com.christelldev.easyreferplus.data.model.ResendCodeRequest
import com.christelldev.easyreferplus.data.model.VerifyPhoneRequest
import com.christelldev.easyreferplus.data.model.VerifyPhoneResponse
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import retrofit2.Response

sealed class RegisterResult {
    data class VerifySuccess(val response: VerifyPhoneResponse) : RegisterResult()
    data object ResendSuccess : RegisterResult()
    data class ConfirmSuccess(val verificationToken: String) : RegisterResult()
    data class RegistrationSuccess(
        val userId: Int,
        val referralCode: String,
        val accessToken: String?,
        val refreshToken: String?,
        val expiresIn: Int?
    ) : RegisterResult()
    data class Error(val registerError: RegisterError, val remainingAttempts: Int? = null) : RegisterResult()
    data class RateLimitError(val waitSeconds: Int, val message: String? = null) : RegisterResult()
}

sealed class RegisterError {
    data object PhoneAlreadyExists : RegisterError()
    data object InvalidCode : RegisterError()
    data object InvalidToken : RegisterError()
    data class ValidationError(val fieldErrors: Map<String, String>) : RegisterError()
    data object RateLimited : RegisterError()
    data object GlobalRateLimitExceeded : RegisterError()
    data object GlobalVerificationLimitExceeded : RegisterError()
    data object MaxAttemptsExceeded : RegisterError()
    data object MaxResendAttemptsExceeded : RegisterError()
    data object ServerUnavailable : RegisterError()
    data class Unknown(val message: String) : RegisterError()
}

data class ApiErrorBody(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("error_type")
    val errorType: String?,
    @SerializedName("remaining_attempts")
    val remainingAttempts: Int?,
    @SerializedName("wait_seconds")
    val waitSeconds: Int?,
    @SerializedName("errors")
    val errors: Any?, // Puede ser List<ValidationError> o List<String>
    @SerializedName("detail")
    val detail: List<PydanticError>?
)

// Nuevo formato de error de Pydantic/FastAPI
data class PydanticError(
    @SerializedName("loc")
    val loc: List<String>?,
    @SerializedName("msg")
    val msg: String?,
    @SerializedName("type")
    val type: String?
)

class RegisterRepository(
    private val apiService: RegisterApiService,
    private val sharedPreferences: android.content.SharedPreferences
) {
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
    }

    // Guardar tokens tras registro o login
    fun saveTokens(accessToken: String?, refreshToken: String?, expiresIn: Int?) {
        if (accessToken == null) return
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putLong(KEY_TOKEN_EXPIRY, System.currentTimeMillis() + ((expiresIn ?: 1800) * 1000))
            apply()
        }
        android.util.Log.e("RegisterRepo", "Tokens guardados correctamente")
    }
    suspend fun verifyPhone(phone: String): RegisterResult {
        return try {
            val request = VerifyPhoneRequest(phone)
            val response = apiService.verifyPhone(request)

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    RegisterResult.VerifySuccess(body)
                } ?: RegisterResult.Error(RegisterError.Unknown("Respuesta vacía"))
            } else {
                parseVerifyError(response)
            }
        } catch (e: Exception) {
            parseException(e)
        }
    }

    suspend fun resendCode(phone: String): RegisterResult {
        return try {
            val request = ResendCodeRequest(phone)
            val response = apiService.resendCode(request)

            if (response.isSuccessful) {
                RegisterResult.ResendSuccess
            } else {
                parseResendError(response)
            }
        } catch (e: Exception) {
            parseException(e)
        }
    }

    private fun parseResendError(response: Response<*>): RegisterResult {
        return try {
            val errorBody = response.errorBody()?.string()
            val apiError = Gson().fromJson(errorBody, ApiErrorBody::class.java)
            val errorType = apiError.errorType

            when {
                response.code() == 429 && errorType == "global_verification_limit_exceeded" -> {
                    RegisterResult.Error(RegisterError.GlobalVerificationLimitExceeded)
                }
                response.code() == 429 && errorType == "global_rate_limit_exceeded" -> {
                    val waitSeconds = apiError.waitSeconds ?: 60
                    RegisterResult.Error(RegisterError.GlobalRateLimitExceeded)
                }
                response.code() == 400 && errorType == "too_many_resend_attempts" -> {
                    RegisterResult.Error(RegisterError.MaxResendAttemptsExceeded)
                }
                response.code() == 429 -> {
                    val waitSeconds = apiError.waitSeconds ?: 60
                    RegisterResult.RateLimitError(waitSeconds, apiError.message)
                }
                else -> RegisterResult.Error(RegisterError.Unknown(apiError.message))
            }
        } catch (e: Exception) {
            when (response.code()) {
                400 -> RegisterResult.Error(RegisterError.MaxResendAttemptsExceeded)
                429 -> RegisterResult.RateLimitError(60, null)
                else -> RegisterResult.Error(RegisterError.Unknown("Error al reenviar código"))
            }
        }
    }

    suspend fun confirmCode(phone: String, code: String): RegisterResult {
        return try {
            val request = ConfirmCodeRequest(phone, code)
            val response = apiService.confirmCode(request)

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.verified && body.verificationToken != null) {
                        RegisterResult.ConfirmSuccess(body.verificationToken)
                    } else {
                        RegisterResult.Error(RegisterError.InvalidCode)
                    }
                } ?: RegisterResult.Error(RegisterError.InvalidCode)
            } else {
                val error = parseConfirmError(response)
                error
            }
        } catch (e: Exception) {
            parseException(e)
        }
    }

    suspend fun completeRegistration(
        verificationToken: String,
        referralCode: String?,
        cedulaRuc: String,
        nombres: String,
        apellidos: String,
        email: String,
        password: String,
        confirmPassword: String,
        isAdult: Boolean,
        acceptsPrivacyPolicy: Boolean
    ): RegisterResult {
        return try {
            android.util.Log.e("REGISTER_DEBUG", "=== INICIANDO COMPLETE REGISTRATION ===")

            val request = CompleteRegistrationRequest(
                verificationToken = verificationToken,
                referralCode = referralCode,
                cedulaRuc = cedulaRuc,
                nombres = nombres,
                apellidos = apellidos,
                empresaNombre = null,
                email = email,
                password = password,
                confirmPassword = confirmPassword,
                isAdult = isAdult,
                acceptsPrivacyPolicy = acceptsPrivacyPolicy
            )
            val response = apiService.completeRegistration(request)

            android.util.Log.e("REGISTER_DEBUG", "Response code: ${response.code()}")
            android.util.Log.e("REGISTER_DEBUG", "Response isSuccessful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    android.util.Log.e("REGISTER_DEBUG", "Body success: ${body.success}, message: ${body.message}")
                    android.util.Log.e("REGISTER_DEBUG", "Access token: ${body.accessToken?.take(10)}...")
                    if (body.success && body.userId != null) {
                        // Guardar tokens automáticamente tras registro
                        saveTokens(body.accessToken, body.refreshToken, body.expiresIn)

                        RegisterResult.RegistrationSuccess(
                            body.userId,
                            body.referralCode ?: "",
                            body.accessToken,
                            body.refreshToken,
                            body.expiresIn
                        )
                    } else {
                        RegisterResult.Error(RegisterError.Unknown(body.message))
                    }
                } ?: RegisterResult.Error(RegisterError.Unknown("Respuesta vacía"))
            } else {
                android.util.Log.e("REGISTER_DEBUG", "Llamando parseRegistrationError")
                val error = parseRegistrationError(response)
                error
            }
        } catch (e: Exception) {
            parseException(e)
        }
    }

    // Resultado para obtener la política de privacidad
    sealed class PrivacyPolicyResult {
        data class Success(val title: String, val content: String) : PrivacyPolicyResult()
        data class Error(val message: String) : PrivacyPolicyResult()
    }

    suspend fun getPrivacyPolicy(): PrivacyPolicyResult {
        return try {
            val response = apiService.getPrivacyPolicy()

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.success && body.policy != null) {
                        PrivacyPolicyResult.Success(body.policy.title, body.policy.content)
                    } else {
                        PrivacyPolicyResult.Error("No se pudo obtener la política de privacidad")
                    }
                } ?: PrivacyPolicyResult.Error("Respuesta vacía")
            } else {
                PrivacyPolicyResult.Error("Error al obtener la política de privacidad")
            }
        } catch (e: Exception) {
            PrivacyPolicyResult.Error(e.message ?: "Error de conexión")
        }
    }

    private fun parseVerifyError(response: Response<*>): RegisterResult {
        return try {
            val errorBody = response.errorBody()?.string()
            val apiError = Gson().fromJson(errorBody, ApiErrorBody::class.java)
            val errorType = apiError.errorType

            when {
                response.code() == 429 && errorType == "global_verification_limit_exceeded" -> {
                    RegisterResult.Error(RegisterError.GlobalVerificationLimitExceeded)
                }
                response.code() == 429 && errorType == "global_rate_limit_exceeded" -> {
                    RegisterResult.Error(RegisterError.GlobalRateLimitExceeded)
                }
                response.code() == 429 && errorType == "http_error" -> {
                    // Rate limiting genérico - usar el mensaje del backend
                    val waitSeconds = apiError.waitSeconds ?: 60
                    RegisterResult.RateLimitError(waitSeconds, apiError.message)
                }
                response.code() == 400 -> RegisterResult.Error(RegisterError.PhoneAlreadyExists)
                response.code() == 429 -> {
                    val waitSeconds = apiError.waitSeconds ?: 60
                    RegisterResult.RateLimitError(waitSeconds, apiError.message)
                }
                else -> RegisterResult.Error(RegisterError.Unknown(apiError.message))
            }
        } catch (e: Exception) {
            when (response.code()) {
                400 -> RegisterResult.Error(RegisterError.PhoneAlreadyExists)
                429 -> RegisterResult.RateLimitError(60, null)
                else -> RegisterResult.Error(RegisterError.Unknown("Error al verificar teléfono"))
            }
        }
    }

    private fun parseConfirmError(response: Response<*>): RegisterResult {
        return try {
            val errorBody = response.errorBody()?.string()
            val apiError = Gson().fromJson(errorBody, ApiErrorBody::class.java)
            val errorType = apiError.errorType

            when (response.code()) {
                400 -> {
                    // Manejar según error_type
                    when (errorType) {
                        "too_many_attempts" -> {
                            RegisterResult.Error(RegisterError.MaxAttemptsExceeded)
                        }
                        "invalid_code", "http_error" -> {
                            // Código incorrecto o expirado - intentar parsear intentos restantes del mensaje
                            val remainingAttempts = apiError.remainingAttempts ?: extractRemainingAttempts(apiError.message)
                            if (remainingAttempts == 0) {
                                RegisterResult.Error(RegisterError.MaxAttemptsExceeded)
                            } else {
                                RegisterResult.Error(RegisterError.InvalidCode, remainingAttempts)
                            }
                        }
                        else -> {
                            // Fallback al comportamiento anterior
                            val remainingAttempts = apiError.remainingAttempts ?: extractRemainingAttempts(apiError.message)
                            if (remainingAttempts == 0) {
                                RegisterResult.Error(RegisterError.MaxAttemptsExceeded)
                            } else {
                                RegisterResult.Error(RegisterError.InvalidCode, remainingAttempts)
                            }
                        }
                    }
                }
                429 -> {
                    val waitSeconds = apiError.waitSeconds ?: 60
                    RegisterResult.RateLimitError(waitSeconds, apiError.message)
                }
                else -> RegisterResult.Error(RegisterError.Unknown(apiError.message))
            }
        } catch (e: Exception) {
            when (response.code()) {
                400 -> RegisterResult.Error(RegisterError.InvalidCode)
                429 -> RegisterResult.RateLimitError(60, null)
                else -> RegisterResult.Error(RegisterError.Unknown("Código incorrecto"))
            }
        }
    }

    // Extraer intentos restantes del mensaje del backend
    private fun extractRemainingAttempts(message: String?): Int {
        if (message == null) return 0
        // Buscar patrón como "Intentos restantes: 2" o "2 intentos"
        val regex = "(\\d+)\\s*(?:intento|restante)".toRegex(RegexOption.IGNORE_CASE)
        val match = regex.find(message)
        if (match != null) {
            return match.groupValues.get(1).toIntOrNull() ?: 0
        }
        // Si no encuentra, buscar solo números
        val numberRegex = "(\\d+)".toRegex()
        val numberMatch = numberRegex.find(message)
        return numberMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }

    private fun parseRegistrationError(response: Response<*>): RegisterResult {
        return try {
            val errorBody = response.errorBody()?.string()
            android.util.Log.d("RegisterRepo", "Error response: $errorBody")
            android.util.Log.d("RegisterRepo", "Error code: ${response.code()}")

            val apiError = Gson().fromJson(errorBody, ApiErrorBody::class.java)

            // Primero intentar parsear el formato de Pydantic (detail array)
            val pydanticErrors = apiError.detail?.mapNotNull { detail ->
                val fieldName = detail.loc?.lastOrNull { it != "body" }
                var message = detail.msg
                // Quitar prefijo "Value error, " si existe
                if (message?.startsWith("Value error, ") == true) {
                    message = message.removePrefix("Value error, ")
                }
                if (fieldName != null && message != null) {
                    fieldName to message
                } else {
                    null
                }
            }?.toMap() ?: emptyMap()

            // También verificar el formato anterior (errors array - puede ser List<ValidationError> o List<String>)
            val fieldErrors = when (apiError.errors) {
                is List<*> -> {
                    val errorsList = apiError.errors as List<*>
                    // Verificar si es lista de objetos (ValidationError) o lista de strings
                    if (errorsList.isEmpty()) {
                        emptyMap()
                    } else if (errorsList.first() is String) {
                        // Es una lista de strings - analizar el contenido para determinar el campo
                        val errorMessages = errorsList as List<String>
                        val mappedErrors = mutableMapOf<String, String>()

                        for (msg in errorMessages) {
                            val lowerMsg = msg.lowercase()
                            when {
                                lowerMsg.contains("correo") || lowerMsg.contains("email") -> {
                                    mappedErrors["email"] = msg
                                }
                                lowerMsg.contains("cédula") || lowerMsg.contains("ruc") -> {
                                    mappedErrors["cedula_ruc"] = msg
                                }
                                lowerMsg.contains("referido") || lowerMsg.contains("referral") -> {
                                    mappedErrors["referral_code"] = msg
                                }
                                lowerMsg.contains("password") && lowerMsg.contains("confirm") -> {
                                    mappedErrors["confirm_password"] = msg
                                }
                                lowerMsg.contains("password") -> {
                                    mappedErrors["password"] = msg
                                }
                                else -> {
                                    // Si no se puede determinar, usar como error general
                                    mappedErrors["general"] = msg
                                }
                            }
                        }
                        mappedErrors
                    } else {
                        // Es una lista de objetos ValidationError
                        @Suppress("UNCHECKED_CAST")
                        (errorsList as List<com.christelldev.easyreferplus.data.model.ValidationError>)
                            .associate { it.field to it.message }
                    }
                }
                else -> emptyMap()
            }

            // Combinar ambos formatos
            val allFieldErrors = if (pydanticErrors.isNotEmpty()) pydanticErrors else fieldErrors

            android.util.Log.d("RegisterRepo", "Field errors: $allFieldErrors")

            when (response.code()) {
                400 -> {
                    // Si hay errores de campo, tratarlo como error de validación
                    if (allFieldErrors.isNotEmpty()) {
                        RegisterResult.Error(RegisterError.ValidationError(allFieldErrors))
                    } else {
                        // También verificar si el mensaje indica error de validación
                        val message = apiError.message.lowercase()
                        if (message.contains("cédula") || message.contains("ruc") ||
                            message.contains("email") || message.contains("password") ||
                            message.contains("nombres") || message.contains("apellidos")) {
                            RegisterResult.Error(RegisterError.ValidationError(mapOf("general" to apiError.message)))
                        } else {
                            RegisterResult.Error(RegisterError.InvalidToken)
                        }
                    }
                }
                422 -> {
                    RegisterResult.Error(RegisterError.ValidationError(allFieldErrors))
                }
                else -> RegisterResult.Error(RegisterError.Unknown(apiError.message))
            }
        } catch (e: Exception) {
            android.util.Log.e("RegisterRepo", "Error parsing: ${e.message}")
            when (response.code()) {
                400 -> RegisterResult.Error(RegisterError.InvalidToken)
                422 -> RegisterResult.Error(RegisterError.ValidationError(emptyMap()))
                else -> RegisterResult.Error(RegisterError.Unknown("Error en el registro"))
            }
        }
    }

    private fun parseException(e: Exception): RegisterResult {
        val error = when {
            e is java.io.IOException && e.message?.contains("mantenimiento", ignoreCase = true) == true ->
                RegisterError.Unknown("El servidor está en mantenimiento. Intente más tarde.")
            e is java.net.UnknownHostException ||
            e is java.net.ConnectException ||
            e is java.io.IOException -> RegisterError.ServerUnavailable
            else -> RegisterError.Unknown(e.message ?: "Error desconocido")
        }
        return RegisterResult.Error(error)
    }

    class Factory(private val context: android.content.Context) {
        private val sharedPreferences = context.getSharedPreferences("EasyReferPrefs", android.content.Context.MODE_PRIVATE)

        fun create(): RegisterRepository {
            val retrofit = RetrofitClient.getInstance()
            val apiService = retrofit.create(RegisterApiService::class.java)
            return RegisterRepository(apiService, sharedPreferences)
        }
    }
}
