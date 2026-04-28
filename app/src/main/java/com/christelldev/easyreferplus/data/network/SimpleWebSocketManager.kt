package com.christelldev.easyreferplus.data.network

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

/**
 * WebSocket Manager simple para tiempo real
 * Se conecta cuando se necesita y desconecta cuando no
 */
class SimpleWebSocketManager(
    private val context: Context,
    private val baseUrl: String,
    private val getAccessToken: () -> String
) {
    companion object {
        private const val TAG = "SimpleWS"
        
        const val CHANNEL_REFERRALS = "referrals"
        const val CHANNEL_WITHDRAWALS = "withdrawals"
        const val CHANNEL_EARNINGS = "earnings"
        const val CHANNEL_WALLET = "wallet"
        const val CHANNEL_ORDERS = "orders"
        const val CHANNEL_COMPANY_SALES = "company_sales"
    }

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .pingInterval(15, TimeUnit.SECONDS)
        .build()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gson = Gson()

    // Estado
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    // Canales activos
    private val activeChannels = mutableSetOf<String>()
    private var currentToken: String? = null
    private var isConnecting = false
    private var isShutdown = false

    // Datos en tiempo real
    private val _referralsData = MutableStateFlow<Map<String, Any>?>(null)
    val referralsData: StateFlow<Map<String, Any>?> = _referralsData.asStateFlow()

    private val _withdrawalsData = MutableStateFlow<Map<String, Any>?>(null)
    val withdrawalsData: StateFlow<Map<String, Any>?> = _withdrawalsData.asStateFlow()

    private val _earningsData = MutableStateFlow<Map<String, Any>?>(null)
    val earningsData: StateFlow<Map<String, Any>?> = _earningsData.asStateFlow()

    private val _walletData = MutableStateFlow<Map<String, Any>?>(null)
    val walletData: StateFlow<Map<String, Any>?> = _walletData.asStateFlow()

    private val _ordersData = MutableStateFlow<Map<String, Any>?>(null)
    val ordersData: StateFlow<Map<String, Any>?> = _ordersData.asStateFlow()

    private val _companySalesData = MutableStateFlow<Map<String, Any>?>(null)
    val companySalesData: StateFlow<Map<String, Any>?> = _companySalesData.asStateFlow()

    sealed class ConnectionState {
        data object Disconnected : ConnectionState()
        data object Connecting : ConnectionState()
        data object Connected : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }

    /**
     * Suscribirse a un canal
     */
    fun subscribe(channel: String) {
        Log.d(TAG, "Suscribiendo a: $channel")
        activeChannels.add(channel)
        if (!isConnected()) {
            connect()
        } else {
            sendSubscribe(channel)
        }
    }

    /**
     * Desuscribirse de un canal
     */
    fun unsubscribe(channel: String) {
        Log.d(TAG, "Desuscribiendo de: $channel")
        activeChannels.remove(channel)
        sendUnsubscribe(channel)
        
        if (activeChannels.isEmpty()) {
            disconnect()
        }
    }

    /**
     * Conectar al WebSocket
     */
    fun connect() {
        if (isShutdown || isConnecting || isConnected()) return

        val token = getAccessToken()
        if (token.isNullOrBlank()) {
            _connectionState.value = ConnectionState.Error("No token")
            return
        }

        currentToken = token
        isConnecting = true
        _connectionState.value = ConnectionState.Connecting

        val wsUrl = "${baseUrl.replace("http", "ws")}/ws/notifications?token=$token"
        Log.d(TAG, "Conectando a: $wsUrl")

        val request = Request.Builder().url(wsUrl).build()
        val self = this

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                self.isConnecting = false
                _connectionState.value = ConnectionState.Connected
                Log.d(TAG, "Conectado!")

                // Re-suscribir a canales activos
                activeChannels.forEach { sendSubscribe(it) }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleMessage(text)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                self.webSocket = null
                self.isConnecting = false
                _connectionState.value = ConnectionState.Disconnected
                Log.d(TAG, "Cerrado: $code - $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                self.webSocket = null
                self.isConnecting = false
                _connectionState.value = ConnectionState.Error(t.message ?: "Error")
                Log.e(TAG, "Error: ${t.message}")
            }
        })
    }

    /**
     * Desconectar
     */
    fun disconnect() {
        webSocket?.close(1000, "User disconnect")
        webSocket = null
        currentToken = null
        isConnecting = false
        _connectionState.value = ConnectionState.Disconnected
        activeChannels.clear()
    }

    /**
     * Está conectado?
     */
    fun isConnected(): Boolean =
        webSocket != null && !isConnecting && _connectionState.value is ConnectionState.Connected

    /**
     * Apagar el manager completamente — usar desde onCleared() del ViewModel
     */
    fun shutdown() {
        isShutdown = true
        disconnect()
        scope.coroutineContext[kotlinx.coroutines.Job.Key]?.cancel()
    }

    /**
     * Enviar suscripción
     */
    private fun sendSubscribe(channel: String) {
        val msg = mapOf("action" to "subscribe", "channel" to channel)
        webSocket?.send(gson.toJson(msg))
    }

    /**
     * Enviar desuscripción
     */
    private fun sendUnsubscribe(channel: String) {
        val msg = mapOf("action" to "unsubscribe", "channel" to channel)
        webSocket?.send(gson.toJson(msg))
    }

    /**
     * Manejar mensaje recibido
     */
    private fun handleMessage(text: String) {
        try {
            val json = gson.fromJson(text, Map::class.java)
            val type = json["type"] as? String
            val data = json["data"] as? Map<String, Any>

            when (type) {
                "referrals_update" -> _referralsData.value = data
                "withdrawals_update" -> _withdrawalsData.value = data
                "earnings_update" -> _earningsData.value = data
                "wallet_update" -> _walletData.value = data
                "orders_update" -> _ordersData.value = data
                "company_sale" -> _companySalesData.value = data
                else -> Log.d(TAG, "Mensaje desconocido: $type")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parseando mensaje: ${e.message}")
        }
    }
}
