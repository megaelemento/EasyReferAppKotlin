package com.christelldev.easyreferplus.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.GenerateQRRequest
import com.christelldev.easyreferplus.data.model.ProcessQRRequest
import com.christelldev.easyreferplus.data.model.ProcessReceiptResponse
import com.christelldev.easyreferplus.data.model.ScanQRRequest
import com.christelldev.easyreferplus.data.model.QRTransaction
import com.christelldev.easyreferplus.data.network.QRRepository
import com.christelldev.easyreferplus.data.network.QRResult
import com.christelldev.easyreferplus.data.network.SaleNotificationData
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Modelo simple para empresa del usuario
data class UserCompany(
    val id: Int,
    val name: String,
    val isValidated: Boolean = false
)

data class QRUiState(
    // Generar QR
    val selectedCompanyId: Int? = null,
    val amount: String = "",
    val description: String = "",
    val referralCode: String = "",
    val companies: List<UserCompany> = emptyList(),
    val isLoadingCompanies: Boolean = false,

    // QR Generado
    val qrCode: String? = null,
    val qrPayload: String? = null,
    val qrData: String? = null,
    val amountValue: Double? = null,
    val companyName: String? = null,
    val expiresAt: String? = null,
    val generatedQRBitmap: android.graphics.Bitmap? = null,
    // Legacy
    val generatedQR: QRTransaction? = null,
    val qrImageUrl: String? = null,

    // Escanear QR (entrada manual)
    val qrCodeInput: String = "",
    val qrSecretInput: String = "",

    // QR Escaneado
    val scannedQR: QRTransaction? = null,

    // Procesar transacción
    val buyerDocument: String = "",
    val buyerName: String = "",
    val buyerPhone: String = "",
    val buyerEmail: String = "",
    val paymentMethod: String = "cash",

    // Transacción procesada
    val processedTransaction: com.christelldev.easyreferplus.data.model.ProcessedTransaction? = null,

    // Receipt de transacción (para mostrar después de procesar)
    val receipt: ProcessReceiptResponse? = null,

    // Listas de QR
    val generatedQRs: List<QRTransaction> = emptyList(),
    val scannedQRs: List<QRTransaction> = emptyList(),
    val isLoadingList: Boolean = false,

    // Estados generales
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,

    // Errores de campo
    val fieldErrors: Map<String, String> = emptyMap()
)

