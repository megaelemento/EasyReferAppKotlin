package com.christelldev.easyreferplus.data.model

import com.google.gson.annotations.SerializedName

// Response models for referrals
data class ReferralTreeResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("user_referral_code")
    val userReferralCode: String,
    @SerializedName("referral_tree")
    val referralTree: ReferralTree,
    @SerializedName("totals")
    val totals: ReferralTotals
)

data class ReferralTree(
    @SerializedName("level_1")
    val level1: List<String>,
    @SerializedName("level_2")
    val level2: List<String>,
    @SerializedName("level_3")
    val level3: List<String>
)

data class ReferralTotals(
    @SerializedName("level_1")
    val level1: Int,
    @SerializedName("level_2")
    val level2: Int,
    @SerializedName("level_3")
    val level3: Int,
    @SerializedName("total")
    val total: Int
)

data class SearchReferralResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("search_code")
    val searchCode: String,
    @SerializedName("found")
    val found: Boolean,
    @SerializedName("level")
    val level: Int?
)

data class SearchReferralRequest(
    @SerializedName("code")
    val code: String
)
