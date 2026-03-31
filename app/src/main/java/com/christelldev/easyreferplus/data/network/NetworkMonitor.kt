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
    private const val CACHE_DURATION_MS = 30000L

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun isServerAvailable(): Boolean = withContext(Dispatchers.IO) {
        if (System.currentTimeMillis() - lastServerCheck < CACHE_DURATION_MS) {
            return@withContext serverAvailable ?: true
        }

        lastServerCheck = System.currentTimeMillis()

        serverAvailable = try {
            val url = AppConfig.SERVER_CHECK_URL
            val request = Request.Builder().url(url).get().build()
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
            val response = client.newCall(request).execute()
            val isSuccess = response.isSuccessful
            response.close()
            isSuccess
        } catch (e: Exception) {
            android.util.Log.e("NetworkMonitor", "Error: ${e.message}")
            true // Default true — never block the app
        }

        return@withContext serverAvailable ?: true
    }

    fun getCachedAvailability(): Boolean? {
        if (System.currentTimeMillis() - lastServerCheck < CACHE_DURATION_MS) {
            return serverAvailable
        }
        return null
    }

    fun resetCache() {
        lastServerCheck = 0
        serverAvailable = null
    }
}
