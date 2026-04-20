package com.christelldev.easyreferplus.ui.screens.orders

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.christelldev.easyreferplus.data.model.StoreOrderItem
import com.christelldev.easyreferplus.data.model.StoreOrderSummary
import com.christelldev.easyreferplus.ui.viewmodel.StoreOrdersState
import com.christelldev.easyreferplus.ui.viewmodel.StoreOrdersViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisVentasScreen(
    viewModel: StoreOrdersViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val wsConnected by viewModel.wsConnected.collectAsStateWithLifecycle()
    val newOrderAlert by viewModel.newOrderAlert.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf<String?>(null) }
    var expandedOrderId by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.load()
        viewModel.initWebSocketManager(context)
        viewModel.connectWebSocket()
    }
    DisposableEffect(Unit) { onDispose { viewModel.disconnectWebSocket() } }
    LaunchedEffect(selectedFilter) { viewModel.load(selectedFilter) }

    // Snackbar de nuevo pedido
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(newOrderAlert) {
        if (newOrderAlert) {
            snackbarHostState.showSnackbar("¡Nuevo pedido recibido!")
            viewModel.clearNewOrderAlert()
        }
    }

    // Animación punto verde
    val infiniteTransition = rememberInfiniteTransition(label = "ws_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "pulse"
    )

    val isDark = isSystemInDarkTheme()
    val contentTint = if (isDark) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
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
                // TopAppBar manual con padding de status bar
                TopAppBar(
                    title = { Text("Mis Ventas", fontWeight = FontWeight.Bold, color = contentTint) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = contentTint)
                        }
                    },
                    actions = {
                        if (wsConnected) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .scale(pulseScale)
                                    .size(10.dp)
                                    .background(Color(0xFF22C55E), CircleShape)
                            )
                        }
                        IconButton(onClick = { viewModel.load(selectedFilter) }) {
                            Icon(Icons.Default.Refresh, "Actualizar", tint = contentTint)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    windowInsets = WindowInsets.statusBars
                )

                // Filtros (con padding lateral para consistencia)
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    StoreFilterRow(
                        selected = selectedFilter,
                        onSelect = { selectedFilter = if (it == selectedFilter) null else it }
                    )
                }

                when (val s = state) {
                    is StoreOrdersState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is StoreOrdersState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.ErrorOutline, null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    s.message,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(Modifier.height(16.dp))
                                Button(onClick = { viewModel.load(selectedFilter) }) { Text("Reintentar") }
                            }
                        }
                    }
                    is StoreOrdersState.Success -> {
                        if (s.orders.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Storefront, null,
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        "No hay pedidos",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        "Los pedidos de tu establecimiento aparecerán aquí",
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    "${s.total} pedido(s) en total",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(s.orders, key = { it.id }) { order ->
                                        StoreOrderCard(
                                            order = order,
                                            expanded = expandedOrderId == order.id,
                                            onToggle = {
                                                expandedOrderId =
                                                    if (expandedOrderId == order.id) null else order.id
                                            },
                                            onMarkReady = {
                                                viewModel.markOrderReady(order.id,
                                                    onSuccess = { viewModel.load(selectedFilter) },
                                                    onError = { msg ->
                                                        scope.launch { snackbarHostState.showSnackbar(msg) }
                                                    }
                                                )
                                            }
                                        )
                                    }
                                    // Espacio para la barra de navegación al final de la lista
                                    item { Spacer(modifier = Modifier.navigationBarsPadding()) }
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun StoreFilterRow(selected: String?, onSelect: (String) -> Unit) {
    val filters = listOf(
        "paid_pending_driver" to "Nuevos",
        "driver_assigned" to "Asignados",
        "ready_for_pickup" to "Listos",
        "picked_up" to "En camino",
        "delivered" to "Entregados",
        "completed" to "Completados"
    )
    ScrollableTabRow(
        selectedTabIndex = filters.indexOfFirst { it.first == selected }.coerceAtLeast(0),
        edgePadding = 8.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        filters.forEach { (key, label) ->
            Tab(
                selected = selected == key,
                onClick = { onSelect(key) },
                text = { Text(label, fontSize = 12.sp) }
            )
        }
    }
}

@Composable
private fun StoreOrderCard(
    order: StoreOrderSummary,
    expanded: Boolean,
    onToggle: () -> Unit,
    onMarkReady: () -> Unit = {}
) {
    val (statusLabel, statusColor) = storeOrderStatusInfo(order.status)

    Card(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Pedido #${order.id}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Surface(
                            color = statusColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                statusLabel,
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (!order.buyerName.isNullOrBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(order.buyerName, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "$${String.format("%.2f", order.mySubtotal)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "${order.items.size} ítem(s)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Delivery info
            if (order.deliveryRequired && !order.dropoffAddress.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(order.dropoffAddress, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }

            if (!order.observations.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.ChatBubbleOutline, null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(order.observations, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                }
            }

            // Items detallados (expandidos)
            if (expanded && order.items.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Text("Detalle del pedido", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                order.items.forEach { item ->
                    StoreItemRow(item)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total a recibir", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(
                        "$${String.format("%.2f", order.mySubtotal)}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                }

                // Botón "Marcar como Listo" cuando el conductor ya fue asignado
                if (order.status == "driver_assigned") {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onMarkReady,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("PEDIDO LISTO PARA RECOGER", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                } else if (order.status == "ready_for_pickup") {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF16A34A).copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HourglassEmpty, null,
                                modifier = Modifier.size(16.dp), tint = Color(0xFF16A34A))
                            Spacer(Modifier.width(6.dp))
                            Text("Esperando al conductor para recoger",
                                fontSize = 12.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Expandir indicador
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun StoreItemRow(item: StoreOrderItem) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "${item.quantity}x ${item.productName}",
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            "$${String.format("%.2f", item.subtotal)}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun storeOrderStatusInfo(status: String): Pair<String, Color> = when (status) {
    "paid_pending_driver" -> "Nuevo pedido" to Color(0xFFF59E0B)
    "driver_assigned"     -> "Conductor asignado" to Color(0xFF3B82F6)
    "ready_for_pickup"    -> "Listo para recoger" to Color(0xFF16A34A)
    "picked_up"           -> "En camino" to Color(0xFF8B5CF6)
    "delivered"           -> "Entregado" to Color(0xFF22C55E)
    "payout_ready"        -> "Pago en proceso" to Color(0xFF06B6D4)
    "completed"           -> "Completado" to Color(0xFF10B981)
    "disputed"            -> "En disputa" to Color(0xFFEF4444)
    "resolved"            -> "Resuelto" to Color(0xFF6B7280)
    else                  -> status to Color(0xFF6B7280)
}
