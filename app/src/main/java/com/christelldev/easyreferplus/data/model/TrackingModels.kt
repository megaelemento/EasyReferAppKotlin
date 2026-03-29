package com.christelldev.easyreferplus.data.model

import com.google.gson.annotations.SerializedName

data class OrderTrackingInfo(
    @SerializedName("order_id") val orderId: Int,
    @SerializedName("status") val status: String,
    @SerializedName("total") val total: Double,
    @SerializedName("subtotal") val subtotal: Double,
    @SerializedName("delivery_fee") val deliveryFee: Double,
    @SerializedName("dropoff_address") val dropoffAddress: String?,
    @SerializedName("observations") val observations: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("paid_at") val paidAt: String?,
    @SerializedName("delivered_at") val deliveredAt: String?,
    @SerializedName("company_name") val companyName: String?,
    @SerializedName("items_count") val itemsCount: Int,
    @SerializedName("items") val items: List<TrackingOrderItem> = emptyList(),
    @SerializedName("delivery") val delivery: TrackingDelivery?,
    @SerializedName("driver") val driver: TrackingDriver?,
    @SerializedName("driver_location") val driverLocation: TrackingLocation?,
    @SerializedName("vehicle") val vehicle: TrackingVehicle?
)

data class TrackingOrderItem(
    @SerializedName("product_name") val productName: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("unit_price") val unitPrice: Double
)

data class TrackingDelivery(
    @SerializedName("status") val status: String?,
    @SerializedName("pickup_lat") val pickupLat: Double?,
    @SerializedName("pickup_lng") val pickupLng: Double?,
    @SerializedName("dropoff_lat") val dropoffLat: Double?,
    @SerializedName("dropoff_lng") val dropoffLng: Double?,
    @SerializedName("delivery_photo_url") val deliveryPhotoUrl: String?,
    @SerializedName("arrived_at_pickup") val arrivedAtPickup: String?,
    @SerializedName("arrived_at_dropoff") val arrivedAtDropoff: String?
)

data class TrackingDriver(
    @SerializedName("driver_id") val driverId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String?,
    @SerializedName("selfie_url") val selfieUrl: String?
)

data class TrackingLocation(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
)

data class TrackingVehicle(
    @SerializedName("type") val type: String?,
    @SerializedName("plate") val plate: String?,
    @SerializedName("brand") val brand: String?,
    @SerializedName("model") val model: String?,
    @SerializedName("color") val color: String?
)

data class OrderEtaResponse(
    @SerializedName("eta_minutes") val etaMinutes: Int?,
    @SerializedName("distance_km") val distanceKm: Double?,
    @SerializedName("driver_lat") val driverLat: Double?,
    @SerializedName("driver_lng") val driverLng: Double?,
    @SerializedName("destination") val destination: String?,
    @SerializedName("message") val message: String?
)
