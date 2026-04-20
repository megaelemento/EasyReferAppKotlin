package com.christelldev.easyreferplus.ui.screens.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.data.model.CompanyPayment
import com.christelldev.easyreferplus.ui.theme.DesignConstants
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyPaymentsScreen(
    companyName: String,
    pendingAmount: Double,
    payments: List<CompanyPayment>,
    isLoading: Boolean,
    isRegistering: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onRegisterPayment: (String, String, Double, String?) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    onClearMessages: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearMessages()
        }
    }

    // Form state
    var documentNumber by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // ESTADO AVANZADO DE MONTO (Banking Style)
    var amountTextFieldValue by remember { 
        mutableStateOf(TextFieldValue(text = "0.00", selection = TextRange(4)))
    }

    val onAmountChanged = { newValue: TextFieldValue ->
        val digits = newValue.text.filter { it.isDigit() }
        val newText = if (digits.isEmpty() || digits.toLong() == 0L) "0.00"
        else {
            val value = digits.toLong() / 100.0
            String.format(Locale.US, "%.2f", value)
        }
        amountTextFieldValue = TextFieldValue(text = newText, selection = TextRange(newText.length))
    }

    // Show success dialog
    var showSuccessDialog by remember { mutableStateOf(false) }
    var lastSuccessAmount by remember { mutableStateOf(0.0) }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            onClearMessages()
            lastSuccessAmount = amountTextFieldValue.text.toDoubleOrNull() ?: 0.0
            showSuccessDialog = true
            documentNumber = ""
            bankName = ""
            amountTextFieldValue = TextFieldValue(text = "0.00", selection = TextRange(4))
            notes = ""
        }
    }

    // Success Dialog (Elegant Style)
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = DesignConstants.SuccessColor.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CheckCircle, null, tint = DesignConstants.SuccessColor, modifier = Modifier.size(48.dp))
                    }
                }
            },
            title = {
                Text("¡Pago Reportado!", fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Se ha registrado tu pago por", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                    Text("$${String.format(Locale.US, "%.2f", lastSuccessAmount)}", fontWeight = FontWeight.Black, color = DesignConstants.SuccessColor, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Pendiente de verificación administrativa.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DesignConstants.SuccessColor),
                    shape = RoundedCornerShape(DesignConstants.BUTTON_CORNER_RADIUS)
                ) {
                    Text("Entendido", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = if (isDark) DesignConstants.SurfaceCardDark else Color.White,
            shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS)
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
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

            val contentColor = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White

            Column(modifier = Modifier.fillMaxSize()) {
                // TopAppBar manual con padding de status bar
                TopAppBar(
                    title = {
                        Text(
                            text = "Pago de Comisiones",
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
                    actions = {
                        IconButton(onClick = onRefresh) {
                            Icon(
                                Icons.Default.Refresh,
                                null,
                                tint = contentColor
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    windowInsets = WindowInsets.statusBars
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Resumen de Empresa y Deuda
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 4.dp,
                            shadowElevation = 8.dp
                        ) {
                            Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(56.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Business, null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        companyName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "Deuda de Comisiones",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "$${String.format(Locale.US, "%.2f", pendingAmount)}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }

                    // Formulario Estilo Bancario
                    item {
                        ElegantSectionHeader("Reportar Pago Realizado", isDark)
                    }

                    if (pendingAmount <= 0.0) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 2.dp
                            ) {
                                Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(48.dp), tint = DesignConstants.SuccessColor)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Sin comisiones pendientes", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Esta empresa no tiene deuda de comisiones por el momento.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    } else {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 2.dp
                            ) {
                                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    // Campo de Monto Protagonista
                                    Text("Monto Transferido", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    OutlinedTextField(
                                        value = amountTextFieldValue,
                                        onValueChange = onAmountChanged,
                                        textStyle = MaterialTheme.typography.displayMedium.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.fillMaxWidth(),
                                        prefix = { Text("$", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent, cursorColor = MaterialTheme.colorScheme.primary),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    Box(modifier = Modifier.width(180.dp).height(2.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)))

                                    val finalAmount = amountTextFieldValue.text.toDoubleOrNull() ?: 0.0
                                    if (finalAmount > pendingAmount) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "El monto no puede superar $${String.format(Locale.US, "%.2f", pendingAmount)}",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.labelSmall,
                                            textAlign = TextAlign.Center
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(32.dp))

                                    // Otros campos
                                    ElegantTextField(value = documentNumber, onValueChange = { documentNumber = it }, label = "Nº de Comprobante", icon = Icons.Default.Description, isDark = isDark)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    ElegantTextField(value = bankName, onValueChange = { bankName = it }, label = "Banco / Método de Pago", icon = Icons.Default.AccountBalance, isDark = isDark)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    ElegantTextField(value = notes, onValueChange = { notes = it }, label = "Notas adicionales", icon = Icons.AutoMirrored.Filled.Notes, isDark = isDark)

                                    Spacer(modifier = Modifier.height(32.dp))

                                    val canSubmit = !isRegistering && documentNumber.isNotBlank() && bankName.isNotBlank() && finalAmount > 0 && finalAmount <= pendingAmount

                                    Button(
                                        onClick = { onRegisterPayment(documentNumber, bankName, finalAmount, notes.ifBlank { null }) },
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        shape = RoundedCornerShape(DesignConstants.BUTTON_CORNER_RADIUS),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        enabled = canSubmit
                                    ) {
                                        if (isRegistering) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                                        else {
                                            Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text("ENVIAR REPORTE", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Historial
                    item { ElegantSectionHeader("Historial Reciente", isDark) }

                    if (isLoading) {
                        item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) } }
                    } else if (payments.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ) {
                                Column(modifier = Modifier.padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.AutoMirrored.Filled.ReceiptLong, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Sin pagos registrados", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else {
                        items(payments) { payment ->
                            ElegantPaymentItem(payment = payment, isDark = isDark)
                        }
                    }
                    // Espacio para la barra de navegación
                    item { Spacer(modifier = Modifier.navigationBarsPadding().height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ElegantSectionHeader(title: String, isDark: Boolean) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Black,
        color = if (isDark) Color.White else DesignConstants.TextPrimary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun ElegantTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isDark: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = DesignConstants.PrimaryColor, modifier = Modifier.size(20.dp)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DesignConstants.PrimaryColor,
            unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f),
            focusedLabelColor = DesignConstants.PrimaryColor
        )
    )
}

@Composable
private fun ElegantPaymentItem(payment: CompanyPayment, isDark: Boolean) {
    val statusColor = when (payment.status) {
        "verified" -> DesignConstants.SuccessColor
        "rejected" -> DesignConstants.ErrorColor
        else -> DesignConstants.WarningColor
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) DesignConstants.SurfaceCardDark else Color.White,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(14.dp), color = statusColor.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) { Icon(if (payment.status == "verified") Icons.Default.CheckCircle else Icons.Default.AccessTime, null, tint = statusColor, modifier = Modifier.size(24.dp)) }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("$${String.format(Locale.US, "%.2f", payment.amount)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = if (isDark) DesignConstants.TextPrimaryDark else DesignConstants.TextPrimary)
                Text("${payment.bankName} • ${payment.documentNumber}", style = MaterialTheme.typography.bodySmall, color = DesignConstants.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.15f)) {
                Text(
                    text = when (payment.status) { "verified" -> "Verificado"; "rejected" -> "Rechazado"; else -> "Pendiente" },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = statusColor
                )
            }
        }
    }
}
