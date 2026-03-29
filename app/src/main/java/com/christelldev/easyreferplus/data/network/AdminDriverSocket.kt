package com.christelldev.easyreferplus.data.network

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AdminDriverSocket(private val token: String) {
    sealed class Event {
        data class LocationUpdate(val driverId: Int, val lat: Double, val lng: Double) : Event()
        data class DutyChange(val driverId: Int, val isOnDuty: Boolean) : Event()
    }

    private val _events = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .pingInterval(3, TimeUnit.SECONDS)
        .build()

    fun connect() {
        val url = "${AppConfig.WS_URL}/ws/admin/drivers/live?token=$token"
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val type = json.optString("type")
                    val driverId = json.optInt("driver_id", -1)
                    when (type) {
                        "location_update" -> {
                            val lat = json.optDouble("lat", 0.0)
                            val lng = json.optDouble("lng", 0.0)
                            _events.trySend(Event.LocationUpdate(driverId, lat, lng))
                        }
                        "duty_change" -> {
                            val isOnDuty = json.optBoolean("is_on_duty", false)
                            _events.trySend(Event.DutyChange(driverId, isOnDuty))
                        }
                    }
                } catch (_: Exception) {}
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                android.util.Log.e("AdminDriverSocket", "Connection failure: ${t.message}")
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "Goodbye")
        webSocket = null
    }
}
