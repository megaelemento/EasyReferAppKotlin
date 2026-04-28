package com.christelldev.easyreferplus.ui.screens.driver

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.RingtoneManager
import android.view.WindowManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.christelldev.easyreferplus.data.model.AvailableOrder
import com.christelldev.easyreferplus.data.model.DriverProfile
import com.christelldev.easyreferplus.service.DriverForegroundService
import com.christelldev.easyreferplus.ui.viewmodel.DriverUiState
import com.christelldev.easyreferplus.ui.viewmodel.DriverViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverHomeScreen(
    viewModel: DriverViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToActiveOrder: () -> Unit,
    onNavigateToZones: () -> Unit = {},
    onNavigateToInvitations: () -> Unit = {},
    onNavigateToConfig: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
) {
    val profile by viewModel.profile.collectAsState()
    val availableOrders by viewModel.availableOrders.collectAsState()
    val activeOrder by viewModel.activeOrder.collectAsState()
    val state by viewModel.state.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()
    val newOrderAlert by viewModel.newOrderAlert.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    // Reproducir alerta cuando llega un pedido nuevo
    LaunchedEffect(newOrderAlert) {
        if (newOrderAlert > 0) {
            try {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val ringtone = RingtoneManager.getRingtone(context, uri)
                ringtone?.play()
            } catch (_: Exception) {}
        }
    }

    // Mantener pantalla encendida cuando el ajuste está activo y el conductor está en turno
    val shouldKeepScreenOn = keepScreenOn && profile?.isOnDuty == true
    val activity = context as? android.app.Activity
    DisposableEffect(shouldKeepScreenOn) {
        if (shouldKeepScreenOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    var deviceLocation by remember { mutableStateOf<LatLng?>(null) }

    val hasLocationPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-0.1807, -78.4678), 16f)
    }

    val earningsToday by viewModel.earningsToday.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAll()
        viewModel.loadEarningsToday()
        viewModel.startLiveSocket()
        viewModel.startOrderPolling()
        try { MapsInitializer.initialize(context) } catch (_: Exception) {}
        context.getSharedPreferences("EasyReferPrefs", android.content.Context.MODE_PRIVATE)
            .edit().putBoolean("is_driver_account", true).apply()
    }

    LaunchedEffect(profile?.isOnDuty) {
        if (profile?.isOnDuty == true) {
            DriverForegroundService.start(context)
        } else if (profile?.isOnDuty == false) {
            DriverForegroundService.stop(context)
        }
    }

    DisposableEffect(Unit) {
        val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as? LocationManager
        val listener = LocationListener { location: Location ->
            val newPos = LatLng(location.latitude, location.longitude)
            deviceLocation = newPos
            viewModel.updateIdleLocation(newPos.latitude, newPos.longitude)
        }
        try {
            if (hasLocationPermission.value) {
                val lastKnown = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    ?: locationManager?.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                lastKnown?.let { loc ->
                    deviceLocation = LatLng(loc.latitude, loc.longitude)
                }
                locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1f, listener)
                try {
                    locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000L, 3f, listener)
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
        onDispose {
            try { locationManager?.removeUpdates(listener) } catch (_: Exception) {}
            viewModel.stopLiveSocket()
        }
    }

    var isAutoFollowEnabled by remember { mutableStateOf(true) }
    var lastGestureMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (cameraPositionState.isMoving && cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE) {
            isAutoFollowEnabled = false
            lastGestureMs = System.currentTimeMillis()
        }
    }

    LaunchedEffect(lastGestureMs) {
        if (lastGestureMs > 0L) {
            kotlinx.coroutines.delay(15_000)
            isAutoFollowEnabled = true
            deviceLocation?.let {
                cameraPositionState.animate(CameraUpdateFactory.newLatLng(it))
            }
        }
    }

    var initialCenterDone by remember { mutableStateOf(false) }
    LaunchedEffect(deviceLocation) {
        if (isAutoFollowEnabled) {
            deviceLocation?.let {
                if (!initialCenterDone) {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 16f))
                    initialCenterDone = true
                } else {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLng(it))
                }
            }
        }
    }

    LaunchedEffect(activeOrder) {
        if (activeOrder != null) onNavigateToActiveOrder()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Capa 1: Mapa (Fondo)
            if (profile != null) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false),
                    properties = MapProperties(isMyLocationEnabled = hasLocationPermission.value),
                    contentPadding = PaddingValues(top = 100.dp, bottom = 220.dp)
                ) {
                    availableOrders.forEach { order ->
                        if (order.pickupLat != null && order.pickupLng != null) {
                            Marker(
                                state = MarkerState(position = LatLng(order.pickupLat, order.pickupLng)),
                                title = "Recoger: Pedido #${order.id}",
                                snippet = order.pickupAddress,
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                            )
                        }
                        if (order.dropoffLat != null && order.dropoffLng != null) {
                            Marker(
                                state = MarkerState(position = LatLng(order.dropoffLat, order.dropoffLng)),
                                title = "Entregar: Pedido #${order.id}",
                                snippet = order.dropoffAddress,
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                            )
                        }
                    }
                }
            }

            // Capa 2: Gradiente Superior Consistente
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

            val contentTint = if (isDark) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface

            // Capa 3: TopAppBar y UI
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text("Panel de Entregas", fontWeight = FontWeight.Bold, color = contentTint) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = contentTint)
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.loadAll() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = contentTint)
                        }
                        IconButton(onClick = onNavigateToConfig) {
                            Icon(Icons.Default.Settings, contentDescription = "Configuración", tint = contentTint)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    windowInsets = WindowInsets.statusBars
                )

                if (profile == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Botón de centrar en mi ubicación
            SmallFloatingActionButton(
                onClick = {
                    isAutoFollowEnabled = true
                    deviceLocation?.let { scope.launch { cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 16f)) } }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp, bottom = 120.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = if (deviceLocation != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                shape = CircleShape
            ) {
                Icon(Icons.Default.MyLocation, "Mi ubicación")
            }

            // Controles inferiores
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (profile?.isOnDuty == true && availableOrders.isNotEmpty()) {
                    Column(
                        modifier = Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableOrders.take(5).forEach { order ->
                            AvailableOrderCard(
                                order = order,
                                onAccept = {
                                    viewModel.acceptOrder(order.id,
                                        onSuccess = { viewModel.loadActiveOrder() },
                                        onError = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                                    )
                                },
                                onReject = { viewModel.rejectOrder(order.id) }
                            )
                        }
                        if (availableOrders.size > 5) {
                            Text(
                                "+ ${availableOrders.size - 5} pedidos más disponibles",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
                
                if (profile?.isOnDuty == true && earningsToday != null) {
                    EarningsTodayCard(
                        earnings = earningsToday!!.earningsToday,
                        deliveries = earningsToday!!.deliveriesCount,
                        onNavigateToHistory = onNavigateToHistory
                    )
                }

                profile?.let {
                    DutyToggleCard(
                        profile = it,
                        isLoading = state is DriverUiState.Loading,
                        onToggle = {
                            viewModel.toggleOnDuty { ok, msg ->
                                if (ok) viewModel.loadAvailableOrders()
                                else scope.launch { snackbarHostState.showSnackbar(msg) }
                            }
                        }
                    )
                }
                
                // Margen inferior para barra de navegación
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }
}

@Composable
private fun DutyToggleCard(profile: DriverProfile, isLoading: Boolean, onToggle: () -> Unit) {
    val bgColor by animateColorAsState(
        if (profile.isOnDuty) Color(0xFF1B5E20) else MaterialTheme.colorScheme.surface,
        label = "dutyBg"
    )
    val contentColor = if (profile.isOnDuty) Color.White else MaterialTheme.colorScheme.onSurface
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PowerSettingsNew, null, tint = contentColor)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (profile.isOnDuty) "Turno Activo" else "Fuera de Turno",
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
            if (isLoading) CircularProgressIndicator(Modifier.size(24.dp), color = contentColor)
            else Switch(checked = profile.isOnDuty, onCheckedChange = { onToggle() })
        }
    }
}

