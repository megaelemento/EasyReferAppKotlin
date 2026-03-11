package com.christelldev.easyreferplus.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.data.model.TransactionHistoryItem
import com.christelldev.easyreferplus.ui.theme.AppBlue
import com.christelldev.easyreferplus.ui.viewmodel.HistoryTab
import com.christelldev.easyreferplus.ui.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

// Constants for consistent styling - Following HomeScreen design
private val CARD_CORNER_RADIUS = 20.dp
private val CARD_ELEVATION = 8.dp
private val CARD_MARGIN_HORIZONTAL = 16.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onTransactionClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val uiState by viewModel.uiState.collectAsState()

    // Extraer estados del ViewModel
    val transactions = uiState.transactions
    val isLoading = uiState.isLoading
    val totalSales = uiState.totalSales
    val totalPurchases = uiState.totalPurchases
    val currentTab = uiState.currentTab

    // Inicializar datos cuando la pantalla esté lista
    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    // Funciones de callback
    val onTabChange: (HistoryTab) -> Unit = { tab ->
        viewModel.setTab(tab)
    }
    val onRefresh: () -> Unit = {
        viewModel.loadHistory()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Historial",
                        color = MaterialTheme.colorScheme.surface,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = currentTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = currentTab == HistoryTab.ALL,
                    onClick = { onTabChange(HistoryTab.ALL) },
                    text = { Text("Todo (${totalSales + totalPurchases})") },
                    icon = { Icon(Icons.Default.List, contentDescription = null) }
                )
                Tab(
                    selected = currentTab == HistoryTab.SALES,
                    onClick = { onTabChange(HistoryTab.SALES) },
                    text = { Text("Ventas ($totalSales)") },
                    icon = { Icon(Icons.Default.Store, contentDescription = null) }
                )
                Tab(
                    selected = currentTab == HistoryTab.PURCHASES,
                    onClick = { onTabChange(HistoryTab.PURCHASES) },
                    text = { Text("Compras ($totalPurchases)") },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) }
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (transactions.isEmpty()) {
                EmptyHistoryContent(
                    isSalesTab = currentTab == HistoryTab.SALES,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(transactions) { transaction ->
                        HistoryTransactionCard(
                            transaction = transaction,
                            onClick = { onTransactionClick(transaction.transactionId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryTransactionCard(
    transaction: TransactionHistoryItem,
    onClick: () -> Unit
) {
    val isSale = transaction.type == "sale"
    val primaryColor = if (isSale) Color(0xFF4CAF50) else AppBlue

    // Following HomeScreen design
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(CARD_CORNER_RADIUS))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSale) Icons.Default.Store else Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = transaction.companyName ?: "Empresa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TypeBadge(isSale = isSale)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isSale) "Comprador: ${transaction.buyerName ?: "N/A"}" else "Vendedor: ${transaction.sellerName ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                transaction.transactionDate?.let { date ->
                    Text(
                        text = formatDate(date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // Amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${transaction.currency} ${String.format("%.2f", transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                if (isSale) {
                    Text(
                        text = "Venta",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4CAF50)
                    )
                } else {
                    Text(
                        text = "Compra",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun TypeBadge(isSale: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSale) Color(0xFF4CAF50).copy(alpha = 0.15f) else AppBlue.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = if (isSale) "Venta" else "Compra",
            style = MaterialTheme.typography.labelSmall,
            color = if (isSale) Color(0xFF4CAF50) else AppBlue,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun EmptyHistoryContent(
    isSalesTab: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sin transacciones",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isSalesTab)
                    "No has realizado ventas todavía"
                else
                    "No has realizado compras todavía",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: return dateString)
    } catch (e: Exception) {
        dateString
    }
}
