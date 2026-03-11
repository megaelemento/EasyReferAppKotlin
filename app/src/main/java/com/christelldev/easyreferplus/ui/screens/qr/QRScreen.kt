package com.christelldev.easyreferplus.ui.screens.qr

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import com.christelldev.easyreferplus.data.model.ProcessReceiptResponse
import com.christelldev.easyreferplus.ui.screens.qr.ReceiptScreen
import coil.compose.AsyncImage
import com.christelldev.easyreferplus.data.network.SaleNotificationData
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.data.model.QRCompany
import com.christelldev.easyreferplus.data.model.QRTransaction
import com.christelldev.easyreferplus.ui.theme.AppBlue
import com.christelldev.easyreferplus.ui.viewmodel.QRViewModel
import com.christelldev.easyreferplus.ui.viewmodel.UserCompany
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Constants for consistent styling - Following HomeScreen design
private val CARD_CORNER_RADIUS = 20.dp
private val CARD_ELEVATION = 8.dp
private val CARD_MARGIN_HORIZONTAL = 16.dp
private val SECTION_SPACING = 20.dp

// Modern color palette - Following HomeScreen design
private val GradientPrimary = listOf(Color(0xFF03A9F4), Color(0xFF2196F3))
private val GradientSuccess = listOf(Color(0xFF10B981), Color(0xFF34D399))
private val GradientPurple = listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA))
private val GradientTeal = listOf(Color(0xFF14B8A6), Color(0xFF2DD4BF))
private val GradientOrange = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24))

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ScannedQRInfo(
    scannedQR: QRTransaction,
    onClearScan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Código QR Escaneado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                InfoRow(label = "Empresa", value = scannedQR.company?.companyName ?: "N/A")
                scannedQR.description?.let {
                    InfoRow(label = "Descripción", value = it)
                }
                InfoRow(label = "Monto", value = "${scannedQR.currency} ${String.format("%.2f", scannedQR.amount)}")
                scannedQR.referralCode?.let {
                    InfoRow(label = "Referido", value = it)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onClearScan,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Escanear otro QR")
                }
            }
        }
    }
}

