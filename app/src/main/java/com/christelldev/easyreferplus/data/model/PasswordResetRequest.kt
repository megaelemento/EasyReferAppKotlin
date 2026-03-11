package com.christelldev.easyreferplus.data.model

import com.google.gson.annotations.SerializedName

// Request para solicitar reset de contraseña
data class PasswordResetRequest(
    @SerializedName("phone")
    val phone: String
)

// Request para verificar código de reset
data class VerifyResetCodeRequest(
    @SerializedName("phone")
    val phone: String,
    @SerializedName("code")
    val code: String
)

// Response de verificación de código
data class VerifyResetCodeResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("verified")
    val verified: Boolean?,
    @SerializedName("reset_token")
    val resetToken: String?
)

// Request para cambiar contraseña
data class ResetPasswordRequest(
    @SerializedName("reset_token")
    val resetToken: String,
    @SerializedName("new_password")
    val newPassword: String,
    @SerializedName("confirm_password")
    val confirmPassword: String
)

// Response genérico
data class PasswordResetResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("verified")
    val verified: Boolean?,
    @SerializedName("reset_token")
    val resetToken: String?,
    @SerializedName("error_type")
    val errorType: String?,
    @SerializedName("errors")
    val errors: Any?,
    @SerializedName("wait_time")
    val waitTime: Int?
)
