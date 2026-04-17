package com.christelldev.easyreferplus.ui.screens.history

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.data.model.TransactionHistoryItem
import com.christelldev.easyreferplus.ui.theme.DesignConstants
import com.christelldev.easyreferplus.ui.viewmodel.HistoryTab
import com.christelldev.easyreferplus.ui.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onTransactionClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()

    LaunchedEffect(Unit) {
        viewModel.initialize()
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
                            text = "Historial",
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
                        IconButton(onClick = { viewModel.loadHistory() }) {
                            Icon(Icons.Default.Refresh, null, tint = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )

                // Tabs Modernas
                TabRow(
                    selectedTabIndex = uiState.currentTab.ordinal,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.currentTab.ordinal]),
                            color = if (isDark) MaterialTheme.colorScheme.primary else Color.White
                        )
                    }
                ) {
                    HistoryTabItem(
                        selected = uiState.currentTab == HistoryTab.ALL,
                        label = "Todo",
                        count = uiState.totalSales + uiState.totalPurchases,
                        onClick = { viewModel.setTab(HistoryTab.ALL) },
                        isDark = isDark
                    )
                    HistoryTabItem(
                        selected = uiState.currentTab == HistoryTab.SALES,
                        label = "Ventas",
                        count = uiState.totalSales,
                        onClick = { viewModel.setTab(HistoryTab.SALES) },
                        isDark = isDark
                    )
                    HistoryTabItem(
                        selected = uiState.currentTab == HistoryTab.PURCHASES,
                        label = "Compras",
                        count = uiState.totalPurchases,
                        onClick = { viewModel.setTab(HistoryTab.PURCHASES) },
                        isDark = isDark
                    )
                }

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (uiState.transactions.isEmpty()) {
                    EmptyHistoryState(uiState.currentTab == HistoryTab.SALES)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.transactions, key = { it.transactionId }) { transaction ->
                            HistoryTransactionCardPremium(
                                transaction = transaction,
                                onClick = { onTransactionClick(transaction.transactionId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryTabItem(selected: Boolean, label: String, count: Int, onClick: () -> Unit, isDark: Boolean) {
    Tab(
        selected = selected,
        onClick = onClick,
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (selected && !isDark) Color.White else MaterialTheme.colorScheme.onSurface
                )
                if (count > 0) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = if (selected && !isDark) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = count.toString(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (selected && !isDark) Color.White else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun HistoryTransactionCardPremium(
    transaction: TransactionHistoryItem,
    onClick: () -> Unit
) {
    val isSale = transaction.type == "sale"
    val accentColor = if (isSale) Color(0xFF10B981) else MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de Flujo
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isSale) Icons.AutoMirrored.Filled.TrendingUp else Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información Central
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.companyName ?: "Empresa",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isSale) "Cliente: ${transaction.buyerName ?: "N/A"}" else "Vendedor: ${transaction.sellerName ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val formattedDate = remember(transaction.transactionDate) { formatDate(transaction.transactionDate ?: "") }
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Monto y Tipo
            Column(horizontalAlignment = Alignment.End) {
                val formattedAmount = remember(transaction.amount, transaction.currency) {
                    "${transaction.currency} ${String.format(java.util.Locale.US, "%.2f", transaction.amount)}"
                }
                Text(
                    text = formattedAmount,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )
                Surface(
                    color = accentColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = if (isSale) "VENTA" else "COMPRA",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryState(isSalesTab: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Sin movimientos",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black
        )
        Text(
            text = if (isSalesTab) "Aún no has registrado ventas en tu historial." else "Aún no has realizado compras en nuestra red.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM, yyyy • HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: return dateString)
    } catch (e: Exception) {
        dateString
    }
}
