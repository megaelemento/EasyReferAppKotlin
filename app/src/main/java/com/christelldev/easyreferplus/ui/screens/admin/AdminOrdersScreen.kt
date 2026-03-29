package com.christelldev.easyreferplus.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.christelldev.easyreferplus.data.model.CompanyOrderSummary
import com.christelldev.easyreferplus.ui.viewmodel.AdminDeliveryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(
    viewModel: AdminDeliveryViewModel,
    onNavigateBack: () -> Unit
) {
    val orders by viewModel.companyOrders.collectAsState()
    val isLoading by viewModel.ordersLoading.collectAsState()
    val total by viewModel.ordersTotal.collectAsState()

    var selectedFilter by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(selectedFilter) {
        viewModel.loadCompanyOrders(page = 1, status = selectedFilter)
    }

    // Paginación: cargar más al llegar al final
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= orders.size - 3 && !isLoading && orders.size < total
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            val nextPage = (orders.size / 30) + 1
            viewModel.loadCompanyOrders(page = nextPage, status = selectedFilter)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Pedidos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadCompanyOrders(1, selectedFilter) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {

            // Filtros de estado
            StatusFilterRow(selected = selectedFilter, onSelect = { selectedFilter = it })

            // Contador
            if (total > 0) {
                Text(
                    "$total pedido(s) en total",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isLoading && orders.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (orders.isEmpty()) {
                EmptyOrdersState()
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(orders, key = { it.id }) { order ->
                        CompanyOrderCard(order = order)
                    }
                    if (isLoading) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusFilterRow(selected: String?, onSelect: (String?) -> Unit) {
    val filters = listOf(
        null to "Todos",
        "driver_assigned" to "Asignado",
        "picked_up" to "En camino",
        "delivered" to "Entregado",
        "completed" to "Completado",
        "cancelled" to "Cancelado",
    )
    ScrollableTabRow(
        selectedTabIndex = filters.indexOfFirst { it.first == selected }.coerceAtLeast(0),
        edgePadding = 8.dp,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        filters.forEachIndexed { _, (status, label) ->
            Tab(
                selected = selected == status,
                onClick = { onSelect(status) },
                text = { Text(label, fontSize = 13.sp) }
            )
        }
    }
}

@Composable
private fun CompanyOrderCard(order: CompanyOrderSummary) {
    val statusInfo = deliveryStatusInfo(order.deliveryStatus ?: order.status)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(14.dp)) {
            // Header: id + badge de estado
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Pedido #${order.id}", fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall)
                    Text(
                        order.paidAt?.take(16)?.replace("T", " ") ?: order.createdAt?.take(10) ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(shape = RoundedCornerShape(20.dp), color = statusInfo.bgColor) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(statusInfo.icon, null, modifier = Modifier.size(12.dp),
                            tint = statusInfo.textColor)
                        Spacer(Modifier.width(4.dp))
                        Text(statusInfo.label, fontSize = 11.sp,
                            fontWeight = FontWeight.Bold, color = statusInfo.textColor)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

            Row(Modifier.fillMaxWidth()) {
                // Conductor + dirección
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (order.driverName != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(4.dp))
                            Text(order.driverName, style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Text("Sin conductor asignado",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (order.dropoffAddress != null) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Place, null, modifier = Modifier.size(13.dp)
                                .padding(top = 1.dp),
                                tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(3.dp))
                            Text(order.dropoffAddress, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                        }
                    }
                    Text("${order.itemsCount} artículo(s)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Tarifa
                Column(horizontalAlignment = Alignment.End) {
                    Text("$${String.format("%.2f", order.deliveryFee)}",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary)
                    Text("tarifa", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun EmptyOrdersState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(40.dp)) {
            Icon(Icons.Default.Inbox, null, modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            Spacer(Modifier.height(16.dp))
            Text("Sin pedidos", fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge)
            Text("No hay pedidos con este filtro aún.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp))
        }
    }
}

private data class StatusInfo(
    val label: String, val bgColor: Color, val textColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
private fun deliveryStatusInfo(status: String): StatusInfo = when (status) {
    "accepted", "assigned" -> StatusInfo("Asignado",
        Color(0xFFE3F2FD), Color(0xFF1565C0), Icons.Default.Person)
    "picked_up", "in_transit" -> StatusInfo("En camino",
        Color(0xFFFFF3E0), Color(0xFFE65100), Icons.Default.LocalShipping)
    "delivered" -> StatusInfo("Entregado",
        Color(0xFFE8F5E9), Color(0xFF2E7D32), Icons.Default.CheckCircle)
    "completed" -> StatusInfo("Completado",
        Color(0xFFE8F5E9), Color(0xFF1B5E20), Icons.Default.TaskAlt)
    "cancelled", "failed" -> StatusInfo("Cancelado",
        Color(0xFFFFEBEE), Color(0xFFB71C1C), Icons.Default.Cancel)
    "paid_pending_driver" -> StatusInfo("Sin conductor",
        Color(0xFFFFF9C4), Color(0xFFF57F17), Icons.Default.HourglassEmpty)
    else -> StatusInfo(status, Color(0xFFF5F5F5), Color(0xFF424242), Icons.Default.Info)
}
