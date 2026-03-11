package com.christelldev.easyreferplus.data.model

import com.google.gson.annotations.SerializedName

data class CategoryInfo(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("is_active")
    val isActive: Boolean
)

data class ServiceInfo(
    @SerializedName("id")
    val id: Int,
    @SerializedName("category_id")
    val categoryId: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("category_name")
    val categoryName: String?
)
