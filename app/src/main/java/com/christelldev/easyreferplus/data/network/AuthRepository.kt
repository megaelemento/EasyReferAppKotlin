package com.christelldev.easyreferplus.data.network

import android.content.Context
import android.content.SharedPreferences
import com.christelldev.easyreferplus.data.model.LoginRequest
import com.christelldev.easyreferplus.data.model.RefreshTokenRequest
import com.christelldev.easyreferplus.data.model.ValidateSessionResponse
import com.christelldev.easyreferplus.data.model.LoginResponse
import com.christelldev.easyreferplus.data.model.RefreshTokenResponse
import com.christelldev.easyreferplus.data.model.LogoutRequest
import com.christelldev.easyreferplus.data.model.LogoutResponse
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import retrofit2.Response

sealed class AuthResult {
    data class Success(val loginResponse: LoginResponse) : AuthResult()
    data class Error(val authError: AuthError) : AuthResult()
}

sealed class AuthError {
    data object InvalidCredentials : AuthError()
    data object InvalidPhone : AuthError()
    data object AccountBlocked : AuthError()
    data object TooManyAttempts : AuthError()
    data object ServerUnavailable : AuthError()
    data class Unknown(val message: String) : AuthError()
}

data class ApiErrorResponse(
    @SerializedName("error")
    val error: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Int
)

