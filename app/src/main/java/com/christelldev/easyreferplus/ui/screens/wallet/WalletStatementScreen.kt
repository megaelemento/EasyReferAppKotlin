package com.christelldev.easyreferplus.ui.screens.wallet

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.christelldev.easyreferplus.data.model.WalletStatementItem
import com.christelldev.easyreferplus.ui.viewmodel.WalletViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletStatementScreen(
    viewModel: WalletViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var selectedFilter by remember { mutableStateOf("Todo") }

    // Cargar datos al inicio
    LaunchedEffect(Unit) {
        viewModel.loadStatement(refresh = true)
    }

    // Infinite scroll
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= uiState.statementItems.size - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && uiState.hasMoreItems && !uiState.isLoadingStatement) {
            viewModel.loadMoreStatement()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estado de Cuenta", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filtros
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("Todo", "Este mes", "Esta semana", "Hoy")
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = {
                            selectedFilter = filter
                            val (start, end) = getDateRangeForFilter(filter)
                            viewModel.loadStatement(refresh = true, startDate = start, endDate = end)
                        },
                        label = { Text(filter) },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Contenido principal
            if (uiState.isLoadingStatement && uiState.statementItems.isEmpty()) {
                LoadingSkeletons()
            } else if (uiState.statementItems.isEmpty()) {
                EmptyState()
            } else {
                // Resumen
                val summary = calculateSummary(uiState.statementItems)
                SummaryCard(
                    totalSent = summary.first,
                    totalReceived = summary.second,
                    netBalance = summary.third
                )

                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Agrupar por fecha
                    val groupedItems = uiState.statementItems.groupBy { item ->
                        getDateGroup(item.createdAt)
                    }

                    groupedItems.forEach { (dateLabel, items) ->
                        item {
                            Text(
                                text = dateLabel,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        items(items.size) { index ->
                            val item = items[index]
                            StatementItemCard(item = item)
                        }
                    }

                    // Loading indicator al final
                    if (uiState.isLoadingStatement && uiState.statementItems.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingSkeletons() {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse),
        label = "alpha"
    )
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha))
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AccountBalanceWallet,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("No tienes transferencias aún", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Tus movimientos aparecerán aquí", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Center)
    }
}

private fun calculateSummary(items: List<WalletStatementItem>): Triple<Double, Double, Double> {
    val sent = items.filter { it.type == "sent" }.sumOf { it.amount }
    val received = items.filter { it.type == "received" }.sumOf { it.amount }
    return Triple(sent, received, received - sent)
}

@Composable
private fun SummaryCard(totalSent: Double, totalReceived: Double, netBalance: Double) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Enviado", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("-${"%.2f".format(totalSent)}", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Recibido", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("+${"%.2f".format(totalReceived)}", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Neto", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val color = if (netBalance >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                Text("${"%.2f".format(netBalance)}", fontWeight = FontWeight.Bold, color = color)
            }
        }
    }
}

private fun getDateRangeForFilter(filter: String): Pair<String?, String?> {
    if (filter == "Todo") return Pair(null, null)
    val iso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }
    val cal = Calendar.getInstance()
    val end = iso.format(cal.time)
    when (filter) {
        "Hoy" -> cal.set(Calendar.HOUR_OF_DAY, 0).also { cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0) }
        "Esta semana" -> cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek).also { cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0) }
        "Este mes" -> cal.set(Calendar.DAY_OF_MONTH, 1).also { cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0) }
    }
    return Pair(iso.format(cal.time), end)
}

private fun getDateGroup(createdAt: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(createdAt) ?: return createdAt
        val cal = Calendar.getInstance()
        val today = Calendar.getInstance()
        cal.time = date
        return when {
            cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Hoy"
            cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) - 1 -> "Ayer"
            else -> SimpleDateFormat("dd MMM yyyy", Locale("es", "EC")).format(date)
        }
    } catch (e: Exception) { createdAt.take(10) }
}

@Composable
private fun StatementItemCard(item: WalletStatementItem) {
    val isSent = item.type == "sent"
    val amountColor = if (isSent) Color(0xFFEF4444) else Color(0xFF10B981)
    val amountSign = if (isSent) "-" else "+"
    val iconBg = if (isSent) Color(0xFFFEE2E2) else Color(0xFFD1FAE5)
    val iconTint = if (isSent) Color(0xFFEF4444) else Color(0xFF10B981)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSent) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.counterpartName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Text(item.counterpartPhone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                item.description?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Light)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$amountSign\$ ${"%.2f".format(item.amount)}",
                    fontWeight = FontWeight.Bold,
                    color = amountColor,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Saldo: \$ ${"%.2f".format(item.balanceAfter)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = item.createdAt.substringAfter("T").take(5),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
