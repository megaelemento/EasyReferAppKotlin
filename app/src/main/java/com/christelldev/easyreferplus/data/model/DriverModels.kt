package com.christelldev.easyreferplus.data.model

import com.google.gson.annotations.SerializedName

data class DriverProfile(
    @SerializedName("id") val id: Int,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("is_on_duty") val isOnDuty: Boolean,
    @SerializedName("current_lat") val currentLat: Double?,
    @SerializedName("current_lng") val currentLng: Double?
)

data class AvailableOrder(
    @SerializedName("id") val id: Int,
    @SerializedName("delivery_fee") val deliveryFee: Double,
    @SerializedName("total") val total: Double = 0.0,
    @SerializedName("items_count") val itemsCount: Int = 0,
    @SerializedName("dropoff_address") val dropoffAddress: String?,
    @SerializedName("dropoff_lat") val dropoffLat: Double?,
    @SerializedName("dropoff_lng") val dropoffLng: Double?,
    @SerializedName("pickup_lat") val pickupLat: Double? = null,
    @SerializedName("pickup_lng") val pickupLng: Double? = null,
    @SerializedName("pickup_address") val pickupAddress: String? = null
)

data class ActiveDriverOrder(
    @SerializedName("id") val id: Int,
    @SerializedName("status") val orderStatus: String = "",
    @SerializedName("delivery_status") val deliveryStatus: String,
    @SerializedName("total") val total: Double,
    @SerializedName("delivery_fee") val deliveryFee: Double,
    @SerializedName("dropoff_address") val dropoffAddress: String?,
    @SerializedName("dropoff_lat") val dropoffLat: Double?,
    @SerializedName("dropoff_lng") val dropoffLng: Double?,
    @SerializedName("pickup_lat") val pickupLat: Double? = null,
    @SerializedName("pickup_lng") val pickupLng: Double? = null
)

data class DriverActionResponse(val success: Boolean, val message: String? = null)
data class LocationUpdate(val lat: Double, val lng: Double)

data class OnDutyToggleResponse(
    @SerializedName("is_on_duty") val is_on_duty: Boolean,
    @SerializedName("message") val message: String? = null
)

data class ActiveOrderResponse(
    @SerializedName("active_order") val activeOrder: ActiveDriverOrder?
)

data class DriverInvitation(
    @SerializedName("id") val id: Int,
    @SerializedName("company_id") val companyId: Int,
    @SerializedName("company_name") val companyName: String?,
    @SerializedName("status") val status: String,
    @SerializedName("created_at") val createdAt: String?
)

data class AdminDriverLocation(
    @SerializedName("id") val driverId: Int,
    @SerializedName("is_on_duty") val isOnDuty: Boolean,
    @SerializedName("is_busy") val isBusy: Boolean = false,
    @SerializedName("active_order_id") val activeOrderId: Int? = null,
    @SerializedName("lat") val lat: Double?,
    @SerializedName("lng") val lng: Double?,
    @SerializedName("name") val name: String?,
    @SerializedName("vehicle_plate") val vehiclePlate: String? = null,
    @SerializedName("vehicle_type") val vehicleType: String? = null
)

data class CompanyOrderSummary(
    @SerializedName("id") val id: Int,
    @SerializedName("status") val status: String,
    @SerializedName("delivery_status") val deliveryStatus: String?,
    @SerializedName("total") val total: Double,
    @SerializedName("delivery_fee") val deliveryFee: Double,
    @SerializedName("dropoff_address") val dropoffAddress: String?,
    @SerializedName("driver_name") val driverName: String?,
    @SerializedName("paid_at") val paidAt: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("items_count") val itemsCount: Int = 0
)

data class CompanyOrdersResponse(
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("orders") val orders: List<CompanyOrderSummary>
)

data class DeliveryPhotoResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("photo_url") val photoUrl: String?
)

data class DriverEarningsTodayResponse(
    @SerializedName("earnings_today") val earningsToday: Double,
    @SerializedName("deliveries_count") val deliveriesCount: Int,
    @SerializedName("date") val date: String?
)
