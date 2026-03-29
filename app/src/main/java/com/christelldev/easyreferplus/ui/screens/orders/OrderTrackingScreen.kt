package com.christelldev.easyreferplus.ui.screens.orders

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.christelldev.easyreferplus.ui.viewmodel.OrderTrackingState
import com.christelldev.easyreferplus.ui.viewmodel.OrderTrackingViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    viewModel: OrderTrackingViewModel,
    orderId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToChat: ((orderId: Int, driverName: String) -> Unit)? = null,
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(orderId) { viewModel.loadTracking(orderId) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-0.1807, -78.4678), 14f)
    }

    // Auto-fit bounds when markers load
    LaunchedEffect(state.pickupLatLng, state.dropoffLatLng, state.driverLatLng) {
        val points = listOfNotNull(state.pickupLatLng, state.dropoffLatLng, state.driverLatLng)
        if (points.size >= 2) {
            val bounds = LatLngBounds.builder().apply { points.forEach { include(it) } }.build()
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 80))
        } else if (points.size == 1) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(points[0], 15f))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pedido #${orderId}", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            if (onNavigateToChat != null && state.driverName != null && !state.isDelivered) {
                FloatingActionButton(
                    onClick = { onNavigateToChat(orderId, state.driverName ?: "Repartidor") },
                    containerColor = Color(0xFF1565C0),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, "Chat", tint = Color.White)
                }
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (state.error != null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ErrorOutline, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Text(state.error!!, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                }
            }
            return@Scaffold
        }

        Box(Modifier.fillMaxSize().padding(padding)) {
            // ── Map ──────────────────────────────────────────────────
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
            ) {
                // Pickup marker (green)
                state.pickupLatLng?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = state.companyName ?: "Punto de recogida",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    )
                }
                // Dropoff marker (red)
                state.dropoffLatLng?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Punto de entrega",
                        snippet = state.dropoffAddress,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                }
                // Driver marker (blue)
                state.driverLatLng?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = state.driverName ?: "Repartidor",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                        rotation = state.driverHeading,
                        flat = true
                    )
                }
            }

            // ── Progress Stepper (top) ───────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .align(Alignment.TopCenter),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 6.dp
            ) {
                DeliveryProgressStepper(
                    orderStatus = state.orderStatus,
                    deliveryStatus = state.deliveryStatus,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            // ── Arriving banner ──────────────────────────────────────
            if (state.isDriverArriving && !state.isDelivered) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 90.dp)
                        .align(Alignment.TopCenter),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF10B981)
                ) {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DirectionsBike, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Tu repartidor está llegando!",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // ── Center on markers buttons ────────────────────────────
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.pickupLatLng?.let { pickup ->
                    SmallFloatingActionButton(
                        onClick = { scope.launch { cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(pickup, 16f)) } },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = Color(0xFF2E7D32)
                    ) { Icon(Icons.Default.Store, "Recogida", Modifier.size(18.dp)) }
                }
                state.driverLatLng?.let { driver ->
                    SmallFloatingActionButton(
                        onClick = { scope.launch { cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(driver, 16f)) } },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = Color(0xFF1565C0)
                    ) { Icon(Icons.Default.DirectionsBike, "Repartidor", Modifier.size(18.dp)) }
                }
                state.dropoffLatLng?.let { dropoff ->
                    SmallFloatingActionButton(
                        onClick = { scope.launch { cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(dropoff, 16f)) } },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = Color(0xFFD32F2F)
                    ) { Icon(Icons.Default.Place, "Entrega", Modifier.size(18.dp)) }
                }
            }

            // ── Bottom card (driver info + ETA + actions) ────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 12.dp
            ) {
                DriverInfoCard(
                    state = state,
                    onCallDriver = {
                        state.driverPhone?.let { phone ->
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        }
                    }
                )
            }
        }
    }
}

// ─── Progress Stepper ────────────────────────────────────────────────────────

private data class StepInfo(val label: String, val icon: ImageVector)

private val steps = listOf(
    StepInfo("Confirmado", Icons.Default.CheckCircle),
    StepInfo("Preparando", Icons.Default.Restaurant),
    StepInfo("Recogido", Icons.Default.Inventory2),
    StepInfo("En camino", Icons.Default.DirectionsBike),
    StepInfo("Entregado", Icons.Default.Home),
)

@Composable
private fun DeliveryProgressStepper(
    orderStatus: String,
    deliveryStatus: String?,
    modifier: Modifier = Modifier
) {
    val activeIndex = when {
        orderStatus == "delivered" || orderStatus == "completed" -> 4
        deliveryStatus == "picked_up" || orderStatus == "picked_up" -> 3
        deliveryStatus == "accepted" || orderStatus == "ready_for_pickup" -> 2
        orderStatus == "driver_assigned" -> 1
        orderStatus == "paid_pending_driver" -> 0
        else -> 0
    }

    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        steps.forEachIndexed { index, step ->
            val isCompleted = index <= activeIndex
            val isActive = index == activeIndex
            val color by animateColorAsState(
                when {
                    isCompleted -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                }, label = "step$index"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    Modifier
                        .size(if (isActive) 32.dp else 26.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) color else Color.Transparent)
                        .then(
                            if (!isCompleted) Modifier.background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape
                            ) else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        step.icon, null,
                        modifier = Modifier.size(if (isActive) 18.dp else 14.dp),
                        tint = if (isCompleted) MaterialTheme.colorScheme.onPrimary
                               else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    step.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCompleted) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

// ─── Driver Info Card ────────────────────────────────────────────────────────

@Composable
private fun DriverInfoCard(
    state: OrderTrackingState,
    onCallDriver: () -> Unit
) {
    Column(modifier = Modifier.padding(20.dp)) {
        // ETA prominente
        if (!state.isDelivered && state.etaMinutes != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Schedule, null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "Llega en ~${state.etaMinutes} min",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                if (state.distanceKm != null) {
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "${String.format("%.1f", state.distanceKm)} km",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        if (state.isDelivered) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981), modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(10.dp))
                Text("Pedido entregado", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black, color = Color(0xFF10B981))
            }
            Spacer(Modifier.height(14.dp))
        }

        // Driver info row
        if (state.driverName != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Driver avatar
                Box(
                    Modifier.size(48.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.driverSelfieUrl != null) {
                        AsyncImage(
                            model = state.driverSelfieUrl,
                            contentDescription = "Foto repartidor",
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(state.driverName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    if (state.vehiclePlate != null) {
                        Text(
                            "${state.vehicleType ?: "Vehículo"} · ${state.vehiclePlate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Action buttons
                if (state.driverPhone != null) {
                    IconButton(onClick = onCallDriver) {
                        Icon(Icons.Default.Call, "Llamar", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        } else {
            // No driver yet
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(12.dp))
                Text("Buscando repartidor...", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Order summary
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        state.companyName ?: "Pedido",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "${state.itemsCount} artículo${if (state.itemsCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "$${String.format("%.2f", state.total)}",
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.navigationBarsPadding())
    }
}
