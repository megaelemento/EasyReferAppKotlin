package com.christelldev.easyreferplus.ui.screens.wallet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.christelldev.easyreferplus.data.model.WalletStatementItem
import com.christelldev.easyreferplus.ui.theme.DesignConstants
import com.christelldev.easyreferplus.ui.viewmodel.WalletViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletStatementScreen(
    viewModel: WalletViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var selectedFilter by remember { mutableStateOf("Todo") }
    var selectedItem by remember { mutableStateOf<WalletStatementItem?>(null) }
    val isDark = isSystemInDarkTheme()

    LaunchedEffect(Unit) {
        viewModel.loadStatement(refresh = true)
    }

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

            val contentColor = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White

            Column(modifier = Modifier.fillMaxSize()) {
                // Cabecera Premium con insets de status bar
                TopAppBar(
                    title = {
                        Text(
                            text = "Estado de Cuenta",
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

                // Filtros Modernos
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
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
                            label = { Text(filter, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (isDark) MaterialTheme.colorScheme.primary else Color.White,
                                selectedLabelColor = if (isDark) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                labelColor = if (isDark) MaterialTheme.colorScheme.onSurface else Color.White.copy(alpha = 0.8f)
                            ),
                            border = if (selectedFilter == filter) null else FilterChipDefaults.filterChipBorder(enabled = true, selected = false, borderColor = if (isDark) MaterialTheme.colorScheme.outline else Color.White.copy(alpha = 0.3f))
                        )
                    }
                }

                if (uiState.isLoadingStatement && uiState.statementItems.isEmpty()) {
                    LoadingSkeletonsPremium()
                } else if (uiState.statementItems.isEmpty()) {
                    EmptyStatementState()
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            val summary = calculateSummary(uiState.statementItems)
                            StatementSummaryCard(
                                totalSent = summary.first,
                                totalReceived = summary.second,
                                netBalance = summary.third
                            )
                        }

                        val groupedItems = uiState.statementItems.groupBy { item -> getDateGroup(item.createdAt) }

                        groupedItems.forEach { (dateLabel, items) ->
                            item {
                                Text(
                                    text = dateLabel.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                                )
                            }
                            
                            items(items) { item ->
                                StatementItemCardPremium(item = item, onClick = { selectedItem = item })
                            }
                        }

                        if (uiState.isLoadingStatement) {
                            item { Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(modifier = Modifier.size(32.dp)) } }
                        }
                        
                        // Espacio para la barra de navegación al final de la lista
                        item { Spacer(modifier = Modifier.navigationBarsPadding()) }
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
fun StatementSummaryCard(totalSent: Double, totalReceived: Double, netBalance: Double) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SummaryItem("Enviado", totalSent, Color(0xFFEF4444), Icons.Default.ArrowUpward)
            Box(modifier = Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)))
            SummaryItem("Recibido", totalReceived, Color(0xFF10B981), Icons.Default.ArrowDownward)
            Box(modifier = Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)))
            SummaryItem("Neto", netBalance, if (netBalance >= 0) Color(0xFF10B981) else Color(0xFFEF4444), Icons.Default.AccountBalanceWallet)
        }
    }
}

@Composable
fun SummaryItem(label: String, amount: Double, color: Color, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color.copy(alpha = 0.6f), modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            text = "$${String.format(java.util.Locale.US, "%.2f", amount)}",
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
}

@Composable
private fun StatementItemCardPremium(item: WalletStatementItem, onClick: () -> Unit) {
    val isSent = item.type == "sent"
    val accentColor = if (isSent) Color(0xFFEF4444) else Color(0xFF10B981)
    val phone = if (item.counterpartPhone.startsWith("+593")) "0${item.counterpartPhone.drop(4)}" else item.counterpartPhone

    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(44.dp), shape = CircleShape, color = accentColor.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(if (isSent) Icons.AutoMirrored.Filled.CallMade else Icons.AutoMirrored.Filled.CallReceived, null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.counterpartName, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(phone, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                item.description?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isSent) "-$${String.format("%.2f", item.amount)}" else "+$${String.format("%.2f", item.amount)}",
                    fontWeight = FontWeight.Black, color = accentColor, style = MaterialTheme.typography.bodyLarge
                )
                Text(item.createdAt.substringAfter("T").take(5), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
private fun LoadingSkeletonsPremium() {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse),
        label = "alpha"
    )
    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(4) {
            Box(modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)))
        }
    }
}

@Composable
private fun EmptyStatementState() {
    Column(modifier = Modifier.fillMaxSize().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.History, null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Sin movimientos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Text("Tus transferencias y cobros aparecerán detallados aquí.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun calculateSummary(items: List<WalletStatementItem>): Triple<Double, Double, Double> {
    val sent = items.filter { it.type == "sent" }.sumOf { it.amount }
    val received = items.filter { it.type == "received" }.sumOf { it.amount }
    return Triple(sent, received, received - sent)
}

private fun getDateGroup(createdAt: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(createdAt) ?: return createdAt
        val cal = Calendar.getInstance(); val today = Calendar.getInstance()
        cal.time = date
        return when {
            cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Hoy"
            cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) - 1 -> "Ayer"
            else -> SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("es-EC")).format(date)
        }
    } catch (e: Exception) { createdAt.take(10) }
}

private fun getDateRangeForFilter(filter: String): Pair<String?, String?> {
    if (filter == "Todo") return Pair(null, null)
    val iso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply { timeZone = TimeZone.getDefault() }
    val cal = Calendar.getInstance(); val end = iso.format(cal.time)
    when (filter) {
        "Hoy" -> {
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
        }
        "Esta semana" -> {
            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
        }
        "Este mes" -> {
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
        }
    }

    return Pair(iso.format(cal.time), end)
}
