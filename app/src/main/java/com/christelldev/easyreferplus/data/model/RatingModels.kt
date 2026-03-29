package com.christelldev.easyreferplus.data.model

import com.google.gson.annotations.SerializedName

// ─── Requests ─────────────────────────────────────────────────────────────────

data class RatingRequest(
    @SerializedName("driver_rating") val driverRating: Float?,
    @SerializedName("company_rating") val companyRating: Float?,
    @SerializedName("driver_tags") val driverTags: List<String> = emptyList(),
    @SerializedName("company_tags") val companyTags: List<String> = emptyList(),
    @SerializedName("comment") val comment: String? = null
)

data class TipRequest(
    @SerializedName("amount") val amount: Double
)

// ─── Responses ────────────────────────────────────────────────────────────────

data class RatingResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("order_id") val orderId: Int,
    @SerializedName("driver_rating") val driverRating: Float?,
    @SerializedName("company_rating") val companyRating: Float?,
    @SerializedName("driver_tags") val driverTags: List<String> = emptyList(),
    @SerializedName("company_tags") val companyTags: List<String> = emptyList(),
    @SerializedName("comment") val comment: String?
)

data class MyRatingResponse(
    @SerializedName("rating") val rating: RatingDetail?
)

data class RatingDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("driver_rating") val driverRating: Float?,
    @SerializedName("company_rating") val companyRating: Float?,
    @SerializedName("driver_tags") val driverTags: List<String> = emptyList(),
    @SerializedName("company_tags") val companyTags: List<String> = emptyList(),
    @SerializedName("comment") val comment: String?,
    @SerializedName("created_at") val createdAt: String?
)

data class TipResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("order_id") val orderId: Int,
    @SerializedName("amount") val amount: Double,
    @SerializedName("is_finalized") val isFinalized: Boolean,
    @SerializedName("minutes_remaining") val minutesRemaining: Int?
)

data class TipDetailResponse(
    @SerializedName("tip") val tip: TipDetail?
)

data class TipDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("amount") val amount: Double,
    @SerializedName("is_finalized") val isFinalized: Boolean,
    @SerializedName("minutes_remaining") val minutesRemaining: Int?,
    @SerializedName("created_at") val createdAt: String?
)

data class RatingConfigResponse(
    @SerializedName("tip_adjustment_minutes") val tipAdjustmentMinutes: Int,
    @SerializedName("tip_suggested_percentages") val tipSuggestedPercentages: List<Int>,
    @SerializedName("driver_tags") val driverTags: List<String>,
    @SerializedName("company_tags") val companyTags: List<String>
)
