package com.christelldev.easyreferplus.data.model

import com.google.gson.annotations.SerializedName

// ==================== PRODUCT CATEGORIES ====================

data class ProductCategory(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("icon")
    val icon: String? = null,
    @SerializedName("is_active")
    val isActive: Boolean = true,
    @SerializedName("display_order")
    val displayOrder: Int = 0,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

// ==================== PRODUCT IMAGES ====================

data class ProductImage(
    @SerializedName("id")
    val id: Int,
    @SerializedName("product_id")
    val productId: Int,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("is_primary")
    val isPrimary: Boolean = false
)

// ==================== PRODUCT (EXTENDED) ====================

data class Product(
    @SerializedName("id")
    val id: Int,
    @SerializedName("product_name")
    val productName: String,
    @SerializedName("product_description")
    val productDescription: String? = null,
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("product_category_id")
    val productCategoryId: Int? = null,
    @SerializedName("size")
    val size: String? = null,
    @SerializedName("weight")
    val weight: String? = null,
    @SerializedName("dimensions")
    val dimensions: String? = null,
    @SerializedName("keywords")
    val keywords: String? = null,
    @SerializedName("quantity")
    val quantity: Int = 0,
    @SerializedName("price")
    val price: Double,
    @SerializedName("offer_price")
    val offerPrice: Double? = null,
    @SerializedName("specific_commission_percentage")
    val specificCommissionPercentage: Double? = null,
    @SerializedName("use_company_default")
    val useCompanyDefault: Boolean = true,
    @SerializedName("status")
    val status: String = "draft",
    @SerializedName("display_order")
    val displayOrder: Int = 0,
    @SerializedName("is_active")
    val isActive: Boolean = true,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("images")
    val images: List<ProductImage> = emptyList(),
    @SerializedName("effective_commission_percentage")
    val effectiveCommissionPercentage: Double? = null,
    @SerializedName("current_price")
    val currentPrice: Double? = null,
    @SerializedName("manage_stock")
    val manageStock: Boolean = true
) {
    val displayPrice: Double
        get() = currentPrice ?: offerPrice ?: price

    val primaryImage: ProductImage?
        get() = images.firstOrNull { it.isPrimary } ?: images.firstOrNull()
}

// ==================== CART ITEMS ====================

data class CartItem(
    @SerializedName("id")
    val id: Int,
    @SerializedName("product_id")
    val productId: Int,
    @SerializedName("product_name")
    val productName: String,
    @SerializedName("product_description")
    val productDescription: String? = null,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("price")
    val price: Double,
    @SerializedName("offer_price")
    val offerPrice: Double? = null,
    @SerializedName("current_price")
    val currentPrice: Double? = null,
    @SerializedName("company_id")
    val companyId: Int,
    @SerializedName("company_name")
    val companyName: String? = null,
    @SerializedName("size")
    val size: String? = null,
    @SerializedName("weight")
    val weight: String? = null,
    @SerializedName("dimensions")
    val dimensions: String? = null,
    @SerializedName("images")
    val images: List<ProductImage> = emptyList(),
    @SerializedName("subtotal")
    val subtotal: Double
) {
    val displayPrice: Double
        get() = currentPrice ?: offerPrice ?: price
}

// ==================== CART RESPONSE ====================

data class CartResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("cart_count")
    val cartCount: Int? = null,
    @SerializedName("items")
    val items: List<CartItem>? = null
)

// ==================== CHECKOUT RESPONSE ====================

data class CheckoutItem(
    @SerializedName("cart_item_id")
    val cartItemId: Int? = null,
    @SerializedName("product_id")
    val productId: Int,
    @SerializedName("product_name")
    val productName: String,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("unit_price")
    val unitPrice: Double,
    @SerializedName("subtotal")
    val subtotal: Double,
    @SerializedName("company_name")
    val companyName: String? = null,
    @SerializedName("company_id")
    val companyId: Int? = null
)

data class CheckoutQRCode(
    @SerializedName("qr_id")
    val qrId: Int,
    @SerializedName("transaction_id")
    val transactionId: String,
    @SerializedName("qr_code")
    val qrCode: String,
    @SerializedName("qr_secret")
    val qrSecret: String,
    @SerializedName("qr_payload")
    val qrPayload: String,
    @SerializedName("qr_data_url")
    val qrDataUrl: String,
    @SerializedName("company_id")
    val companyId: Int,
    @SerializedName("company_name")
    val companyName: String,
    @SerializedName("items")
    val items: List<CheckoutItem>? = null,
    @SerializedName("item_count")
    val itemCount: Int,
    @SerializedName("total")
    val total: Double,
    @SerializedName("expires_at")
    val expiresAt: String
)

