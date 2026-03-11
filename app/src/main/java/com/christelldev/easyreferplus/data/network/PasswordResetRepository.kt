package com.christelldev.easyreferplus.data.network

import com.christelldev.easyreferplus.data.model.PasswordResetRequest
import com.christelldev.easyreferplus.data.model.ResetPasswordRequest
import com.christelldev.easyreferplus.data.model.VerifyResetCodeRequest
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import retrofit2.Response

sealed class PasswordResetResult {
    data object RequestSuccess : PasswordResetResult()
    data class VerifySuccess(val resetToken: String) : PasswordResetResult()
    data object ResetSuccess : PasswordResetResult()
    data class ResendSuccess(val waitTime: Int) : PasswordResetResult()
    data class Error(val error: PasswordResetError, val message: String? = null, val remainingAttempts: Int? = null) : PasswordResetResult()
    data class RateLimitError(val waitSeconds: Int, val message: String? = null) : PasswordResetResult()
}

sealed class PasswordResetError {
    data object UserNotFound : PasswordResetError()
    data object AccountDisabled : PasswordResetError()
    data object PhoneNotVerified : PasswordResetError()
    data object DailyLimitExceeded : PasswordResetError()
    data class InvalidCode(val message: String? = null) : PasswordResetError()
    data class TooManyAttempts(val message: String? = null) : PasswordResetError()
    data object InvalidToken : PasswordResetError()
    data class WeakPassword(val errors: List<String>) : PasswordResetError()
    data object SamePassword : PasswordResetError()
    data class ValidationError(val fieldErrors: Map<String, String>) : PasswordResetError()
    data object ServerUnavailable : PasswordResetError()
    data class Unknown(val message: String) : PasswordResetError()
}

data class PasswordResetApiError(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("error_type")
    val errorType: String?,
    @SerializedName("errors")
    val errors: Any?,
    @SerializedName("remaining_attempts")
    val remainingAttempts: Int?,
    @SerializedName("wait_seconds")
    val waitSeconds: Int?
)

