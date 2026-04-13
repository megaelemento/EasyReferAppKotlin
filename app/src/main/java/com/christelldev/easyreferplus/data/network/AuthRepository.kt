package com.christelldev.easyreferplus.data.network

import android.content.Context
import android.content.SharedPreferences
import com.christelldev.easyreferplus.data.model.LoginRequest
import com.christelldev.easyreferplus.data.model.RefreshTokenRequest
import com.christelldev.easyreferplus.data.model.LoginResponse
import com.christelldev.easyreferplus.data.model.RefreshTokenResponse
import com.christelldev.easyreferplus.data.model.LogoutRequest
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
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
    // Mutex para asegurar que solo una solicitud de refresh ocurra a la vez
    private val refreshMutex = Mutex()

    // SharedFlow para notificar eventos de logout a la UI (MainActivity)
    private val _logoutEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val logoutEvent = _logoutEvent.asSharedFlow()

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
        private const val KEY_USER_NOMBRES = "user_nombres"
        private const val PREF_NAME = "EasyReferPrefs"
    }

    fun triggerLogout(message: String = "Tu sesión ha expirado. Inicia sesión nuevamente.") {
        clearAllData()
        _logoutEvent.tryEmit(message)
    }

    suspend fun login(request: LoginRequest): AuthResult {
        return try {
            val response = apiService.login(request)
            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    saveTokens(loginResponse)
                    registerFcmToken(loginResponse.accessToken)
                    AuthResult.Success(loginResponse)
                } ?: AuthResult.Error(AuthError.Unknown("Respuesta vacía del servidor"))
            } else {
                AuthResult.Error(parseError(response))
            }
        } catch (e: Exception) {
            AuthResult.Error(handleNetworkException(e))
        }
    }

    private suspend fun registerFcmToken(accessToken: String) {
        try {
            val token = suspendCancellableCoroutine<String> { cont ->
                com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            sharedPreferences.edit().putString("fcm_token", token).apply()
            apiService.updateFcmToken(
                authorization = "Bearer $accessToken",
                body = mapOf("token" to token),
            )
            android.util.Log.d("AuthRepo", "Token FCM registrado OK: ${token.take(20)}…")
        } catch (e: Exception) {
            android.util.Log.w("AuthRepo", "Error registrando FCM token: ${e.message}")
        }
    }

    private fun handleNetworkException(e: Exception): AuthError {
        return when {
            e is java.io.IOException && e.message?.contains("mantenimiento", ignoreCase = true) == true ->
                AuthError.Unknown("El servidor está en mantenimiento. Intente más tarde.")
            e is java.net.UnknownHostException || e is java.net.ConnectException || e is java.io.IOException ->
                AuthError.ServerUnavailable
            else -> AuthError.Unknown(e.message ?: "Error desconocido")
        }
    }

    private fun parseError(response: Response<*>): AuthError {
        return try {
            val errorBody = response.errorBody()?.string()
            val apiError = Gson().fromJson(errorBody, ApiErrorResponse::class.java)
            when (response.code()) {
                400 -> if (apiError.message.contains("teléfono", true) || apiError.message.contains("phone", true))
                    AuthError.InvalidPhone else AuthError.InvalidCredentials
                401 -> AuthError.InvalidCredentials
                403 -> AuthError.AccountBlocked
                429 -> AuthError.TooManyAttempts
                in 500..599 -> AuthError.ServerUnavailable
                else -> AuthError.Unknown(apiError.message ?: "Error en la autenticación")
            }
        } catch (e: Exception) {
            when (response.code()) {
                401 -> AuthError.InvalidCredentials
                403 -> AuthError.AccountBlocked
                429 -> AuthError.TooManyAttempts
                in 500..599 -> AuthError.ServerUnavailable
                else -> AuthError.Unknown("Error en la autenticación")
            }
        }
    }

    private fun saveTokens(response: LoginResponse) {
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, response.accessToken)
            putString(KEY_REFRESH_TOKEN, response.refreshToken)
            putLong(KEY_TOKEN_EXPIRY, System.currentTimeMillis() + (response.expiresIn * 1000))
            // Guardar nombre para AppLock screen
            val nombres = response.nombres ?: response.user?.name
            if (nombres != null) putString(KEY_USER_NOMBRES, nombres)
            apply()
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

    fun getAccessToken(): String? = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    fun getRefreshToken(): String? = sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    fun isLoggedIn(): Boolean = getAccessToken() != null
    fun getUserNombres(): String? = sharedPreferences.getString(KEY_USER_NOMBRES, null)

    fun getUserId(): Int {
        val token = getAccessToken() ?: return 0
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return 0
            val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING))
            val json = org.json.JSONObject(payload)
            json.optInt("user_id", 0)
        } catch (_: Exception) { 0 }
    }
    fun getTokenExpiryTime(): Long = sharedPreferences.getLong(KEY_TOKEN_EXPIRY, 0L)

    fun isTokenExpired(): Boolean {
        val expiryTime = getTokenExpiryTime()
        return expiryTime > 0 && System.currentTimeMillis() >= expiryTime
    }

    fun isTokenExpiringSoon(): Boolean {
        val expiryTime = getTokenExpiryTime()
        val fiveMinutesFromNow = System.currentTimeMillis() + (5 * 60 * 1000)
        return expiryTime > 0 && expiryTime <= fiveMinutesFromNow
    }

    fun clearAllData() {
        sharedPreferences.edit().clear().apply()
    }

    /**
     * Refresca el access token usando el refresh token.
     *
     * - PROACTIVO: si el token aún es válido → skip.
     * - REACTIVO (AuthInterceptor tras 401): [forceRefresh]=true.
     *
     * Con refresh_token_rotation=true, cada llamada al endpoint de refresh consume
     * el refresh token anterior. Para evitar la condición de carrera cuando múltiples
     * hilos reciben 401 simultáneamente: se captura el access token ANTES de adquirir
     * el mutex. Si al entrar al mutex el token ya cambió, otro hilo refrescó primero
     * y no se hace una segunda llamada (que fallaría con el token ya rotado).
     */
    suspend fun refreshToken(forceRefresh: Boolean = false): Boolean {
        // Capturar el access token ANTES de esperar el mutex
        val tokenBeforeWait = if (forceRefresh) getAccessToken() else null

        return refreshMutex.withLock {
            // Condición de carrera: si otro hilo ya refrescó mientras esperábamos el mutex,
            // el access token habrá cambiado → reutilizar el nuevo token sin llamar al servidor.
            if (forceRefresh && tokenBeforeWait != null && tokenBeforeWait != getAccessToken()) {
                return@withLock getAccessToken() != null
            }

            val expiryTime = getTokenExpiryTime()
            // Skip si no se fuerza Y el token es válido (o no conocemos el expiry).
            if (!forceRefresh && getAccessToken() != null &&
                (expiryTime == 0L || (!isTokenExpired() && !isTokenExpiringSoon()))
            ) return@withLock true

            val refreshToken = getRefreshToken() ?: return@withLock false
            return@withLock try {
                val response = apiService.refreshToken(RefreshTokenRequest(refreshToken))
                if (response.isSuccessful) {
                    response.body()?.let {
                        saveTokensFromRefresh(it)
                        true
                    } ?: false
                } else {
                    // 401/403 = refresh token inválido → sesión terminada
                    // 429 y otros = error temporal → mantener sesión para reintentar
                    response.code() != 401 && response.code() != 403
                }
            } catch (e: Exception) {
                true // Error de red, no cerrar sesión
            }
        }
    }

    suspend fun validateSessionWithServer(): Boolean {
        val accessToken = getAccessToken() ?: return false
        val result = try {
            val response = apiService.validateSession("Bearer $accessToken", getRefreshToken())
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.valid == true) {
                    if (body.needsRefresh == true) refreshToken() else true
                } else {
                    clearAllData()
                    false
                }
            } else {
                if (response.code() == 401 || response.code() == 403) {
                    clearAllData()
                    false
                } else true
            }
        } catch (e: Exception) {
            true // Error de red, mantener sesión
        }
        // Re-sincronizar token FCM en cada inicio de app con sesión válida
        // para que el backend siempre tenga el token correcto del dispositivo actual
        if (result) registerFcmToken(getAccessToken() ?: accessToken)
        return result
    }

    suspend fun logout(): Boolean {
        val accessToken = getAccessToken()
        val refreshToken = getRefreshToken()
        // Limpiar token FCM del backend ANTES de borrar el access token local
        if (accessToken != null) {
            try { apiService.clearFcmToken("Bearer $accessToken") } catch (_: Exception) {}
        }
        clearAllData()
        if (accessToken == null) return true
        return try {
            val request = refreshToken?.let { LogoutRequest(it) }
            val response = apiService.logout("Bearer $accessToken", refreshToken, request)
            response.isSuccessful || response.code() == 500
        } catch (e: Exception) {
            true
        }
    }

    class Factory(private val context: Context) {
        fun create(): AuthRepository {
            val apiService = RetrofitClient.getInstance().create(ApiService::class.java)
            val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return AuthRepository(apiService, sharedPreferences)
        }
    }
}
