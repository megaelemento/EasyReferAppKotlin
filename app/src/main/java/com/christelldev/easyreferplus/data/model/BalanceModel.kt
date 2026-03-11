package com.christelldev.easyreferplus.data.model

import com.google.gson.annotations.SerializedName

// Balance y estadísticas del usuario
data class BalanceResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("balance")
    val balance: UserBalance?
)

data class UserBalance(
    @SerializedName("available_balance")
    val availableBalance: Double,
    @SerializedName("pending_balance")
    val pendingBalance: Double,
    @SerializedName("total_earned")
    val totalEarned: Double,
    @SerializedName("total_withdrawn")
    val totalWithdrawn: Double
)
