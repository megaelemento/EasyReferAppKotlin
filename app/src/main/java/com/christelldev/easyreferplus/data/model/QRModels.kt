package com.christelldev.easyreferplus.data.model

import com.google.gson.annotations.SerializedName
import com.christelldev.easyreferplus.data.model.CompanyProduct

// ==================== REQUEST MODELS ====================

// Solicitud para generar código QR
data class GenerateQRRequest(
    @SerializedName("company_id")
    val companyId: Int,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("currency")
    val currency: String = "USD",
    @SerializedName("description")
    val description: String,
    @SerializedName("referral_code")
    val referralCode: String? = null
)

// Solicitud para escanear código QR
data class ScanQRRequest(
    @SerializedName("qr_code")
    val qrCode: String,
    @SerializedName("qr_secret")
    val qrSecret: String
)

// Solicitud para procesar transacción QR
data class ProcessQRRequest(
    @SerializedName("qr_code")
    val qrCode: String,
    @SerializedName("qr_secret")
    val qrSecret: String,
    @SerializedName("buyer_document")
    val buyerDocument: String,
    @SerializedName("buyer_name")
    val buyerName: String,
    @SerializedName("buyer_phone")
    val buyerPhone: String,
    @SerializedName("buyer_email")
    val buyerEmail: String? = null,
    @SerializedName("external_transaction_id")
    val externalTransactionId: String? = null,
    @SerializedName("payment_method")
    val paymentMethod: String = "cash"
)

// ==================== RESPONSE MODELS ====================

// Datos de la empresa en respuestas QR
data class QRCompany(
    @SerializedName("id")
    val id: Int,
    @SerializedName("company_name")
    val companyName: String,
    @SerializedName("company_description")
    val companyDescription: String? = null,
    @SerializedName("logo_url")
    val logoUrl: String? = null,
    @SerializedName("address")
    val address: String? = null,
    @SerializedName("whatsapp_number")
    val whatsappNumber: String? = null
)

// Datos de la transacción QR
data class QRTransaction(
    @SerializedName("id")
    val id: Int,
    @SerializedName("qr_code")
    val qrCode: String,
    @SerializedName("qr_secret")
    val qrSecret: String? = null,
    @SerializedName("company_id")
    val companyId: Int,
    @SerializedName("company")
    val company: QRCompany? = null,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("currency")
    val currency: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("referral_code_used")
    val referralCodeUsed: String? = null,
    @SerializedName("referral_code")
    val referralCode: String? = null,
    @SerializedName("is_used")
    val isUsed: Boolean = false,
    @SerializedName("is_expired")
    val isExpired: Boolean = false,
    @SerializedName("is_valid")
    val isValid: Boolean = true,
    @SerializedName("expires_at")
    val expiresAt: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("scan_timestamp")
    val scanTimestamp: String? = null,
    @SerializedName("was_scanned")
    val wasScanned: Boolean = false,
    @SerializedName("transaction_completed")
    val transactionCompleted: Boolean = false,
    @SerializedName("transaction_id")
    val transactionId: Int? = null
)

// Distribución de comisiones
data class ReferralDistribution(
    @SerializedName("level_1_amount")
    val level1Amount: Double? = null,
    @SerializedName("level_2_amount")
    val level2Amount: Double? = null,
    @SerializedName("level_3_amount")
    val level3Amount: Double? = null,
    @SerializedName("platform_amount")
    val platformAmount: Double? = null
)

// Transacción procesada
data class ProcessedTransaction(
    @SerializedName("id")
    val id: Int,
    @SerializedName("external_transaction_id")
    val externalTransactionId: String? = null,
    @SerializedName("company_id")
    val companyId: Int,
    @SerializedName("buyer_document")
    val buyerDocument: String,
    @SerializedName("buyer_name")
    val buyerName: String,
    @SerializedName("buyer_phone")
    val buyerPhone: String,
    @SerializedName("buyer_email")
    val buyerEmail: String? = null,
    @SerializedName("total_amount")
    val totalAmount: Double,
    @SerializedName("total_commission_amount")
    val totalCommissionAmount: Double,
    @SerializedName("referral_code_used")
    val referralCodeUsed: String? = null,
    @SerializedName("referral_user_id")
    val referralUserId: Int? = null,
    @SerializedName("transaction_status")
    val transactionStatus: String,
    @SerializedName("commission_calculation_status")
    val commissionCalculationStatus: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("purchase_date")
    val purchaseDate: String? = null
)

