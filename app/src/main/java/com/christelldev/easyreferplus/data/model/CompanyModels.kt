package com.christelldev.easyreferplus.data.model

import com.google.gson.annotations.SerializedName

// Solicitud de registro de empresa
data class RegisterCompanyRequest(
    @SerializedName("company_name")
    val companyName: String,
    @SerializedName("company_description")
    val companyDescription: String,
    @SerializedName("product_description")
    val productDescription: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("city")
    val city: String,
    @SerializedName("province")
    val province: String,
    @SerializedName("latitude")
    val latitude: Double? = null,
    @SerializedName("longitude")
    val longitude: Double? = null,
    @SerializedName("website")
    val website: String? = null,
    @SerializedName("facebook_url")
    val facebookUrl: String? = null,
    @SerializedName("instagram_url")
    val instagramUrl: String? = null,
    @SerializedName("whatsapp_number")
    val whatsappNumber: String? = null,
    @SerializedName("commission_percentage")
    val commissionPercentage: Double,
    @SerializedName("initial_products")
    val initialProducts: List<CompanyProduct>? = null,
    @SerializedName("category_id")
    val categoryId: Int? = null,
    @SerializedName("service_id")
    val serviceId: Int? = null
)

// Solicitud de actualización de empresa
data class UpdateCompanyRequest(
    @SerializedName("company_name")
    val companyName: String,
    @SerializedName("company_description")
    val companyDescription: String,
    @SerializedName("product_description")
    val productDescription: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("city")
    val city: String,
    @SerializedName("province")
    val province: String,
    @SerializedName("latitude")
    val latitude: Double? = null,
    @SerializedName("longitude")
    val longitude: Double? = null,
    @SerializedName("website")
    val website: String? = null,
    @SerializedName("facebook_url")
    val facebookUrl: String? = null,
    @SerializedName("instagram_url")
    val instagramUrl: String? = null,
    @SerializedName("whatsapp_number")
    val whatsappNumber: String? = null,
    @SerializedName("commission_percentage")
    val commissionPercentage: Double,
    @SerializedName("initial_products")
    val initialProducts: List<CompanyProduct>? = null,
    @SerializedName("category_id")
    val categoryId: Int? = null,
    @SerializedName("service_id")
    val serviceId: Int? = null
)

// Respuesta de actualización de empresa
data class UpdateCompanyResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("company")
    val company: UserCompanyResponse? = null
)

// Producto de empresa (para registro y respuesta del API)
data class CompanyProduct(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("company_id")
    val companyId: Int? = null,
    @SerializedName("product_name")
    val productName: String,
    @SerializedName("product_description")
    val productDescription: String? = null,
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("price")
    val price: Double? = null,
    @SerializedName("specific_commission_percentage")
    val specificCommissionPercentage: Double? = null,
    @SerializedName("use_company_default")
    val useCompanyDefault: Boolean = true,
    @SerializedName("display_order")
    val displayOrder: Int? = null,
    @SerializedName("is_active")
    val isActive: Boolean = true,
    @SerializedName("effective_commission_percentage")
    val effectiveCommissionPercentage: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    // Campos adicionales del API
    @SerializedName("images")
    val images: List<ProductImage>? = null,
    @SerializedName("current_price")
    val currentPrice: Double? = null,
    @SerializedName("offer_price")
    val offerPrice: Double? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("quantity")
    val quantity: Int? = null,
    @SerializedName("size")
    val size: String? = null,
    @SerializedName("weight")
    val weight: String? = null,
    @SerializedName("dimensions")
    val dimensions: String? = null,
    @SerializedName("product_category_id")
    val productCategoryId: Int? = null
)

data class RegisterCompanyResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("company_id")
    val companyId: Int?,
    @SerializedName("company_name")
    val companyName: String?
)

// Modelo para errores de validación
data class CompanyValidationError(
    @SerializedName("field")
    val field: String,
    @SerializedName("message")
    val message: String
)

data class RegisterCompanyErrorResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("error_type")
    val errorType: String?,
    @SerializedName("errors")
    val errors: List<CompanyValidationError>?
)

// Respuestas de ciudades y provincias
data class CityResponse(
    @SerializedName("success")
    val success: Boolean? = null,
    @SerializedName("cities")
    val cities: List<String>? = null
)

// ==================== COMPANY PAYMENTS ====================

