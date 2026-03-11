package com.christelldev.easyreferplus.data.model

import com.google.gson.annotations.SerializedName

// Paso 1: Verificar teléfono
data class VerifyPhoneRequest(
    @SerializedName("phone")
    val phone: String
)

data class VerifyPhoneResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("phone")
    val phone: String
)

// Reenviar código SMS
data class ResendCodeRequest(
    @SerializedName("phone")
    val phone: String
)

data class ResendCodeResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String
)

// Paso 2: Confirmar código SMS
data class ConfirmCodeRequest(
    @SerializedName("phone")
    val phone: String,
    @SerializedName("code")
    val code: String
)

data class ConfirmCodeResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("verified")
    val verified: Boolean,
    @SerializedName("verification_token")
    val verificationToken: String?
)

// Paso 3: Completar registro
data class CompleteRegistrationRequest(
    @SerializedName("verification_token")
    val verificationToken: String,
    @SerializedName("referral_code")
    val referralCode: String?,
    @SerializedName("cedula_ruc")
    val cedulaRuc: String,
    @SerializedName("nombres")
    val nombres: String,
    @SerializedName("apellidos")
    val apellidos: String,
    @SerializedName("empresa_nombre")
    val empresaNombre: String?,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("confirm_password")
    val confirmPassword: String,
    @SerializedName("is_adult")
    val isAdult: Boolean,
    @SerializedName("accepts_privacy_policy")
    val acceptsPrivacyPolicy: Boolean
)

data class CompleteRegistrationResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("user_id")
    val userId: Int?,
    @SerializedName("referral_code")
    val referralCode: String?,
    // Tokens de sesión (retornados tras registro)
    @SerializedName("access_token")
    val accessToken: String?,
    @SerializedName("refresh_token")
    val refreshToken: String?,
    @SerializedName("expires_in")
    val expiresIn: Int?
)

// Errores de validación
data class ValidationError(
    @SerializedName("field")
    val field: String,
    @SerializedName("message")
    val message: String
)

// Política de privacidad
data class PrivacyPolicy(
    @SerializedName("id")
    val id: Int,
    @SerializedName("version")
    val version: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("is_current")
    val isCurrent: Boolean,
    @SerializedName("created_at")
    val createdAt: String
)

data class PrivacyPolicyResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("policy")
    val policy: PrivacyPolicy?
)