// Paginación
data class QRPagination(
    @SerializedName("page")
    val page: Int,
    @SerializedName("per_page")
    val perPage: Int,
    @SerializedName("total")
    val total: Int,
    @SerializedName("pages")
    val pages: Int
)

// Respuesta de generación de QR
data class GenerateQRResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("qr_code")
    val qrCode: String? = null,
    @SerializedName("qr_payload")
    val qrPayload: String? = null,
    @SerializedName("qr_data")
    val qrData: String? = null,
    @SerializedName("amount")
    val amount: Double? = null,
    @SerializedName("company_name")
    val companyName: String? = null,
    @SerializedName("expires_at")
    val expiresAt: String? = null,
    // Campos legacy (por compatibilidad)
    @SerializedName("qr_transaction")
    val qrTransaction: QRTransaction? = null,
    @SerializedName("qr_image_url")
    val qrImageUrl: String? = null
)

// Respuesta de escaneo de QR (nuevo formato)
data class ScanQRResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("qr_code")
    val qrCode: String?,
    @SerializedName("amount")
    val amount: Double?,
    @SerializedName("currency")
    val currency: String?,
    @SerializedName("company_id")
    val companyId: Int?,
    @SerializedName("company_name")
    val companyName: String?,
    @SerializedName("company_description")
    val companyDescription: String?,
    @SerializedName("seller_user_id")
    val sellerUserId: Int?,
    @SerializedName("seller_name")
    val sellerName: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("expires_at")
    val expiresAt: String?,
    @SerializedName("message")
    val message: String,
    // Legacy field for backward compatibility
    @SerializedName("qr_transaction")
    val qrTransaction: QRTransaction? = null
)

// Respuesta de procesamiento de QR
data class ProcessQRResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("transaction")
    val transaction: ProcessedTransaction? = null,
    @SerializedName("qr_transaction")
    val qrTransaction: QRTransaction? = null,
    @SerializedName("referral_distribution")
    val referralDistribution: ReferralDistribution? = null
)

// Respuesta de estado de QR
data class QRStatusResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("qr_transaction")
    val qrTransaction: QRTransaction? = null
)

// Respuesta de lista de QR generados
data class QRListResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("qr_transactions")
    val qrTransactions: List<QRTransaction>? = null,
    @SerializedName("pagination")
    val pagination: QRPagination? = null
)

// Modelo de error de validación
data class QRValidationError(
    @SerializedName("field")
    val field: String,
    @SerializedName("message")
    val message: String
)

// Respuesta de error
data class QRErrorResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("error_type")
    val errorType: String? = null,
    @SerializedName("errors")
    val errors: List<QRValidationError>? = null
)

// ==================== USER COMPANIES ====================

