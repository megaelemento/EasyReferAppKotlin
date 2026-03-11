package com.christelldev.easyreferplus.data.model

import com.google.gson.annotations.SerializedName

// ==================== ADMIN EARNINGS MODELS ====================

data class AdminEarningsResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: List<AdminEarning>?,
    @SerializedName("stats")
    val stats: AdminEarningsStats?,
    @SerializedName("pagination")
    val pagination: AdminEarningsPagination?
)

data class AdminEarningsPagination(
    @SerializedName("page")
    val page: Int,
    @SerializedName("per_page")
    val perPage: Int,
    @SerializedName("total")
    val total: Int,
    @SerializedName("total_pages")
    val totalPages: Int
)

data class AdminEarning(
    @SerializedName("id")
    val id: Int,
    @SerializedName("user_id")
    val userId: Int?,
    @SerializedName("user_name")
    val userName: String?,
    @SerializedName("user_phone")
    val userPhone: String?,
    @SerializedName("user_referral_code")
    val userReferralCode: String?,
    @SerializedName("transaction_id")
    val transactionId: Int?,
    @SerializedName("company_name")
    val companyName: String?,
    @SerializedName("referral_level")
    val referralLevel: Int?,
    @SerializedName("commission_type")
    val commissionType: String?,
    @SerializedName("percentage_of_distribution")
    val percentageOfDistribution: Double?,
    @SerializedName("percentage_of_sale")
    val percentageOfSale: Double?,
    @SerializedName("gross_commission")
    val grossCommission: Double?,
    @SerializedName("net_commission")
    val netCommission: Double?,
    @SerializedName("payment_status")
    val paymentStatus: String?,
    @SerializedName("scheduled_payment_date")
    val scheduledPaymentDate: String?,
    @SerializedName("actual_payment_date")
    val actualPaymentDate: String?,
    @SerializedName("payment_reference")
    val paymentReference: String?,
    @SerializedName("payment_method")
    val paymentMethod: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("description")
    val description: String?
)

data class AdminEarningsStats(
    @SerializedName("total_commissions")
    val totalCommissions: Int,
    @SerializedName("pending_commissions")
    val pendingCommissions: Int,
    @SerializedName("paid_commissions")
    val paidCommissions: Int,
    @SerializedName("level_1_commissions")
    val level1Commissions: Int,
    @SerializedName("level_2_commissions")
    val level2Commissions: Int,
    @SerializedName("level_3_commissions")
    val level3Commissions: Int,
    @SerializedName("total_earnings")
    val totalEarnings: Double,
    @SerializedName("paid_earnings")
    val paidEarnings: Double
)

// ==================== ADMIN USER EARNINGS MODELS ====================

data class AdminUserEarningsResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("user")
    val user: AdminUserInfo?,
    @SerializedName("data")
    val data: List<AdminUserEarning>?,
    @SerializedName("stats")
    val stats: AdminUserEarningsStats?,
    @SerializedName("pagination")
    val pagination: AdminEarningsPagination?
)

data class AdminUserInfo(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("referral_code")
    val referralCode: String?
)

data class AdminUserEarning(
    @SerializedName("id")
    val id: Int,
    @SerializedName("transaction_id")
    val transactionId: Int?,
    @SerializedName("company_name")
    val companyName: String?,
    @SerializedName("referral_level")
    val referralLevel: Int?,
    @SerializedName("commission_type")
    val commissionType: String?,
    @SerializedName("percentage_of_distribution")
    val percentageOfDistribution: Double?,
    @SerializedName("gross_commission")
    val grossCommission: Double?,
    @SerializedName("net_commission")
    val netCommission: Double?,
    @SerializedName("payment_status")
    val paymentStatus: String?,
    @SerializedName("scheduled_payment_date")
    val scheduledPaymentDate: String?,
    @SerializedName("actual_payment_date")
    val actualPaymentDate: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("description")
    val description: String?
)

data class AdminUserEarningsStats(
    @SerializedName("total_commissions")
    val totalCommissions: Int,
    @SerializedName("paid_commissions")
    val paidCommissions: Int,
    @SerializedName("pending_commissions")
    val pendingCommissions: Int,
    @SerializedName("total_earnings")
    val totalEarnings: Double,
    @SerializedName("paid_earnings")
    val paidEarnings: Double
)

// ==================== ADMIN EARNINGS ERROR ====================

data class AdminEarningsError(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("detail")
    val detail: String?
)
