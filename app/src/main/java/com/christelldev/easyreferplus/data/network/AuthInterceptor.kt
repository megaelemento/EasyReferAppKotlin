package com.christelldev.easyreferplus.data.network

import android.content.Context
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

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

            // forceRefresh=true: ignorar el skip proactivo y hacer siempre la llamada
            // de red. Con rotation=true, esto consume el refresh token actual y emite uno nuevo.
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
            return chain.proceed(originalRequest)
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
        val keywords = listOf("password_changed", "contraseña", "invalidada", "session_revoked")
        return keywords.any { errorBody.contains(it, ignoreCase = true) }
    }
}
