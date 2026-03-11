package com.christelldev.easyreferplus.data.model

import com.google.gson.annotations.SerializedName

data class LogoutRequest(
    @SerializedName("refresh_token")
    val refreshToken: String? = null
)

data class LogoutResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String
)
