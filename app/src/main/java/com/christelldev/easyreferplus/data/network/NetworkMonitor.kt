package com.christelldev.easyreferplus.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object NetworkMonitor {

    private var lastServerCheck: Long = 0
    private var serverAvailable: Boolean? = null
    private const val CACHE_DURATION_MS = 30000L // 30 segundos

    // Verificar si hay conexión a internet
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Verificar si el servidor está disponible
    // CORREGIDO: Verificación real del servidor
    suspend fun isServerAvailable(): Boolean = withContext(Dispatchers.IO) {
        // Usar cache si está reciente
        if (System.currentTimeMillis() - lastServerCheck < CACHE_DURATION_MS) {
            return@withContext serverAvailable ?: true // Default true para no bloquear
        }

        lastServerCheck = System.currentTimeMillis()

        serverAvailable = try {
            val request = Request.Builder()
                .url(AppConfig.SERVER_CHECK_URL)
                .get()
                .build()

            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()

            val response = client.newCall(request).execute()
            val isSuccess = response.isSuccessful
            response.close()
            isSuccess
        } catch (e: Exception) {
            android.util.Log.e("NetworkMonitor", "Error checking server: ${e.message}")
            true // Default true para no bloquear
        }

        return@withContext serverAvailable ?: true
    }

    // Forzar nueva verificación
    fun resetCache() {
        lastServerCheck = 0
        serverAvailable = null
    }
}
