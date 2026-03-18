package com.christelldev.easyreferplus.ui.screens.wallet

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.data.model.WalletStatementItem
import com.christelldev.easyreferplus.ui.theme.DesignConstants
import com.christelldev.easyreferplus.ui.viewmodel.WalletViewModel
import com.christelldev.easyreferplus.util.BiometricHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: WalletViewModel = viewModel(),
    onNavigateToTransfer: () -> Unit,
    onNavigateToStatement: () -> Unit,
    onNavigateToSetPin: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    var pendingPin by remember { mutableStateOf<String?>(null) }
    var selectedItem by remember { mutableStateOf<WalletStatementItem?>(null) }

    LaunchedEffect(uiState.hasPinSet) {
        val pin = pendingPin
        if (uiState.hasPinSet && pin != null) {
            runCatching { BiometricHelper.storePin(context, pin) }
            pendingPin = null
        }
    }

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
                            text = "Mi Billetera",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )

                if (uiState.isLoading && !uiState.hasLoadedOnce) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(16.dp)) }

                        // BALANCE CARD STAR
                        item {
                            BalanceCard(
                                available = uiState.availableBalance,
                                total = uiState.totalBalance
                            )
                        }

                        item { Spacer(modifier = Modifier.height(24.dp)) }

                        // ACCIONES RÁPIDAS
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                WalletActionButton(
                                    label = "Transferir",
                                    icon = Icons.AutoMirrored.Filled.Send,
                                    color = MaterialTheme.colorScheme.primary,
                                    onClick = onNavigateToTransfer,
                                    modifier = Modifier.weight(1f)
                                )
                                WalletActionButton(
                                    label = "Movimientos",
                                    icon = Icons.Default.History,
                                    color = MaterialTheme.colorScheme.secondary,
                                    onClick = onNavigateToStatement,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item { Spacer(modifier = Modifier.height(32.dp)) }

                        // ÚLTIMOS MOVIMIENTOS
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Últimos movimientos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                                TextButton(onClick = onNavigateToStatement) {
                                    Text("Ver todo", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (uiState.statementItems.isEmpty()) {
                            item {
                                EmptyTransactionsState()
                            }
                        } else {
                            items(uiState.statementItems.take(5)) { txItem ->
                                TransactionItem(
                                    item = txItem,
                                    isDark = isDark,
                                    onClick = { selectedItem = txItem }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    selectedItem?.let { item ->
        TransactionDetailSheet(item = item, onDismiss = { selectedItem = null })
    }
}

@Composable
fun BalanceCard(available: Double, total: Double) {
    val pending = total - available
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
                Text("SALDO DISPONIBLE", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$${String.format(java.util.Locale.US, "%.2f", available)}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (pending > 0.01) {
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Saldo Total", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$${String.format(java.util.Locale.US, "%.2f", total)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Pendiente", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$${String.format(java.util.Locale.US, "%.2f", pending)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
        }
    }
}

@Composable
fun WalletActionButton(label: String, icon: ImageVector, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(60.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(label, fontWeight = FontWeight.ExtraBold, color = color, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun TransactionItem(item: WalletStatementItem, isDark: Boolean, onClick: () -> Unit) {
    val isSent = item.type == "sent"
    val accentColor = if (isSent) MaterialTheme.colorScheme.error else Color(0xFF10B981)
    val amountPrefix = if (isSent) "-$${String.format(java.util.Locale.US, "%.2f", item.amount)}" 
                       else "+$${String.format(java.util.Locale.US, "%.2f", item.amount)}"

    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isSent) Icons.AutoMirrored.Filled.CallMade else Icons.AutoMirrored.Filled.CallReceived,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.counterpartName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = if (item.counterpartPhone.startsWith("+593")) "0${item.counterpartPhone.drop(4)}" else item.counterpartPhone,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(amountPrefix, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black, color = accentColor)
                Text(item.createdAt.substringAfter("T").take(5), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
fun EmptyTransactionsState() {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.AutoMirrored.Filled.ReceiptLong, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Sin movimientos aún", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
