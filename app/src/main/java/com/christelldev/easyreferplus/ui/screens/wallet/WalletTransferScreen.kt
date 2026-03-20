package com.christelldev.easyreferplus.ui.screens.wallet

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.data.model.TransferResponse
import com.christelldev.easyreferplus.ui.theme.DesignConstants
import com.christelldev.easyreferplus.ui.viewmodel.WalletUiState
import com.christelldev.easyreferplus.ui.viewmodel.WalletViewModel
import com.christelldev.easyreferplus.util.BiometricHelper
import com.christelldev.easyreferplus.util.ContactsHelper
import com.christelldev.easyreferplus.util.DeviceContact
import com.christelldev.easyreferplus.util.WalletReceiptImageBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletTransferScreen(
    viewModel: WalletViewModel = viewModel(),
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as FragmentActivity
    var step by remember { mutableIntStateOf(1) }
    val isDark = isSystemInDarkTheme()
    var pendingPin by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.clearTransferSuccess()
        if (!uiState.hasLoadedOnce) viewModel.loadBalance()
    }

    // Inicializar PIN local si el usuario entra directo sin pasar por WalletScreen
    LaunchedEffect(uiState.hasLoadedOnce) {
        if (uiState.hasLoadedOnce) {
            val pinAlreadyStored = runCatching { BiometricHelper.isPinStored(context) }.getOrDefault(false)
            if (!pinAlreadyStored) {
                val newPin = BiometricHelper.generatePin()
                pendingPin = newPin
                viewModel.setPin(newPin)
            }
        }
    }

    LaunchedEffect(uiState.hasPinSet) {
        val pin = pendingPin
        if (uiState.hasPinSet && pin != null) {
            runCatching { BiometricHelper.storePin(context, pin) }
            pendingPin = null
        }
    }

    LaunchedEffect(uiState.recipientVerified) {
        if (uiState.recipientVerified && step == 1) {
            step = 2
            viewModel.clearRecipientVerified()
        }
    }

    LaunchedEffect(uiState.transferSuccess) {
        if (uiState.transferSuccess != null) step = 4
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (step != 4) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                        .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), Color.Transparent)))
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                if (step != 4) {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Nueva Transferencia",
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { if (step > 1) step-- else onBack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (step) {
                        1 -> StepOneContactPicker(
                            uiState = uiState,
                            context = context,
                            onCheckRecipient = { viewModel.checkAndSetRecipient(it) },
                            onDismissNotRegistered = { viewModel.dismissRecipientNotRegistered() }
                        )
                        2 -> StepTwoAmountForm(
                            uiState = uiState,
                            onContinue = { amount, desc ->
                                viewModel.updateAmount(amount)
                                viewModel.updateDescription(desc)
                                step = 3
                            }
                        )
                        3 -> StepThreeBiometric(
                            uiState = uiState,
                            onAuthenticate = {
                                BiometricHelper.showPrompt(
                                    activity = activity,
                                    onSuccess = {
                                        val pin = BiometricHelper.getStoredPin(context)
                                        if (pin != null) viewModel.executeTransferDirect(pin)
                                    },
                                    onError = {}
                                )
                            },
                            onClearError = { viewModel.clearError() }
                        )
                        4 -> StepFourSuccess(
                            transferResponse = uiState.transferSuccess,
                            onNewTransfer = {
                                viewModel.clearTransferSuccess()
                                viewModel.updateRecipientPhone("")
                                viewModel.updateAmount("0.00")
                                viewModel.updateDescription("")
                                step = 1
                            },
                            onGoToWallet = onSuccess
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepOneContactPicker(
    uiState: WalletUiState,
    context: Context,
    onCheckRecipient: (String) -> Unit,
    onDismissNotRegistered: () -> Unit
) {
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermission = it }
    
    var contacts by remember { mutableStateOf<List<DeviceContact>>(emptyList()) }
    var contactsLoaded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var manualPhone by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            scope.launch {
                contacts = ContactsHelper.loadContacts(context)
                contactsLoaded = true
            }
        }
    }

    val filteredContacts = remember(contacts, searchQuery) { ContactsHelper.filter(contacts, searchQuery).take(50) }

    if (uiState.recipientNotRegistered) {
        NotRegisteredScreen(onBack = onDismissNotRegistered)
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(16.dp)) }
        
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Transferencia Manual", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = manualPhone,
                        onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) manualPhone = it },
                        label = { Text("Número de teléfono") },
                        placeholder = { Text("0987654321") },
                        leadingIcon = { Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            if (uiState.isCheckingRecipient) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            else if (manualPhone.length == 10) {
                                IconButton(onClick = { onCheckRecipient(manualPhone) }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Go)
                    )
                    if (uiState.recipientCheckError != null) {
                        Text(uiState.recipientCheckError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                Icon(Icons.Default.Contacts, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Contactos de tu agenda", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
            }
        }

        if (!hasPermission) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                    onClick = { permissionLauncher.launch(Manifest.permission.READ_CONTACTS) }
                ) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Permitir acceso a contactos", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        } else {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar contacto...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }

            if (!contactsLoaded) {
                item { Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
            } else {
                items(filteredContacts) { contact ->
                    ContactRow(contact = contact, onClick = { onCheckRecipient(contact.phone) })
                }
            }
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun ContactRow(contact: DeviceContact, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = Color.Transparent
    ) {
        Row(modifier = Modifier.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(44.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(contact.name.take(1).uppercase(), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(contact.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(contact.phone, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun StepTwoAmountForm(
    uiState: WalletUiState,
    onContinue: (amount: String, desc: String) -> Unit
) {
    var desc by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // ESTADO AVANZADO DE MONTO (Banking Style QA)
    var amountTextFieldValue by remember { 
        mutableStateOf(TextFieldValue(
            text = if (uiState.amount == "0.00" || uiState.amount.isEmpty()) "0.00" else uiState.amount,
            selection = TextRange(uiState.amount.length)
        ))
    }

    val onAmountChanged = { newValue: TextFieldValue ->
        val digits = newValue.text.filter { it.isDigit() }
        val newText = if (digits.isEmpty() || digits.toLong() == 0L) "0.00"
        else {
            val value = digits.toLong() / 100.0
            String.format(java.util.Locale.US, "%.2f", value)
        }
        amountTextFieldValue = TextFieldValue(text = newText, selection = TextRange(newText.length))
    }

    val amountDouble = amountTextFieldValue.text.toDoubleOrNull() ?: 0.0
    val exceedsBalance = amountDouble > uiState.availableBalance
    val canContinue = amountDouble > 0 && !exceedsBalance

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(scrollState).imePadding()) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary) }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Enviar a", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(uiState.recipientName.ifBlank { uiState.recipientPhone }, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodyLarge)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Disponible", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$${String.format(java.util.Locale.US, "%.2f", uiState.availableBalance)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Monto a transferir", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = amountTextFieldValue,
                onValueChange = onAmountChanged,
                textStyle = MaterialTheme.typography.displayMedium.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Black, color = if (exceedsBalance) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("$", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent, cursorColor = MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Box(modifier = Modifier.width(200.dp).height(2.dp).background(if (exceedsBalance) MaterialTheme.colorScheme.error.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)))
            
            if (exceedsBalance) {
                Text("Saldo insuficiente", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 12.dp), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text("Concepto (Opcional)") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onContinue(amountTextFieldValue.text, desc) },
            enabled = canContinue,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(DesignConstants.BUTTON_CORNER_RADIUS),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Revisar Transferencia", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun StepThreeBiometric(
    uiState: WalletUiState,
    onAuthenticate: () -> Unit,
    onClearError: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Confirmar Operación", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(32.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            tonalElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("VALOR A ENVIAR", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("$${uiState.amount}", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(uiState.recipientName.ifBlank { uiState.recipientPhone }, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Icon(Icons.Default.Fingerprint, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Autenticación Requerida", fontWeight = FontWeight.Bold)
        Text("Confirma tu identidad para autorizar el envío", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.weight(1f))

        AnimatedVisibility(visible = uiState.transferError != null) {
            Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(uiState.transferError ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    IconButton(onClick = onClearError) { Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error) }
                }
            }
        }

        Button(
            onClick = onAuthenticate,
            enabled = !uiState.isTransferring,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (uiState.isTransferring) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else {
                Icon(Icons.Default.Security, null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Confirmar y Enviar", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StepFourSuccess(
    transferResponse: TransferResponse?,
    onNewTransfer: () -> Unit,
    onGoToWallet: () -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val scrollState = rememberScrollState()
    
    // Formateadores
    val amountFormat = java.text.DecimalFormat("$#,##0.00")
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("es-EC"))
    val now = dateFormat.format(java.util.Date())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDark) {
                        listOf(DesignConstants.SurfaceDark, DesignConstants.BackgroundDark)
                    } else {
                        DesignConstants.GradientSuccess
                    }
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Icono de Éxito con Animación de Escala (Simulada por elevación y forma)
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = if (isDark) DesignConstants.SuccessColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.25f),
                border = androidx.compose.foundation.BorderStroke(4.dp, Color.White.copy(alpha = 0.5f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "¡Envío Exitoso!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "La transferencia se ha procesado correctamente",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Card de Detalles (Comprobante) con Glassmorphism
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS),
                color = if (isDark) DesignConstants.SurfaceCardDark else Color.White,
                tonalElevation = DesignConstants.CARD_ELEVATION,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "COMPROBANTE DIGITAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDark) DesignConstants.TextSecondaryDark else DesignConstants.TextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = amountFormat.format(transferResponse?.amount ?: 0.0),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = if (isDark) DesignConstants.SuccessColor else DesignConstants.PrimaryDark
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Detalles de la transacción
                    DetailRow("Destinatario", transferResponse?.recipientName ?: "N/A", isDark)
                    DetailRow("Referencia", transferResponse?.id?.toString()?.take(12)?.uppercase() ?: "---", isDark)
                    DetailRow("Fecha", now, isDark)
                    DetailRow("Estado", "Completado", isDark, isStatus = true)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = { if (transferResponse != null) WalletReceiptImageBuilder.share(context, transferResponse) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) DesignConstants.PrimaryColor.copy(alpha = 0.15f) else Color(0xFFE3F2FD),
                            contentColor = if (isDark) DesignConstants.PrimaryColor else Color(0xFF1565C0)
                        ),
                        shape = RoundedCornerShape(DesignConstants.BUTTON_CORNER_RADIUS)
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Compartir Recibo", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
            
            // Botones de Acción Final
            Button(
                onClick = onGoToWallet,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(DesignConstants.BUTTON_CORNER_RADIUS),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = if (isDark) DesignConstants.BackgroundDark else DesignConstants.PrimaryDark
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text("Finalizar", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            TextButton(
                onClick = onNewTransfer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Hacer otra transferencia",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, isDark: Boolean, isStatus: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isDark) DesignConstants.TextSecondaryDark else DesignConstants.TextSecondary
        )
        
        if (isStatus) {
            Surface(
                color = DesignConstants.SuccessColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = value,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = DesignConstants.SuccessColor
                )
            }
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) DesignConstants.TextPrimaryDark else DesignConstants.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun NotRegisteredScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Surface(modifier = Modifier.size(100.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary) }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("Usuario no encontrado", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Text("Este número no está registrado en Enfoque Refer. ¡Invítalo a unirse y gana comisiones!", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(40.dp))
        Button(onClick = { /* Share App */ }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
            Text("Invitar Amigo", fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onBack) { Text("Probar con otro número") }
    }
}
