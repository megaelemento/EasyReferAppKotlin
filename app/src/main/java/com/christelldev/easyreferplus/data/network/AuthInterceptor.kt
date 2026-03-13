package com.christelldev.easyreferplus.data.network

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.christelldev.easyreferplus.MainActivity
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val authRepository: AuthRepository,
    private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // No agregar token a requests que no lo necesitan
        val path = originalRequest.url.encodedPath
        if (path.contains("/auth/login") ||
            path.contains("/auth/register") ||
            path.contains("/auth/refresh") ||
            path.contains("/password-reset") ||
            path.contains("/cities/")) {
            return chain.proceed(originalRequest)
        }

        // Obtener token
        var token = authRepository.getAccessToken()

        // Verificar si el token está expirado o por expirar antes de hacer la solicitud
        // Si está expirado o por expirar (en 5 min), intentar refresh primero
        if (authRepository.isTokenExpired() || authRepository.isTokenExpiringSoon()) {
            runBlocking {
                try {
                    authRepository.refreshToken()
                } catch (e: Exception) {
                    false
                }
            }
            // Siempre leer el token actual de SharedPreferences después del intento de refresh.
            // Si el refresh de ESTA solicitud falló por rate limit, otra solicitud concurrente
            // puede haber renovado el token exitosamente — usamos ese token nuevo.
            token = authRepository.getAccessToken()
        }

        // Si sigue sin haber token después del intento de refresh, verificar si hay error de red
        // Si es error de red, permitir la solicitud sin token (el servidor puede o no aceptarla)
        if (token.isNullOrBlank()) {
            // Verificar si hay conectividad antes de cerrar sesión
            return try {
                chain.proceed(originalRequest)
            } catch (e: Exception) {
                // Error de red - NO cerrar sesión, permitir reintento después
                // Cerrar la solicitud original y crear una nueva para retornar
                chain.proceed(originalRequest)
            }
        }

        // Agregar token al request
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return try {
            val response = chain.proceed(authenticatedRequest)

            // Si es 401, intentar refresh
            if (response.code == 401) {
                // Verificar si es un error de contraseña cambiada
                val isPasswordChanged = try {
                    val errorBody = response.peekBody(Long.MAX_VALUE).string()
                    errorBody.contains("password_changed") || errorBody.contains("contraseña") || errorBody.contains("invalidada")
                } catch (e: Exception) {
                    false
                }

                response.close()

                // Si es por cambio de contraseña, forzar logout inmediatamente
                if (isPasswordChanged) {
                    performLogout()
                    return chain.proceed(originalRequest)
                }

                // Intentar refresh (el resultado no importa directamente; lo que importa
                // es el token guardado en SharedPreferences después del intento)
                runBlocking {
                    try {
                        authRepository.refreshToken()
                    } catch (e: Exception) {
                        false
                    }
                }

                // Leer siempre el token más reciente de SharedPreferences.
                // Si nuestro refresh falló por rate limit (429), una solicitud concurrente
                // puede haber renovado el token — en ese caso usamos ese token nuevo.
                val latestToken = authRepository.getAccessToken()
                if (!latestToken.isNullOrBlank() && !authRepository.isTokenExpired()) {
                    val retryRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer $latestToken")
                        .build()
                    return chain.proceed(retryRequest)
                }

                // El refresh falló y el token sigue siendo inválido - cerrar sesión porque:
                // - Refresh token no encontrado en Redis (sesión expirada en servidor)
                // - Sesión invalidada explícitamente desde el servidor
                performLogout()
                return chain.proceed(originalRequest)
            }

            response
        } catch (e: Exception) {
            // Error de red - NO cerrar sesión
            // Reintentar la solicitud
            try {
                chain.proceed(originalRequest)
            } catch (e2: Exception) {
                // Si falla de nuevo, permitir el error sin cerrar sesión
                throw e
            }
        }
    }

    /**
     * Cerrar sesión solo debe llamarse explícitamente cuando:
     * - El usuario hace logout manualmente
     * - El usuario cierra sesión desde gestión de sesiones
     * - El servidor indica que la sesión fue invalidada por seguridad (no solo 401)
     */
    private fun performLogout() {
        try {
            runBlocking {
                try {
                    authRepository.logout()
                } catch (e: Exception) {
                    // Ignorar errores en logout
                }
            }
        } catch (e: Exception) {
            // Ignorar errores
        }

        // Mostrar mensaje de error
        try {
            Toast.makeText(context, "Tu sesión ha sido cerrada. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            // Ignorar errores de Toast
        }

        // Pequeño delay para que se muestre el toast
        try {
            Thread.sleep(500)
        } catch (e: Exception) {
            // Ignorar
        }

        // Reiniciar la app al login
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Ignorar errores
        }
    }
}
