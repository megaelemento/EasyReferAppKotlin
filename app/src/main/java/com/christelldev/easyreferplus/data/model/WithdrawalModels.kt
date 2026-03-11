package com.christelldev.easyreferplus.data.model

import com.google.gson.annotations.SerializedName

// =====================================================
// BALANCE DE COMISIONES
// =====================================================

data class CommissionBalanceResponse(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("level_1_balance")
    val level1Balance: Double,
    @SerializedName("level_2_balance")
    val level2Balance: Double,
    @SerializedName("level_3_balance")
    val level3Balance: Double,
    @SerializedName("total_balance")
    val totalBalance: Double,
    @SerializedName("total_withdrawn")
    val totalWithdrawn: Double,
    @SerializedName("pending_withdrawal")
    val pendingWithdrawal: Double,
    @SerializedName("available_for_withdrawal")
    val availableForWithdrawal: Double,
    @SerializedName("minimum_withdrawal_amount")
    val minimumWithdrawalAmount: Double,
    @SerializedName("can_request_withdrawal")
    val canRequestWithdrawal: Boolean
)

// =====================================================
// CUENTAS BANCARIAS
// =====================================================

data class BankAccountResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("bank_name")
    val bankName: String,
    @SerializedName("account_type")
    val accountType: String,
    @SerializedName("account_number")
    val accountNumber: String,
    @SerializedName("masked_account_number")
    val maskedAccountNumber: String?,
    @SerializedName("account_holder_name")
    val accountHolderName: String,
    @SerializedName("account_holder_document")
    val accountHolderDocument: String?,
    @SerializedName("bank_code")
    val bankCode: String?,
    @SerializedName("is_default")
    val isDefault: Boolean
)

data class BankAccountListResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("accounts")
    val accounts: List<BankAccountResponse>,
    @SerializedName("default_account_id")
    val defaultAccountId: Int?
)

// Request para crear cuenta bancaria
data class CreateBankAccountRequest(
    @SerializedName("bank_name")
    val bankName: String,
    @SerializedName("account_type")
    val accountType: String,  // "ahorros" o "corriente"
    @SerializedName("account_number")
    val accountNumber: String,
    @SerializedName("account_holder_name")
    val accountHolderName: String,
    @SerializedName("account_holder_document")
    val accountHolderDocument: String,
    @SerializedName("is_default")
    val isDefault: Boolean = true
)

// =====================================================
// SOLICITUDES DE RETIRO
// =====================================================

data class WithdrawalRequestResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("requested_amount")
    val requestedAmount: Double,
    @SerializedName("available_balance")
    val availableBalance: Double,
    @SerializedName("status")
    val status: String,
    @SerializedName("bank_account")
    val bankAccount: BankAccountResponse?,
    @SerializedName("review_notes")
    val reviewNotes: String?,
    @SerializedName("rejection_reason")
    val rejectionReason: String?,
    @SerializedName("transaction_reference")
    val transactionReference: String?,
    @SerializedName("reviewed_by_name")
    val reviewedByName: String?,
    @SerializedName("reviewed_at")
    val reviewedAt: String?,
    @SerializedName("processed_at")
    val processedAt: String?,
    @SerializedName("created_at")
    val createdAt: String
)

data class WithdrawalRequestListResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("requests")
    val requests: List<WithdrawalRequestResponse>,
    @SerializedName("total")
    val total: Int,
    @SerializedName("page")
    val page: Int,
    @SerializedName("total_pages")
    val totalPages: Int
)

// Request para crear solicitud de retiro
data class CreateWithdrawalRequest(
    @SerializedName("requested_amount")
    val requestedAmount: Double,
    @SerializedName("bank_account_id")
    val bankAccountId: Int
)

// Respuesta exitosa de operaciones de retiro
data class WithdrawalSuccessResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: WithdrawalSuccessData?
)

data class WithdrawalSuccessData(
    @SerializedName("account_id")
    val accountId: Int?,
    @SerializedName("request_id")
    val requestId: Int?,
    @SerializedName("estimated_processing_time")
    val estimatedProcessingTime: String?
)

// =====================================================
// SOLICITUDES DE RETIRO - ADMIN
// =====================================================

// Solicitud de retiro con información del usuario (para admin)
data class AdminWithdrawalResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("user_name")
    val userName: String,
    @SerializedName("user_phone")
    val userPhone: String,
    @SerializedName("user_email")
    val userEmail: String?,
    @SerializedName("requested_amount")
    val requestedAmount: Double,
    @SerializedName("available_balance")
    val availableBalance: Double,
    @SerializedName("status")
    val status: String,
    @SerializedName("bank_account")
    val bankAccount: BankAccountResponse?,
    @SerializedName("review_notes")
    val reviewNotes: String?,
    @SerializedName("rejection_reason")
    val rejectionReason: String?,
    @SerializedName("transaction_reference")
    val transactionReference: String?,
    @SerializedName("reviewed_by_name")
    val reviewedByName: String?,
    @SerializedName("reviewed_at")
    val reviewedAt: String?,
    @SerializedName("processed_at")
    val processedAt: String?,
    @SerializedName("created_at")
    val createdAt: String
)

// Lista de solicitudes de retiro para admin
data class AdminWithdrawalsListResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("withdrawals")
    val withdrawals: List<AdminWithdrawalResponse>,
    @SerializedName("total")
    val total: Int,
    @SerializedName("pending_count")
    val pendingCount: Int,
    @SerializedName("approved_count")
    val approvedCount: Int,
    @SerializedName("rejected_count")
    val rejectedCount: Int,
    @SerializedName("postponed_count")
    val postponedCount: Int,
    @SerializedName("page")
    val page: Int,
    @SerializedName("total_pages")
    val totalPages: Int
)

// Request para aprobar/rechazar/postergar retiro
data class AdminWithdrawalActionRequest(
    @SerializedName("status")
    val status: String, // "approved", "rejected", "postponed"
    @SerializedName("review_notes")
    val reviewNotes: String? = null,
    @SerializedName("rejection_reason")
    val rejectionReason: String? = null,
    @SerializedName("transaction_reference")
    val transactionReference: String? = null,
    @SerializedName("processing_notes")
    val processingNotes: String? = null
)

// Respuesta de acción de retiro
data class AdminWithdrawalActionResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("withdrawal")
    val withdrawal: AdminWithdrawalResponse?
)
