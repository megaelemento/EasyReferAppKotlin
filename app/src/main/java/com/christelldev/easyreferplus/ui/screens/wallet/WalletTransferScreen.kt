package com.christelldev.easyreferplus.ui.screens.wallet

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.text.TextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.christelldev.easyreferplus.data.model.TransferResponse
import com.christelldev.easyreferplus.ui.viewmodel.WalletUiState
import com.christelldev.easyreferplus.ui.viewmodel.WalletViewModel
import com.christelldev.easyreferplus.util.BiometricHelper
import com.christelldev.easyreferplus.util.ContactsHelper
import com.christelldev.easyreferplus.util.DeviceContact
import com.christelldev.easyreferplus.util.WalletReceiptImageBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletTransferScreen(
    viewModel: WalletViewModel = viewModel(),
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as FragmentActivity
    var step by remember { mutableIntStateOf(1) }

    // Auto-advance to step 2 when recipient is verified
    LaunchedEffect(uiState.recipientVerified) {
        if (uiState.recipientVerified && step == 1) {
            step = 2
            viewModel.clearRecipientVerified()
        }
    }

    // Auto-advance to step 4 on transfer success
    LaunchedEffect(uiState.transferSuccess) {
        if (uiState.transferSuccess != null) {
            step = 4
        }
    }

    val isDarkTheme = isSystemInDarkTheme()
    val appBarBg = MaterialTheme.colorScheme.surface
    val appBarContent = MaterialTheme.colorScheme.onSurface

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (step) {
                            4 -> "Éxito"
                            else -> "Nueva Transferencia"
                        },
                        fontWeight = FontWeight.Bold,
                        color = appBarContent
                    )
                },
                navigationIcon = {
                    if (step != 4) {
                        IconButton(onClick = {
                            if (step > 1) step-- else onBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = appBarContent)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = appBarBg)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (step) {
                1 -> StepOneContactPicker(
                    uiState = uiState,
                    context = context,
                    onCheckRecipient = { phone -> viewModel.checkAndSetRecipient(phone) },
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
                        viewModel.updateAmount("")
                        viewModel.updateDescription("")
                        step = 1
                    },
                    onGoToWallet = onSuccess
                )
            }
        }
    }
}

// ─── STEP 1: Contact Picker ────────────────────────────────────────────────

