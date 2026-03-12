package com.christelldev.easyreferplus.ui.screens.earnings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.christelldev.easyreferplus.data.model.CommissionResponse
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
private val GradientPurple = listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA))

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
    // Nuevos parámetros para lista de comisiones
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
    onFilterChange: (String) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    // Mostrar error
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            // El error se muestra en la UI
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis Ganancias",
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
            } else if (isEmpty) {
                EmptyEarningsContent(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                EarningsContent(
                    totalEarned = totalEarned,
                    totalPaid = totalPaid,
                    totalPending = totalPending,
                    totalCommissions = totalCommissions,
                    pendingCount = pendingCount,
                    paidCount = paidCount,
                    scheduledCount = scheduledCount,
                    level1Earnings = level1Earnings,
                    level2Earnings = level2Earnings,
                    level3Earnings = level3Earnings,
                    level1Percentage = level1Percentage,
                    level2Percentage = level2Percentage,
                    level3Percentage = level3Percentage,
                    topCompanies = topCompanies,
                    commissions = commissions,
                    isLoadingCommissions = isLoadingCommissions,
                    hasMoreCommissions = hasMoreCommissions,
                    selectedFilter = selectedFilter,
                    errorMessage = errorMessage,
                    onClearError = onClearError,
                    onLoadMoreCommissions = onLoadMoreCommissions,
                    onFilterChange = onFilterChange
                )
            }
        }
    }
}

@Composable
private fun EmptyEarningsContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.AccountBalanceWallet,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Sin ganancias aún",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Cuando generes comisiones a través de referidos, aquí verás tus ganancias.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EarningsContent(
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
    errorMessage: String?,
    onClearError: () -> Unit,
    onLoadMoreCommissions: () -> Unit,
    onFilterChange: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = CARD_MARGIN_HORIZONTAL),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Error message
        if (errorMessage != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
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
                        IconButton(onClick = onClearError) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }

        // Resumen principal
        item {
            EarningsSummaryCard(
                totalEarned = totalEarned,
                totalPaid = totalPaid,
                totalPending = totalPending
            )
        }

        // Estadísticas de comisiones
        item {
            CommissionsStatsCard(
                totalCommissions = totalCommissions,
                pendingCount = pendingCount,
                paidCount = paidCount,
                scheduledCount = scheduledCount
            )
        }

        // Ganancias por nivel
        item {
            EarningsByLevelCard(
                level1Earnings = level1Earnings,
                level2Earnings = level2Earnings,
                level3Earnings = level3Earnings,
                level1Percentage = level1Percentage,
                level2Percentage = level2Percentage,
                level3Percentage = level3Percentage
            )
        }

        // Empresas top
        if (topCompanies.isNotEmpty()) {
            item {
                TopCompaniesCard(topCompanies = topCompanies)
            }
        }

        // Filtros de comisiones
        item {
            CommissionsFilterSection(
                selectedFilter = selectedFilter,
                onFilterChange = onFilterChange
            )
        }

        // Lista de comisiones
        item {
            Text(
                text = "Detalle de Comisiones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (commissions.isEmpty() && !isLoadingCommissions) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay comisiones con el filtro seleccionado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            items(commissions) { commission ->
                CommissionItem(commission = commission)
            }

            // Indicador de carga para paginación
            if (isLoadingCommissions) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = AppBlue
                        )
                    }
                }
            }

            // Botón para cargar más
            if (hasMoreCommissions && !isLoadingCommissions) {
                item {
                    TextButton(
                        onClick = onLoadMoreCommissions,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cargar más")
                    }
                }
            }
        }

        // Espacio final
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EarningsSummaryCard(
    totalEarned: Double,
    totalPaid: Double,
    totalPending: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CARD_MARGIN_HORIZONTAL)
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
                    text = "Total Ganado",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$${String.format("%.2f", totalEarned)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.surface
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Total pagado
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Pagado",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "$${String.format("%.2f", totalPaid)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.surface
                        )
                    }

                    // Total pendiente
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Pendiente",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "$${String.format("%.2f", totalPending)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFFB74D)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommissionsStatsCard(
    totalCommissions: Int,
    pendingCount: Int,
    paidCount: Int,
    scheduledCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CARD_MARGIN_HORIZONTAL)
            .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(CARD_CORNER_RADIUS)),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Comisiones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppBlue
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Total",
                    value = totalCommissions.toString(),
                    color = AppBlue
                )
                StatItem(
                    label = "Pendientes",
                    value = pendingCount.toString(),
                    color = AppOrange
                )
                StatItem(
                    label = "Pagadas",
                    value = paidCount.toString(),
                    color = AppGreen
                )
                StatItem(
                    label = "Programadas",
                    value = scheduledCount.toString(),
                    color = Color(0xFF9C27B0)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun EarningsByLevelCard(
    level1Earnings: Double,
    level2Earnings: Double,
    level3Earnings: Double,
    level1Percentage: Double,
    level2Percentage: Double,
    level3Percentage: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CARD_MARGIN_HORIZONTAL)
            .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(CARD_CORNER_RADIUS)),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Ganancias por Nivel",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppBlue
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Nivel 1
            LevelRow(
                level = "Nivel 1",
                description = "Referidos directos",
                amount = level1Earnings,
                percentage = "${level1Percentage.toInt()}%",
                gradient = GradientSuccess
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Nivel 2
            LevelRow(
                level = "Nivel 2",
                description = "Referidos de tus referidos",
                amount = level2Earnings,
                percentage = "${level2Percentage.toInt()}%",
                gradient = GradientOrange
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Nivel 3
            LevelRow(
                level = "Nivel 3",
                description = "Nivel 3 de referido",
                amount = level3Earnings,
                percentage = "${level3Percentage.toInt()}%",
                gradient = GradientPurple
            )
        }
    }
}

