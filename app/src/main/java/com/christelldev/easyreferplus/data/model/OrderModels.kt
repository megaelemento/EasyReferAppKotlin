package com.christelldev.easyreferplus.data.model

import com.google.gson.annotations.SerializedName

data class OrderOut(
    @SerializedName("id") val id: Int,
    @SerializedName("status") val status: String,
    @SerializedName("total") val total: Double,
    @SerializedName("subtotal") val subtotal: Double,
    @SerializedName("delivery_fee") val deliveryFee: Double,
    @SerializedName("delivery_required") val deliveryRequired: Boolean,
    @SerializedName("dropoff_address") val dropoffAddress: String?,
    @SerializedName("dropoff_lat") val dropoffLat: Double?,
    @SerializedName("dropoff_lng") val dropoffLng: Double?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("paid_at") val paidAt: String?,
    @SerializedName("observations") val observations: String?,
    @SerializedName("items") val items: List<OrderItemOut> = emptyList()
)

data class OrderItemOut(
    @SerializedName("product_name") val productName: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("unit_price") val unitPrice: Double,
    @SerializedName("company_name") val companyName: String?
)

data class OrderCreateRequest(
    @SerializedName("use_cart") val useCart: Boolean = true,
    @SerializedName("delivery_required") val deliveryRequired: Boolean = false,
    @SerializedName("delivery_company_id") val deliveryCompanyId: Int? = null,
    @SerializedName("dropoff_address") val dropoffAddress: String? = null,
    @SerializedName("dropoff_lat") val dropoffLat: Double? = null,
    @SerializedName("dropoff_lng") val dropoffLng: Double? = null
)

data class OrderCreateResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("status") val status: String,
    @SerializedName("total") val total: Double
)

data class DeliveryOption(
    @SerializedName("company_id") val companyId: Int,
    @SerializedName("company_name") val companyName: String,
    @SerializedName("delivery_fee") val deliveryFee: Double,
    @SerializedName("distance_km") val distanceKm: Double = 0.0,
    @SerializedName("available") val available: Boolean = true,
    @SerializedName("vehicles_count") val vehiclesCount: Int = 0
)

data class DeliveryOptionsResponse(
    @SerializedName("options") val options: List<DeliveryOption>
)

data class SimulatePaymentResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("order_id") val orderId: Int,
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

data class SavedAddress(
    @SerializedName("id") val id: Int,
    @SerializedName("label") val label: String,
    @SerializedName("address") val address: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double,
    @SerializedName("is_default") val isDefault: Boolean = false
)

data class CreateAddressRequest(
    @SerializedName("label") val label: String,
    @SerializedName("address") val address: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double,
    @SerializedName("is_default") val isDefault: Boolean = false
)

data class UpdateAddressRequest(
    @SerializedName("label") val label: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("lat") val lat: Double? = null,
    @SerializedName("lng") val lng: Double? = null,
    @SerializedName("is_default") val isDefault: Boolean? = null
)

data class SimpleSuccessResponse(val success: Boolean = true, val message: String? = null)

// Modelos para "Mis Ventas" (órdenes recibidas por el establecimiento)
data class StoreOrderItem(
    @SerializedName("product_name") val productName: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("unit_price") val unitPrice: Double,
    @SerializedName("subtotal") val subtotal: Double
)

data class StoreOrderSummary(
    @SerializedName("id") val id: Int,
    @SerializedName("status") val status: String,
    @SerializedName("buyer_name") val buyerName: String?,
    @SerializedName("my_subtotal") val mySubtotal: Double,
    @SerializedName("delivery_required") val deliveryRequired: Boolean,
    @SerializedName("dropoff_address") val dropoffAddress: String?,
    @SerializedName("observations") val observations: String?,
    @SerializedName("paid_at") val paidAt: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("items") val items: List<StoreOrderItem> = emptyList()
)

data class StoreOrdersResponse(
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("orders") val orders: List<StoreOrderSummary>
)
