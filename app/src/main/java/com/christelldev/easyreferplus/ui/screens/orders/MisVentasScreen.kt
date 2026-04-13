package com.christelldev.easyreferplus.ui.screens.orders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.load()
        viewModel.initWebSocketManager(context)
        viewModel.connectWebSocket()
    }
    DisposableEffect(Unit) { onDispose { viewModel.disconnectWebSocket() } }
    LaunchedEffect(selectedFilter) { viewModel.load(selectedFilter) }

    LaunchedEffect(newOrderAlert) {
        if (newOrderAlert) {
            snackbarHostState.showSnackbar("¡Nuevo pedido recibido! Revisa la lista.")
            viewModel.clearNewOrderAlert()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ws_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.4f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "pulse"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mis Pedidos", fontWeight = FontWeight.Bold)
                        if (wsConnected) {
                            Text("En vivo", fontSize = 11.sp, color = Color(0xFF22C55E))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    if (wsConnected) {
                        Box(
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .scale(pulseScale)
                                .size(9.dp)
                                .background(Color(0xFF22C55E), CircleShape)
                        )
                    }
                    IconButton(onClick = { viewModel.load(selectedFilter) }) {
                        Icon(Icons.Default.Refresh, "Actualizar")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            StoreFilterRow(selected = selectedFilter, onSelect = {
                selectedFilter = if (it == selectedFilter) null else it
            })

            when (val s = state) {
                is StoreOrdersState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is StoreOrdersState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                            Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(s.message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { viewModel.load(selectedFilter) }) { Text("Reintentar") }
                        }
                    }
                }
                is StoreOrdersState.Success -> {
                    if (s.orders.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                                Icon(Icons.Default.Storefront, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(64.dp))
                                Spacer(Modifier.height(16.dp))
                                Text("Sin pedidos", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Los pedidos de tu establecimiento aparecerán aquí",
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text(
                                    "${s.total} pedido(s)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            items(s.orders, key = { it.id }) { order ->
                                StoreOrderCard(
                                    order = order,
                                    expanded = expandedOrderId == order.id,
                                    onToggle = {
                                        expandedOrderId = if (expandedOrderId == order.id) null else order.id
                                    },
                                    onAccept = {
                                        viewModel.acceptOrder(order.id,
                                            onSuccess = { viewModel.load(selectedFilter) },
                                            onError = { msg ->
                                                scope.launch { snackbarHostState.showSnackbar(msg) }
                                            }
                                        )
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
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun StoreFilterRow(selected: String?, onSelect: (String) -> Unit) {
    val filters = listOf(
        "pending_acceptance" to "Nuevos",
        "paid_pending_driver" to "Aceptados",
        "driver_assigned" to "Con conductor",
        "ready_for_pickup" to "Listos",
        "picked_up" to "En camino",
        "delivered" to "Entregados",
        "completed" to "Completados"
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { (key, label) ->
            val isSelected = selected == key
            val (_, color) = storeOrderStatusInfo(key)
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(key) },
                label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color.copy(alpha = 0.15f),
                    selectedLabelColor = color,
                    selectedLeadingIconColor = color,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    selectedBorderColor = color,
                    selectedBorderWidth = 1.dp,
                )
            )
        }
    }
}

@Composable
private fun StoreOrderCard(
    order: StoreOrderSummary,
    expanded: Boolean,
    onToggle: () -> Unit,
    onAccept: () -> Unit = {},
    onMarkReady: () -> Unit = {}
) {
    val (statusLabel, statusColor) = storeOrderStatusInfo(order.status)
    val isNew = order.status == "pending_acceptance"

    Card(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isNew) 6.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isNew)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isNew)
            androidx.compose.foundation.BorderStroke(2.dp, statusColor)
        else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Badge nuevo pedido
            if (isNew) {
                Surface(
                    color = statusColor,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.NotificationsActive, null, modifier = Modifier.size(14.dp), tint = Color.White)
                        Text("NUEVO PEDIDO", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
            }

            // Header: número + estado + total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Pedido #${order.id}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Spacer(Modifier.height(2.dp))
                    Surface(
                        color = statusColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            statusLabel,
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "$${String.format("%.2f", order.mySubtotal)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("${order.items.size} producto(s)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(10.dp))

            // Buyer
            if (!order.buyerName.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(order.buyerName, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(6.dp))
            }

            // Dirección de entrega
            if (order.deliveryRequired && !order.dropoffAddress.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp), tint = Color(0xFFEF4444))
                    Text(order.dropoffAddress, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(6.dp))
            }

            // Observaciones
            if (!order.observations.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.ChatBubbleOutline, null, modifier = Modifier.size(16.dp), tint = Color(0xFFF59E0B))
                    Text(order.observations, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }
                Spacer(Modifier.height(6.dp))
            }

            // Productos — siempre visibles (primeros 3), resto al expandir
            Spacer(Modifier.height(4.dp))
            Text("Productos:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            val visibleItems = if (expanded) order.items else order.items.take(3)
            visibleItems.forEach { item -> StoreItemRow(item) }
            if (!expanded && order.items.size > 3) {
                Text(
                    "+ ${order.items.size - 3} producto(s) más",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Detalle extra al expandir
            AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total a recibir", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(
                            "$${String.format("%.2f", order.mySubtotal)}",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(Modifier.height(8.dp))

                    // Botón ACEPTAR
                    if (order.status == "pending_acceptance") {
                        Button(
                            onClick = onAccept,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                        ) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("ACEPTAR PEDIDO", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Al aceptar, se notificará a los conductores disponibles",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Botón LISTO PARA RECOGER
                    if (order.status == "driver_assigned") {
                        Button(
                            onClick = onMarkReady,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Icon(Icons.Default.Inventory2, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("PEDIDO LISTO PARA RECOGER", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                        }
                    }

                    // Estado informativo
                    if (order.status == "ready_for_pickup") {
                        Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF16A34A).copy(alpha = 0.10f), modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.HourglassTop, null, modifier = Modifier.size(18.dp), tint = Color(0xFF16A34A))
                                Text("Esperando al conductor para recoger", fontSize = 13.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    if (order.status == "picked_up") {
                        Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF8B5CF6).copy(alpha = 0.10f), modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.DeliveryDining, null, modifier = Modifier.size(18.dp), tint = Color(0xFF8B5CF6))
                                Text("El conductor va en camino al cliente", fontSize = 13.sp, color = Color(0xFF8B5CF6), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Chevron expandir
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Colapsar" else "Expandir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun StoreItemRow(item: StoreOrderItem) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(22.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text("${item.quantity}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Text(item.productName, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(
            "$${String.format("%.2f", item.subtotal)}",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private fun storeOrderStatusInfo(status: String): Pair<String, Color> = when (status) {
    "pending_acceptance"  -> "Nuevo — Requiere aceptación" to Color(0xFFF97316)
    "paid_pending_driver" -> "Aceptado — Buscando conductor" to Color(0xFFF59E0B)
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
