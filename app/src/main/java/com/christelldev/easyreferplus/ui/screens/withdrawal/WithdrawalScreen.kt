package com.christelldev.easyreferplus.ui.screens.withdrawal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.data.model.BankAccountResponse
import com.christelldev.easyreferplus.data.model.WithdrawalRequestResponse
import com.christelldev.easyreferplus.ui.theme.DesignConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawalScreen(
    availableBalance: Double,
    totalEarned: Double,
    totalWithdrawn: Double,
    pendingWithdrawal: Double,
    minimumWithdrawalAmount: Double,
    canRequestWithdrawal: Boolean,
    bankAccounts: List<BankAccountResponse>,
    selectedAccountId: Int?,
    withdrawalRequests: List<WithdrawalRequestResponse>,
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
    onConnectWebSocket: () -> Unit,
    onDisconnectWebSocket: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { onDisconnectWebSocket() }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Gradiente superior sutil
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp)
                    .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), Color.Transparent)))
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // Cabecera Premium
                TopAppBar(
                    title = {
                        Text(
                            text = "Retiros",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = onRefresh) {
                            Icon(Icons.Default.Refresh, null, tint = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(8.dp)) }

                        // BALANCE CARD STAR
                        item {
                            WithdrawalBalanceCard(
                                available = availableBalance,
                                earned = totalEarned,
                                withdrawn = totalWithdrawn,
                                pending = pendingWithdrawal,
                                minAmount = minimumWithdrawalAmount,
                                canRequest = canRequestWithdrawal,
                                onWithdrawClick = { showWithdrawDialog = true }
                            )
                        }

                        // CUENTAS BANCARIAS
                        item {
                            BankAccountsSection(
                                accounts = bankAccounts,
                                selectedId = selectedAccountId,
                                onSelect = onSelectAccount,
                                onAdd = { showAddAccountDialog = true }
                            )
                        }

                        // MENSAJES DE ESTADO
                        if (successMessage != null || errorMessage != null) {
                            item {
                                StatusMessageCard(
                                    message = successMessage ?: errorMessage ?: "",
                                    isError = errorMessage != null,
                                    onDismiss = onClearMessages
                                )
                            }
                        }

                        // HISTORIAL DE RETIROS
                        if (withdrawalRequests.isNotEmpty()) {
                            item {
                                Text("Historial de Retiros", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                            }
                            items(withdrawalRequests) { request ->
                                WithdrawalItem(request)
                            }
                        } else {
                            item { EmptyWithdrawalsState() }
                        }
                    }
                }
            }
        }

        if (showAddAccountDialog) {
            AddBankAccountDialog(
                isLoading = isCreatingAccount,
                onDismiss = { showAddAccountDialog = false },
                onConfirm = onCreateAccount
            )
        }

        if (showWithdrawDialog) {
            WithdrawDialog(
                balance = availableBalance,
                minAmount = minimumWithdrawalAmount,
                isLoading = isRequestingWithdrawal,
                onDismiss = { showWithdrawDialog = false },
                onConfirm = onRequestWithdrawal
            )
        }
    }
}

