package com.christelldev.easyreferplus.data.model

import com.google.gson.annotations.SerializedName

data class UserSavedLocation(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    val alias: String? = null,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    @SerializedName("is_favorite") val isFavorite: Boolean,
    @SerializedName("use_count") val useCount: Int,
    @SerializedName("last_used") val lastUsed: String,
    @SerializedName("created_at") val createdAt: String
)

data class SaveLocationRequest(
    val alias: String? = null,
    val address: String,
    val latitude: Double,
    val longitude: Double
)

data class UpdateLocationRequest(
    val alias: String? = null,
    @SerializedName("is_favorite") val isFavorite: Boolean? = null
)