@Composable
private fun AvailableOrderCard(order: AvailableOrder, onAccept: () -> Unit, onReject: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Pedido #${order.id}", fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text("$${String.format(Locale.US, "%.2f", order.deliveryFee)}",
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF2E7D32),
                    style = MaterialTheme.typography.titleMedium)
            }
            if (order.pickupAddress != null) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Store, null,
                        modifier = Modifier.size(14.dp).padding(top = 2.dp),
                        tint = Color(0xFF1565C0))
                    Spacer(Modifier.width(4.dp))
                    Text("Recoger: ${order.pickupAddress}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (order.dropoffAddress != null) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Place, null,
                        modifier = Modifier.size(14.dp).padding(top = 2.dp),
                        tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(4.dp))
                    Text("Entregar: ${order.dropoffAddress}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text("${order.itemsCount} artículo(s) · Total $${String.format(Locale.US, "%.2f", order.total)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("RECHAZAR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("ACEPTAR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun EarningsTodayCard(
    earnings: Double,
    deliveries: Int,
    onNavigateToHistory: () -> Unit
) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Payments, null,
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Hoy: $${String.format(Locale.US, "%.2f", earnings)}",
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    "$deliveries entrega${if (deliveries != 1) "s" else ""} completada${if (deliveries != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onNavigateToHistory) {
                Icon(Icons.Default.History, "Historial", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
