package com.christelldev.easyreferplus.data.model

import com.google.gson.annotations.SerializedName

// Modelo de perfil de usuario
data class UserProfile(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("nombres")
    val nombres: String,
    @SerializedName("apellidos")
    val apellidos: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("cedula_ruc")
    val cedulaRuc: String,
    @SerializedName("is_ruc")
    val isRuc: Boolean,
    @SerializedName("empresa_nombre")
    val empresaNombre: String?,
    @SerializedName("empresa_status")
    val empresaStatus: String?,
    @SerializedName("empresa_activa")
    val empresaActiva: Boolean?,
    @SerializedName("referral_code")
    val referralCode: String?,
    @SerializedName("phone_verified")
    val phoneVerified: Boolean,
    @SerializedName("is_verified")
    val isVerified: Boolean,
    @SerializedName("has_company")
    val hasCompany: Boolean,
    @SerializedName("status")
    val status: String?,
    @SerializedName("role")
    val role: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("selfie_url")
    val selfieUrl: String? = null
) {
    val isAdmin: Boolean
        get() = role?.lowercase() == "admin"
        
    val isSuspended: Boolean
        get() = status?.lowercase() == "suspended"
}

// Solicitud de actualización de perfil
data class UpdateProfileRequest(
    @SerializedName("nombres")
    val nombres: String,
    @SerializedName("apellidos")
    val apellidos: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("cedula_ruc")
    val cedulaRuc: String,
    @SerializedName("phone")
    val phone: String? = null
)

// Respuesta de actualización de perfil
data class UpdateProfileResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("updated_fields")
    val updatedFields: List<String>?,
    @SerializedName("profile")
    val profile: UserProfile?
)

// Respuesta de perfil
data class ProfileResponse(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("nombres")
    val nombres: String,
    @SerializedName("apellidos")
    val apellidos: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("cedula_ruc")
    val cedulaRuc: String,
    @SerializedName("is_ruc")
    val isRuc: Boolean,
    @SerializedName("empresa_nombre")
    val empresaNombre: String?,
    @SerializedName("empresa_status")
    val empresaStatus: String?,
    @SerializedName("empresa_activa")
    val empresaActiva: Boolean?,
    @SerializedName("referral_code")
    val referralCode: String?,
    @SerializedName("phone_verified")
    val phoneVerified: Boolean,
    @SerializedName("is_verified")
    val isVerified: Boolean,
    @SerializedName("has_company")
    val hasCompany: Boolean,
    @SerializedName("status")
    val status: String?,
    @SerializedName("role")
    val role: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("selfie_url")
    val selfieUrl: String? = null
) {
    val isAdmin: Boolean
        get() = role?.lowercase() == "admin"
        
    val isSuspended: Boolean
        get() = status?.lowercase() == "suspended"
}