@Composable
private fun LevelRow(
    level: String,
    description: String,
    amount: Double,
    percentage: String,
    gradient: List<Color>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(brush = Brush.linearGradient(colors = gradient))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = level,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.surface
            )
            Text(
                text = "$description ($percentage)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            )
        }
        Text(
            text = "$${String.format("%.2f", amount)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun TopCompaniesCard(topCompanies: List<Pair<String, Double>>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CARD_MARGIN_HORIZONTAL)
            .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(CARD_CORNER_RADIUS)),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Empresas que más generaron",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppBlue
            )
            Spacer(modifier = Modifier.height(16.dp))

            topCompanies.forEachIndexed { index, (companyName, amount) ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.linearGradient(colors = GradientPrimary)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.surface
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = companyName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Text(
                        text = "$${String.format("%.2f", amount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppGreen
                    )
                }
            }
        }
    }
}

// =====================================================
// SECCIÓN DE FILTROS
// =====================================================

@Composable
private fun CommissionsFilterSection(
    selectedFilter: String,
    onFilterChange: (String) -> Unit
) {
    val filters = listOf(
        "all" to "Todas",
        "pending" to "Pendientes",
        "paid" to "Pagadas"
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        items(filters) { (filterKey, filterLabel) ->
            FilterChip(
                selected = selectedFilter == filterKey,
                onClick = { onFilterChange(filterKey) },
                label = { Text(filterLabel) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AppBlue,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

// =====================================================
// ITEM DE COMISIÓN
// =====================================================

@Composable
private fun CommissionItem(commission: CommissionResponse) {
    val statusColor = when (commission.paymentStatus) {
        "paid" -> AppGreen
        "pending" -> AppOrange
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }

    val statusText = when (commission.paymentStatus) {
        "paid" -> "Pagado"
        "pending" -> "Pendiente"
        else -> commission.paymentStatus ?: "Desconocido"
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
                .padding(12.dp)
        ) {
            // Header: Empresa y Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = commission.companyName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cliente
            if (!commission.buyerName.isNullOrBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = commission.buyerName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Nivel de referido
            commission.referralLevel?.let { level ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountTree,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Nivel $level",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Fecha
            commission.transactionDate?.let { date ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = date.take(10),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Montos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Comisión Neta",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "$${String.format("%.2f", commission.netCommission)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppGreen
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Bruta",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "$${String.format("%.2f", commission.grossCommission)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
