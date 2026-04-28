package com.christelldev.easyreferplus.data.network

import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class DriverLiveSocket(private val token: String) {
    private var webSocket: WebSocket? = null
    @Volatile private var isConnected = false
    @Volatile private var isStopped = false
    private val reconnectHandler = Handler(Looper.getMainLooper())

    private val client = OkHttpClient.Builder()
        .pingInterval(3, TimeUnit.SECONDS)
        .build()

    fun connect() {
        isStopped = false
        _connect()
    }

    private fun _connect() {
        if (isStopped) return
        val url = "${AppConfig.WS_URL}/ws/drivers/live?token=$token"
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
                Log.d("DriverLiveSocket", "Conectado al servidor")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                Log.w("DriverLiveSocket", "Cerrado: $code $reason")
                scheduleReconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                Log.e("DriverLiveSocket", "Error: ${t.message}")
                scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        if (isStopped) return
        Log.d("DriverLiveSocket", "Reconectando en 3s...")
        reconnectHandler.postDelayed({
            if (!isStopped) _connect()
        }, 3000)
    }

    fun sendLocation(lat: Double, lng: Double) {
        if (!isConnected) return
        val msg = JSONObject().apply {
            put("type", "location_update")
            put("lat", lat)
            put("lng", lng)
        }.toString()
        webSocket?.send(msg)
    }

    fun sendDutyChange(isOnDuty: Boolean) {
        // El backend ya recibe el cambio de turno via REST (toggle_on_duty).
        // Este envío por WS es un refuerzo adicional si la conexión está activa.
        if (!isConnected) return
        val msg = JSONObject().apply {
            put("type", "duty_change")
            put("is_on_duty", isOnDuty)
        }.toString()
        webSocket?.send(msg)
    }

    fun disconnect() {
        isStopped = true
        isConnected = false
        webSocket?.close(1000, "Goodbye")
        webSocket = null
    }
}