class QRViewModel(
    private val repository: QRRepository,
    private val getAccessToken: () -> String
) : ViewModel() {

    private val _uiState = MutableStateFlow(QRUiState())
    val uiState: StateFlow<QRUiState> = _uiState.asStateFlow()

    private val authorization: String get() = "Bearer ${getAccessToken()}"

    // ==================== GENERATE QR ====================

    fun updateSelectedCompany(companyId: Int?) {
        _uiState.value = _uiState.value.copy(
            selectedCompanyId = companyId,
            fieldErrors = _uiState.value.fieldErrors - "company_id"
        )
    }

    fun updateAmount(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        _uiState.value = _uiState.value.copy(
            amount = filtered,
            fieldErrors = _uiState.value.fieldErrors - "amount"
        )
    }

    fun updateDescription(value: String) {
        _uiState.value = _uiState.value.copy(
            description = value,
            fieldErrors = _uiState.value.fieldErrors - "description"
        )
    }

    fun updateReferralCode(value: String) {
        _uiState.value = _uiState.value.copy(referralCode = value.uppercase().trim())
    }

    fun setCompanies(companies: List<UserCompany>) {
        _uiState.value = _uiState.value.copy(
            companies = companies,
            isLoadingCompanies = false
        )
    }

    fun setLoadingCompanies(loading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoadingCompanies = loading)
    }

    private fun validateGenerateQR(): Boolean {
        val state = _uiState.value
        val errors = mutableMapOf<String, String>()

        if (state.selectedCompanyId == null) {
            errors["company_id"] = "Selecciona una empresa"
        }

        if (state.amount.isBlank()) {
            errors["amount"] = "El monto es obligatorio"
        } else {
            val amount = state.amount.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                errors["amount"] = "El monto debe ser mayor a 0"
            }
        }

        // Descripción es opcional ahora
        // if (state.description.isBlank()) {
        //     errors["description"] = "La descripción es obligatoria"
        // }

        _uiState.value = _uiState.value.copy(fieldErrors = errors)
        return errors.isEmpty()
    }

    fun generateQR() {
        if (!validateGenerateQR()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Por favor complete todos los campos"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                generatedQR = null,
                qrImageUrl = null,
                qrData = null
            )

            val state = _uiState.value
            val request = GenerateQRRequest(
                companyId = state.selectedCompanyId!!,
                amount = state.amount.toDouble(),
                currency = "USD",
                description = state.description.trim(),
                referralCode = state.referralCode.takeIf { it.isNotBlank() }
            )

            when (val result = repository.generateQR(authorization, request)) {
                is QRResult.GenerateSuccess -> {
                    // Generar QR localmente usando ZXing
                    val qrBitmap = result.qrPayload?.let { payload ->
                        generateQRBitmap(payload)
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        qrCode = result.qrCode,
                        qrPayload = result.qrPayload,
                        qrData = result.qrData,
                        amountValue = result.amount,
                        companyName = result.companyName,
                        expiresAt = result.expiresAt,
                        generatedQRBitmap = qrBitmap,
                        // Legacy
                        generatedQR = result.qrTransaction,
                        qrImageUrl = result.qrImageUrl,
                        successMessage = result.message
                    )
                }
                is QRResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        fieldErrors = result.errors?.associate { it.field to it.message } ?: emptyMap()
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error desconocido"
                    )
                }
            }
        }
    }

    // ==================== SCAN QR ====================

    fun updateQRCodeInput(value: String) {
        _uiState.value = _uiState.value.copy(
            qrCodeInput = value.trim(),
            fieldErrors = _uiState.value.fieldErrors - "qr_code"
        )
    }

    fun updateQRSecretInput(value: String) {
        _uiState.value = _uiState.value.copy(
            qrSecretInput = value.trim(),
            fieldErrors = _uiState.value.fieldErrors - "qr_secret"
        )
    }

    fun setScannedQRData(qrCode: String, qrSecret: String) {
        _uiState.value = _uiState.value.copy(
            qrCodeInput = qrCode,
            qrSecretInput = qrSecret
        )
    }

    private fun validateScanQR(): Boolean {
        val state = _uiState.value
        val errors = mutableMapOf<String, String>()

        if (state.qrCodeInput.isBlank()) {
            errors["qr_code"] = "El código QR es obligatorio"
        }

        // El qr_secret ya no es obligatorio - el backend lo obtiene de la BD
        // if (state.qrSecretInput.isBlank()) {
        //     errors["qr_secret"] = "El secreto QR es obligatorio"
        // }

        _uiState.value = _uiState.value.copy(fieldErrors = errors)
        return errors.isEmpty()
    }

    fun scanQR() {
        if (!validateScanQR()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Por favor ingrese los datos del código QR"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                scannedQR = null,
                processedTransaction = null,
                receipt = null
            )

            val state = _uiState.value
            // El escaneo ahora procesa automáticamente y devuelve el recibo
            val request = ScanQRRequest(
                qrCode = state.qrCodeInput,
                qrSecret = state.qrSecretInput
            )

            when (val result = repository.scanQR(authorization, request)) {
                is QRResult.ProcessSuccess -> {
                    // El escaneo procesó automáticamente - mostrar recibo
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        scannedQR = null,
                        processedTransaction = result.transaction,
                        receipt = result.receipt,
                        successMessage = result.message
                    )
                }
                is QRResult.ScanSuccess -> {
                    // Legacy: si el servidor devuelve solo scan success, procesar automáticamente
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        scannedQR = result.qrTransaction,
                        successMessage = result.message
                    )
                }
                is QRResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        fieldErrors = result.errors?.associate { it.field to it.message } ?: emptyMap()
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error desconocido"
                    )
                }
            }
        }
    }

    // ==================== PROCESS QR ====================

    fun updateBuyerDocument(value: String) {
        _uiState.value = _uiState.value.copy(
            buyerDocument = value.filter { it.isDigit() },
            fieldErrors = _uiState.value.fieldErrors - "buyer_document"
        )
    }

    fun updateBuyerName(value: String) {
        _uiState.value = _uiState.value.copy(
            buyerName = value.uppercase().trim(),
            fieldErrors = _uiState.value.fieldErrors - "buyer_name"
        )
    }

    fun updateBuyerPhone(value: String) {
        val digits = value.filter { it.isDigit() }
        val formatted = when {
            digits.startsWith("593") -> "+$digits"
            digits.startsWith("0") && digits.length > 1 -> "+593${digits.substring(1)}"
            digits.isNotEmpty() && !digits.startsWith("593") -> "+593$digits"
            else -> digits
        }
        _uiState.value = _uiState.value.copy(
            buyerPhone = formatted,
            fieldErrors = _uiState.value.fieldErrors - "buyer_phone"
        )
    }

    fun updateBuyerEmail(value: String) {
        _uiState.value = _uiState.value.copy(
            buyerEmail = value.trim().lowercase(),
            fieldErrors = _uiState.value.fieldErrors - "buyer_email"
        )
    }

    fun updatePaymentMethod(value: String) {
        _uiState.value = _uiState.value.copy(paymentMethod = value)
    }

    private fun validateProcessQR(): Boolean {
        val state = _uiState.value
        val errors = mutableMapOf<String, String>()

        if (state.buyerDocument.isBlank()) {
            errors["buyer_document"] = "El documento es obligatorio"
        } else if (state.buyerDocument.length < 10) {
            errors["buyer_document"] = "Documento inválido"
        }

        if (state.buyerName.isBlank()) {
            errors["buyer_name"] = "El nombre es obligatorio"
        }

        if (state.buyerPhone.isBlank()) {
            errors["buyer_phone"] = "El teléfono es obligatorio"
        }

        // Email es opcional pero si se ingresa debe ser válido
        if (state.buyerEmail.isNotBlank()) {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.buyerEmail).matches()) {
                errors["buyer_email"] = "Email inválido"
            }
        }

        _uiState.value = _uiState.value.copy(fieldErrors = errors)
        return errors.isEmpty()
    }

    fun processQR() {
        if (!validateProcessQR()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Por favor complete todos los campos requeridos"
            )
            return
        }

        val scannedQR = _uiState.value.scannedQR
        if (scannedQR == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "No hay código QR escaneado"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val state = _uiState.value
            val request = ProcessQRRequest(
                qrCode = scannedQR.qrCode,
                qrSecret = state.qrSecretInput,
                buyerDocument = state.buyerDocument,
                buyerName = state.buyerName,
                buyerPhone = state.buyerPhone,
                buyerEmail = state.buyerEmail.takeIf { it.isNotBlank() },
                externalTransactionId = null,
                paymentMethod = state.paymentMethod
            )

            when (val result = repository.processQR(authorization, request)) {
                is QRResult.ProcessSuccess -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        processedTransaction = result.transaction,
                        receipt = result.receipt,
                        successMessage = result.message
                    )
                }
                is QRResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        fieldErrors = result.errors?.associate { it.field to it.message } ?: emptyMap()
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error desconocido"
                    )
                }
            }
        }
    }

    // ==================== LIST QR TRANSACTIONS ====================

    fun loadGeneratedQRs(page: Int = 1) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingList = true)

            when (val result = repository.getMyGeneratedQRs(authorization, page)) {
                is QRResult.ListSuccess -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingList = false,
                        generatedQRs = result.qrTransactions
                    )
                }
                is QRResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingList = false,
                        errorMessage = result.message
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isLoadingList = false)
                }
            }
        }
    }

    fun loadScannedQRs(page: Int = 1) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingList = true)

            when (val result = repository.getMyScannedQRs(authorization, page)) {
                is QRResult.ListSuccess -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingList = false,
                        scannedQRs = result.qrTransactions
                    )
                }
                is QRResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingList = false,
                        errorMessage = result.message
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isLoadingList = false)
                }
            }
        }
    }

    // ==================== RESET & CLEAR ====================

    fun clearGeneratedQR() {
        _uiState.value = _uiState.value.copy(
            qrCode = null,
            qrPayload = null,
            qrData = null,
            amountValue = null,
            companyName = null,
            expiresAt = null,
            generatedQRBitmap = null,
            generatedQR = null,
            qrImageUrl = null,
            amount = "",
            description = "",
            referralCode = ""
        )
    }

    // Mostrar recibo de venta desde notificación WebSocket
    fun showSaleReceiptFromNotification(notification: SaleNotificationData) {
        val receipt = ProcessReceiptResponse(
            success = true,
            transactionId = notification.transactionId,
            qrCode = notification.qrCode,
            amount = notification.amount,
            currency = notification.currency,
            transactionType = "sale",
            sellerId = 0,
            sellerName = "",
            sellerPhone = null,
            companyId = 0,
            companyName = notification.companyName,
            buyerId = null,
            buyerDocument = notification.buyerDocument,
            buyerName = notification.buyerName,
            buyerPhone = notification.buyerPhone,
            buyerEmail = null,
            paymentMethod = "qr_scan",
            referralCodeUsed = notification.referralCodeUsed,
            transactionDate = notification.scanTimestamp,
            expiresAt = null,
            message = "Venta completada"
        )
        _uiState.value = _uiState.value.copy(receipt = receipt)
    }

    fun clearScannedQR() {
        _uiState.value = _uiState.value.copy(
            scannedQR = null,
            qrCodeInput = "",
            qrSecretInput = "",
            buyerDocument = "",
            buyerName = "",
            buyerPhone = "",
            buyerEmail = "",
            paymentMethod = "cash",
            processedTransaction = null,
            receipt = null,
            isSuccess = false
        )
    }

    fun dismissReceipt() {
        _uiState.value = _uiState.value.copy(
            receipt = null,
            isSuccess = false,
            scannedQR = null,
            qrCodeInput = "",
            qrSecretInput = "",
            buyerDocument = "",
            buyerName = "",
            buyerPhone = "",
            buyerEmail = "",
            paymentMethod = "cash",
            processedTransaction = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun resetState() {
        _uiState.value = QRUiState()
    }

    // Generar código QR localmente usando ZXing
    private fun generateQRBitmap(content: String): Bitmap? {
        return try {
            val size = 512 // Tamaño del QR
            val hints = hashMapOf<EncodeHintType, Any>()
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            hints[EncodeHintType.MARGIN] = 1

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)

            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    class Factory(
        private val repository: QRRepository,
        private val getAccessToken: () -> String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(QRViewModel::class.java)) {
                return QRViewModel(repository, getAccessToken) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
