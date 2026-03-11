package com.christelldev.easyreferplus.data.network

import com.christelldev.easyreferplus.data.model.*
import retrofit2.Response

// Result sealed class for session operations
sealed class SessionsResult {
    data class Success(val sessions: List<SessionInfo>, val maxSessions: Int) : SessionsResult()
    data class Error(val message: String) : SessionsResult()
}

sealed class InvalidateResult {
    data class Success(val message: String, val isCurrentSession: Boolean) : InvalidateResult()
    data class Error(val message: String) : InvalidateResult()
}

sealed class LogoutAllResult {
    data class Success(val message: String, val sessionsClosed: Int) : LogoutAllResult()
    data class Error(val message: String) : LogoutAllResult()
}

class SessionRepository(private val apiService: ApiService) {

    suspend fun getSessions(authorization: String): SessionsResult {
        return try {
            val response = apiService.getSessions(authorization)
            if (response.isSuccessful) {
                response.body()?.let { sessionsResponse ->
                    if (sessionsResponse.success) {
                        SessionsResult.Success(
                            sessions = sessionsResponse.sessions,
                            maxSessions = sessionsResponse.maxSessions
                        )
                    } else {
                        SessionsResult.Error(sessionsResponse.message ?: "Error al obtener sesiones")
                    }
                } ?: SessionsResult.Error("Respuesta vacía")
            } else {
                SessionsResult.Error(parseError(response.code()))
            }
        } catch (e: Exception) {
            val message = when {
                e is java.io.IOException && e.message?.contains("mantenimiento", ignoreCase = true) == true ->
                    "El servidor está en mantenimiento. Intente más tarde."
                else -> e.message ?: "Error desconocido"
            }
            SessionsResult.Error(message)
        }
    }

    suspend fun invalidateSession(authorization: String, sessionId: String): InvalidateResult {
        return try {
            val response = apiService.invalidateSession(authorization, InvalidateSessionRequest(sessionId))
            if (response.isSuccessful) {
                response.body()?.let { invalidateResponse ->
                    if (invalidateResponse.success) {
                        // Check if the closed session was the current one
                        val isCurrent = invalidateResponse.message.contains("actual") ||
                                invalidateResponse.message.contains("inicie sesión nuevamente")
                        InvalidateResult.Success(invalidateResponse.message, isCurrent)
                    } else {
                        InvalidateResult.Error(invalidateResponse.message)
                    }
                } ?: InvalidateResult.Error("Respuesta vacía")
            } else {
                InvalidateResult.Error(parseError(response.code()))
            }
        } catch (e: Exception) {
            val message = when {
                e is java.io.IOException && e.message?.contains("mantenimiento", ignoreCase = true) == true ->
                    "El servidor está en mantenimiento. Intente más tarde."
                else -> e.message ?: "Error desconocido"
            }
            InvalidateResult.Error(message)
        }
    }

    suspend fun logoutAllExceptCurrent(authorization: String): LogoutAllResult {
        return try {
            val response = apiService.logoutAllExceptCurrent(authorization)
            if (response.isSuccessful) {
                response.body()?.let { logoutResponse ->
                    if (logoutResponse.success) {
                        LogoutAllResult.Success(
                            message = logoutResponse.message,
                            sessionsClosed = logoutResponse.sessionsClosed
                        )
                    } else {
                        LogoutAllResult.Error(logoutResponse.message)
                    }
                } ?: LogoutAllResult.Error("Respuesta vacía")
            } else {
                LogoutAllResult.Error(parseError(response.code()))
            }
        } catch (e: Exception) {
            val message = when {
                e is java.io.IOException && e.message?.contains("mantenimiento", ignoreCase = true) == true ->
                    "El servidor está en mantenimiento. Intente más tarde."
                else -> e.message ?: "Error desconocido"
            }
            LogoutAllResult.Error(message)
        }
    }

    suspend fun forceLogoutAll(authorization: String): LogoutAllResult {
        return try {
            val response = apiService.forceLogoutAll(authorization)
            if (response.isSuccessful) {
                response.body()?.let { logoutResponse ->
                    if (logoutResponse.success) {
                        LogoutAllResult.Success(
                            message = logoutResponse.message,
                            sessionsClosed = logoutResponse.sessionsClosed
                        )
                    } else {
                        LogoutAllResult.Error(logoutResponse.message)
                    }
                } ?: LogoutAllResult.Error("Respuesta vacía")
            } else {
                LogoutAllResult.Error(parseError(response.code()))
            }
        } catch (e: Exception) {
            val message = when {
                e is java.io.IOException && e.message?.contains("mantenimiento", ignoreCase = true) == true ->
                    "El servidor está en mantenimiento. Intente más tarde."
                else -> e.message ?: "Error desconocido"
            }
            LogoutAllResult.Error(message)
        }
    }

    private fun parseError(code: Int): String {
        return when (code) {
            401 -> "No autorizado - Es posible que esta función no esté disponible"
            403 -> "Acceso denegado"
            404 -> "Función de sesiones no disponible en el servidor"
            500 -> "Error del servidor"
            else -> "Error: $code"
        }
    }

    companion object {
        fun Factory(): SessionRepository {
            val apiService = RetrofitClient.getInstance().create(ApiService::class.java)
            return SessionRepository(apiService)
        }
    }
}
