package com.christelldev.easyreferplus.data.model

import com.google.gson.annotations.SerializedName
import com.christelldev.easyreferplus.data.model.AdminUserInfo

// =====================================================
// RESUMEN DE GANANCIAS DEL USUARIO
// =====================================================

data class EarningsSummaryResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("total_commissions")
    val totalCommissions: Int,
    @SerializedName("total_earned")
    val totalEarned: Double,
    @SerializedName("total_paid")
    val totalPaid: Double,
    @SerializedName("total_pending")
    val totalPending: Double,
    @SerializedName("pending_count")
    val pendingCount: Int,
    @SerializedName("paid_count")
    val paidCount: Int,
    @SerializedName("scheduled_count")
    val scheduledCount: Int,
    @SerializedName("earnings_by_level")
    val earningsByLevel: EarningsByLevel?,
    @SerializedName("commission_percentages")
    val commissionPercentages: CommissionPercentages?,
    @SerializedName("top_companies")
    val topCompanies: List<CompanyEarnings>?
)

data class EarningsByLevel(
    @SerializedName("level_1")
    val level1: Double,
    @SerializedName("level_2")
    val level2: Double,
    @SerializedName("level_3")
    val level3: Double
)

// Porcentajes de comisión desde el backend
data class CommissionPercentages(
    @SerializedName("level_1")
    val level1: Double,
    @SerializedName("level_2")
    val level2: Double,
    @SerializedName("level_3")
    val level3: Double,
    @SerializedName("platform")
    val platform: Double
)

data class CompanyEarnings(
    @SerializedName("company_name")
    val companyName: String,
    @SerializedName("total_earned")
    val totalEarned: Double
)

// =====================================================
// COMISIÓN INDIVIDUAL (para lista de comisiones)
// =====================================================

data class CommissionResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("transaction_id")
    val transactionId: Int?,
    @SerializedName("company_name")
    val companyName: String,
    @SerializedName("buyer_document")
    val buyerDocument: String?,
    @SerializedName("buyer_name")
    val buyerName: String?,
    @SerializedName("referral_level")
    val referralLevel: Int?,
    @SerializedName("commission_type")
    val commissionType: String?,
    @SerializedName("gross_commission")
    val grossCommission: Double,
    @SerializedName("net_commission")
    val netCommission: Double,
    @SerializedName("payment_status")
    val paymentStatus: String?,
    @SerializedName("scheduled_payment_date")
    val scheduledPaymentDate: String?,
    @SerializedName("actual_payment_date")
    val actualPaymentDate: String?,
    @SerializedName("payment_reference")
    val paymentReference: String?,
    @SerializedName("payment_notes")
    val paymentNotes: String?,
    @SerializedName("transaction_date")
    val transactionDate: String?,
    @SerializedName("created_at")
    val createdAt: String?
)

// =====================================================
// ADMIN: RESUMEN GENERAL DE GANANCIAS
// =====================================================

data class AdminEarningsSummaryResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("timestamp")
    val timestamp: String?,
    @SerializedName("users")
    val users: AdminUsersStats?,
    @SerializedName("companies")
    val companies: AdminCompaniesStats?,
    @SerializedName("transactions")
    val transactions: AdminTransactionsStats?,
    @SerializedName("user_commissions")
    val userCommissions: AdminUserCommissionsStats?,
    @SerializedName("platform_earnings")
    val platformEarnings: AdminPlatformEarnings?
)

data class AdminUsersStats(
    @SerializedName("total")
    val total: Int,
    @SerializedName("verified")
    val verified: Int,
    @SerializedName("pending_verification")
    val pendingVerification: Int
)

data class AdminCompaniesStats(
    @SerializedName("total")
    val total: Int,
    @SerializedName("validated")
    val validated: Int,
    @SerializedName("pending")
    val pending: Int,
    @SerializedName("active")
    val active: Int
)

data class AdminTransactionsStats(
    @SerializedName("total")
    val total: Int,
    @SerializedName("completed")
    val completed: Int,
    @SerializedName("total_sales_volume")
    val totalSalesVolume: Double,
    @SerializedName("total_commissions_generated")
    val totalCommissionsGenerated: Double
)

data class AdminUserCommissionsStats(
    @SerializedName("total_commissions")
    val totalCommissions: Int,
    @SerializedName("total_earned")
    val totalEarned: Double,
    @SerializedName("total_paid")
    val totalPaid: Double,
    @SerializedName("total_pending")
    val totalPending: Double,
    @SerializedName("total_scheduled")
    val totalScheduled: Double,
    @SerializedName("by_level")
    val byLevel: AdminEarningsByLevel?
)

data class AdminEarningsByLevel(
    @SerializedName("level_1")
    val level1: Double,
    @SerializedName("level_2")
    val level2: Double,
    @SerializedName("level_3")
    val level3: Double
)

data class AdminPlatformEarnings(
    @SerializedName("total")
    val total: Double,
    @SerializedName("orphan_referrals")
    val orphanReferrals: Double,
    @SerializedName("platform_commission")
    val platformCommission: Double
)

// =====================================================
// ADMIN: RESUMEN DE GANANCIAS DE USUARIO ESPECÍFICO
// =====================================================

data class AdminUserEarningsSummaryResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("user")
    val user: AdminUserInfo?,
    @SerializedName("referrals_count")
    val referralsCount: Int,
    @SerializedName("commissions")
    val commissions: AdminUserCommissionsDetail?,
    @SerializedName("top_companies")
    val topCompanies: List<CompanyEarnings>?
)

data class AdminUserCommissionsDetail(
    @SerializedName("total_count")
    val totalCount: Int,
    @SerializedName("total_earned")
    val totalEarned: Double,
    @SerializedName("total_paid")
    val totalPaid: Double,
    @SerializedName("total_pending")
    val totalPending: Double,
    @SerializedName("total_scheduled")
    val totalScheduled: Double,
    @SerializedName("by_level")
    val byLevel: AdminEarningsByLevel?
)
