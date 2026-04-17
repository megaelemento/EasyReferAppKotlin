package com.christelldev.easyreferplus.ui.screens.orders

import androidx.compose.animation.core.*
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
import com.christelldev.easyreferplus.data.model.OrderItemOut
import com.christelldev.easyreferplus.data.model.OrderOut
import com.christelldev.easyreferplus.ui.viewmodel.OrderListState
import com.christelldev.easyreferplus.ui.viewmodel.OrderViewModel
import kotlinx.coroutines.launch

private val trackableStatuses = setOf("driver_assigned", "ready_for_pickup", "picked_up")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisComprasScreen(
    viewModel: OrderViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTracking: (Int) -> Unit = {},
    onNavigateToRating: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val state by viewModel.ordersState.collectAsState()
    val wsConnected by viewModel.wsConnected.collectAsState()
    val wsMessage by viewModel.wsMessage.collectAsState()
    var selectedOrder by remember { mutableStateOf<OrderOut?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var confirmingReceiptFor by remember { mutableStateOf<Int?>(null) }
    var cancellingOrderFor by remember { mutableStateOf<Int?>(null) }
    var showCancelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadMyOrders()
        viewModel.initWebSocketManager(context)
        viewModel.connectWebSocket()
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.disconnectWebSocket() }
    }

    // Keep selectedOrder in sync when WS updates status
    LaunchedEffect(state) {
        val current = selectedOrder ?: return@LaunchedEffect
        val updated = (state as? OrderListState.Success)?.orders?.find { it.id == current.id }
        if (updated != null && updated.status != current.status) selectedOrder = updated
    }

    // Mostrar mensajes informativos del servidor (ej. conductor rechazó el pedido)
    LaunchedEffect(wsMessage) {
        val msg = wsMessage ?: return@LaunchedEffect
        scope.launch { snackbarHostState.showSnackbar(msg) }
        viewModel.clearWsMessage()
    }

    // Pulsing animation for the live indicator dot
    val livePulse = rememberInfiniteTransition(label = "livePulse")
    val liveDotScale by livePulse.animateFloat(
        initialValue = 0.8f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            tween(800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "liveDot"
    )

    val isDark = isSystemInDarkTheme()
    val contentTint = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), Color.Transparent)
                        )
                    )
            )
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text("Mis Compras", fontWeight = FontWeight.Bold, color = contentTint) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = contentTint)
                        }
                    },
                    actions = {
                        if (wsConnected) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .scale(liveDotScale)
                                        .background(Color(0xFF4CAF50), CircleShape)
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.loadMyOrders() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = contentTint)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
                when (state) {
                    is OrderListState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is OrderListState.Error -> {
                        val msg = (state as OrderListState.Error).message
                        Column(
                            Modifier.fillMaxSize().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.ErrorOutline, null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                            Spacer(Modifier.height(16.dp))
                            Text(msg, textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(20.dp))
                            Button(onClick = { viewModel.loadMyOrders() }) { Text("Reintentar") }
                        }
                    }
                    is OrderListState.Success -> {
                        val orders = (state as OrderListState.Success).orders
                        if (orders.isEmpty()) {
                            EmptyOrders()
                        } else {
                            val activeStatuses = setOf("pending_payment", "paid_pending_driver", "driver_assigned", "ready_for_pickup", "picked_up")
                            val activeOrders = orders.filter { it.status in activeStatuses }
                            val pastOrders = orders.filter { it.status !in activeStatuses }
                            var selectedTab by remember { mutableIntStateOf(if (activeOrders.isNotEmpty()) 0 else 1) }

                            Column(modifier = Modifier.fillMaxSize()) {
                                TabRow(
                                    selectedTabIndex = selectedTab,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    Tab(
                                        selected = selectedTab == 0,
                                        onClick = { selectedTab = 0 },
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (activeOrders.isNotEmpty()) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .background(Color(0xFF4CAF50), CircleShape)
                                                    )
                                                    Spacer(Modifier.width(6.dp))
                                                }
                                                Text("Activos (${activeOrders.size})", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    )
                                    Tab(
                                        selected = selectedTab == 1,
                                        onClick = { selectedTab = 1 },
                                        text = { Text("Historial (${pastOrders.size})", fontWeight = FontWeight.Bold) }
                                    )
                                }

                                val displayOrders = if (selectedTab == 0) activeOrders else pastOrders

                                if (displayOrders.isEmpty()) {
                                    Box(
                                        Modifier.fillMaxSize().padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            if (selectedTab == 0) "No tienes pedidos activos"
                                            else "No tienes pedidos anteriores",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(displayOrders, key = { it.id }) { order ->
                                            OrderCard(
                                                order = order,
                                                onClick = { selectedOrder = order },
                                                onTrack = if (order.status in trackableStatuses) {
                                                    { onNavigateToTracking(order.id) }
                                                } else null,
                                                onRate = if (order.status in setOf("delivered", "completed")) {
                                                    { onNavigateToRating(order.id) }
                                                } else null
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    // Diálogo de confirmación de cancelación
    if (showCancelDialog) {
        val orderToCancel = selectedOrder
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            icon = { Icon(Icons.Default.Cancel, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("¿Cancelar pedido?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "El pedido #${orderToCancel?.id} aún no tiene conductor asignado. " +
                    "¿Deseas cancelarlo? Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        val orderId = orderToCancel?.id ?: return@Button
                        cancellingOrderFor = orderId
                        viewModel.cancelOrder(orderId,
                            onSuccess = {
                                cancellingOrderFor = null
                                selectedOrder = null
                                scope.launch { snackbarHostState.showSnackbar("Pedido cancelado") }
                            },
                            onError = { msg ->
                                cancellingOrderFor = null
                                scope.launch { snackbarHostState.showSnackbar(msg) }
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Sí, cancelar") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("No, mantener") }
            }
        )
    }

    // Detalle de orden como bottom sheet
    selectedOrder?.let { order ->
        OrderDetailSheet(
            order = order,
            isConfirming = confirmingReceiptFor == order.id,
            isCancelling = cancellingOrderFor == order.id,
            onDismiss = { selectedOrder = null },
            onCancelOrder = { showCancelDialog = true },
            onTrack = if (order.status in trackableStatuses) {
                { onNavigateToTracking(order.id) }
            } else null,
            onRate = if (order.status in setOf("delivered", "completed")) {
                {
                    selectedOrder = null
                    onNavigateToRating(order.id)
                }
            } else null,
            onConfirmReceipt = {
                confirmingReceiptFor = order.id
                viewModel.confirmReceipt(order.id,
                    onSuccess = {
                        confirmingReceiptFor = null
                        selectedOrder = selectedOrder?.copy(status = "completed")
                        scope.launch { snackbarHostState.showSnackbar("¡Recepción confirmada! Gracias por tu compra.") }
                    },
                    onError = { msg ->
                        confirmingReceiptFor = null
                        scope.launch { snackbarHostState.showSnackbar(msg) }
                    }
                )
            }
        )
    }
}

// ─── Tarjeta de orden ─────────────────────────────────────────────────────────

private val liveStatuses = setOf("paid_pending_driver", "driver_assigned", "picked_up")

@Composable
private fun OrderCard(order: OrderOut, onClick: () -> Unit, onTrack: (() -> Unit)? = null, onRate: (() -> Unit)? = null) {
    val statusInfo = orderStatusInfo(order.status)
    val isLive = order.status in liveStatuses

    val pulse = rememberInfiniteTransition(label = "badge_pulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 1f, targetValue = if (isLive) 1.06f else 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "badge_scale"
    )

    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        shadowElevation = 3.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Número de orden
                Column(Modifier.weight(1f)) {
                    Text(
                        "Pedido #${order.id}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        order.createdAt?.take(10) ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Badge de estado
                Surface(
                    modifier = Modifier.scale(pulseScale),
                    shape = RoundedCornerShape(20.dp),
                    color = statusInfo.bgColor
                ) {
                    Row(
                        Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isLive) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(statusInfo.textColor, CircleShape)
                            )
                            Spacer(Modifier.width(4.dp))
                        } else {
                            Icon(statusInfo.icon, null,
                                modifier = Modifier.size(13.dp), tint = statusInfo.textColor)
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(statusInfo.label, fontSize = 11.sp,
                            fontWeight = FontWeight.Bold, color = statusInfo.textColor)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(Modifier.height(10.dp))

            Row(Modifier.fillMaxWidth()) {
                // Items
                Column(Modifier.weight(1f)) {
                    Text("${order.items.size} artículo(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    order.items.take(2).forEach { item ->
                        Text("${item.quantity}× ${item.productName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1)
                    }
                    if (order.items.size > 2) {
                        Text("+ ${order.items.size - 2} más",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
                // Total
                Column(horizontalAlignment = Alignment.End) {
                    if (order.deliveryRequired && order.deliveryFee > 0) {
                        Text("Delivery: $${String.format("%.2f", order.deliveryFee)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        "$${String.format("%.2f", order.total)}",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (order.deliveryRequired && order.dropoffAddress != null) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, null, modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text(order.dropoffAddress, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }

            // Botón de rastreo para pedidos activos con conductor
            if (onTrack != null) {
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = onTrack,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) {
                    Icon(Icons.Default.MyLocation, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Rastrear pedido", fontWeight = FontWeight.Bold)
                }
            }

            // Botón de calificar para pedidos entregados
            if (onRate != null) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onRate,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Star, null, modifier = Modifier.size(16.dp),
                        tint = Color(0xFFF57C00))
                    Spacer(Modifier.width(8.dp))
                    Text("Calificar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─── Detalle de orden (BottomSheet) ───────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderDetailSheet(
    order: OrderOut,
    onDismiss: () -> Unit,
    isConfirming: Boolean = false,
    onConfirmReceipt: () -> Unit = {},
    isCancelling: Boolean = false,
    onCancelOrder: () -> Unit = {},
    onTrack: (() -> Unit)? = null,
    onRate: (() -> Unit)? = null
) {
    val statusInfo = orderStatusInfo(order.status)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp).navigationBarsPadding()
        ) {
            // Header
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Pedido #${order.id}", fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleLarge)
                    Text(order.createdAt?.take(16)?.replace("T", " ") ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(shape = RoundedCornerShape(20.dp), color = statusInfo.bgColor) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(statusInfo.icon, null, modifier = Modifier.size(15.dp), tint = statusInfo.textColor)
                        Spacer(Modifier.width(5.dp))
                        Text(statusInfo.label, fontWeight = FontWeight.Bold,
                            fontSize = 12.sp, color = statusInfo.textColor)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Artículos (agrupados por establecimiento)
            Text("Artículos", fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))

            // Agrupar por company_name para mostrar el establecimiento
            val byStore = order.items.groupBy { it.companyName ?: "Establecimiento" }
            byStore.forEach { (storeName, storeItems) ->
                if (byStore.size > 1) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Store, null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(4.dp))
                        Text(storeName, style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.height(4.dp))
                }
                storeItems.forEach { item ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                        Text("${item.quantity}×", color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Text(item.productName, modifier = Modifier.weight(1f))
                        Text("$${String.format("%.2f", item.unitPrice * item.quantity)}",
                            fontWeight = FontWeight.SemiBold)
                    }
                }
                if (byStore.size > 1) Spacer(Modifier.height(6.dp))
            }

            Spacer(Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(Modifier.height(12.dp))

            // Totales
            if (order.deliveryRequired) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$${String.format("%.2f", order.subtotal)}")
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Delivery", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$${String.format("%.2f", order.deliveryFee)}")
                }
                Spacer(Modifier.height(4.dp))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium)
                Text("$${String.format("%.2f", order.total)}",
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary)
            }

            // Dirección de entrega
            if (order.deliveryRequired && order.dropoffAddress != null) {
                Spacer(Modifier.height(16.dp))
                Surface(shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Place, null, tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(order.dropoffAddress, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Observaciones del comprador
            if (!order.observations.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.ChatBubbleOutline, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Indicaciones", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(order.observations, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Fecha de pago
            if (order.paidAt != null) {
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFF2E7D32))
                    Spacer(Modifier.width(6.dp))
                    Text("Pagado: ${order.paidAt.take(16).replace("T", " ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Botón de rastreo en tiempo real
            if (onTrack != null) {
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        onDismiss()
                        onTrack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) {
                    Icon(Icons.Default.MyLocation, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("RASTREAR EN MAPA", fontWeight = FontWeight.Bold)
                }
            }

            // Botón de calificación — pedidos entregados
            if (onRate != null) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onRate,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFC107)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFC107))
                ) {
                    Icon(Icons.Default.Star, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("CALIFICAR PEDIDO", fontWeight = FontWeight.Bold)
                }
            }

            // Botón de cancelación — solo mientras busca conductor
            if (order.status == "paid_pending_driver") {
                Spacer(Modifier.height(20.dp))
                OutlinedButton(
                    onClick = onCancelOrder,
                    enabled = !isCancelling,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    if (isCancelling) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.error,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Cancel, null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isCancelling) "Cancelando..." else "CANCELAR PEDIDO",
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Solo disponible mientras buscamos conductor",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Botón de confirmación de recepción — solo cuando el pedido fue entregado
            if (order.status == "delivered") {
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onConfirmReceipt,
                    enabled = !isConfirming,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
                ) {
                    if (isConfirming) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.VerifiedUser, null, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isConfirming) "Confirmando..." else "CONFIRMAR RECEPCIÓN",
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Confirma que recibiste tu pedido correctamente",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ─── Estado vacío ─────────────────────────────────────────────────────────────

@Composable
private fun EmptyOrders() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
            Icon(Icons.Default.ShoppingBag, null, modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            Spacer(Modifier.height(20.dp))
            Text("Aún no tienes compras", fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge)
            Text("Explora los comercios y realiza tu primer pedido.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp))
        }
    }
}

// ─── Mapeador de estados ──────────────────────────────────────────────────────

private data class StatusInfo(
    val label: String,
    val bgColor: Color,
    val textColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
private fun orderStatusInfo(status: String): StatusInfo {
    return when (status) {
        "pending_payment" -> StatusInfo(
            "Pendiente de pago",
            Color(0xFFFFF3E0), Color(0xFFE65100), Icons.Default.HourglassEmpty
        )
        "paid_pending_driver" -> StatusInfo(
            "Buscando conductor",
            Color(0xFFE3F2FD), Color(0xFF1565C0), Icons.Default.SearchOff
        )
        "driver_assigned" -> StatusInfo(
            "Conductor asignado",
            Color(0xFFE8F5E9), Color(0xFF2E7D32), Icons.Default.Person
        )
        "ready_for_pickup" -> StatusInfo(
            "Listo para recoger",
            Color(0xFFE8F5E9), Color(0xFF1B5E20), Icons.Default.CheckCircle
        )
        "picked_up" -> StatusInfo(
            "En camino",
            Color(0xFFE3F2FD), Color(0xFF0D47A1), Icons.Default.LocalShipping
        )
        "delivered" -> StatusInfo(
            "Entregado",
            Color(0xFFE8F5E9), Color(0xFF1B5E20), Icons.Default.CheckCircle
        )
        "completed" -> StatusInfo(
            "Completado",
            Color(0xFFE8F5E9), Color(0xFF1B5E20), Icons.Default.TaskAlt
        )
        "cancelled" -> StatusInfo(
            "Cancelado",
            Color(0xFFFFEBEE), Color(0xFFB71C1C), Icons.Default.Cancel
        )
        "refunded" -> StatusInfo(
            "Reembolsado",
            Color(0xFFF3E5F5), Color(0xFF6A1B9A), Icons.Default.AssignmentReturn
        )
        else -> StatusInfo(
            status, Color(0xFFF5F5F5), Color(0xFF424242), Icons.Default.Info
        )
    }
}
