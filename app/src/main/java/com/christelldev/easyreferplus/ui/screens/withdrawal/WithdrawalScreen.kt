package com.christelldev.easyreferplus.ui.screens.withdrawal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.ui.theme.AppBlue
import com.christelldev.easyreferplus.ui.theme.AppGreen
import com.christelldev.easyreferplus.ui.theme.AppOrange

// Constantes de diseño elegante
private val CARD_CORNER_RADIUS = 20.dp
private val CARD_ELEVATION = 8.dp
private val CARD_MARGIN_HORIZONTAL = 16.dp
private val GradientPrimary = listOf(Color(0xFF03A9F4), Color(0xFF2196F3))
private val GradientSuccess = listOf(Color(0xFF10B981), Color(0xFF34D399))
private val GradientOrange = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawalScreen(
    availableBalance: Double,
    totalEarned: Double,
    totalWithdrawn: Double,
    pendingWithdrawal: Double,
    minimumWithdrawalAmount: Double,
    canRequestWithdrawal: Boolean,
    bankAccounts: List<com.christelldev.easyreferplus.data.model.BankAccountResponse>,
    selectedAccountId: Int?,
    withdrawalRequests: List<com.christelldev.easyreferplus.data.model.WithdrawalRequestResponse>,
    isLoading: Boolean,
    isCreatingAccount: Boolean,
    isRequestingWithdrawal: Boolean,
    successMessage: String?,
    errorMessage: String?,
    onSelectAccount: (Int) -> Unit,
    onCreateAccount: (String, String, String, String, String) -> Unit,
    onRequestWithdrawal: (Double) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    onClearMessages: () -> Unit,
    // WebSocket
    onConnectWebSocket: () -> Unit,
    onDisconnectWebSocket: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    // WebSocket: desconectar al salir (el connect lo maneja el LaunchedEffect del padre)
    DisposableEffect(Unit) {
        onDispose {
            onDisconnectWebSocket()
        }
    }

    // Mostrar mensajes
    LaunchedEffect(successMessage, errorMessage) {
        // Los mensajes se muestran en la UI
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Retiros",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBlue,
                    titleContentColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.surface,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AppBlue
                )
            } else {
                WithdrawalContent(
                    availableBalance = availableBalance,
                    totalEarned = totalEarned,
                    totalWithdrawn = totalWithdrawn,
                    pendingWithdrawal = pendingWithdrawal,
                    minimumWithdrawalAmount = minimumWithdrawalAmount,
                    canRequestWithdrawal = canRequestWithdrawal,
                    bankAccounts = bankAccounts,
                    selectedAccountId = selectedAccountId,
                    withdrawalRequests = withdrawalRequests,
                    successMessage = successMessage,
                    errorMessage = errorMessage,
                    onSelectAccount = onSelectAccount,
                    onAddAccountClick = { showAddAccountDialog = true },
                    onWithdrawClick = { showWithdrawDialog = true },
                    onClearMessages = onClearMessages
                )
            }
        }

        // Diálogo para agregar cuenta bancaria
        if (showAddAccountDialog) {
            AddBankAccountDialog(
                isLoading = isCreatingAccount,
                onDismiss = { showAddAccountDialog = false },
                onConfirm = { bankName, accountType, accountNumber, holderName, document ->
                    onCreateAccount(bankName, accountType, accountNumber, holderName, document)
                    showAddAccountDialog = false
                }
            )
        }

        // Diálogo para solicitar retiro
        if (showWithdrawDialog) {
            WithdrawDialog(
                availableBalance = availableBalance,
                minimumAmount = minimumWithdrawalAmount,
                isLoading = isRequestingWithdrawal,
                onDismiss = { showWithdrawDialog = false },
                onConfirm = { amount ->
                    onRequestWithdrawal(amount)
                    showWithdrawDialog = false
                }
            )
        }
    }
}

