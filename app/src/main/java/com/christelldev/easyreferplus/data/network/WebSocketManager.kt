package com.christelldev.easyreferplus.data.network

import com.christelldev.easyreferplus.data.model.WalletTransferNotification
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

/**
 * Modelo para notificaciones de venta recibidas por WebSocket
 */
data class SaleNotification(
    @SerializedName("type")
    val type: String,
    @SerializedName("data")
    val data: SaleNotificationData?,
    @SerializedName("timestamp")
    val timestamp: String?
)

data class SaleNotificationData(
    @SerializedName("transaction_id")
    val transactionId: String,
    @SerializedName("qr_code")
    val qrCode: String,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("currency")
    val currency: String,
    @SerializedName("company_name")
    val companyName: String,
    @SerializedName("buyer_name")
    val buyerName: String,
    @SerializedName("buyer_document")
    val buyerDocument: String,
    @SerializedName("buyer_phone")
    val buyerPhone: String,
    @SerializedName("referral_code_used")
    val referralCodeUsed: String?,
    @SerializedName("scan_timestamp")
    val scanTimestamp: String
)

/**
 * WebSocket Manager para notificaciones en tiempo real
 * Usa JWT token para autenticación
 */
class WebSocketManager(
    private val baseUrl: String,
    private val getAccessToken: () -> String
) {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .pingInterval(15, TimeUnit.SECONDS)
        .build()

    private val _notifications = MutableSharedFlow<SaleNotificationData?>(replay = 0)
    val notifications: SharedFlow<SaleNotificationData?> = _notifications.asSharedFlow()

    // Callback directo para notificaciones de venta
    var onNotificationReceived: ((SaleNotificationData) -> Unit)? = null

    // Flow para notificaciones de transferencias de billetera entrantes
    private val _walletTransferFlow = MutableSharedFlow<WalletTransferNotification>(replay = 0)
    val walletTransferFlow: SharedFlow<WalletTransferNotification> = _walletTransferFlow.asSharedFlow()

    private val _connectionState = MutableSharedFlow<ConnectionState>(replay = 1)
    val connectionState: SharedFlow<ConnectionState> = _connectionState.asSharedFlow()

    private var isConnecting = false
    fun isConnecting(): Boolean = isConnecting
    private var currentToken: String? = null

    sealed class ConnectionState {
        data object Disconnected : ConnectionState()
        data object Connecting : ConnectionState()
        data object Connected : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }

    /**
     * Conectar al WebSocket con el token JWT
     */
    fun connect() {
        if (isConnecting) {
            return
        }

        val token = getAccessToken()
        if (token.isNullOrBlank()) {
            _connectionState.tryEmit(ConnectionState.Error("No token available"))
            return
        }

        // Evitar reconectar si ya tenemos una conexión activa con el mismo token
        if (webSocket != null && currentToken == token) {
            return
        }

        // Cerrar conexión anterior si el token cambió
        if (currentToken != token) {
            disconnect()
        }

        currentToken = token
        isConnecting = true

        // AppConfig.WS_URL ya convierte http/https a ws/wss correctamente
        // Solo agregamos el endpoint y el token
        val finalWsUrl = "$baseUrl/ws/notifications?token=$token"

        // Log para debugging
        android.util.Log.d("WebSocket", "Conectando a: $finalWsUrl")
        android.util.Log.d("WebSocket", "Token (primeros 20 chars): ${token.take(20)}...")

        val request = Request.Builder()
            .url(finalWsUrl)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnecting = false
                _connectionState.tryEmit(ConnectionState.Connected)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val gson = Gson()

                    // Leer el campo "type" para enrutar el mensaje al flow correcto
                    val rawType = gson.fromJson(text, Map::class.java)["type"] as? String

                    when (rawType) {
                        "sale_notification" -> {
                            val notification = gson.fromJson(text, SaleNotification::class.java)
                            if (notification.data != null) {
                                _notifications.tryEmit(notification.data)
                                onNotificationReceived?.invoke(notification.data)
                            }
                        }
                        "wallet_transfer_received" -> {
                            val walletNotification = gson.fromJson(text, WalletTransferNotification::class.java)
                            _walletTransferFlow.tryEmit(walletNotification)
                        }
                        else -> {
                            // Tipo no reconocido — ignorar silenciosamente
                        }
                    }
                } catch (e: Exception) {
                    // Error silencioso
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnecting = false
                webSocket.cancel()
                this@WebSocketManager.webSocket = null

                when (code) {
                    1000 -> _connectionState.tryEmit(ConnectionState.Disconnected)
                    4001 -> _connectionState.tryEmit(ConnectionState.Error("Token requerido"))
                    4002 -> _connectionState.tryEmit(ConnectionState.Error("Token inválido o expirado"))
                    4003 -> _connectionState.tryEmit(ConnectionState.Error("Usuario no identificado"))
                    4004 -> _connectionState.tryEmit(ConnectionState.Error("Error de autenticación"))
                    else -> _connectionState.tryEmit(ConnectionState.Error(reason))
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnecting = false
                webSocket.cancel()
                this@WebSocketManager.webSocket = null
                android.util.Log.e("WebSocket", "Error de conexión: ${t.message}", t)
                _connectionState.tryEmit(ConnectionState.Error(t.message ?: "Connection failed"))
            }
        })

        _connectionState.tryEmit(ConnectionState.Connecting)
    }

    /**
     * Desconectar del WebSocket
     */
    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        currentToken = null
        isConnecting = false
        _connectionState.tryEmit(ConnectionState.Disconnected)
    }

    /**
     * Reconectar si hay token disponible
     */
    fun reconnect() {
        if (getAccessToken() != null) {
            connect()
        }
    }

    /**
     * Verificar si está conectado
     */
    fun isConnected(): Boolean = webSocket != null && !isConnecting
}