class PasswordResetRepository(
    private val apiService: PasswordResetApiService
) {
    suspend fun requestPasswordReset(phone: String): PasswordResetResult {
        return try {
            val request = PasswordResetRequest(phone)
            val response = apiService.requestPasswordReset(request)

            if (response.isSuccessful) {
                PasswordResetResult.RequestSuccess
            } else {
                parseRequestError(response)
            }
        } catch (e: Exception) {
            parseException(e)
        }
    }

    suspend fun verifyResetCode(phone: String, code: String): PasswordResetResult {
        return try {
            val request = VerifyResetCodeRequest(phone, code)
            val response = apiService.verifyResetCode(request)

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.verified == true && body.resetToken != null) {
                        PasswordResetResult.VerifySuccess(body.resetToken)
                    } else {
                        PasswordResetResult.Error(PasswordResetError.InvalidCode())
                    }
                } ?: PasswordResetResult.Error(PasswordResetError.InvalidCode())
            } else {
                parseVerifyError(response)
            }
        } catch (e: Exception) {
            parseException(e)
        }
    }

    suspend fun resetPassword(token: String, newPassword: String, confirmPassword: String): PasswordResetResult {
        return try {
            val request = ResetPasswordRequest(token, newPassword, confirmPassword)
            val response = apiService.resetPassword(request)

            if (response.isSuccessful) {
                PasswordResetResult.ResetSuccess
            } else {
                parseResetError(response)
            }
        } catch (e: Exception) {
            parseException(e)
        }
    }

    suspend fun resendResetCode(phone: String): PasswordResetResult {
        return try {
            val request = PasswordResetRequest(phone)
            val response = apiService.resendResetCode(request)

            if (response.isSuccessful) {
                val waitTime = response.body()?.waitTime ?: 60
                PasswordResetResult.ResendSuccess(waitTime)
            } else {
                parseResendError(response)
            }
        } catch (e: Exception) {
            parseException(e)
        }
    }

    private fun parseRequestError(response: Response<*>): PasswordResetResult {
        return try {
            val errorBody = response.errorBody()?.string()
            val apiError = Gson().fromJson(errorBody, PasswordResetApiError::class.java)
            val errorType = apiError.errorType

            when (errorType) {
                "user_not_found" -> PasswordResetResult.Error(PasswordResetError.UserNotFound, apiError.message)
                "account_disabled" -> PasswordResetResult.Error(PasswordResetError.AccountDisabled, apiError.message)
                "phone_not_verified" -> PasswordResetResult.Error(PasswordResetError.PhoneNotVerified, apiError.message)
                "daily_limit_exceeded" -> PasswordResetResult.Error(PasswordResetError.DailyLimitExceeded, apiError.message)
                "rate_limited" -> {
                    val waitSeconds = apiError.waitSeconds ?: 60
                    PasswordResetResult.RateLimitError(waitSeconds, apiError.message)
                }
                else -> PasswordResetResult.Error(PasswordResetError.Unknown(apiError.message))
            }
        } catch (e: Exception) {
            when (response.code()) {
                400 -> PasswordResetResult.Error(PasswordResetError.UserNotFound)
                429 -> PasswordResetResult.RateLimitError(60)
                else -> PasswordResetResult.Error(PasswordResetError.Unknown("Error al solicitar reset"))
            }
        }
    }

    private fun parseVerifyError(response: Response<*>): PasswordResetResult {
        return try {
            val errorBody = response.errorBody()?.string()
            val apiError = Gson().fromJson(errorBody, PasswordResetApiError::class.java)
            val errorType = apiError.errorType

            when (errorType) {
                "invalid_code" -> {
                    val remainingAttempts = apiError.remainingAttempts ?: extractRemainingAttempts(apiError.message)
                    if (remainingAttempts == 0) {
                        PasswordResetResult.Error(PasswordResetError.TooManyAttempts(), apiError.message)
                    } else {
                        PasswordResetResult.Error(PasswordResetError.InvalidCode(), apiError.message, remainingAttempts)
                    }
                }
                "too_many_attempts" -> PasswordResetResult.Error(PasswordResetError.TooManyAttempts(), apiError.message)
                else -> PasswordResetResult.Error(PasswordResetError.Unknown(apiError.message))
            }
        } catch (e: Exception) {
            when (response.code()) {
                400 -> PasswordResetResult.Error(PasswordResetError.InvalidCode())
                else -> PasswordResetResult.Error(PasswordResetError.Unknown("Error al verificar código"))
            }
        }
    }

    private fun parseResetError(response: Response<*>): PasswordResetResult {
        return try {
            val errorBody = response.errorBody()?.string()
            val apiError = Gson().fromJson(errorBody, PasswordResetApiError::class.java)
            val errorType = apiError.errorType

            when (errorType) {
                "invalid_token" -> PasswordResetResult.Error(PasswordResetError.InvalidToken, apiError.message)
                "weak_password" -> {
                    val errors = parsePasswordErrors(apiError.errors)
                    PasswordResetResult.Error(PasswordResetError.WeakPassword(errors), apiError.message)
                }
                "same_password" -> PasswordResetResult.Error(PasswordResetError.SamePassword, apiError.message)
                "validation_error" -> {
                    val fieldErrors = parseFieldErrors(apiError.errors)
                    PasswordResetResult.Error(PasswordResetError.ValidationError(fieldErrors), apiError.message)
                }
                else -> PasswordResetResult.Error(PasswordResetError.Unknown(apiError.message))
            }
        } catch (e: Exception) {
            when (response.code()) {
                400 -> PasswordResetResult.Error(PasswordResetError.InvalidToken)
                422 -> PasswordResetResult.Error(PasswordResetError.ValidationError(emptyMap()))
                else -> PasswordResetResult.Error(PasswordResetError.Unknown("Error al cambiar contraseña"))
            }
        }
    }

    private fun parseResendError(response: Response<*>): PasswordResetResult {
        return try {
            val errorBody = response.errorBody()?.string()
            val apiError = Gson().fromJson(errorBody, PasswordResetApiError::class.java)
            val errorType = apiError.errorType

            when (errorType) {
                "rate_limited" -> {
                    val waitSeconds = apiError.waitSeconds ?: 60
                    PasswordResetResult.RateLimitError(waitSeconds, apiError.message)
                }
                "daily_limit_exceeded" -> PasswordResetResult.Error(PasswordResetError.DailyLimitExceeded, apiError.message)
                else -> PasswordResetResult.Error(PasswordResetError.Unknown(apiError.message))
            }
        } catch (e: Exception) {
            when (response.code()) {
                429 -> PasswordResetResult.RateLimitError(60)
                else -> PasswordResetResult.Error(PasswordResetError.Unknown("Error al reenviar código"))
            }
        }
    }

    private fun parsePasswordErrors(errors: Any?): List<String> {
        if (errors == null) return emptyList()
        return if (errors is List<*>) {
            errors.filterIsInstance<String>()
        } else {
            emptyList()
        }
    }

    private fun parseFieldErrors(errors: Any?): Map<String, String> {
        if (errors == null) return emptyMap()
        return emptyMap() // Por ahora vacío, se puede implementar si hay errores de campo
    }

    private fun extractRemainingAttempts(message: String?): Int {
        if (message == null) return 0
        val regex = "(\\d+)\\s*(?:intento|restante)".toRegex(RegexOption.IGNORE_CASE)
        val match = regex.find(message)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }

    private fun parseException(e: Exception): PasswordResetResult {
        val error = when {
            e is java.io.IOException && e.message?.contains("mantenimiento", ignoreCase = true) == true ->
                PasswordResetError.Unknown("El servidor está en mantenimiento. Intente más tarde.")
            e is java.net.UnknownHostException ||
            e is java.net.ConnectException ||
            e is java.io.IOException -> PasswordResetError.ServerUnavailable
            else -> PasswordResetError.Unknown(e.message ?: "Error desconocido")
        }
        return PasswordResetResult.Error(error)
    }

    class Factory {
        fun create(): PasswordResetRepository {
            val retrofit = RetrofitClient.getInstance()
            val apiService = retrofit.create(PasswordResetApiService::class.java)
            return PasswordResetRepository(apiService)
        }
    }
}