@Composable
private fun WithdrawalContent(
    availableBalance: Double,
    totalEarned: Double,
    totalWithdrawn: Double,
    pendingWithdrawal: Double,
    minimumWithdrawalAmount: Double,
    canRequestWithdrawal: Boolean,
    bankAccounts: List<com.christelldev.easyreferplus.data.model.BankAccountResponse>,
    selectedAccountId: Int?,
    withdrawalRequests: List<com.christelldev.easyreferplus.data.model.WithdrawalRequestResponse>,
    successMessage: String?,
    errorMessage: String?,
    onSelectAccount: (Int) -> Unit,
    onAddAccountClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onClearMessages: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = CARD_MARGIN_HORIZONTAL),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mensajes de éxito/error
        if (successMessage != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AppGreen.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = successMessage,
                            color = AppGreen,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onClearMessages) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = AppGreen)
                        }
                    }
                }
            }
        }

        if (errorMessage != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onClearMessages) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
        }

        // Balance Card
        item {
            BalanceCard(
                availableBalance = availableBalance,
                totalEarned = totalEarned,
                totalWithdrawn = totalWithdrawn,
                pendingWithdrawal = pendingWithdrawal,
                canRequestWithdrawal = canRequestWithdrawal,
                minimumWithdrawalAmount = minimumWithdrawalAmount,
                onWithdrawClick = onWithdrawClick
            )
        }

        // Cuentas bancarias
        item {
            BankAccountsSection(
                bankAccounts = bankAccounts,
                selectedAccountId = selectedAccountId,
                onSelectAccount = onSelectAccount,
                onAddAccount = onAddAccountClick
            )
        }

        // Historial de retiros
        if (withdrawalRequests.isNotEmpty()) {
            item {
                Text(
                    text = "Historial de Retiros",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(withdrawalRequests) { request ->
                WithdrawalRequestCard(request = request)
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun BalanceCard(
    availableBalance: Double,
    totalEarned: Double,
    totalWithdrawn: Double,
    pendingWithdrawal: Double,
    canRequestWithdrawal: Boolean,
    minimumWithdrawalAmount: Double,
    onWithdrawClick: () -> Unit
) {
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
                .background(brush = Brush.horizontalGradient(colors = GradientPrimary))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Disponible para Retirar",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$${String.format("%.2f", availableBalance)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.surface
                )

                if (!canRequestWithdrawal && availableBalance > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Monto mínimo: $${String.format("%.2f", minimumWithdrawalAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Ganado", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        Text("$${String.format("%.2f", totalEarned)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.surface)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Retirado", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        Text("$${String.format("%.2f", totalWithdrawn)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.surface)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Pendiente", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        Text("$${String.format("%.2f", pendingWithdrawal)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFFFB74D))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onWithdrawClick,
                    enabled = canRequestWithdrawal,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f).copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = if (canRequestWithdrawal) AppGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Solicitar Retiro",
                        color = if (canRequestWithdrawal) AppGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun BankAccountsSection(
    bankAccounts: List<com.christelldev.easyreferplus.data.model.BankAccountResponse>,
    selectedAccountId: Int?,
    onSelectAccount: (Int) -> Unit,
    onAddAccount: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(CARD_CORNER_RADIUS)),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cuentas Bancarias",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onAddAccount) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agregar")
                }
            }

            if (bankAccounts.isEmpty()) {
                Text(
                    text = "No tienes cuentas registradas. Agrega una cuenta para solicitar retiros.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                bankAccounts.forEach { account ->
                    val isSelected = account.id == selectedAccountId
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSelectAccount(account.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) AppBlue.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = account.bankName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${account.accountType}: ${account.maskedAccountNumber ?: account.accountNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = account.accountHolderName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Seleccionada", tint = AppBlue)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WithdrawalRequestCard(request: com.christelldev.easyreferplus.data.model.WithdrawalRequestResponse) {
    val statusColor = when (request.status.lowercase()) {
        "completed", "approved" -> AppGreen
        "pending" -> AppOrange
        "rejected", "cancelled" -> Color.Red
        "postponed" -> Color(0xFF2196F3) // Blue
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }

    val statusLabel = when (request.status.lowercase()) {
        "completed" -> stringResource(R.string.completed)
        "approved" -> stringResource(R.string.approved)
        "pending" -> stringResource(R.string.pending)
        "rejected" -> stringResource(R.string.rejected)
        "cancelled" -> stringResource(R.string.cancelled)
        "postponed" -> stringResource(R.string.postponed)
        else -> request.status.uppercase()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$${String.format("%.2f", request.requestedAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = request.bankAccount?.bankName ?: "Cuenta bancaria",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = request.createdAt.take(10),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = statusColor.copy(alpha = 0.1f),
                        labelColor = statusColor
                    )
                )
            }

            // Mostrar motivo de rechazo o notas si existen
            if (!request.rejectionReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${stringResource(R.string.reason)}: ${request.rejectionReason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Red.copy(alpha = 0.7f)
                )
            }

            if (!request.reviewNotes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${stringResource(R.string.notes)}: ${request.reviewNotes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Mostrar referencia de transacción si existe (aprobado/completado)
            if (!request.transactionReference.isNullOrBlank() && (request.status.lowercase() == "approved" || request.status.lowercase() == "completed")) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${stringResource(R.string.transaction_reference)}: ${request.transactionReference}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppGreen
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBankAccountDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String) -> Unit
) {
    var bankName by remember { mutableStateOf("") }
    var accountType by remember { mutableStateOf("ahorros") }
    var accountNumber by remember { mutableStateOf("") }
    var holderName by remember { mutableStateOf("") }
    var document by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Cuenta Bancaria") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Nombre del Banco") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = if (accountType == "ahorros") "Ahorros" else "Corriente",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Tipo de Cuenta") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ahorros") },
                            onClick = {
                                accountType = "ahorros"
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Corriente") },
                            onClick = {
                                accountType = "corriente"
                                expanded = false
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it.filter { c -> c.isDigit() } },
                    label = { Text("Número de Cuenta") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedTextField(
                    value = holderName,
                    onValueChange = { holderName = it },
                    label = { Text("Nombre del Titular") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = document,
                    onValueChange = { document = it.filter { c -> c.isDigit() } },
                    label = { Text("Cédula/RUC del Titular") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(bankName, accountType, accountNumber, holderName, document)
                },
                enabled = !isLoading && bankName.isNotBlank() && accountNumber.isNotBlank() &&
                        holderName.isNotBlank() && document.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.surface
                    )
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun WithdrawDialog(
    availableBalance: Double,
    minimumAmount: Double,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Solicitar Retiro") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Disponible: $${String.format("%.2f", availableBalance)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "Monto mínimo: $${String.format("%.2f", minimumAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it.filter { c -> c.isDigit() || c == '.' }
                        error = null
                    },
                    label = { Text("Monto a retirar") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    prefix = { Text("$") },
                    isError = error != null
                )

                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Botones de monto rápido
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(10.0, 25.0, 50.0, 100.0).forEach { quickAmount ->
                        if (quickAmount <= availableBalance && quickAmount >= minimumAmount) {
                            FilterChip(
                                selected = amount.toDoubleOrNull() == quickAmount,
                                onClick = { amount = quickAmount.toInt().toString() },
                                label = { Text("$${quickAmount.toInt()}") }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    when {
                        amountValue == null -> error = "Monto inválido"
                        amountValue < minimumAmount -> error = "Monto mínimo: $${String.format("%.2f", minimumAmount)}"
                        amountValue > availableBalance -> error = "No tienes suficiente saldo"
                        else -> onConfirm(amountValue)
                    }
                },
                enabled = !isLoading && amount.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.surface
                    )
                } else {
                    Text("Confirmar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