@Composable
fun TransactionResultCard(
    transaction: com.christelldev.easyreferplus.data.model.ProcessedTransaction,
    onNewScan: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.transaction_completed),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoRow(label = stringResource(R.string.transaction_id), value = transaction.id.toString())
                    InfoRow(label = stringResource(R.string.total_amount), value = "${String.format("%.2f", transaction.totalAmount)}")
                    InfoRow(label = stringResource(R.string.commission_amount), value = "${String.format("%.2f", transaction.totalCommissionAmount)}")
                    InfoRow(label = stringResource(R.string.status), value = transaction.transactionStatus)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNewScan,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppBlue)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.scan_another_qr),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun SaleNotificationDialog(
    notification: SaleNotificationData,
    onDismiss: () -> Unit,
    onViewReceipt: () -> Unit
) {
    // Format timestamp
    val formattedDate = try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(notification.scanTimestamp)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        notification.scanTimestamp
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "Nueva Venta!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Alguien acaba de comprar con tu código QR",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Amount
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AppBlue.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Monto",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${notification.currency} ${String.format("%.2f", notification.amount)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppBlue
                        )
                    }
                }

                // Buyer info
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        InfoRow(label = "Comprador", value = notification.buyerName)
                        InfoRow(label = "Documento", value = notification.buyerDocument)
                        InfoRow(label = "Teléfono", value = notification.buyerPhone)
                        InfoRow(label = "Fecha", value = formattedDate)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onViewReceipt,
                colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ver Recibo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScreen(
    viewModel: QRViewModel,
    hasCompany: Boolean,
    hasActiveCompany: Boolean = false,
    companies: List<UserCompany>,
    onGenerateQRSuccess: () -> Unit = {},
    onQRScannedByBuyer: () -> Unit = {},
    onBack: () -> Unit,
    onNavigateToCompany: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    // Si no hay empresa activa, empezar en pestaña de escanear (índice 1)
    var selectedTab by remember { mutableIntStateOf(if (hasActiveCompany) 0 else 1) }
    var showQRScanner by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show sale receipt when notification has been received
    val saleReceipt = uiState.receipt
    if (saleReceipt != null && saleReceipt.transactionType == "sale") {
        ReceiptScreen(
            receipt = saleReceipt,
            onDone = {
                viewModel.dismissReceipt()
            },
            onShare = {
                val shareText = buildString {
                    appendLine("🧾 Recibo de Venta EasyRefer+")
                    appendLine()
                    appendLine("ID: ${saleReceipt.transactionId}")
                    appendLine("Monto: ${saleReceipt.currency} ${String.format("%.2f", saleReceipt.amount)}")
                    appendLine("Empresa: ${saleReceipt.companyName}")
                    appendLine()
                    appendLine("Comprador: ${saleReceipt.buyerName}")
                    appendLine("Documento: ${saleReceipt.buyerDocument}")
                    appendLine("Teléfono: ${saleReceipt.buyerPhone}")
                    appendLine()
                    appendLine("Método de Pago: ${saleReceipt.paymentMethod.replaceFirstChar { it.uppercase() }}")
                    saleReceipt.transactionDate?.let {
                        val formattedDate = try { it.replace("T", " ").substringBefore(".") } catch (e: Exception) { it }
                        appendLine("Fecha: $formattedDate")
                    }
                    saleReceipt.referralCodeUsed?.let {
                        appendLine("Referido: $it")
                    }
                    appendLine()
                    appendLine("Generado por EasyRefer+")
                }

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Recibo EasyRefer+ - Venta")
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Compartir recibo"))
            }
        )
        return
    }

    // Show QR Scanner
    if (showQRScanner) {
        QRScannerScreen(
            onQRCodeScanned = { code, secret ->
                viewModel.setScannedQRData(code, secret)
                // Delay pequeño para que el estado se actualice antes de llamar scanQR
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    showQRScanner = false
                    viewModel.scanQR()
                }, 100)
            },
            onClose = { showQRScanner = false }
        )
        return
    }

    // Show Receipt after successful payment
    uiState.receipt?.let { receipt ->
        ReceiptScreen(
            receipt = receipt,
            onDone = {
                viewModel.dismissReceipt()
            },
            onShare = {
                val isSale = receipt.transactionType == "sale"
                val transactionTypeText = if (isSale) "Venta" else "Compra"

                val shareText = buildString {
                    appendLine("🧾 Recibo de Transacción EasyRefer+")
                    appendLine()
                    appendLine("Tipo: $transactionTypeText")
                    appendLine("ID: ${receipt.transactionId}")
                    appendLine("Monto: ${receipt.currency} ${String.format("%.2f", receipt.amount)}")
                    appendLine("Empresa: ${receipt.companyName}")
                    appendLine()
                    if (isSale) {
                        appendLine("Comprador: ${receipt.buyerName}")
                        appendLine("Documento: ${receipt.buyerDocument}")
                        appendLine("Teléfono: ${receipt.buyerPhone}")
                    } else {
                        appendLine("Vendedor: ${receipt.sellerName}")
                        receipt.sellerPhone?.let { appendLine("Teléfono: $it") }
                    }
                    appendLine()
                    appendLine("Método de Pago: ${receipt.paymentMethod.replaceFirstChar { it.uppercase() }}")
                    receipt.transactionDate?.let {
                        val formattedDate = try { it.replace("T", " ").substringBefore(".") } catch (e: Exception) { it }
                        appendLine("Fecha: $formattedDate")
                    }
                    receipt.referralCodeUsed?.let {
                        appendLine("Referido: $it")
                    }
                    appendLine()
                    appendLine("Generado por EasyRefer+")
                }

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Recibo EasyRefer+ - $transactionTypeText")
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Compartir recibo"))
            }
        )
        return
    }

    // Set companies in viewmodel
    LaunchedEffect(companies) {
        viewModel.setCompanies(companies)
    }

    // Notificar cuando se genera un QR exitosamente (para activar WebSocket)
    LaunchedEffect(uiState.qrPayload, uiState.generatedQR) {
        if (uiState.qrPayload != null || uiState.generatedQR != null) {
            onGenerateQRSuccess()
        }
    }

    // Show error messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    // Show success messages
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.qr_payments),
                        color = MaterialTheme.colorScheme.surface,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBlue
                ),
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppBlue)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .imePadding()
                    .padding(16.dp)
            ) {
                // Tabs for Generate/Scan
                if (hasCompany) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = AppBlue
                    ) {
                        // Solo mostrar pestaña de generar QR si hay empresa activa
                        if (hasActiveCompany) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = { Text(stringResource(R.string.generate_qr)) },
                                icon = { Icon(Icons.Default.QrCode, contentDescription = null) }
                            )
                        }
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text(stringResource(R.string.scan_qr)) },
                            icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                when {
                    // Show generated QR if exists (prioritize showing the QR)
                    uiState.qrPayload != null || uiState.generatedQR != null -> {
                        FullScreenQRDisplay(
                            qrBitmap = uiState.generatedQRBitmap,
                            amount = uiState.amountValue ?: uiState.generatedQR?.amount,
                            companyName = uiState.companyName,
                            qrCode = uiState.qrCode ?: uiState.generatedQR?.qrCode,
                            onGenerateNew = viewModel::clearGeneratedQR
                        )
                    }
                    // Show generate QR form if user has company and tab is set to Generate
                    hasCompany && selectedTab == 0 -> {
                        GenerateQRSection(
                            companies = companies,
                            selectedCompanyId = uiState.selectedCompanyId,
                            amount = uiState.amount,
                            description = uiState.description,
                            referralCode = uiState.referralCode,
                            fieldErrors = uiState.fieldErrors,
                            generatedQR = uiState.generatedQR,
                            qrImageUrl = uiState.qrImageUrl,
                            qrData = uiState.qrData,
                            onCompanySelected = viewModel::updateSelectedCompany,
                            onAmountChanged = viewModel::updateAmount,
                            onDescriptionChanged = viewModel::updateDescription,
                            onReferralCodeChanged = viewModel::updateReferralCode,
                            onGenerateQR = viewModel::generateQR,
                            onClearQR = viewModel::clearGeneratedQR
                        )
                    }
                    // Show scan QR form (or when no company)
                    else -> {
                        // Si hay receipt, mostrarlo directamente (el escaneo procesó automáticamente)
                        if (uiState.receipt != null) {
                            ReceiptScreen(
                                receipt = uiState.receipt!!,
                                onDone = {
                                    viewModel.dismissReceipt()
                                },
                                onShare = {
                                    val isSale = uiState.receipt!!.transactionType == "sale"
                                    val transactionTypeText = if (isSale) "Venta" else "Compra"

                                    val shareText = buildString {
                                        appendLine("🧾 Recibo de Transacción EasyRefer+")
                                        appendLine()
                                        appendLine("Tipo: $transactionTypeText")
                                        appendLine("ID: ${uiState.receipt!!.transactionId}")
                                        appendLine("Monto: ${uiState.receipt!!.currency} ${String.format("%.2f", uiState.receipt!!.amount)}")
                                        appendLine("Empresa: ${uiState.receipt!!.companyName}")
                                        appendLine()
                                        if (!isSale) {
                                            appendLine("Comprador: ${uiState.receipt!!.buyerName}")
                                            appendLine("Documento: ${uiState.receipt!!.buyerDocument}")
                                            appendLine("Teléfono: ${uiState.receipt!!.buyerPhone}")
                                        } else {
                                            appendLine("Vendedor: ${uiState.receipt!!.sellerName}")
                                            uiState.receipt!!.sellerPhone?.let { appendLine("Teléfono: $it") }
                                        }
                                        appendLine()
                                        appendLine("Método de Pago: ${uiState.receipt!!.paymentMethod.replaceFirstChar { it.uppercase() }}")
                                        uiState.receipt!!.transactionDate?.let {
                                            val formattedDate = try { it.replace("T", " ").substringBefore(".") } catch (e: Exception) { it }
                                            appendLine("Fecha: $formattedDate")
                                        }
                                        uiState.receipt!!.referralCodeUsed?.let {
                                            appendLine("Referido: $it")
                                        }
                                        appendLine()
                                        appendLine("Generado por EasyRefer+")
                                    }

                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Recibo EasyRefer+ - $transactionTypeText")
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Compartir recibo"))
                                }
                            )
                        } else if (uiState.scannedQR == null) {
                            ScanQRSection(
                                showRegisterCompany = !hasCompany,
                                onRegisterCompany = onNavigateToCompany,
                                onScanWithCamera = { showQRScanner = true }
                            )
                        } else {
                            // Legacy: mostrar info del QR escaneado (sin formulario)
                            ScannedQRInfo(
                                scannedQR = uiState.scannedQR!!,
                                onClearScan = viewModel::clearScannedQR
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateQRSection(
    companies: List<UserCompany>,
    selectedCompanyId: Int?,
    amount: String,
    description: String,
    referralCode: String,
    fieldErrors: Map<String, String>,
    generatedQR: QRTransaction?,
    qrImageUrl: String?,
    qrData: String?,
    onCompanySelected: (Int?) -> Unit,
    onAmountChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onReferralCodeChanged: (String) -> Unit,
    onGenerateQR: () -> Unit,
    onClearQR: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCompany = companies.find { it.id == selectedCompanyId }

    // Si hay una sola empresa, seleccionarla automáticamente
    LaunchedEffect(companies) {
        if (companies.size == 1 && selectedCompanyId == null) {
            onCompanySelected(companies.first().id)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (generatedQR == null) {
            // Form to generate QR - Following HomeScreen design
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(CARD_CORNER_RADIUS)),
                shape = RoundedCornerShape(CARD_CORNER_RADIUS),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.generate_new_qr),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Company dropdown - solo mostrar si hay más de una empresa
                    if (companies.size > 1) {
                        Text(
                            text = stringResource(R.string.select_company),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedCompany?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                placeholder = { Text(stringResource(R.string.select_company_placeholder)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppBlue,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                isError = fieldErrors.containsKey("company_id")
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                companies.forEach { company ->
                                    DropdownMenuItem(
                                        text = { Text(company.name) },
                                        onClick = {
                                            onCompanySelected(company.id)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        fieldErrors["company_id"]?.let { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    } else if (companies.size == 1) {
                        // Mostrar la empresa seleccionada si solo hay una
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = AppBlue.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = AppBlue
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    text = companies.first().name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Amount field
                    OutlinedTextField(
                        value = amount,
                        onValueChange = onAmountChanged,
                        label = { Text(stringResource(R.string.amount)) },
                        placeholder = { Text(stringResource(R.string.amount_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        isError = fieldErrors.containsKey("amount"),
                        prefix = { Text("$") }
                    )
                    fieldErrors["amount"]?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Description field (optional)
                    OutlinedTextField(
                        value = description,
                        onValueChange = onDescriptionChanged,
                        label = { Text(stringResource(R.string.description_optional)) },
                        placeholder = { Text(stringResource(R.string.description_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        isError = fieldErrors.containsKey("description")
                    )
                    fieldErrors["description"]?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Generate button
                    Button(
                        onClick = onGenerateQR,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = stringResource(R.string.generate_qr_code),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FullScreenQRDisplay(
    qrBitmap: android.graphics.Bitmap?,
    amount: Double?,
    companyName: String?,
    qrCode: String?,
    onGenerateNew: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mostrar el código QR generado localmente - Following HomeScreen design
        if (qrBitmap != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(CARD_CORNER_RADIUS)),
                shape = RoundedCornerShape(CARD_CORNER_RADIUS),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = stringResource(R.string.qr_code_image_description),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
            }
        } else {
            // Si no se pudo generar el QR localmente
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(CARD_CORNER_RADIUS)),
                shape = RoundedCornerShape(CARD_CORNER_RADIUS),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Error al generar el código QR",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    // Mostrar código como fallback
                    qrCode?.let { code ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = code,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nombre de la empresa
        companyName?.let { name ->
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Monto a pagar en número grandes
        amount?.let { value ->
            Text(
                text = "$${String.format("%.2f", value)}",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = AppBlue
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón para generar otro QR
        Button(
            onClick = onGenerateNew,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(
                imageVector = Icons.Default.QrCode,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = stringResource(R.string.generate_another_qr),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
fun ScanQRSection(
    showRegisterCompany: Boolean = false,
    onRegisterCompany: () -> Unit = {},
    onScanWithCamera: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(CARD_CORNER_RADIUS)),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.scan_qr_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.scan_qr_instructions),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botón para escanear con cámara - Following HomeScreen gradient style
            Button(
                onClick = onScanWithCamera,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(CARD_CORNER_RADIUS),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(colors = GradientPrimary)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.scan_with_camera),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            // Register company prompt if no company
            if (showRegisterCompany) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.register_company_to_generate_qr),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onRegisterCompany,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text(
                        text = stringResource(R.string.register_company),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProcessTransactionSection(
    scannedQR: QRTransaction,
    buyerDocument: String,
    buyerName: String,
    buyerPhone: String,
    buyerEmail: String,
    paymentMethod: String,
    fieldErrors: Map<String, String>,
    processedTransaction: com.christelldev.easyreferplus.data.model.ProcessedTransaction?,
    onBuyerDocumentChanged: (String) -> Unit,
    onBuyerNameChanged: (String) -> Unit,
    onBuyerPhoneChanged: (String) -> Unit,
    onBuyerEmailChanged: (String) -> Unit,
    onPaymentMethodChanged: (String) -> Unit,
    onProcess: () -> Unit,
    onClearScan: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Scanned QR details - Following HomeScreen design
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(CARD_CORNER_RADIUS)),
            shape = RoundedCornerShape(CARD_CORNER_RADIUS),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = GradientPrimary.map { it.copy(alpha = 0.1f) }
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.qr_scanned_success),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppBlue
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Mostrar nombre de la empresa si está disponible
                    scannedQR.company?.let { company ->
                        InfoRow(label = stringResource(R.string.company_name_label), value = company.companyName)
                    } ?: InfoRow(label = stringResource(R.string.company_id), value = scannedQR.companyId.toString())

                    InfoRow(label = stringResource(R.string.amount), value = "${scannedQR.currency} ${String.format("%.2f", scannedQR.amount)}")
                    InfoRow(label = stringResource(R.string.description), value = scannedQR.description)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (processedTransaction == null) {
            // Buyer information form - Following HomeScreen design
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(CARD_CORNER_RADIUS)),
                shape = RoundedCornerShape(CARD_CORNER_RADIUS),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.buyer_information),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Buyer Document
                    OutlinedTextField(
                        value = buyerDocument,
                        onValueChange = onBuyerDocumentChanged,
                        label = { Text(stringResource(R.string.buyer_document)) },
                        placeholder = { Text(stringResource(R.string.buyer_document_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        isError = fieldErrors.containsKey("buyer_document")
                    )
                    fieldErrors["buyer_document"]?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Buyer Name
                    OutlinedTextField(
                        value = buyerName,
                        onValueChange = onBuyerNameChanged,
                        label = { Text(stringResource(R.string.buyer_name)) },
                        placeholder = { Text(stringResource(R.string.buyer_name_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        isError = fieldErrors.containsKey("buyer_name")
                    )
                    fieldErrors["buyer_name"]?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Buyer Phone
                    OutlinedTextField(
                        value = buyerPhone,
                        onValueChange = onBuyerPhoneChanged,
                        label = { Text(stringResource(R.string.buyer_phone)) },
                        placeholder = { Text(stringResource(R.string.buyer_phone_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        isError = fieldErrors.containsKey("buyer_phone")
                    )
                    fieldErrors["buyer_phone"]?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Buyer Email (optional)
                    OutlinedTextField(
                        value = buyerEmail,
                        onValueChange = onBuyerEmailChanged,
                        label = { Text(stringResource(R.string.buyer_email_optional)) },
                        placeholder = { Text(stringResource(R.string.buyer_email_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        isError = fieldErrors.containsKey("buyer_email")
                    )
                    fieldErrors["buyer_email"]?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Process button
                    Button(
                        onClick = onProcess,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = stringResource(R.string.confirm_payment),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    TextButton(
                        onClick = onClearScan,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.scan_another_qr))
                    }
                }
            }
        } else {
            // Show transaction result
            TransactionResultCard(
                transaction = processedTransaction,
                onNewScan = onClearScan
            )
        }
    }
}