@Composable
fun WithdrawalBalanceCard(available: Double, earned: Double, withdrawn: Double, pending: Double, minAmount: Double, canRequest: Boolean, onWithdrawClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountBalanceWallet, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("DISPONIBLE PARA RETIRO", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$${String.format(java.util.Locale.US, "%.2f", available)}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (!canRequest && available > 0) {
                Text(
                    "Monto mínimo: $${String.format("%.2f", minAmount)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SmallStat("Ganado", earned, MaterialTheme.colorScheme.onSurface)
                SmallStat("Retirado", withdrawn, MaterialTheme.colorScheme.onSurface)
                SmallStat("Pendiente", pending, MaterialTheme.colorScheme.tertiary)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onWithdrawClick,
                enabled = canRequest,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            ) {
                Icon(Icons.Default.Payments, null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Solicitar Retiro de Fondos", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun SmallStat(label: String, value: Double, color: Color) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("$${String.format(java.util.Locale.US, "%.2f", value)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun BankAccountsSection(accounts: List<BankAccountResponse>, selectedId: Int?, onSelect: (Int) -> Unit, onAdd: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Mis Cuentas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                IconButton(onClick = onAdd, modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape).size(32.dp)) {
                    Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            if (accounts.isEmpty()) {
                Text("No tienes cuentas registradas.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                accounts.forEach { account ->
                    val isSelected = account.id == selectedId
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onSelect(account.id) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.AccountBalance, null, tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(account.bankName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(account.maskedAccountNumber ?: account.accountNumber, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WithdrawalItem(request: WithdrawalRequestResponse) {
    val statusColor = when (request.status.lowercase()) {
        "completed", "approved" -> Color(0xFF10B981)
        "pending" -> Color(0xFFF59E0B)
        else -> MaterialTheme.colorScheme.error
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = statusColor.copy(alpha = 0.1f)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.HistoryEdu, null, tint = statusColor, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("$${String.format(java.util.Locale.US, "%.2f", request.requestedAmount)}", fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyLarge)
                        Text(request.bankAccount?.bankName ?: "Banco", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Surface(color = statusColor.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text(request.status.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = statusColor, fontSize = 10.sp)
                }
            }
            Text(request.createdAt.take(10), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun StatusMessageCard(message: String, isError: Boolean, onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isError) MaterialTheme.colorScheme.errorContainer else Color(0xFF10B981).copy(alpha = 0.1f)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (isError) Icons.Default.Error else Icons.Default.CheckCircle, null, tint = if (isError) MaterialTheme.colorScheme.error else Color(0xFF10B981))
            Spacer(modifier = Modifier.width(12.dp))
            Text(message, color = if (isError) MaterialTheme.colorScheme.onErrorContainer else Color(0xFF10B981), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = if (isError) MaterialTheme.colorScheme.error else Color(0xFF10B981), modifier = Modifier.size(16.dp)) }
        }
    }
}

@Composable
fun EmptyWithdrawalsState() {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.History, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Sin retiros registrados", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBankAccountDialog(isLoading: Boolean, onDismiss: () -> Unit, onConfirm: (String, String, String, String, String) -> Unit) {
    var bank by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("ahorros") }
    var number by remember { mutableStateOf("") }
    var holder by remember { mutableStateOf("") }
    var doc by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Cuenta Bancaria", fontWeight = FontWeight.Black) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = bank, onValueChange = { bank = it }, label = { Text("Banco") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(value = type.uppercase(), onValueChange = {}, readOnly = true, label = { Text("Tipo") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("AHORROS") }, onClick = { type = "ahorros"; expanded = false })
                        DropdownMenuItem(text = { Text("CORRIENTE") }, onClick = { type = "corriente"; expanded = false })
                    }
                }
                OutlinedTextField(value = number, onValueChange = { number = it.filter { c -> c.isDigit() } }, label = { Text("Número de Cuenta") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = holder, onValueChange = { holder = it }, label = { Text("Titular") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = doc, onValueChange = { doc = it.filter { c -> c.isDigit() } }, label = { Text("Cédula/RUC") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(bank, type, number, holder, doc) }, enabled = !isLoading && bank.isNotBlank() && number.isNotBlank(), shape = RoundedCornerShape(12.dp)) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White) else Text("Guardar Cuenta", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
private fun WithdrawDialog(balance: Double, minAmount: Double, isLoading: Boolean, onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Solicitar Retiro", fontWeight = FontWeight.Black) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Disponible: $${String.format("%.2f", balance)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' }; error = null },
                    label = { Text("Monto a retirar") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), prefix = { Text("$") }, isError = error != null
                )
                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(10.0, 50.0, 100.0).forEach { val q = it; FilterChip(selected = amount == q.toString(), onClick = { amount = q.toInt().toString() }, label = { Text("$$q") }, shape = RoundedCornerShape(8.dp)) }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val v = amount.toDoubleOrNull()
                if (v == null || v < minAmount) error = "Mínimo $minAmount" else if (v > balance) error = "Saldo insuficiente" else onConfirm(v)
            }, enabled = !isLoading && amount.isNotBlank(), shape = RoundedCornerShape(12.dp)) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White) else Text("Confirmar Retiro", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        shape = RoundedCornerShape(28.dp)
    )
}
