package com.christelldev.easyreferplus.data.model

import com.google.gson.annotations.SerializedName

// Modelos para gestión de sesiones - matching backend API

data class SessionInfo(
    @SerializedName("session_id")
    val sessionId: String,
    @SerializedName("created_at")
    val createdAt: Long,          // Unix timestamp
    @SerializedName("last_used")
    val lastUsed: Long,           // Unix timestamp
    @SerializedName("ip_address")
    val ipAddress: String?,
    @SerializedName("device_info")
    val deviceInfo: String?,
    @SerializedName("user_agent")
    val userAgent: String?,
    @SerializedName("is_current")
    val isCurrent: Boolean = false
)

data class SessionsResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String?,
    @SerializedName("total_sessions")
    val totalSessions: Int,
    @SerializedName("max_sessions")
    val maxSessions: Int,
    @SerializedName("sessions")
    val sessions: List<SessionInfo>
)

data class InvalidateSessionRequest(
    @SerializedName("session_id")
    val sessionId: String
)

data class InvalidateSessionResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("session_id")
    val sessionId: String
)

data class LogoutAllResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("sessions_closed")
    val sessionsClosed: Int
)

// Modelo para validar sesión
data class ValidateSessionResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("valid")
    val valid: Boolean,
    @SerializedName("needs_refresh")
    val needsRefresh: Boolean? = null,
    @SerializedName("message")
    val message: String?,
    @SerializedName("user_id")
    val userId: Int?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("session_info")
    val sessionInfo: SessionValidationInfo?,
    @SerializedName("active_sessions")
    val activeSessions: Int?,
    @SerializedName("account_status")
    val accountStatus: AccountStatus?
)

data class SessionValidationInfo(
    @SerializedName("refresh_token_valid")
    val refreshTokenValid: Boolean?,
    @SerializedName("session_id")
    val sessionId: String?,
    @SerializedName("last_used")
    val lastUsed: Long?,
    @SerializedName("reason")
    val reason: String?
)

data class AccountStatus(
    @SerializedName("is_active")
    val isActive: Boolean?,
    @SerializedName("phone_verified")
    val phoneVerified: Boolean?,
    @SerializedName("is_locked")
    val isLocked: Boolean?
)
