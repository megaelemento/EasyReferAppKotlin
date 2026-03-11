package com.christelldev.easyreferplus.data.model

import com.google.gson.annotations.SerializedName

data class HealthResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("version")
    val version: String?,
    @SerializedName("environment")
    val environment: String?
)