@Composable
private fun StepOneContactPicker(
    uiState: WalletUiState,
    context: Context,
    onCheckRecipient: (String) -> Unit,
    onDismissNotRegistered: () -> Unit
) {
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    var contacts by remember { mutableStateOf<List<DeviceContact>>(emptyList()) }
    var contactsLoaded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var manualPhone by remember { mutableStateOf("") }
    var manualError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            contactsLoaded = false
            scope.launch {
                contacts = ContactsHelper.loadContacts(context)
                contactsLoaded = true
            }
        }
    }

    val filteredContacts = remember(contacts, searchQuery) {
        ContactsHelper.filter(contacts, searchQuery).take(100)
    }

    // "Not registered" full-screen overlay
    if (uiState.recipientNotRegistered) {
        NotRegisteredScreen(
            onBack = onDismissNotRegistered,
            onShare = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "¡Únete a Easy Refer y transfiere dinero fácilmente!\n" +
                                "Descarga la app: https://easyreferapp.com/download"
                    )
                }
                context.startActivity(Intent.createChooser(shareIntent, "Compartir Easy Refer"))
            }
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Manual phone entry
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Ingresa el número de teléfono",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = manualPhone,
                onValueChange = {
                    if (it.length <= 10 && it.all { c -> c.isDigit() }) {
                        manualPhone = it
                        manualError = null
                    }
                },
                label = { Text("Ej: 0987654321") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                trailingIcon = {
                    when {
                        uiState.isCheckingRecipient && manualPhone.length == 10 -> {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(12.dp).size(20.dp),
                                strokeWidth = 2.dp
                            )
                        }
                        manualPhone.length == 10 && manualPhone.startsWith("0") -> {
                            IconButton(onClick = {
                                manualError = null
                                onCheckRecipient(manualPhone)
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Continuar",
                                    tint = Color(0xFF2196F3)
                                )
                            }
                        }
                        manualPhone.isNotEmpty() -> {
                            IconButton(onClick = { manualPhone = ""; manualError = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = manualError != null
            )
            if (manualError != null) {
                Text(manualError!!, color = Color.Red, fontSize = 12.sp)
            }

            // Error from API check
            if (uiState.recipientCheckError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(uiState.recipientCheckError!!, color = Color.Red, fontSize = 12.sp)
            }
        }

        // Divider
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    "  o  ",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }
        }

        // Contact section header
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Contacts,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Selecciona un contacto de tu agenda",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (!hasPermission) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Contacts,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Permite el acceso a tus contactos para buscar destinatarios fácilmente",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.READ_CONTACTS) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Permitir acceso a contactos")
                        }
                    }
                }
            }
        } else {
            // Search field
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar por nombre o número") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Contact count hint
            if (contacts.isNotEmpty()) {
                item {
                    Text(
                        text = if (searchQuery.isBlank()) "${contacts.size} contactos" else "${filteredContacts.size} resultados",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Loading state (only while contacts haven't finished loading)
            if (!contactsLoaded) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                }
            } else if (contacts.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No se encontraron contactos en tu agenda", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    }
                }
            }

            // No results
            if (contacts.isNotEmpty() && filteredContacts.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("Sin resultados para \"$searchQuery\"", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Contact items
            items(filteredContacts) { contact ->
                ContactRow(
                    contact = contact,
                    isChecking = uiState.isCheckingRecipient,
                    onClick = { onCheckRecipient(contact.phone) }
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ContactRow(
    contact: DeviceContact,
    isChecking: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isChecking) { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFF2196F3).copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.name.firstOrNull()?.uppercase() ?: "?",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2196F3),
                fontSize = 18.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = contact.phone,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Default.Phone,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun NotRegisteredScreen(
    onBack: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFF3E0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = Color(0xFFFF9800),
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Tu contacto no tiene Easy Refer",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Para enviarle dinero, tu contacto necesita tener la app instalada y una cuenta activa.",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onShare,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
        ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Invitar a Easy Refer", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Buscar otro contacto", fontWeight = FontWeight.Medium)
        }
    }
}

// ─── STEP 2: Amount Form ───────────────────────────────────────────────────

@Composable
private fun StepTwoAmountForm(
    uiState: WalletUiState,
    onContinue: (amount: String, desc: String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()

    val bgColor = if (isDark) Color(0xFF0D1117) else Color(0xFFF8FAFF)
    val textPrimary = if (isDark) Color(0xFFE6EDF3) else Color(0xFF1E293B)
    val textHint = if (isDark) Color(0xFF484F58) else Color(0xFFCBD5E1)
    val textSecondary = if (isDark) Color(0xFF8B949E) else Color(0xFF64748B)
    val dividerColor = if (isDark) Color(0xFF21262D) else Color(0xFFEEF2F7)
    val progressBg = if (isDark) Color(0xFF21262D) else Color(0xFFE2E8F0)

    val amountDouble = amount.toDoubleOrNull()
    val exceedsBalance = amountDouble != null && amountDouble > uiState.availableBalance
    val isAmountValid = amountDouble != null && amountDouble > 0 && !exceedsBalance
    val progressRatio = if (amountDouble != null && uiState.availableBalance > 0)
        (amountDouble / uiState.availableBalance).coerceIn(0.0, 1.0).toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(scrollState)
            .background(bgColor)
    ) {
        // ── Header destinatario ───────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF1565C0), Color(0xFF2196F3))))
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Enviando a", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    if (uiState.recipientName.isNotBlank()) {
                        Text(uiState.recipientName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }
                    Text(uiState.recipientPhone, fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Disponible", fontSize = 10.sp, color = Color.White.copy(alpha = 0.65f))
                    Text(
                        "\$${String.format("%.2f", uiState.availableBalance)}",
                        fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White
                    )
                }
            }
        }

        // ── Entrada de monto grande y centrada ────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp, bottom = 16.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "¿Cuánto deseas enviar?",
                fontSize = 14.sp,
                color = textSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "$",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Light,
                    color = if (amount.isEmpty()) textHint else Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.width(6.dp))
                BasicTextField(
                    value = amount,
                    onValueChange = {
                        if (it.matches(Regex("^\\d{0,8}\\.?\\d{0,2}$"))) {
                            amount = it
                            errorMsg = null
                        }
                    },
                    textStyle = TextStyle(
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            exceedsBalance -> Color(0xFFEF4444)
                            else -> textPrimary
                        },
                        textAlign = TextAlign.Start
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box {
                            if (amount.isEmpty()) {
                                Text(
                                    "0.00",
                                    fontSize = 54.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textHint
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Barra de progreso del saldo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(progressBg)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressRatio)
                        .height(5.dp)
                        .background(
                            if (exceedsBalance) Color(0xFFEF4444) else Color(0xFF2196F3)
                        )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            when {
                exceedsBalance -> Text(
                    "Supera tu saldo disponible de \$${String.format("%.2f", uiState.availableBalance)}",
                    color = Color(0xFFEF4444), fontSize = 12.sp, textAlign = TextAlign.Center
                )
                isAmountValid -> Text(
                    "Monto valido",
                    color = Color(0xFF10B981), fontSize = 12.sp
                )
                else -> Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // ── Divisor ───────────────────────────────────────────
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(dividerColor))

        // ── Descripción ───────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text("Descripcion (opcional)") },
                leadingIcon = {
                    Icon(Icons.Default.Notes, contentDescription = null, tint = Color(0xFF94A3B8))
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 2
            )
        }

        if (errorMsg != null) {
            Text(
                errorMsg!!,
                color = Color(0xFFEF4444),
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Botón continuar ───────────────────────────────────
        Button(
            onClick = {
                when {
                    amountDouble == null || amountDouble <= 0 -> errorMsg = "Ingresa un monto valido"
                    amountDouble > uiState.availableBalance -> errorMsg = "Saldo insuficiente"
                    else -> { errorMsg = null; onContinue(amount, desc) }
                }
            },
            enabled = isAmountValid,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3),
                disabledContainerColor = Color(0xFFE2E8F0)
            )
        ) {
            Text(
                "Continuar",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isAmountValid) Color.White else Color(0xFF94A3B8)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ─── STEP 3: Biometric ────────────────────────────────────────────────────

@Composable
private fun StepThreeBiometric(
    uiState: WalletUiState,
    onAuthenticate: () -> Unit,
    onClearError: () -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF0D1117) else Color(0xFFF8FAFF)
    val cardBg = if (isDark) Color(0xFF161B22) else Color(0xFFF5F9FF)
    val textPrimary = if (isDark) Color(0xFFE6EDF3) else Color(0xFF1E293B)
    val textSecondary = if (isDark) Color(0xFF8B949E) else Color.Gray
    val errorCardBg = if (isDark) Color(0xFF3D1515) else Color(0xFFFEE2E2)
    val hasBiometrics = remember { BiometricHelper.canAuthenticate(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Enviando a", color = textSecondary)
                if (uiState.recipientName.isNotBlank()) {
                    Text(
                        uiState.recipientName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }
                Text(
                    uiState.recipientPhone,
                    fontSize = if (uiState.recipientName.isNotBlank()) 14.sp else 28.sp,
                    fontWeight = if (uiState.recipientName.isNotBlank()) FontWeight.Normal else FontWeight.Bold,
                    color = textSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "\$${String.format("%.2f", uiState.amount.toDoubleOrNull() ?: 0.0)}",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3)
                )
                if (uiState.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(uiState.description, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Color(0xFF2196F3).copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = null,
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Verifica tu identidad",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = textPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (hasBiometrics)
                "Usa tu huella, rostro o PIN del dispositivo\npara confirmar la transferencia"
            else
                "Usa el PIN, patrón o contraseña\nde tu dispositivo para confirmar",
            fontSize = 14.sp,
            color = textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        AnimatedVisibility(visible = uiState.transferError != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = errorCardBg)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiState.transferError ?: "",
                        color = Color(0xFFDC2626),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onClearError) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Red)
                    }
                }
            }
        }

        Button(
            onClick = onAuthenticate,
            enabled = !uiState.isTransferring,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
        ) {
            if (uiState.isTransferring) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (hasBiometrics) "Confirmar con biometría" else "Confirmar con PIN / patrón",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─── STEP 4: Success ──────────────────────────────────────────────────────

private fun formatReceiptDate(isoDate: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(isoDate) ?: return isoDate
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("es", "EC")).format(date)
    } catch (e: Exception) { isoDate.take(16).replace("T", " ") }
}

private fun buildShareText(transfer: TransferResponse): String {
    val date = formatReceiptDate(transfer.createdAt)
    val amount = String.format("%.2f", transfer.amount)
    val ref = transfer.id.toString().padStart(6, '0')
    return """
╔══════════════════════════╗
║       ENFOQUE REFER      ║
║    Comprobante de Pago   ║
╚══════════════════════════╝

  Referencia   #$ref
  Fecha        $date
  Estado       APROBADO

  ──────────────────────────
         MONTO ENVIADO
           $ $amount
  ──────────────────────────

  BENEFICIARIO
  Nombre     ${transfer.recipientName}
  Telefono   ${transfer.recipientPhone}

  ──────────────────────────
  Documento valido como
  constancia de transferencia
  ──────────────────────────

         Enfoque Refer
    """.trimIndent()
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF1E293B),
    valueBold: Boolean = false,
    textPrimary: Color = Color(0xFF1E293B)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color(0xFF94A3B8), fontSize = 13.sp)
        Text(
            value,
            fontWeight = if (valueBold) FontWeight.Bold else FontWeight.SemiBold,
            fontSize = 13.sp,
            color = if (valueColor == Color(0xFF1E293B)) textPrimary else valueColor
        )
    }
}