// Empresa del usuario (respuesta completa)
data class UserCompanyResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("company_name")
    val companyName: String,
    @SerializedName("company_description")
    val companyDescription: String? = null,
    @SerializedName("product_description")
    val productDescription: String? = null,
    @SerializedName("address")
    val address: String? = null,
    @SerializedName("city")
    val city: String? = null,
    @SerializedName("province")
    val province: String? = null,
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
    val commissionPercentage: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("is_active")
    val isActive: Boolean = false,
    @SerializedName("validation_notes")
    val validationNotes: String? = null,
    @SerializedName("rejection_reason")
    val rejectionReason: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("validated_at")
    val validatedAt: String? = null,
    @SerializedName("logo_url")
    val logoUrl: String? = null,
    @SerializedName("logo_filename")
    val logoFilename: String? = null,
    @SerializedName("has_logo")
    val hasLogo: Boolean = false,
    @SerializedName("owner_id")
    val ownerId: Int? = null,
    @SerializedName("owner_name")
    val ownerName: String? = null,
    @SerializedName("is_validated")
    val isValidated: Boolean = false,
    @SerializedName("total_products")
    val totalProducts: Int = 0,
    @SerializedName("products")
    val products: List<CompanyProduct>? = null,
    @SerializedName("category_id")
    val categoryId: Int? = null,
    @SerializedName("category_name")
    val categoryName: String? = null,
    @SerializedName("service_id")
    val serviceId: Int? = null,
    @SerializedName("service_name")
    val serviceName: String? = null
)

// Respuesta de búsqueda de empresas
data class CompaniesSearchResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("companies")
    val companies: List<UserCompanyResponse>? = null,
    @SerializedName("total")
    val total: Int = 0,
    @SerializedName("page")
    val page: Int = 1,
    @SerializedName("per_page")
    val perPage: Int = 20,
    @SerializedName("total_pages")
    val totalPages: Int = 1
)

// Respuesta de empresas del usuario
data class UserCompaniesResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("companies")
    val companies: List<UserCompanyResponse>? = null
)

// Respuesta de mi empresa (single company)
data class MyCompanyResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("company")
    val company: UserCompanyResponse? = null
)

// ==================== UPDATED RESPONSE MODELS FOR NEW API ====================

// Nueva respuesta de escaneo (validación)
data class QRScanInfo(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("qr_code")
    val qrCode: String,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("currency")
    val currency: String,
    @SerializedName("company_id")
    val companyId: Int,
    @SerializedName("company_name")
    val companyName: String,
    @SerializedName("company_description")
    val companyDescription: String?,
    @SerializedName("seller_user_id")
    val sellerUserId: Int,
    @SerializedName("seller_name")
    val sellerName: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("expires_at")
    val expiresAt: String?,
    @SerializedName("message")
    val message: String
)

// Receipt de transacción
data class TransactionReceipt(
    @SerializedName("transaction_id")
    val transactionId: String,
    @SerializedName("type")
    val type: String, // "sale" o "purchase"
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("currency")
    val currency: String,
    @SerializedName("company_id")
    val companyId: Int?,
    @SerializedName("company_name")
    val companyName: String?,
    @SerializedName("company_address")
    val companyAddress: String?,
    @SerializedName("company_phone")
    val companyPhone: String?,
    @SerializedName("seller_id")
    val sellerId: Int?,
    @SerializedName("seller_name")
    val sellerName: String?,
    @SerializedName("seller_phone")
    val sellerPhone: String?,
    @SerializedName("buyer_id")
    val buyerId: Int?,
    @SerializedName("buyer_name")
    val buyerName: String?,
    @SerializedName("buyer_document")
    val buyerDocument: String?,
    @SerializedName("buyer_phone")
    val buyerPhone: String?,
    @SerializedName("buyer_email")
    val buyerEmail: String?,
    @SerializedName("payment_method")
    val paymentMethod: String?,
    @SerializedName("referral_code_used")
    val referralCodeUsed: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("transaction_date")
    val transactionDate: String?,
    @SerializedName("generated_at")
    val generatedAt: String?,
    @SerializedName("commission_info")
    val commissionInfo: CommissionInfo?
)