// Solicitud para registrar un pago
data class CompanyPaymentRequest(
    @SerializedName("document_number")
    val documentNumber: String,
    @SerializedName("bank_name")
    val bankName: String,
    val amount: Double,
    val notes: String? = null
)

// Modelo de un pago registrado
data class CompanyPayment(
    val id: Int,
    @SerializedName("document_number")
    val documentNumber: String,
    @SerializedName("bank_name")
    val bankName: String,
    val amount: Double,
    val status: String, // "pending", "verified", "rejected"
    val notes: String?,
    @SerializedName("transfer_photo_url")
    val transferPhotoUrl: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("verified_at")
    val verifiedAt: String?,
    @SerializedName("verification_notes")
    val verificationNotes: String?
)

// Response para historial de pagos
data class PaymentHistoryResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("company")
    val company: CompanyInfo?,
    @SerializedName("pending_commissions_amount")
    val pendingCommissionsAmount: Double?,
    @SerializedName("payments")
    val payments: List<CompanyPayment>?
)

// Info de empresa en historial de pagos
data class CompanyInfo(
    @SerializedName("id")
    val id: Int,
    @SerializedName("company_name")
    val companyName: String,
    @SerializedName("payment_status")
    val paymentStatus: String?
)

// Response para registrar pago
data class RegisterPaymentResponse(
    @SerializedName("success")
    val success: Boolean,
    val message: String,
    @SerializedName("payment_id")
    val paymentId: Int?
)

// Response para verificar acceso a pagos
data class PaymentAccessResponse(
    @SerializedName("can_access")
    val canAccess: Boolean,
    @SerializedName("company_id")
    val companyId: Int?,
    @SerializedName("company_name")
    val companyName: String?,
    @SerializedName("company_status")
    val companyStatus: String?,
    @SerializedName("pending_amount")
    val pendingAmount: Double?,
    @SerializedName("payments_count")
    val paymentsCount: Int?,
    val message: String?
)

// ==================== LOGO UPLOAD ====================

data class LogoUploadResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("logo_info")
    val logoInfo: LogoInfo? = null,
    @SerializedName("logo_url")
    val logoUrl: String? = null,
    @SerializedName("logo_filename")
    val logoFilename: String? = null
)

data class LogoInfo(
    @SerializedName("url")
    val url: String? = null,
    @SerializedName("filename")
    val filename: String? = null,
    @SerializedName("size")
    val size: Int? = null,
    @SerializedName("mime_type")
    val mimeType: String? = null
)

// Response para upload de selfie
data class SelfieUploadResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("selfie_info")
    val selfieInfo: SelfieInfo? = null
)

data class SelfieInfo(
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("url")
    val url: String? = null,
    @SerializedName("filename")
    val filename: String? = null,
    @SerializedName("size")
    val size: Int? = null,
    @SerializedName("mime_type")
    val mimeType: String? = null,
    @SerializedName("uploaded_at")
    val uploadedAt: String? = null
)

// Response para delete de selfie
data class SelfieDeleteResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String
)

// ─── Tienda Online ────────────────────────────────────────────────────────────

data class MyStoreResponse(
    @SerializedName("has_company") val hasCompany: Boolean = false,
    @SerializedName("store_enabled") val storeEnabled: Boolean = false,
    @SerializedName("store_slug") val storeSlug: String? = null,
    @SerializedName("store_template_id") val storeTemplateId: Int = 1,
    @SerializedName("store_primary_color") val storePrimaryColor: String = "#6366f1",
    @SerializedName("store_secondary_color") val storeSecondaryColor: String = "#f59e0b",
    @SerializedName("store_tagline") val storeTagline: String? = null,
    @SerializedName("store_font") val storeFont: String = "inter",
    @SerializedName("store_banner_url") val storeBannerUrl: String? = null,
    @SerializedName("store_url") val storeUrl: String? = null,
    @SerializedName("company_name") val companyName: String = "",
    @SerializedName("is_validated") val isValidated: Boolean = false,
    @SerializedName("products_count") val productsCount: Int = 0
)

data class StoreSetupResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("store_enabled") val storeEnabled: Boolean = false,
    @SerializedName("store_url") val storeUrl: String? = null,
    @SerializedName("message") val message: String = ""
)

data class StoreToggleResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("store_enabled") val storeEnabled: Boolean = false,
    @SerializedName("message") val message: String = ""
)

data class SlugCheckResponse(
    @SerializedName("available") val available: Boolean,
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("reason") val reason: String? = null
)
