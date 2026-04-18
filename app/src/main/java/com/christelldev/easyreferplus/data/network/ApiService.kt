package com.christelldev.easyreferplus.data.network

import com.christelldev.easyreferplus.data.model.LoginRequest
import com.christelldev.easyreferplus.data.model.LogoutRequest
import com.christelldev.easyreferplus.data.model.RefreshTokenRequest
import com.christelldev.easyreferplus.data.model.RegisterCompanyRequest
import com.christelldev.easyreferplus.data.model.RegisterCompanyResponse
import com.christelldev.easyreferplus.data.model.UpdateCompanyRequest
import com.christelldev.easyreferplus.data.model.UpdateCompanyResponse
import com.christelldev.easyreferplus.data.model.LoginResponse
import com.christelldev.easyreferplus.data.model.RefreshTokenResponse
import com.christelldev.easyreferplus.data.model.LogoutResponse
import com.christelldev.easyreferplus.data.model.ReferralTreeResponse
import com.christelldev.easyreferplus.data.model.SearchReferralResponse
import com.christelldev.easyreferplus.data.model.GenerateQRRequest
import com.christelldev.easyreferplus.data.model.GenerateQRResponse
import com.christelldev.easyreferplus.data.model.ScanQRRequest
import com.christelldev.easyreferplus.data.model.ScanQRResponse
import com.christelldev.easyreferplus.data.model.ProcessQRRequest
import com.christelldev.easyreferplus.data.model.ProcessQRResponse
import com.christelldev.easyreferplus.data.model.ProcessReceiptResponse
import com.christelldev.easyreferplus.data.model.UserProfile
import com.christelldev.easyreferplus.data.model.UpdateProfileRequest
import com.christelldev.easyreferplus.data.model.UpdateProfileResponse
import com.christelldev.easyreferplus.data.model.ProfileResponse
import com.christelldev.easyreferplus.data.model.QRStatusResponse
import com.christelldev.easyreferplus.data.model.QRListResponse
import com.christelldev.easyreferplus.data.model.TransactionHistoryResponse
import com.christelldev.easyreferplus.data.model.ReceiptResponse
import com.christelldev.easyreferplus.data.model.UserCompaniesResponse
import com.christelldev.easyreferplus.data.model.UserCompanyResponse
import com.christelldev.easyreferplus.data.model.CompaniesSearchResponse
import com.christelldev.easyreferplus.data.model.BalanceResponse
import com.christelldev.easyreferplus.data.model.SessionsResponse
import com.christelldev.easyreferplus.data.model.ValidateSessionResponse
import com.christelldev.easyreferplus.data.model.InvalidateSessionRequest
import com.christelldev.easyreferplus.data.model.InvalidateSessionResponse
import com.christelldev.easyreferplus.data.model.LogoutAllResponse
import com.christelldev.easyreferplus.data.model.AdminEarningsResponse
import com.christelldev.easyreferplus.data.model.AdminUserEarningsResponse
import com.christelldev.easyreferplus.data.model.CompanyPaymentRequest
import com.christelldev.easyreferplus.data.model.PaymentHistoryResponse
import com.christelldev.easyreferplus.data.model.PaymentAccessResponse
import com.christelldev.easyreferplus.data.model.RegisterPaymentResponse
import com.christelldev.easyreferplus.data.model.EarningsSummaryResponse
import com.christelldev.easyreferplus.data.model.AdminEarningsSummaryResponse
import com.christelldev.easyreferplus.data.model.AdminUserEarningsSummaryResponse
import com.christelldev.easyreferplus.data.model.CommissionResponse
import com.christelldev.easyreferplus.data.model.CommissionBalanceResponse
import com.christelldev.easyreferplus.data.model.BankAccountListResponse
import com.christelldev.easyreferplus.data.model.CreateBankAccountRequest
import com.christelldev.easyreferplus.data.model.WithdrawalSuccessResponse
import com.christelldev.easyreferplus.data.model.CreateWithdrawalRequest
import com.christelldev.easyreferplus.data.model.WithdrawalRequestListResponse
import com.christelldev.easyreferplus.data.model.AdminWithdrawalsListResponse
import com.christelldev.easyreferplus.data.model.AdminWithdrawalResponse
import com.christelldev.easyreferplus.data.model.AdminWithdrawalActionRequest
import com.christelldev.easyreferplus.data.model.AdminWithdrawalActionResponse
import com.christelldev.easyreferplus.data.model.LogoUploadResponse
import com.christelldev.easyreferplus.data.model.SelfieUploadResponse
import com.christelldev.easyreferplus.data.model.SelfieDeleteResponse
import com.christelldev.easyreferplus.data.model.CategoryInfo
import com.christelldev.easyreferplus.data.model.ServiceInfo
import com.christelldev.easyreferplus.data.model.ProductCategory
import com.christelldev.easyreferplus.data.model.Product
import com.christelldev.easyreferplus.data.model.ProductImage
import com.christelldev.easyreferplus.data.model.CartItem
import com.christelldev.easyreferplus.data.model.CartResponse
import com.christelldev.easyreferplus.data.model.CheckoutResponse
import com.christelldev.easyreferplus.data.model.CartCountResponse
import com.christelldev.easyreferplus.data.model.CreateProductRequest
import com.christelldev.easyreferplus.data.model.UpdateProductRequest
import com.christelldev.easyreferplus.data.model.ImageUploadResponse
import com.christelldev.easyreferplus.data.model.ProductResponse
import com.christelldev.easyreferplus.data.model.ProductSearchResponse
import com.christelldev.easyreferplus.data.model.WalletBalanceResponse
import com.christelldev.easyreferplus.data.model.WalletApiResponse
import com.christelldev.easyreferplus.data.model.SetPinRequest
import com.christelldev.easyreferplus.data.model.ChangePinRequest
import com.christelldev.easyreferplus.data.model.TransferRequest
import com.christelldev.easyreferplus.data.model.TransferResponse
import com.christelldev.easyreferplus.data.model.WalletStatementItem
import com.christelldev.easyreferplus.data.model.WalletStatementResponse
import com.christelldev.easyreferplus.data.model.CheckRecipientResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @PUT("api/auth/fcm-token")
    suspend fun updateFcmToken(
        @Header("Authorization") authorization: String,
        @Body body: Map<String, String>,
    ): Response<Unit>

    @DELETE("api/auth/fcm-token")
    suspend fun clearFcmToken(
        @Header("Authorization") authorization: String,
    ): Response<Unit>

    @POST("api/auth/logout")
    suspend fun logout(
        @Header("Authorization") authorization: String,
        @Header("X-Refresh-Token") refreshTokenHeader: String? = null,
        @Body request: LogoutRequest? = null
    ): Response<LogoutResponse>

    @GET("api/auth/my-referrals")
    suspend fun getMyReferrals(
        @Header("Authorization") authorization: String
    ): Response<ReferralTreeResponse>

    @GET("api/auth/search-referral")
    suspend fun searchReferral(
        @Header("Authorization") authorization: String,
        @Query("code") code: String
    ): Response<SearchReferralResponse>

    @POST("api/companies/register")
    suspend fun registerCompany(
        @Header("Authorization") authorization: String,
        @Body request: RegisterCompanyRequest
    ): Response<RegisterCompanyResponse>

    @PUT("api/companies/my-company/update")
    suspend fun updateCompany(
        @Header("Authorization") authorization: String,
        @Body request: UpdateCompanyRequest
    ): Response<UpdateCompanyResponse>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>

    @GET("api/auth/validate-session")
    suspend fun validateSession(
        @Header("Authorization") authorization: String,
        @Header("X-Refresh-Token") refreshToken: String? = null
    ): Response<ValidateSessionResponse>

    // Endpoints de perfil de usuario
    @GET("api/auth/profile")
    suspend fun getProfile(
        @Header("Authorization") authorization: String
    ): Response<ProfileResponse>

    @PUT("api/auth/profile")
    suspend fun updateProfile(
        @Header("Authorization") authorization: String,
        @Body request: UpdateProfileRequest
    ): Response<UpdateProfileResponse>

    // Endpoints de selfie (foto de perfil)
    @Multipart
    @POST("api/auth/profile/upload-selfie")
    suspend fun uploadSelfie(
        @Header("Authorization") authorization: String,
        @Part selfie: okhttp3.MultipartBody.Part
    ): Response<SelfieUploadResponse>

    @DELETE("api/auth/profile/delete-selfie")
    suspend fun deleteSelfie(
        @Header("Authorization") authorization: String
    ): Response<SelfieDeleteResponse>

    // Endpoints de ciudades y provincias
    @GET("api/cities/ecuador")
    suspend fun getAllCities(@Query("province") province: String? = null): Response<List<String>>

    @GET("api/cities/ecuador/provinces")
    suspend fun getProvinces(): Response<List<String>>

    @GET("api/cities/ecuador/province-cities/{province}")
    suspend fun getCitiesByProvince(@Path("province") province: String): Response<List<String>>

    // ==================== CATEGORÍAS Y SERVICIOS ====================
    @GET("api/categories-services/categories")
    suspend fun getCategories(): Response<List<CategoryInfo>>

    @GET("api/categories-services/services")
    suspend fun getServices(@Query("category_id") categoryId: Int? = null): Response<List<ServiceInfo>>

    // ==================== QR TRANSACTIONS ====================

    @POST("api/qr-transactions/generate")
    suspend fun generateQR(
        @Header("Authorization") authorization: String,
        @Body request: GenerateQRRequest
    ): Response<GenerateQRResponse>

    @POST("api/qr-transactions/scan")
    suspend fun scanQR(
        @Header("Authorization") authorization: String,
        @Body request: ScanQRRequest
    ): Response<ProcessReceiptResponse>

    @POST("api/qr-transactions/process")
    suspend fun processQR(
        @Header("Authorization") authorization: String,
        @Body request: ProcessQRRequest
    ): Response<ProcessReceiptResponse>

    @GET("api/qr-transactions/status/{qr_code}")
    suspend fun getQRStatus(
        @Header("Authorization") authorization: String,
        @Path("qr_code") qrCode: String
    ): Response<QRStatusResponse>

    @GET("api/qr-transactions/my-generated")
    suspend fun getMyGeneratedQRs(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<QRListResponse>

    @GET("api/qr-transactions/my-scanned")
    suspend fun getMyScannedQRs(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<QRListResponse>

    // ==================== QR HISTORY ====================

    @GET("api/qr-transactions/history")
    suspend fun getTransactionHistory(
        @Header("Authorization") authorization: String,
        @Query("transaction_type") transactionType: String? = null, // "sales" | "purchases"
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<TransactionHistoryResponse>

    @GET("api/qr-transactions/receipt/{transactionId}")
    suspend fun getTransactionReceipt(
        @Header("Authorization") authorization: String,
        @Path("transactionId") transactionId: String
    ): Response<ReceiptResponse>

    // ==================== PRODUCT SEARCH ====================

    @GET("api/companies/search-products")
    suspend fun searchProducts(
        @Header("Authorization") authorization: String,
        @Query("q") query: String? = null,
        @Query("category_id") categoryId: Int? = null,
        @Query("min_price") minPrice: Double? = null,
        @Query("max_price") maxPrice: Double? = null,
        @Query("sort_by") sortBy: String? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<ProductSearchResponse>

    // ==================== PUBLIC COMPANIES ====================

    @GET("api/companies")
    suspend fun searchCompanies(
        @Header("Authorization") authorization: String,
        @Query("q") query: String? = null,
        @Query("city") city: String? = null,
        @Query("province") province: String? = null,
        @Query("category_id") categoryId: Int? = null,
        @Query("service_id") serviceId: Int? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<CompaniesSearchResponse>

    @GET("api/companies/{company_id}")
    suspend fun getCompanyById(
        @Header("Authorization") authorization: String,
        @Path("company_id") companyId: Int
    ): Response<UserCompanyResponse>

    // ==================== USER COMPANIES ====================

    @GET("api/companies/my-company")
    suspend fun getMyCompany(
        @Header("Authorization") authorization: String
    ): Response<UserCompanyResponse>

    // Logo upload endpoints
    @Multipart
    @POST("api/companies/my-company/upload-logo")
    suspend fun uploadCompanyLogo(
        @Header("Authorization") authorization: String,
        @Part logo: okhttp3.MultipartBody.Part
    ): Response<LogoUploadResponse>

    @DELETE("api/companies/my-company/delete-logo")
    suspend fun deleteCompanyLogo(
        @Header("Authorization") authorization: String
    ): Response<LogoUploadResponse>

    @GET("api/companies/my-companies")
    suspend fun getMyCompanies(
        @Header("Authorization") authorization: String
    ): Response<UserCompaniesResponse>

    // ==================== BALANCE & STATS ====================

    @GET("api/auth/withdrawals/balance")
    suspend fun getBalance(
        @Header("Authorization") authorization: String
    ): Response<BalanceResponse>

    // ==================== SESSION MANAGEMENT ====================

    @GET("api/auth/sessions/list")
    suspend fun getSessions(
        @Header("Authorization") authorization: String
    ): Response<SessionsResponse>

    @POST("api/auth/sessions/invalidate-session")
    suspend fun invalidateSession(
        @Header("Authorization") authorization: String,
        @Body request: InvalidateSessionRequest
    ): Response<InvalidateSessionResponse>

    @POST("api/auth/sessions/logout-all-except-current")
    suspend fun logoutAllExceptCurrent(
        @Header("Authorization") authorization: String
    ): Response<LogoutAllResponse>

    @POST("api/auth/sessions/force-logout-all")
    suspend fun forceLogoutAll(
        @Header("Authorization") authorization: String
    ): Response<LogoutAllResponse>

    // ==================== ADMIN EARNINGS ====================

    @GET("api/admin/earnings")
    suspend fun getAdminEarnings(
        @Header("Authorization") authorization: String,
        @Query("search") search: String? = null,
        @Query("filter_param") filter: String? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<AdminEarningsResponse>

    @GET("api/admin/earnings/user/{userId}")
    suspend fun getAdminUserEarnings(
        @Header("Authorization") authorization: String,
        @Path("userId") userId: Int,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<AdminUserEarningsResponse>

    // ==================== COMPANY PAYMENTS ====================

    @POST("api/companies/my-company/payments/register")
    suspend fun registerCompanyPayment(
        @Header("Authorization") authorization: String,
        @Body paymentRequest: CompanyPaymentRequest
    ): Response<RegisterPaymentResponse>

    @GET("api/companies/my-company/payments/history")
    suspend fun getPaymentHistory(
        @Header("Authorization") authorization: String
    ): Response<PaymentHistoryResponse>

    @GET("api/companies/my-company/payments/can-access")
    suspend fun checkPaymentAccess(
        @Header("Authorization") authorization: String
    ): Response<PaymentAccessResponse>

    // =====================================================
    // ENDPOINTS DE GANANCIAS
    // =====================================================

    // Ganancias del usuario autenticado
    @GET("api/companies/transactions/my-earnings/summary")
    suspend fun getMyEarningsSummary(
        @Header("Authorization") authorization: String
    ): Response<EarningsSummaryResponse>

    // Lista de comisiones del usuario autenticado
    @GET("api/companies/transactions/my-commissions")
    suspend fun getMyCommissions(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("payment_status") paymentStatus: String? = null
    ): Response<List<CommissionResponse>>

    // Admin: Resumen general de ganancias
    @GET("admin/earnings/summary")
    suspend fun getAdminEarningsSummary(
        @Header("Authorization") authorization: String
    ): Response<AdminEarningsSummaryResponse>

    // Admin: Resumen de ganancias de un usuario específico
    @GET("admin/earnings/user/{userId}/summary")
    suspend fun getAdminUserEarningsSummary(
        @Header("Authorization") authorization: String,
        @Path("userId") userId: Int
    ): Response<AdminUserEarningsSummaryResponse>

    // =====================================================
    // ENDPOINTS DE RETIROS
    // =====================================================

    // Obtener balance de comisiones (incluye monto mínimo)
    @GET("api/auth/withdrawals/balance/detailed")
    suspend fun getCommissionBalance(
        @Header("Authorization") authorization: String
    ): Response<CommissionBalanceResponse>

    // Obtener mis cuentas bancarias
    @GET("api/auth/withdrawals/bank-accounts")
    suspend fun getMyBankAccounts(
        @Header("Authorization") authorization: String
    ): Response<BankAccountListResponse>

    // Crear cuenta bancaria
    @POST("api/auth/withdrawals/bank-accounts")
    suspend fun createBankAccount(
        @Header("Authorization") authorization: String,
        @Body account: CreateBankAccountRequest
    ): Response<WithdrawalSuccessResponse>

    // Crear solicitud de retiro
    @POST("api/auth/withdrawals/requests")
    suspend fun createWithdrawalRequest(
        @Header("Authorization") authorization: String,
        @Body request: CreateWithdrawalRequest
    ): Response<WithdrawalSuccessResponse>

    // Obtener mis solicitudes de retiro
    @GET("api/auth/withdrawals/requests")
    suspend fun getMyWithdrawalRequests(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<WithdrawalRequestListResponse>

    // Cancelar solicitud de retiro
    @PUT("api/auth/withdrawals/requests/{requestId}/cancel")
    suspend fun cancelWithdrawalRequest(
        @Header("Authorization") authorization: String,
        @Path("requestId") requestId: Int
    ): Response<WithdrawalSuccessResponse>

    // =====================================================
    // ADMIN - RETIROS
    // =====================================================

    // Obtener todas las solicitudes de retiro (admin)
    @GET("api/admin/withdrawals/requests")
    suspend fun getAdminWithdrawals(
        @Header("Authorization") authorization: String,
        @Query("status") status: String? = null, // "pending", "approved", "rejected", "postponed"
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<AdminWithdrawalsListResponse>

    // Obtener detalle de una solicitud de retiro (admin)
    @GET("api/admin/withdrawals/requests/{requestId}")
    suspend fun getAdminWithdrawalDetail(
        @Header("Authorization") authorization: String,
        @Path("requestId") requestId: Int
    ): Response<AdminWithdrawalActionResponse>

    // Revisar solicitud de retiro (aprobar/rechazar/postergar)
    @PUT("api/admin/withdrawals/requests/{requestId}/review")
    suspend fun reviewWithdrawal(
        @Header("Authorization") authorization: String,
        @Path("requestId") requestId: Int,
        @Body request: AdminWithdrawalActionRequest
    ): Response<AdminWithdrawalActionResponse>

    // =====================================================
    // PRODUCT CATEGORIES
    // =====================================================

    // Obtener categorías de productos
    @GET("api/product-categories")
    suspend fun getProductCategories(
        @Header("Authorization") authorization: String,
        @Query("active_only") activeOnly: Boolean = true
    ): Response<List<ProductCategory>>

    // =====================================================
    // PRODUCTS
    // =====================================================

    // Obtener productos de mi empresa
    @GET("api/companies/my-company/products")
    suspend fun getMyCompanyProducts(
        @Header("Authorization") authorization: String
    ): Response<List<Product>>

    // Obtener productos de una empresa específica
    @GET("api/companies/{companyId}/products")
    suspend fun getCompanyProducts(
        @Header("Authorization") authorization: String,
        @Path("companyId") companyId: Int
    ): Response<List<Product>>

    // Crear producto
    @POST("api/companies/my-company/products")
    suspend fun createProduct(
        @Header("Authorization") authorization: String,
        @Body request: CreateProductRequest
    ): Response<ProductResponse>

    // Actualizar producto
    @PUT("api/companies/my-company/products/{productId}")
    suspend fun updateProduct(
        @Header("Authorization") authorization: String,
        @Path("productId") productId: Int,
        @Body request: UpdateProductRequest
    ): Response<ProductResponse>

    // Eliminar producto (soft delete - desactivar)
    @PUT("api/companies/my-company/products/{productId}/deactivate")
    suspend fun deactivateProduct(
        @Header("Authorization") authorization: String,
        @Path("productId") productId: Int
    ): Response<ProductResponse>

    // Eliminar producto (hard delete)
    @DELETE("api/companies/my-company/products/{productId}")
    suspend fun deleteProduct(
        @Header("Authorization") authorization: String,
        @Path("productId") productId: Int
    ): Response<ProductResponse>

    // =====================================================
    // PRODUCT IMAGES
    // =====================================================

    // Subir imagen de producto
    @Multipart
    @POST("api/companies/my-company/products/{productId}/images")
    suspend fun uploadProductImage(
        @Header("Authorization") authorization: String,
        @Path("productId") productId: Int,
        @Part image: okhttp3.MultipartBody.Part,
        @Part("is_primary") isPrimary: okhttp3.RequestBody
    ): Response<ImageUploadResponse>

    // Obtener imágenes de un producto
    @GET("api/companies/my-company/products/{productId}/images")
    suspend fun getProductImages(
        @Header("Authorization") authorization: String,
        @Path("productId") productId: Int
    ): Response<List<ProductImage>>

    // Eliminar imagen de producto
    @DELETE("api/companies/my-company/products/images/{imageId}")
    suspend fun deleteProductImage(
        @Header("Authorization") authorization: String,
        @Path("imageId") imageId: Int
    ): Response<ProductResponse>

    // Establecer imagen como principal
    @PUT("api/companies/my-company/products/images/{imageId}/primary")
    suspend fun setPrimaryProductImage(
        @Header("Authorization") authorization: String,
        @Path("imageId") imageId: Int
    ): Response<ProductResponse>

    // =====================================================
    // CART / SHOPPING CART
    // =====================================================

    // Obtener carrito
    @GET("api/cart")
    suspend fun getCart(
        @Header("Authorization") authorization: String
    ): Response<List<CartItem>>

    // Agregar al carrito
    @POST("api/cart/add")
    suspend fun addToCart(
        @Header("Authorization") authorization: String,
        @Query("product_id") productId: Int,
        @Query("quantity") quantity: Int = 1
    ): Response<CartResponse>

    // Actualizar cantidad en carrito
    @PUT("api/cart/update")
    suspend fun updateCartItem(
        @Header("Authorization") authorization: String,
        @Query("product_id") productId: Int,
        @Query("quantity") quantity: Int
    ): Response<CartResponse>

    // Eliminar item del carrito
    @DELETE("api/cart/remove/{productId}")
    suspend fun removeFromCart(
        @Header("Authorization") authorization: String,
        @Path("productId") productId: Int
    ): Response<CartResponse>

    // Vaciar carrito
    @DELETE("api/cart/clear")
    suspend fun clearCart(
        @Header("Authorization") authorization: String
    ): Response<CartResponse>

    // Checkout - generar código de referencia
    @GET("api/cart/checkout")
    suspend fun checkout(
        @Header("Authorization") authorization: String
    ): Response<CheckoutResponse>

    // Obtener cantidad de items en carrito
    @GET("api/cart/count")
    suspend fun getCartCount(
        @Header("Authorization") authorization: String
    ): Response<CartCountResponse>

    // =====================================================
    // WALLET - BILLETERA DIGITAL
    // =====================================================

    // Establecer PIN de billetera (primera vez)
    @POST("api/wallet/pin/set")
    suspend fun setWalletPin(
        @Body request: SetPinRequest
    ): Response<WalletApiResponse<Unit>>

    // Cambiar PIN de billetera
    @POST("api/wallet/pin/change")
    suspend fun changeWalletPin(
        @Body request: ChangePinRequest
    ): Response<WalletApiResponse<Unit>>

    // Consultar saldo de billetera
    @GET("api/wallet/balance")
    suspend fun getWalletBalance(): Response<WalletBalanceResponse>

    // Realizar transferencia entre usuarios
    @POST("api/wallet/transfer")
    suspend fun walletTransfer(
        @Body request: TransferRequest
    ): Response<TransferResponse>

    // Obtener estado de cuenta / historial de movimientos
    @GET("api/wallet/statement")
    suspend fun getWalletStatement(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Response<WalletStatementResponse>

    // Obtener detalle de una transferencia específica
    @GET("api/wallet/transfer/{id}")
    suspend fun getWalletTransfer(
        @Path("id") id: Int
    ): Response<WalletStatementItem>

    // Verificar si un teléfono está registrado en EasyRefer
    @GET("api/wallet/check-recipient")
    suspend fun checkWalletRecipient(
        @Query("phone") phone: String
    ): Response<CheckRecipientResponse>

    // =====================================================
    // DELIVERY - DRIVER
    // =====================================================

    @GET("api/delivery/drivers/me")
    suspend fun getDriverProfile(
        @Header("Authorization") authorization: String
    ): Response<com.christelldev.easyreferplus.data.model.DriverProfile>

    @GET("api/orders/driver/available")
    suspend fun getAvailableOrders(
        @Header("Authorization") authorization: String
    ): Response<List<com.christelldev.easyreferplus.data.model.AvailableOrder>>

    @GET("api/delivery/drivers/me/active-order")
    suspend fun getActiveOrder(
        @Header("Authorization") authorization: String
    ): Response<com.christelldev.easyreferplus.data.model.ActiveOrderResponse>

    @PUT("api/delivery/drivers/me/duty")
    suspend fun toggleDriverDuty(
        @Header("Authorization") authorization: String
    ): Response<com.christelldev.easyreferplus.data.model.OnDutyToggleResponse>

    @PUT("api/delivery/drivers/me/location")
    suspend fun updateDriverLocation(
        @Header("Authorization") authorization: String,
        @Body body: com.christelldev.easyreferplus.data.model.LocationUpdate
    ): Response<com.christelldev.easyreferplus.data.model.DriverActionResponse>

    @PUT("api/orders/{orderId}/driver/accept")
    suspend fun acceptDeliveryOrder(
        @Header("Authorization") authorization: String,
        @Path("orderId") orderId: Int
    ): Response<com.christelldev.easyreferplus.data.model.DriverActionResponse>

    @PUT("api/orders/{orderId}/driver/reject")
    suspend fun rejectOrder(
        @Header("Authorization") authorization: String,
        @Path("orderId") orderId: Int
    ): Response<com.christelldev.easyreferplus.data.model.DriverActionResponse>

    // Alias sin Response wrapper para uso directo desde DriverOrderRequestActivity
    @PUT("api/orders/{orderId}/driver/accept")
    suspend fun acceptOrder(
        @Header("Authorization") authorization: String,
        @Path("orderId") orderId: Int
    ): Response<com.christelldev.easyreferplus.data.model.DriverActionResponse>

    @PUT("api/orders/{orderId}/driver/pickup")
    suspend fun confirmOrderPickup(
        @Header("Authorization") authorization: String,
        @Path("orderId") orderId: Int
    ): Response<com.christelldev.easyreferplus.data.model.DriverActionResponse>

    @PUT("api/orders/{orderId}/driver/deliver")
    suspend fun confirmOrderDelivery(
        @Header("Authorization") authorization: String,
        @Path("orderId") orderId: Int
    ): Response<com.christelldev.easyreferplus.data.model.DriverActionResponse>

    // Foto de evidencia de entrega
    @Multipart
    @POST("api/orders/{orderId}/delivery-photo")
    suspend fun uploadDeliveryPhoto(
        @Header("Authorization") authorization: String,
        @Path("orderId") orderId: Int,
        @Part photo: okhttp3.MultipartBody.Part
    ): Response<com.christelldev.easyreferplus.data.model.DeliveryPhotoResponse>

    // Conductor marca llegada al punto de recogida
    @PUT("api/orders/{orderId}/arrived-pickup")
    suspend fun driverArrivedAtPickup(
        @Header("Authorization") authorization: String,
        @Path("orderId") orderId: Int
    ): Response<com.christelldev.easyreferplus.data.model.DriverActionResponse>

    // Conductor marca llegada al destino
    @PUT("api/orders/{orderId}/arrived-dropoff")
    suspend fun driverArrivedAtDropoff(
        @Header("Authorization") authorization: String,
        @Path("orderId") orderId: Int
    ): Response<com.christelldev.easyreferplus.data.model.DriverActionResponse>

    // Ganancias del conductor hoy
    @GET("api/orders/driver/earnings-today")
    suspend fun getDriverEarningsToday(
        @Header("Authorization") authorization: String
    ): Response<com.christelldev.easyreferplus.data.model.DriverEarningsTodayResponse>

    // Establecimiento marca el pedido como listo para recoger
    @PUT("api/orders/{orderId}/store/ready")
    suspend fun storeMarkOrderReady(
        @Header("Authorization") authorization: String,
        @Path("orderId") orderId: Int
    ): Response<com.christelldev.easyreferplus.data.model.SimpleSuccessResponse>

    // Comprador confirma que recibió el pedido
    @PUT("api/orders/{orderId}/buyer/confirm")
    suspend fun buyerConfirmReceipt(
        @Header("Authorization") authorization: String,
        @Path("orderId") orderId: Int
    ): Response<com.christelldev.easyreferplus.data.model.SimpleSuccessResponse>

    // =====================================================
    // KEYWORDS & SEARCH ALIASES
    // =====================================================

    @PATCH("api/companies/{companyId}/products/{productId}/keywords")
    suspend fun updateProductKeywords(
        @Header("Authorization") authorization: String,
        @Path("companyId") companyId: Int,
        @Path("productId") productId: Int,
        @Body request: com.christelldev.easyreferplus.data.model.UpdateKeywordsRequest
    ): Response<com.christelldev.easyreferplus.data.model.ProductResponse>

    @GET("api/search/aliases")
    suspend fun getSearchAliases(
        @Header("Authorization") authorization: String
    ): Response<com.christelldev.easyreferplus.data.model.AliasListResponse>

    @POST("api/search/aliases")
    suspend fun createSearchAlias(
        @Header("Authorization") authorization: String,
        @Body request: com.christelldev.easyreferplus.data.model.CreateAliasRequest
    ): Response<com.christelldev.easyreferplus.data.model.ProductResponse>

    @DELETE("api/search/aliases/{aliasId}")
    suspend fun deleteSearchAlias(
        @Header("Authorization") authorization: String,
        @Path("aliasId") aliasId: Int
    ): Response<com.christelldev.easyreferplus.data.model.ProductResponse>

    @GET("api/products/feed")
    suspend fun getProductFeed(
        @Header("Authorization") authorization: String,
        @Query("section") section: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Response<com.christelldev.easyreferplus.data.model.FeedResponse>

    // Comprador cancela el pedido (solo mientras busca conductor)
    @PUT("api/orders/{orderId}/cancel")
    suspend fun cancelOrder(
        @Header("Authorization") authorization: String,
        @Path("orderId") orderId: Int
    ): Response<com.christelldev.easyreferplus.data.model.SimpleSuccessResponse>

    @GET("api/delivery/drivers/me/invitations")
    suspend fun getDriverInvitations(
        @Header("Authorization") authorization: String
    ): Response<List<com.christelldev.easyreferplus.data.model.DriverInvitation>>

    @POST("api/delivery/drivers/me/invitations/{invitationId}/accept")
    suspend fun acceptDriverInvitation(
        @Header("Authorization") authorization: String,
        @Path("invitationId") invitationId: Int
    ): Response<com.christelldev.easyreferplus.data.model.DriverActionResponse>

    @POST("api/delivery/drivers/me/invitations/{invitationId}/reject")
    suspend fun rejectDriverInvitation(
        @Header("Authorization") authorization: String,
        @Path("invitationId") invitationId: Int
    ): Response<com.christelldev.easyreferplus.data.model.DriverActionResponse>

    // =====================================================
    // DELIVERY - ADMIN
    // =====================================================

    @GET("api/delivery/drivers/company/live-locations")
    suspend fun getAdminDriverLocations(
        @Header("Authorization") authorization: String
    ): Response<List<com.christelldev.easyreferplus.data.model.AdminDriverLocation>>

    @GET("api/delivery/drivers/company/orders")
    suspend fun getCompanyOrders(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30,
        @Query("status") status: String? = null
    ): Response<com.christelldev.easyreferplus.data.model.CompanyOrdersResponse>

    // =====================================================
    // ORDERS — COMPRADOR
    // =====================================================

    @POST("api/orders/")
    suspend fun createOrder(
        @Header("Authorization") authorization: String,
        @Body request: com.christelldev.easyreferplus.data.model.OrderCreateRequest
    ): Response<com.christelldev.easyreferplus.data.model.OrderCreateResponse>

    @GET("api/orders/delivery-options")
    suspend fun getDeliveryOptions(
        @Header("Authorization") authorization: String,
        @Query("dest_lat") destLat: Double,
        @Query("dest_lng") destLng: Double
    ): Response<com.christelldev.easyreferplus.data.model.DeliveryOptionsResponse>

    @POST("api/orders/{orderId}/simulate-payment")
    suspend fun simulatePayment(
        @Header("Authorization") authorization: String,
        @Path("orderId") orderId: Int
    ): Response<com.christelldev.easyreferplus.data.model.SimulatePaymentResponse>

    @GET("api/orders/")
    suspend fun getMyOrders(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<List<com.christelldev.easyreferplus.data.model.OrderOut>>

    @GET("api/orders/{orderId}")
    suspend fun getOrderDetail(
        @Header("Authorization") authorization: String,
        @Path("orderId") orderId: Int
    ): Response<com.christelldev.easyreferplus.data.model.OrderOut>

    @GET("api/orders/company/received")
    suspend fun getMyReceivedOrders(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("status") status: String? = null
    ): Response<com.christelldev.easyreferplus.data.model.StoreOrdersResponse>

    // ─── Saved Addresses ─────────────────────────────────────────────────────

    @GET("api/orders/saved-addresses")
    suspend fun getSavedAddresses(
        @Header("Authorization") authorization: String
    ): Response<List<com.christelldev.easyreferplus.data.model.SavedAddress>>

    @POST("api/orders/saved-addresses")
    suspend fun createSavedAddress(
        @Header("Authorization") authorization: String,
        @Body request: com.christelldev.easyreferplus.data.model.CreateAddressRequest
    ): Response<com.christelldev.easyreferplus.data.model.SavedAddress>

    @PUT("api/orders/saved-addresses/{addressId}")
    suspend fun updateSavedAddress(
        @Header("Authorization") authorization: String,
        @Path("addressId") addressId: Int,
        @Body request: com.christelldev.easyreferplus.data.model.UpdateAddressRequest
    ): Response<com.christelldev.easyreferplus.data.model.SavedAddress>

    @DELETE("api/orders/saved-addresses/{addressId}")
    suspend fun deleteSavedAddress(
        @Header("Authorization") authorization: String,
        @Path("addressId") addressId: Int
    ): Response<com.christelldev.easyreferplus.data.model.SimpleSuccessResponse>

    // ─── Order Tracking ─────────────────────────────────────────────────────

    @GET("api/orders/{orderId}/tracking")
    suspend fun getOrderTracking(
        @Path("orderId") orderId: Int
    ): Response<com.christelldev.easyreferplus.data.model.OrderTrackingInfo>

    @GET("api/orders/{orderId}/eta")
    suspend fun getOrderEta(
        @Path("orderId") orderId: Int
    ): Response<com.christelldev.easyreferplus.data.model.OrderEtaResponse>

    // =====================================================
    // RATINGS & TIPS
    // =====================================================

    @POST("api/orders/{orderId}/rating")
    suspend fun createRating(
        @Header("Authorization") authorization: String,
        @Path("orderId") orderId: Int,
        @Body request: com.christelldev.easyreferplus.data.model.RatingRequest
    ): Response<com.christelldev.easyreferplus.data.model.RatingResponse>

    @GET("api/orders/{orderId}/rating")
    suspend fun getMyRating(
        @Header("Authorization") authorization: String,
        @Path("orderId") orderId: Int
    ): Response<com.christelldev.easyreferplus.data.model.MyRatingResponse>

    @POST("api/orders/{orderId}/tip")
    suspend fun createOrUpdateTip(
        @Header("Authorization") authorization: String,
        @Path("orderId") orderId: Int,
        @Body request: com.christelldev.easyreferplus.data.model.TipRequest
    ): Response<com.christelldev.easyreferplus.data.model.TipResponse>

    @GET("api/orders/{orderId}/tip")
    suspend fun getTip(
        @Header("Authorization") authorization: String,
        @Path("orderId") orderId: Int
    ): Response<com.christelldev.easyreferplus.data.model.TipDetailResponse>

    @GET("api/orders/ratings/config")
    suspend fun getRatingConfig(
        @Header("Authorization") authorization: String
    ): Response<com.christelldev.easyreferplus.data.model.RatingConfigResponse>

    // ─── Chat ────────────────────────────────────────────────────────────────

    @GET("api/orders/{orderId}/messages")
    suspend fun getChatMessages(
        @Path("orderId") orderId: Int,
        @Query("limit") limit: Int = 50,
        @Query("before_id") beforeId: Int? = null,
    ): Response<com.christelldev.easyreferplus.data.model.ChatMessagesResponse>

    @POST("api/orders/{orderId}/messages")
    suspend fun sendChatMessage(
        @Path("orderId") orderId: Int,
        @Body body: com.christelldev.easyreferplus.data.model.ChatMessageRequest,
    ): Response<com.christelldev.easyreferplus.data.model.ChatSendResponse>

    @PUT("api/orders/{orderId}/messages/read")
    suspend fun markChatMessagesRead(
        @Path("orderId") orderId: Int,
    ): Response<com.christelldev.easyreferplus.data.model.ChatMarkReadResponse>

    @GET("api/orders/{orderId}/unread-count")
    suspend fun getChatUnreadCount(
        @Path("orderId") orderId: Int,
        @Header("Authorization") authorization: String
    ): Response<com.christelldev.easyreferplus.data.model.ChatUnreadResponse>
}