@Composable
private fun StepFourSuccess(
    transferResponse: TransferResponse?,
    onNewTransfer: () -> Unit,
    onGoToWallet: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()

    val bgColor = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surface
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textHint = MaterialTheme.colorScheme.outlineVariant
    val dividerColor = MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(bgColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Header gradiente ──────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF1565C0), Color(0xFF2196F3))))
                .padding(top = 36.dp, bottom = 32.dp, start = 24.dp, end = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(42.dp))
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text("¡Transferencia exitosa!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "\$${String.format("%.2f", transferResponse?.amount ?: 0.0)}",
                    fontSize = 46.sp, fontWeight = FontWeight.Bold, color = Color.White
                )
                Text("enviados", fontSize = 13.sp, color = Color.White.copy(alpha = 0.75f))
            }
        }

        // ── Comprobante ───────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = (-16).dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // Cabecera del comprobante
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Comprobante", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textPrimary)
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFD1FAE5), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text("Completado", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "N° ${transferResponse?.id ?: "—"}",
                    fontSize = 12.sp,
                    color = textHint
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Línea divisoria
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(dividerColor))

                Spacer(modifier = Modifier.height(14.dp))

                ReceiptRow("Destinatario", transferResponse?.recipientName ?: "—", textPrimary = textPrimary)
                Spacer(modifier = Modifier.height(10.dp))
                ReceiptRow("Teléfono", transferResponse?.recipientPhone ?: "—", textPrimary = textPrimary)
                Spacer(modifier = Modifier.height(10.dp))
                ReceiptRow(
                    "Fecha",
                    if (transferResponse != null) formatReceiptDate(transferResponse.createdAt) else "—",
                    textPrimary = textPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(dividerColor))
                Spacer(modifier = Modifier.height(14.dp))

                // Monto destacado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Monto transferido", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    Text(
                        "\$${String.format("%.2f", transferResponse?.amount ?: 0.0)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Botón compartir comprobante (imagen)
                val shareBg = if (isDark) Color(0xFF1C2D3F) else Color(0xFFE3F2FD)
                Button(
                    onClick = {
                        if (transferResponse != null) {
                            WalletReceiptImageBuilder.share(context, transferResponse)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = shareBg)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compartir comprobante", color = Color(0xFF2196F3), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Botones principales ───────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onGoToWallet,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Text("Ir a mi billetera", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            OutlinedButton(
                onClick = onNewTransfer,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Nueva transferencia", fontSize = 14.sp)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
