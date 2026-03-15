package com.christelldev.easyreferplus.data.model

import com.google.gson.annotations.SerializedName

// =====================================================
// MODELOS DE BILLETERA DIGITAL
// =====================================================

data class WalletBalanceResponse(
    @SerializedName("available_balance")
    val availableBalance: Double,
    @SerializedName("total_balance")
    val totalBalance: Double,
    @SerializedName("pending_withdrawal")
    val pendingWithdrawal: Double
)

data class SetPinRequest(
    @SerializedName("pin")
    val pin: String
)

data class ChangePinRequest(
    @SerializedName("current_pin")
    val currentPin: String,
    @SerializedName("new_pin")
    val newPin: String
)

data class TransferRequest(
    @SerializedName("recipient_phone")
    val recipientPhone: String,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("idempotency_key")
    val idempotencyKey: String,
    @SerializedName("pin")
    val pin: String
)

data class TransferResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("recipient_name")
    val recipientName: String,
    @SerializedName("recipient_phone")
    val recipientPhone: String,
    @SerializedName("sender_balance_after")
    val senderBalanceAfter: Double,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("status")
    val status: String
)

data class WalletStatementItem(
    @SerializedName("id")
    val id: Int,
    @SerializedName("type")
    val type: String,         // "sent" | "received"
    @SerializedName("counterpart_name")
    val counterpartName: String,
    @SerializedName("counterpart_phone")
    val counterpartPhone: String,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("balance_after")
    val balanceAfter: Double,
    @SerializedName("description")
    val description: String?,
    @SerializedName("created_at")
    val createdAt: String
)

data class WalletStatementResponse(
    @SerializedName("items")
    val items: List<WalletStatementItem>,
    @SerializedName("total")
    val total: Int,
    @SerializedName("page")
    val page: Int,
    @SerializedName("per_page")
    val perPage: Int,
    @SerializedName("has_more")
    val hasMore: Boolean
)

// Evento WebSocket: transferencia recibida en tiempo real
data class WalletTransferNotification(
    @SerializedName("type")
    val type: String,         // "wallet_transfer_received"
    @SerializedName("transfer_id")
    val transferId: Int,
    @SerializedName("sender_name")
    val senderName: String,
    @SerializedName("sender_phone")
    val senderPhone: String,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("description")
    val description: String?,
    @SerializedName("new_balance")
    val newBalance: Double,
    @SerializedName("created_at")
    val createdAt: String
)

data class WalletApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: T? = null,
    @SerializedName("message")
    val message: String? = null
)

data class CheckRecipientResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("registered") val registered: Boolean,
    @SerializedName("recipient_name") val recipientName: String? = null,
    @SerializedName("message") val message: String? = null
)