// Nueva respuesta de procesamiento (receipt)
data class ProcessReceiptResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("transaction_id")
    val transactionId: String,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("currency")
    val currency: String,
    @SerializedName("transaction_type")
    val transactionType: String,
    @SerializedName("seller_id")
    val sellerId: Int,
    @SerializedName("seller_name")
    val sellerName: String,
    @SerializedName("seller_phone")
    val sellerPhone: String?,
    @SerializedName("company_id")
    val companyId: Int,
    @SerializedName("company_name")
    val companyName: String,
    @SerializedName("buyer_id")
    val buyerId: Int?,
    @SerializedName("buyer_document")
    val buyerDocument: String,
    @SerializedName("buyer_name")
    val buyerName: String,
    @SerializedName("buyer_phone")
    val buyerPhone: String,
    @SerializedName("buyer_email")
    val buyerEmail: String?,
    @SerializedName("payment_method")
    val paymentMethod: String,
    @SerializedName("referral_code_used")
    val referralCodeUsed: String?,
    @SerializedName("transaction_date")
    val transactionDate: String?,
    @SerializedName("expires_at")
    val expiresAt: String?,
    @SerializedName("qr_code")
    val qrCode: String?,
    @SerializedName("message")
    val message: String
) {
    // Genera ID display: ultimos 4 digitos del transaction_id o qr_code
    fun getDisplayId(): String {
        // Usar los últimos 4 caracteres del transaction_id para identificar
        val last4 = transactionId.takeLast(4).uppercase()
        return last4
    }
}

// Historial de transacción
data class TransactionHistoryItem(
    @SerializedName("transaction_id")
    val transactionId: String,
    @SerializedName("type")
    val type: String, // "sale" o "purchase"
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("currency")
    val currency: String,
    @SerializedName("company_id")
    val companyId: Int?,
    @SerializedName("company_name")
    val companyName: String?,
    @SerializedName("seller_user_id")
    val sellerUserId: Int?,
    @SerializedName("seller_name")
    val sellerName: String?,
    @SerializedName("buyer_user_id")
    val buyerUserId: Int?,
    @SerializedName("buyer_name")
    val buyerName: String?,
    @SerializedName("buyer_document")
    val buyerDocument: String?,
    @SerializedName("payment_method")
    val paymentMethod: String?,
    @SerializedName("referral_code_used")
    val referralCodeUsed: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("transaction_date")
    val transactionDate: String?,
    @SerializedName("created_at")
    val createdAt: String?
)

// Paginación para historial
data class HistoryPagination(
    @SerializedName("page")
    val page: Int,
    @SerializedName("per_page")
    val perPage: Int,
    @SerializedName("total")
    val total: Int,
    @SerializedName("total_pages")
    val totalPages: Int
)

// Stats de historial
data class HistoryStats(
    @SerializedName("total_sales")
    val totalSales: Int,
    @SerializedName("total_purchases")
    val totalPurchases: Int
)

// Respuesta de historial
data class TransactionHistoryResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: List<TransactionHistoryItem>?,
    @SerializedName("pagination")
    val pagination: HistoryPagination?,
    @SerializedName("stats")
    val stats: HistoryStats?
)

// Respuesta de receipt individual
data class ReceiptResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("receipt")
    val receipt: TransactionReceipt?
)

// Información de comisión en el receipt
data class CommissionInfo(
    @SerializedName("total_commission_amount")
    val totalCommissionAmount: Double?,
    @SerializedName("company_commission_percentage")
    val companyCommissionPercentage: Double?,
    @SerializedName("commission_details")
    val commissionDetails: List<CommissionDetail>?
)

data class CommissionDetail(
    @SerializedName("level")
    val level: Int?,
    @SerializedName("user_id")
    val userId: Int?,
    @SerializedName("user_name")
    val userName: String?,
    @SerializedName("user_phone")
    val userPhone: String?,
    @SerializedName("user_referral_code")
    val userReferralCode: String?,
    @SerializedName("commission_type")
    val commissionType: String?,
    @SerializedName("percentage_of_distribution")
    val percentageOfDistribution: Double?,
    @SerializedName("percentage_of_sale")
    val percentageOfSale: Double?,
    @SerializedName("gross_commission")
    val grossCommission: Double?,
    @SerializedName("net_commission")
    val netCommission: Double?,
    @SerializedName("payment_status")
    val paymentStatus: String?,
    @SerializedName("note")
    val note: String?
)
