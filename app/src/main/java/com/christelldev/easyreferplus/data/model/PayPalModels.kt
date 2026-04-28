package com.christelldev.easyreferplus.data.model

import com.google.gson.annotations.SerializedName

data class CreatePayPalOrderRequest(
    @SerializedName("amount") val amount: Double,
    @SerializedName("order_id") val orderId: Int? = null,
    @SerializedName("notes") val notes: String? = null
)

data class CreatePayPalOrderResponse(
    @SerializedName("paypal_order_id") val paypalOrderId: String,
    @SerializedName("approve_url") val approveUrl: String? = null,
    @SerializedName("status") val status: String,
    @SerializedName("amount") val amount: Double? = null
)

data class CapturePayPalOrderRequest(
    @SerializedName("paypal_order_id") val paypalOrderId: String,
    @SerializedName("notes") val notes: String? = null
)

data class CapturePayPalOrderResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("payment_id") val paymentId: Int?,
    @SerializedName("amount") val amount: Double,
    @SerializedName("status") val status: String
)
