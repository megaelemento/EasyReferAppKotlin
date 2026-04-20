package com.christelldev.easyreferplus.ui.screens.qr

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.data.model.QRTransaction
import com.christelldev.easyreferplus.data.network.SaleNotificationData
import com.christelldev.easyreferplus.ui.theme.DesignConstants
import com.christelldev.easyreferplus.ui.viewmodel.QRViewModel
import com.christelldev.easyreferplus.ui.viewmodel.UserCompany
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableIntStateOf(if (hasActiveCompany) 0 else 1) }
    var showQRScanner by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // GESTIÓN DE PERMISOS DE CÁMARA
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showQRScanner = true
        } else {
            Toast.makeText(context, "Se requiere permiso de cámara para escanear", Toast.LENGTH_LONG).show()
        }
    }

    val requestCameraPermission = {
        val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
            showQRScanner = true
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // ESTADO AVANZADO DE MONTO (Banking Style)
    var amountTextFieldValue by remember {
        val initial = if (uiState.amount.isEmpty()) "0.00" else uiState.amount
        mutableStateOf(TextFieldValue(text = initial, selection = TextRange(initial.length)))
    }

    // Sincronizar el TextFieldValue cuando el estado externo cambie (ej. al limpiar)
    LaunchedEffect(uiState.amount) {
        val display = if (uiState.amount.isEmpty()) "0.00" else uiState.amount
        if (display != amountTextFieldValue.text) {
            amountTextFieldValue = TextFieldValue(text = display, selection = TextRange(display.length))
        }
    }

    val onAmountChanged = remember(viewModel) { { newValue: TextFieldValue ->
        val digits = newValue.text.filter { it.isDigit() }
        
        val newText = if (digits.isEmpty() || digits.toLong() == 0L) {
            "0.00"
        } else {
            val limitedDigits = if (digits.length > 9) digits.substring(0, 9) else digits
            val amountValue = limitedDigits.toLong() / 100.0
            String.format(java.util.Locale.US, "%.2f", amountValue)
        }
        
        // Siempre forzar el cursor al final
        amountTextFieldValue = TextFieldValue(
            text = newText,
            selection = TextRange(newText.length)
        )
        viewModel.updateAmount(newText)
    } }

    LaunchedEffect(companies) { viewModel.setCompanies(companies) }

    LaunchedEffect(uiState.qrPayload, uiState.generatedQR) {
        if (uiState.qrPayload != null || uiState.generatedQR != null) onGenerateQRSuccess()
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    if (showQRScanner) {
        QRScannerScreen(
            onQRCodeScanned = { code, secret ->
                viewModel.setScannedQRData(code, secret)
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    showQRScanner = false
                    viewModel.scanQR()
                }, 100)
            },
            onClose = { showQRScanner = false }
        )
        return
    }

    uiState.receipt?.let { receipt ->
        ReceiptScreen(receipt = receipt, onDone = { viewModel.dismissReceipt() }, onShare = { })
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Gradiente superior profundo que ocupa toda la parte superior incluyendo status bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                Color.Transparent
                            )
                        )
                    )
            )

            val contentColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface

            Column(modifier = Modifier.fillMaxSize()) {
                // TopAppBar manual con padding de status bar
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.qr_payments),
                            fontWeight = FontWeight.ExtraBold,
                            color = contentColor
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                null,
                                tint = contentColor
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    windowInsets = WindowInsets.statusBars
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(scrollState)
                        .imePadding()
                ) {
                    if (hasCompany) {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary,
                            divider = {},
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else Color.White
                                )
                            }
                        ) {
                            if (hasActiveCompany) {
                                Tab(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    text = {
                                        Text(
                                            stringResource(R.string.generate_qr),
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedTab == 0 && !isSystemInDarkTheme()) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                )
                            }
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = {
                                    Text(
                                        stringResource(R.string.scan_qr),
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTab == 1 && !isSystemInDarkTheme()) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    when {
                        uiState.qrPayload != null || uiState.generatedQR != null -> {
                            FullScreenQRDisplay(
                                qrBitmap = uiState.generatedQRBitmap,
                                amount = uiState.amountValue ?: uiState.generatedQR?.amount,
                                companyName = uiState.companyName ?: "Empresa",
                                onGenerateNew = { viewModel.clearGeneratedQR() }
                            )
                        }
                        hasCompany && selectedTab == 0 -> {
                            GenerateQRSection(
                                companies = companies,
                                selectedCompanyId = uiState.selectedCompanyId,
                                amountValue = amountTextFieldValue,
                                description = uiState.description,
                                fieldErrors = uiState.fieldErrors,
                                onCompanySelected = viewModel::updateSelectedCompany,
                                onAmountChanged = onAmountChanged,
                                onDescriptionChanged = viewModel::updateDescription,
                                onGenerateQR = viewModel::generateQR
                            )
                        }
                        else -> {
                            ScanQRSection(
                                showRegisterCompany = !hasCompany,
                                onRegisterCompany = onNavigateToCompany,
                                onScanWithCamera = { requestCameraPermission() }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    // Espacio para la barra de navegación
                    Spacer(modifier = Modifier.navigationBarsPadding())
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
    amountValue: TextFieldValue,
    description: String,
    fieldErrors: Map<String, String>,
    onCompanySelected: (Int?) -> Unit,
    onAmountChanged: (TextFieldValue) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onGenerateQR: () -> Unit
) {
    val selectedCompany = remember(companies, selectedCompanyId) { companies.find { it.id == selectedCompanyId } }
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Monto a recibir",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold
        )
        
        OutlinedTextField(
            value = amountValue,
            onValueChange = onAmountChanged,
            textStyle = MaterialTheme.typography.displayMedium.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = (-1).sp
            ),
            modifier = Modifier.fillMaxWidth(),
            prefix = { Text("$", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            singleLine = true
        )
        
        Box(modifier = Modifier.width(200.dp).height(2.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)))

        Spacer(modifier = Modifier.height(32.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (companies.size > 1) {
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = selectedCompany?.name ?: "Seleccionar Empresa",
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = { Icon(Icons.Default.Business, null, tint = MaterialTheme.colorScheme.primary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            companies.forEach { company ->
                                DropdownMenuItem(text = { Text(company.name) }, onClick = { onCompanySelected(company.id); expanded = false })
                            }
                        }
                    }
                } else if (companies.size == 1) {
                    Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), shape = RoundedCornerShape(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Business, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(companies.first().name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChanged,
                    label = { Text("Concepto (Opcional)") },
                    leadingIcon = { Icon(Icons.Default.Description, null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onGenerateQR,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(DesignConstants.BUTTON_CORNER_RADIUS),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = amountValue.text != "0.00" && (selectedCompanyId != null || companies.size == 1)
                ) {
                    Icon(Icons.Default.QrCode, null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Generar Cobro QR", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun FullScreenQRDisplay(
    qrBitmap: android.graphics.Bitmap?,
    amount: Double?,
    companyName: String,
    onGenerateNew: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(300.dp),
            shape = RoundedCornerShape(32.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            shadowElevation = 12.dp
        ) {
            Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                if (qrBitmap != null) {
                    Image(bitmap = qrBitmap.asImageBitmap(), contentDescription = "QR", modifier = Modifier.fillMaxSize())
                } else {
                    CircularProgressIndicator()
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(companyName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Text(
            text = "$${String.format(java.util.Locale.US, "%.2f", amount ?: 0.0)}",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(40.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onGenerateNew,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            ) {
                Icon(Icons.Default.Refresh, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nuevo", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Share, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Compartir", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ScanQRSection(
    showRegisterCompany: Boolean,
    onRegisterCompany: () -> Unit,
    onScanWithCamera: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Escanear para Pagar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Text(
                "Apunta tu cámara al código QR del comercio para realizar el pago de forma segura.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onScanWithCamera,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Abrir Cámara", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
            }
            
            if (showRegisterCompany) {
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = onRegisterCompany) {
                    Text("¿Eres un comercio? Registra tu empresa", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