class AuthRepository(
    private val apiService: ApiService,
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
        private const val PREF_NAME = "EasyReferPrefs"
    }

    suspend fun login(request: LoginRequest): AuthResult {
        return try {
            val response = apiService.login(request)
            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    saveTokens(loginResponse)
                    AuthResult.Success(loginResponse)
                } ?: AuthResult.Error(AuthError.Unknown("Respuesta vacía del servidor"))
            } else {
                val error = parseError(response)
                AuthResult.Error(error)
            }
        } catch (e: Exception) {
            val error = when {
                e is java.io.IOException && e.message?.contains("mantenimiento", ignoreCase = true) == true ->
                    AuthError.Unknown("El servidor está en mantenimiento. Intente más tarde.")
                e is java.net.UnknownHostException ||
                e is java.net.ConnectException ||
                e is java.io.IOException -> AuthError.ServerUnavailable
                else -> AuthError.Unknown(e.message ?: "Error desconocido")
            }
            AuthResult.Error(error)
        }
    }

    private fun parseError(response: Response<*>): AuthError {
        return try {
            val errorBody = response.errorBody()?.string()
            val apiError = Gson().fromJson(errorBody, ApiErrorResponse::class.java)

            when (response.code()) {
                400 -> {
                    if (apiError.message.contains("teléfono", ignoreCase = true) ||
                        apiError.message.contains("phone", ignoreCase = true)) {
                        AuthError.InvalidPhone
                    } else {
                        AuthError.InvalidCredentials
                    }
                }
                401 -> AuthError.InvalidCredentials
                403 -> AuthError.AccountBlocked
                429 -> AuthError.TooManyAttempts
                500, 502, 503, 504 -> AuthError.ServerUnavailable
                else -> AuthError.Unknown(apiError.message ?: "Error en la autenticación")
            }
        } catch (e: Exception) {
            when (response.code()) {
                401 -> AuthError.InvalidCredentials
                403 -> AuthError.AccountBlocked
                429 -> AuthError.TooManyAttempts
                500, 502, 503, 504 -> AuthError.ServerUnavailable
                else -> AuthError.Unknown("Error en la autenticación")
            }
        }
    }

    private fun saveTokens(response: LoginResponse) {
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, response.accessToken)
            putString(KEY_REFRESH_TOKEN, response.refreshToken)
            putLong(KEY_TOKEN_EXPIRY, System.currentTimeMillis() + (response.expiresIn * 1000))
            apply()
        }
    }

    fun getAccessToken(): String? = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = sharedPreferences.getString(KEY_REFRESH_TOKEN, null)

    fun isLoggedIn(): Boolean = getAccessToken() != null

    fun clearTokens() {
        sharedPreferences.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_TOKEN_EXPIRY)
            apply()
        }
    }

    // Clear ALL cached data on logout
    fun clearAllData() {
        sharedPreferences.edit().clear().apply()
    }

    fun getTokenExpiryTime(): Long = sharedPreferences.getLong(KEY_TOKEN_EXPIRY, 0L)

    fun isTokenExpired(): Boolean {
        val expiryTime = getTokenExpiryTime()
        return expiryTime > 0 && System.currentTimeMillis() >= expiryTime
    }

    // Verificar si el token está por expirar (dentro de 5 minutos)
    // Esto permite renovar el token proactivamente antes de que expire
    fun isTokenExpiringSoon(): Boolean {
        val expiryTime = getTokenExpiryTime()
        val fiveMinutesFromNow = System.currentTimeMillis() + (5 * 60 * 1000)
        return expiryTime > 0 && expiryTime <= fiveMinutesFromNow
    }

    suspend fun refreshToken(): Boolean {
        val refreshToken = getRefreshToken() ?: return false

        return try {
            val response = apiService.refreshToken(RefreshTokenRequest(refreshToken))
            if (response.isSuccessful) {
                response.body()?.let { refreshResponse ->
                    saveTokensFromRefresh(refreshResponse)
                    true
                } ?: false
            } else {
                // Si el refresh falla, NO borrar los tokens
                // Mantener los tokens existentes para que la sesión no se cierre
                // El usuario podrá seguir usando la app hasta que pueda refresh de nuevo
                false
            }
        } catch (e: Exception) {
            // Si hay error de red, NO borrar los tokens
            // Mantener los tokens existentes para que la sesión no se cierre
            false
        }
    }

    /**
     * Valida la sesión con el servidor.
     * Si la sesión es inválida, cierra sesión automáticamente.
     * Si hay error de red, mantiene la sesión (no la cierra).
     * @return true si la sesión es válida, false si es inválida o hay error
     */
    suspend fun validateSessionWithServer(): Boolean {
        val accessToken = getAccessToken() ?: return false

        return try {
            val authorization = "Bearer $accessToken"
            val refreshToken = getRefreshToken()

            val response = apiService.validateSession(authorization, refreshToken)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.valid == true) {
                    // Si necesita refresh, hacerlo automáticamente
                    if (body.needsRefresh == true) {
                        val refreshed = refreshToken()
                        if (refreshed) {
                            // Refresh exitoso, sesión válida
                            return true
                        } else {
                            // Refresh falló, cerrar sesión
                            clearAllData()
                            return false
                        }
                    }
                    true
                } else {
                    // Sesión inválida según el servidor - cerrar sesión
                    clearAllData()
                    false
                }
            } else {
                // Error al validar - según el código de error
                when (response.code()) {
                    401, 403 -> {
                        // Sesión expirada o invalidada - cerrar sesión
                        clearAllData()
                        false
                    }
                    else -> {
                        // Otros errores (500, 502, 503, etc.) - mantener sesión
                        // Puede ser error temporal del servidor
                        true
                    }
                }
            }
        } catch (e: Exception) {
            // Error de red - mantener sesión
            // El usuario puede seguir usando la app y reintentar
            true
        }
    }

    private fun saveTokensFromRefresh(response: RefreshTokenResponse) {
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, response.accessToken)
            putString(KEY_REFRESH_TOKEN, response.refreshToken)
            putLong(KEY_TOKEN_EXPIRY, System.currentTimeMillis() + (response.expiresIn * 1000))
            apply()
        }
    }

    suspend fun logout(): Boolean {
        val accessToken = getAccessToken()
        val refreshToken = getRefreshToken()

        // Siempre limpiar todos los datos locales, incluso si la llamada falla
        clearAllData()

        if (accessToken == null) {
            return true // No hay sesión activa
        }

        return try {
            val authorization = "Bearer $accessToken"
            val request = if (refreshToken != null) {
                LogoutRequest(refreshToken)
            } else {
                null
            }

            val response = apiService.logout(
                authorization = authorization,
                refreshTokenHeader = refreshToken,
                request = request
            )

            // El logout es exitoso si la respuesta es 200 o 500 (como indica la documentación)
            response.isSuccessful || response.code() == 500
        } catch (e: Exception) {
            // En caso de error de red, los tokens ya fueron limpiados
            true
        }
    }

    class Factory(private val context: Context) {
        fun create(): AuthRepository {
            val retrofit = RetrofitClient.getInstance()
            val apiService = retrofit.create(ApiService::class.java)
            val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return AuthRepository(apiService, sharedPreferences)
        }
    }
}