data class CheckoutResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("reference_code")
    val referenceCode: String? = null,
    @SerializedName("qr_codes")
    val qrCodes: List<CheckoutQRCode>? = null,
    @SerializedName("total_items")
    val totalItems: Int? = null,
    @SerializedName("total_amount")
    val totalAmount: Double? = null,
    @SerializedName("company_count")
    val companyCount: Int? = null,
    @SerializedName("payment_url")
    val paymentUrl: String? = null,
    @SerializedName("items")
    val items: List<CheckoutItem>? = null,
    @SerializedName("total")
    val total: Double? = null,
    @SerializedName("payment_method")
    val paymentMethod: String? = null,
    @SerializedName("instructions")
    val instructions: String? = null,
    @SerializedName("message")
    val message: String? = null
)

// ==================== CART COUNT ====================

data class CartCountResponse(
    @SerializedName("cart_count")
    val cartCount: Int
)

// ==================== PRODUCT REQUEST ====================

data class CreateProductRequest(
    @SerializedName("product_name")
    val productName: String,
    @SerializedName("product_description")
    val productDescription: String? = null,
    @SerializedName("product_category_id")
    val productCategoryId: Int? = null,
    @SerializedName("size")
    val size: String? = null,
    @SerializedName("weight")
    val weight: String? = null,
    @SerializedName("dimensions")
    val dimensions: String? = null,
    @SerializedName("keywords")
    val keywords: String? = null,
    @SerializedName("quantity")
    val quantity: Int = 0,
    @SerializedName("price")
    val price: Double,
    @SerializedName("offer_price")
    val offerPrice: Double? = null,
    @SerializedName("specific_commission_percentage")
    val specificCommissionPercentage: Double? = null,
    @SerializedName("use_company_default")
    val useCompanyDefault: Boolean = true,
    @SerializedName("status")
    val status: String = "active",
    @SerializedName("manage_stock")
    val manageStock: Boolean = true
)

data class UpdateProductRequest(
    @SerializedName("product_name")
    val productName: String? = null,
    @SerializedName("product_description")
    val productDescription: String? = null,
    @SerializedName("product_category_id")
    val productCategoryId: Int? = null,
    @SerializedName("size")
    val size: String? = null,
    @SerializedName("weight")
    val weight: String? = null,
    @SerializedName("dimensions")
    val dimensions: String? = null,
    @SerializedName("quantity")
    val quantity: Int? = null,
    @SerializedName("price")
    val price: Double? = null,
    @SerializedName("offer_price")
    val offerPrice: Double? = null,
    @SerializedName("specific_commission_percentage")
    val specificCommissionPercentage: Double? = null,
    @SerializedName("use_company_default")
    val useCompanyDefault: Boolean? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("is_active")
    val isActive: Boolean? = null,
    @SerializedName("keywords")
    val keywords: String? = null,
    @SerializedName("manage_stock")
    val manageStock: Boolean? = null
)

// ==================== IMAGE UPLOAD RESPONSE ====================

data class ImageUploadResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("image_id")
    val imageId: Int? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("is_primary")
    val isPrimary: Boolean? = null
)

// ==================== GENERIC RESPONSE ====================

data class ProductResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("product_id")
    val productId: Int? = null
)

// ==================== PRODUCT SEARCH ====================

data class ProductSearchResult(
    @SerializedName("product_id") val productId: Int,
    @SerializedName("product_name") val productName: String,
    @SerializedName("product_description") val productDescription: String? = null,
    @SerializedName("price") val price: Double,
    @SerializedName("offer_price") val offerPrice: Double? = null,
    @SerializedName("company_id") val companyId: Int,
    @SerializedName("company_name") val companyName: String,
    @SerializedName("company_logo_url") val companyLogoUrl: String? = null,
    @SerializedName("primary_image_url") val primaryImageUrl: String? = null,
    @SerializedName("category_id") val categoryId: Int? = null
)

data class ProductSearchResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("products") val products: List<ProductSearchResult>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("per_page") val perPage: Int
)

// ==================== SEARCH ALIASES ====================

data class SearchAlias(
    @SerializedName("id") val id: Int,
    @SerializedName("alias") val alias: String,
    @SerializedName("term") val term: String,
    @SerializedName("created_at") val createdAt: String? = null
)

data class CreateAliasRequest(
    @SerializedName("alias") val alias: String,
    @SerializedName("term") val term: String
)

data class AliasListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("aliases") val aliases: List<SearchAlias>
)

data class UpdateKeywordsRequest(
    @SerializedName("keywords") val keywords: String?
)

// ==================== PRODUCT FEED ====================

data class FeedProduct(
    @SerializedName("product_id") val productId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double,
    @SerializedName("offer_price") val offerPrice: Double? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("company_id") val companyId: Int,
    @SerializedName("company_name") val companyName: String
)

data class FeedResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("products") val products: List<FeedProduct>,
    @SerializedName("total") val total: Int,
    @SerializedName("has_more") val hasMore: Boolean,
    @SerializedName("page") val page: Int,
    @SerializedName("per_page") val perPage: Int
)
