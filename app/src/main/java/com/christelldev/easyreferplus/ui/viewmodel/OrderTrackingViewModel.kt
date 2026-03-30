package com.christelldev.easyreferplus.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.*
import com.christelldev.easyreferplus.data.network.ApiService
import com.christelldev.easyreferplus.data.network.RetrofitClient
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject

data class OrderTrackingState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val orderId: Int = 0,
    val orderStatus: String = "",
    val companyName: String? = null,
    val dropoffAddress: String? = null,
    val total: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val itemsCount: Int = 0,
    val items: List<TrackingOrderItem> = emptyList(),
    // Markers
    val pickupLatLng: LatLng? = null,
    val dropoffLatLng: LatLng? = null,
    val driverLatLng: LatLng? = null,
    val driverHeading: Float = 0f,
    // Driver info
    val driverName: String? = null,
    val driverPhone: String? = null,
    val driverSelfieUrl: String? = null,
    val vehiclePlate: String? = null,
    val vehicleType: String? = null,
    // ETA
    val etaMinutes: Int? = null,
    val distanceKm: Double? = null,
    // Delivery status
    val deliveryStatus: String? = null,
    val deliveryPhotoUrl: String? = null,
    val isDriverArriving: Boolean = false,
    val isDelivered: Boolean = false,
)

class OrderTrackingViewModel(
    private val apiService: ApiService,
    private val getToken: () -> String,
    private val getWsUrl: () -> String
) : ViewModel() {

    private val _state = MutableStateFlow(OrderTrackingState())
    val state: StateFlow<OrderTrackingState> = _state.asStateFlow()

    private var webSocket: WebSocket? = null
    private var etaPollingJob: Job? = null

    fun loadTracking(orderId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, orderId = orderId, error = null)
            try {
                val response = apiService.getOrderTracking(orderId)
                if (response.isSuccessful) {
                    val info = response.body()!!
                    _state.value = _state.value.copy(
                        isLoading = false,
                        orderStatus = info.status,
                        companyName = info.companyName,
                        dropoffAddress = info.dropoffAddress,
                        total = info.total,
                        deliveryFee = info.deliveryFee,
                        itemsCount = info.itemsCount,
                        items = info.items,
                        pickupLatLng = info.delivery?.pickupLat?.let { lat ->
                            info.delivery.pickupLng?.let { lng -> LatLng(lat, lng) }
                        },
                        dropoffLatLng = info.delivery?.dropoffLat?.let { lat ->
                            info.delivery.dropoffLng?.let { lng -> LatLng(lat, lng) }
                        },
                        driverLatLng = info.driverLocation?.let { LatLng(it.lat, it.lng) },
                        driverName = info.driver?.name,
                        driverPhone = info.driver?.phone,
                        driverSelfieUrl = info.driver?.selfieUrl,
                        vehiclePlate = info.vehicle?.plate,
                        vehicleType = info.vehicle?.type,
                        deliveryStatus = info.delivery?.status,
                        deliveryPhotoUrl = info.delivery?.deliveryPhotoUrl,
                        isDelivered = info.status == "delivered" || info.status == "completed",
                    )
                    // Connect WebSocket for real-time updates
                    if (info.status in listOf("driver_assigned", "ready_for_pickup", "picked_up")) {
                        connectWebSocket(orderId)
                        startEtaPolling(orderId)
                    }
                } else {
                    _state.value = _state.value.copy(isLoading = false, error = "Error al cargar tracking")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = "Error de conexión: ${e.message}")
            }
        }
    }

    private fun connectWebSocket(orderId: Int) {
        if (webSocket != null) return
        val token = getToken()
        val wsUrl = getWsUrl().trimEnd('/')
        val url = "${wsUrl}/ws/delivery/$orderId?token=$token"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(ws: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    when (json.optString("type")) {
                        "driver_location" -> {
                            val lat = json.getDouble("lat")
                            val lng = json.getDouble("lng")
                            val heading = json.optDouble("heading", 0.0).toFloat()
                            val eta = if (json.has("eta_minutes") && !json.isNull("eta_minutes"))
                                json.getInt("eta_minutes") else null
                            val dist = if (json.has("distance_km") && !json.isNull("distance_km"))
                                json.getDouble("distance_km") else null
                            val status = json.optString("delivery_status", _state.value.deliveryStatus ?: "")

                            _state.value = _state.value.copy(
                                driverLatLng = LatLng(lat, lng),
                                driverHeading = heading,
                                etaMinutes = eta ?: _state.value.etaMinutes,
                                distanceKm = dist ?: _state.value.distanceKm,
                                deliveryStatus = status,
                            )
                        }
                        "order_status_changed" -> {
                            val status = json.optString("delivery_status", "")
                            _state.value = _state.value.copy(
                                deliveryStatus = status,
                                orderStatus = when (status) {
                                    "picked_up" -> "picked_up"
                                    "delivered" -> "delivered"
                                    else -> _state.value.orderStatus
                                },
                                isDelivered = status == "delivered",
                            )
                        }
                        "driver_arriving" -> {
                            val eta = if (json.has("eta_minutes") && !json.isNull("eta_minutes"))
                                json.getInt("eta_minutes") else null
                            _state.value = _state.value.copy(
                                isDriverArriving = true,
                                etaMinutes = eta ?: _state.value.etaMinutes,
                            )
                        }
                        "order_delivered" -> {
                            val photoUrl = if (json.has("photo_url") && !json.isNull("photo_url"))
                                json.getString("photo_url") else null
                            _state.value = _state.value.copy(
                                isDelivered = true,
                                deliveryStatus = "delivered",
                                orderStatus = "delivered",
                                deliveryPhotoUrl = photoUrl,
                            )
                            stopEtaPolling()
                        }
                        "pong" -> { /* keep alive */ }
                    }
                } catch (e: Exception) {
                    Log.e("TrackingWS", "Error parsing message: ${e.message}")
                }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: okhttp3.Response?) {
                Log.e("TrackingWS", "WebSocket failed: ${t.message}")
                webSocket = null
                // Reconnect after 3s
                viewModelScope.launch {
                    delay(3000)
                    if (_state.value.orderId > 0 && !_state.value.isDelivered) {
                        connectWebSocket(_state.value.orderId)
                    }
                }
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                webSocket = null
            }
        })

        // Send ping every 30s to keep alive
        viewModelScope.launch {
            while (webSocket != null) {
                delay(30_000)
                try {
                    webSocket?.send("{\"type\":\"ping\"}")
                } catch (_: Exception) {}
            }
        }
    }

    private fun startEtaPolling(orderId: Int) {
        etaPollingJob?.cancel()
        etaPollingJob = viewModelScope.launch {
            while (true) {
                delay(30_000) // Poll every 30s
                try {
                    val response = apiService.getOrderEta(orderId)
                    if (response.isSuccessful) {
                        val eta = response.body()
                        if (eta?.etaMinutes != null) {
                            _state.value = _state.value.copy(
                                etaMinutes = eta.etaMinutes,
                                distanceKm = eta.distanceKm,
                            )
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }

    private fun stopEtaPolling() {
        etaPollingJob?.cancel()
        etaPollingJob = null
    }

    override fun onCleared() {
        super.onCleared()
        webSocket?.close(1000, "ViewModel cleared")
        webSocket = null
        stopEtaPolling()
    }

    class Factory(
        private val apiService: ApiService,
        private val getToken: () -> String,
        private val getWsUrl: () -> String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OrderTrackingViewModel(apiService, getToken, getWsUrl) as T
        }
    }
}
