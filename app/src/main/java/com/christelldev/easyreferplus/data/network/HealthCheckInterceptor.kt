package com.christelldev.easyreferplus.data.network

import com.christelldev.easyreferplus.data.model.HealthResponse
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking

object HealthCheckInterceptor {
    private const val CACHE_DURATION_MS = 30000L // 30 segundos

    private var lastCheckTime: Long = 0
    private var cachedIsHealthy: Boolean = true // Por defecto true para no mostrar mantenimiento al inicio

    // URLs del health check - usar AppConfig
    private val knownHealthUrls = listOf(
        AppConfig.HEALTH_URL  // Usa la URL configurada en AppConfig
    )

    // Cliente sin interceptores para evitar recursión infinita
    private val healthClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    // Limpiar cache al inicio para forzar nueva verificación
    init {
        resetCache()
    }

    fun isHealthy(): Boolean {
        // Usar cache si está reciente
        if (System.currentTimeMillis() - lastCheckTime < CACHE_DURATION_MS) {
            return cachedIsHealthy
        }

        // Hacer health check
        lastCheckTime = System.currentTimeMillis()
        
        // Probar URLs conocidas
        for (healthUrl in knownHealthUrls) {
            try {
                val request = Request.Builder()
                    .url(healthUrl)
                    .get()
                    .build()

                val response = healthClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    val healthResponse = Gson().fromJson(body, HealthResponse::class.java)
                    if (healthResponse.success) {
                        android.util.Log.d("HealthCheck", "Servidor disponible: $healthUrl")
                        cachedIsHealthy = true
                        response.close()
                        return true
                    }
                }
                response.close()
            } catch (e: Exception) {
                android.util.Log.w("HealthCheck", "Error conectando a $healthUrl: ${e.message}")
            }
        }
        cachedIsHealthy = false
        return false
    }

    fun resetCache() {
        lastCheckTime = 0
        cachedIsHealthy = true
    }
}

class ServerUnavailableInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // Verificar salud del servidor antes de hacer la petición
        if (!HealthCheckInterceptor.isHealthy()) {
            // Lanzar IOException que será manejada por Retrofit
            throw IOException("En mantenimiento, intente más tarde")
        }
        return chain.proceed(chain.request())
    }
}
