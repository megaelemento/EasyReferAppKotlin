package com.christelldev.easyreferplus.ui.screens.earnings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.data.model.CommissionResponse
import com.christelldev.easyreferplus.ui.theme.DesignConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarningsScreen(
    totalEarned: Double,
    totalPaid: Double,
    totalPending: Double,
    totalCommissions: Int,
    pendingCount: Int,
    paidCount: Int,
    scheduledCount: Int,
    level1Earnings: Double,
    level2Earnings: Double,
    level3Earnings: Double,
    level1Percentage: Double,
    level2Percentage: Double,
    level3Percentage: Double,
    topCompanies: List<Pair<String, Double>>,
    commissions: List<CommissionResponse>,
    isLoadingCommissions: Boolean,
    hasMoreCommissions: Boolean,
    selectedFilter: String,
    isLoading: Boolean,
    isEmpty: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    onClearError: () -> Unit,
    onLoadMoreCommissions: () -> Unit,
    onFilterChange: (String) -> Unit,
    onConnectWebSocket: () -> Unit,
    onDisconnectWebSocket: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    DisposableEffect(Unit) {
        onDispose { onDisconnectWebSocket() }
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

            Column(modifier = Modifier.fillMaxSize()) {
                // Cabecera Premium con insets de status bar
                TopAppBar(
                    title = {
                        Text(
                            text = "Mis Ganancias",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                null,
                                tint = if (isDark) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onRefresh) {
                            Icon(
                                Icons.Default.Refresh,
                                null,
                                tint = if (isDark) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    windowInsets = WindowInsets.statusBars
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (isEmpty) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        EmptyEarningsState()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // TOTAL EARNED CARD STAR
                        item {
                            EarningsSummaryCard(
                                total = totalEarned,
                                paid = totalPaid,
                                pending = totalPending
                            )
                        }

                        // DESGLOSE POR NIVEL
                        item {
                            EarningsLevelSection(
                                l1 = level1Earnings,
                                l2 = level2Earnings,
                                l3 = level3Earnings,
                                p1 = level1Percentage,
                                p2 = level2Percentage,
                                p3 = level3Percentage
                            )
                        }

                        // RENDIMIENTO DASHBOARD
                        item {
                            PerformanceDashboard(
                                total = totalCommissions,
                                pending = pendingCount,
                                paid = paidCount,
                                scheduled = scheduledCount
                            )
                        }

                        // FILTROS Y DETALLE
                        item {
                            Column {
                                Text(
                                    "Historial de Comisiones",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                FilterSection(selectedFilter, onFilterChange)
                            }
                        }

                        if (commissions.isEmpty() && !isLoadingCommissions) {
                            item {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            "Sin movimientos con este filtro",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            items(commissions, key = { it.id }) { commission ->
                                CommissionListItem(commission)
                            }

                            if (isLoadingCommissions) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) { CircularProgressIndicator(modifier = Modifier.size(32.dp)) }
                                }
                            }

                            if (hasMoreCommissions && !isLoadingCommissions) {
                                item {
                                    TextButton(
                                        onClick = onLoadMoreCommissions,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Cargar más movimientos", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Espacio para la barra de navegación al final de la lista
                        item { Spacer(modifier = Modifier.navigationBarsPadding()) }
                    }
                }
            }
        }
    }
}

@Composable
fun EarningsSummaryCard(total: Double, paid: Double, pending: Double) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("TOTAL GENERADO", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$${String.format(java.util.Locale.US, "%.2f", total)}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Cobrado", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$${String.format(java.util.Locale.US, "%.2f", paid)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Por Cobrar", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$${String.format(java.util.Locale.US, "%.2f", pending)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }
    }
}

@Composable
fun EarningsLevelSection(l1: Double, l2: Double, l3: Double, p1: Double, p2: Double, p3: Double) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Ganancias por Nivel", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(20.dp))
            
            LevelRowPremium("Nivel 1", "Directos", l1, "${p1.toInt()}%", Color(0xFF10B981))
            Spacer(modifier = Modifier.height(12.dp))
            LevelRowPremium("Nivel 2", "Indirectos", l2, "${p2.toInt()}%", Color(0xFFF59E0B))
            Spacer(modifier = Modifier.height(12.dp))
            LevelRowPremium("Nivel 3", "Red", l3, "${p3.toInt()}%", Color(0xFF8B5CF6))
        }
    }
}

@Composable
fun LevelRowPremium(label: String, sub: String, amount: Double, percent: String, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.15f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = color.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(label.takeLast(1), fontWeight = FontWeight.Black, color = color)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text("$sub ($percent)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("$${String.format(java.util.Locale.US, "%.2f", amount)}", fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyLarge, color = color)
        }
    }
}

@Composable
fun PerformanceDashboard(total: Int, pending: Int, paid: Int, scheduled: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            PerfItem("Total", total.toString(), MaterialTheme.colorScheme.primary)
            PerfItem("Pend.", pending.toString(), Color(0xFFF59E0B))
            PerfItem("Pagadas", paid.toString(), Color(0xFF10B981))
            PerfItem("Prog.", scheduled.toString(), Color(0xFF8B5CF6))
        }
    }
}

@Composable
fun PerfItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun FilterSection(selected: String, onFilterChange: (String) -> Unit) {
    val filters = listOf("all" to "Todas", "pending" to "Pendientes", "paid" to "Pagadas")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filters) { (key, label) ->
            FilterChip(
                selected = selected == key,
                onClick = { onFilterChange(key) },
                label = { Text(label, fontWeight = FontWeight.Bold) },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun CommissionListItem(commission: CommissionResponse) {
    val statusColor = when (commission.paymentStatus) {
        "paid" -> Color(0xFF10B981)
        "pending" -> Color(0xFFF59E0B)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(44.dp), shape = CircleShape, color = statusColor.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AccountTree, null, tint = statusColor, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(commission.companyName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Cliente: ${commission.buyerName ?: "Anónimo"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${commission.transactionDate?.take(10)} • Nivel ${commission.referralLevel}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$${String.format(java.util.Locale.US, "%.2f", commission.netCommission)}", fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyLarge, color = Color(0xFF10B981))
                Surface(color = statusColor.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                    Text(commission.paymentStatus?.uppercase() ?: "---", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = statusColor, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
fun EmptyEarningsState() {
    Column(modifier = Modifier.fillMaxSize().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.AccountBalanceWallet, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Sin ganancias aún", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Text("Empieza a invitar amigos para generar comisiones de por vida.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}
