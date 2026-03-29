package com.christelldev.easyreferplus.data.network

import android.content.Context
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class AuthInterceptor(
    private val authRepository: AuthRepository,
    private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        if (isPublicPath(path)) return chain.proceed(originalRequest)

        // Refresh proactivo si el token está expirado o por expirar
        if (authRepository.isTokenExpired() || authRepository.isTokenExpiringSoon()) {
            runBlocking { authRepository.refreshToken() }
        }

        val token = authRepository.getAccessToken()
        val authenticatedRequest = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder().header("Authorization", "Bearer $token").build()
        } else originalRequest

        val response = chain.proceed(authenticatedRequest)

        if (response.code == 401) {
            val errorBodyString = try {
                response.peekBody(4096).string()
            } catch (e: Exception) { "" }

            if (shouldForceLogout(errorBodyString)) {
                authRepository.triggerLogout("Tu sesión ha sido invalidada por seguridad.")
                return response
            }

            response.close()

            // forceRefresh=true: el token fue consumido por otro hilo si hay rotación.
            // refreshToken() detecta la carrera comparando el access token antes/después del mutex.
            val refreshed = runBlocking { authRepository.refreshToken(forceRefresh = true) }
            if (refreshed) {
                val newToken = authRepository.getAccessToken()
                if (!newToken.isNullOrBlank()) {
                    return chain.proceed(
                        originalRequest.newBuilder()
                            .header("Authorization", "Bearer $newToken")
                            .build()
                    )
                }
            }

            authRepository.triggerLogout()
            // Devolver respuesta 401 sintética en vez de reintentar sin token
            // (evita bucle infinito de 401 → interceptor → 401 → …)
            return Response.Builder()
                .request(originalRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(401)
                .message("Unauthorized")
                .body("".toResponseBody(null))
                .build()
        }

        return response
    }

    private fun isPublicPath(path: String) =
        path.contains("/auth/login") ||
        path.contains("/auth/register") ||
        path.contains("/auth/refresh") ||
        path.contains("/password-reset") ||
        path.contains("/cities/")

    private fun shouldForceLogout(errorBody: String): Boolean {
        // Solo palabras clave muy específicas de eventos de seguridad del servidor.
        // NUNCA palabras comunes en español ("contraseña", "invalidada") porque
        // aparecen en mensajes de error normales y provocan cierre de sesión falso.
        val keywords = listOf("password_changed", "session_revoked")
        return keywords.any { errorBody.contains(it, ignoreCase = true) }
    }
}
