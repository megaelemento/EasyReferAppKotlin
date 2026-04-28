package com.christelldev.easyreferplus.data.model

import com.google.gson.annotations.SerializedName

data class ActiveDispatch(
    @SerializedName("order_id") val orderId: Int,
    @SerializedName("buyer_name") val buyerName: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("total_items") val totalItems: Int,
    @SerializedName("status") val status: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("delivery_required") val deliveryRequired: Boolean
)

data class DispatchDetail(
    @SerializedName("order_id") val orderId: Int,
    @SerializedName("status") val status: String,
    @SerializedName("total") val total: Double,
    @SerializedName("buyer") val buyer: DispatchBuyer,
    @SerializedName("items") val items: List<DispatchItem>,
    @SerializedName("delivery_required") val deliveryRequired: Boolean,
    @SerializedName("dropoff_lat") val dropoffLat: Double?,
    @SerializedName("dropoff_lng") val dropoffLng: Double?,
    @SerializedName("dropoff_address") val dropoffAddress: String?,
    @SerializedName("observations") val observations: String?
)

data class DispatchBuyer(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String?
)

data class DispatchItem(
    @SerializedName("product_name") val productName: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("unit_price") val unitPrice: Double
)

data class ActiveDispatchesResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("dispatches") val dispatches: List<ActiveDispatch>
)
