package com.christelldev.easyreferplus.ui.screens.driver

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.christelldev.easyreferplus.ui.components.SwipeToConfirmButton
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.christelldev.easyreferplus.ui.viewmodel.DriverViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverOrderScreen(
    viewModel: DriverViewModel,
    onNavigateBack: () -> Unit,
    onOrderCompleted: () -> Unit = {},
    onNavigateToChat: ((orderId: Int, buyerName: String) -> Unit)? = null,
) {
    val activeOrder by viewModel.activeOrder.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var deviceLocation by remember { mutableStateOf<LatLng?>(null) }
    var isPickingUp by remember { mutableStateOf(false) }
    var isDelivering by remember { mutableStateOf(false) }
    var showNavDialog by remember { mutableStateOf(false) }
    var navDestination by remember { mutableStateOf<LatLng?>(null) }
    var showPhotoDialog by remember { mutableStateOf(false) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var arrivedPickupSent by remember { mutableStateOf(false) }
    var pendingCameraLaunch by remember { mutableStateOf(false) }

    // Camera photo capture
    var photoFile by remember { mutableStateOf(File(context.cacheDir, "delivery_photo_${System.currentTimeMillis()}.jpg")) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoFile.exists()) {
            val order = activeOrder ?: return@rememberLauncherForActivityResult
            isUploadingPhoto = true
            val requestBody = photoFile.asRequestBody("image/jpeg".toMediaType())
            val part = MultipartBody.Part.createFormData("photo", photoFile.name, requestBody)
            viewModel.uploadDeliveryPhoto(order.id, part,
                onSuccess = {
                    isUploadingPhoto = false
                    // Now confirm delivery
                    isDelivering = true
                    viewModel.confirmDelivery(order.id,
                        onSuccess = {
                            isDelivering = false
                            viewModel.loadActiveOrder()
                            viewModel.loadProfile()
                        },
                        onError = { msg ->
                            isDelivering = false
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    )
                },
                onError = { msg ->
                    isUploadingPhoto = false
                    scope.launch { snackbarHostState.showSnackbar("Error subiendo foto: $msg") }
                }
            )
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && pendingCameraLaunch) {
            val newFile = File(context.cacheDir, "delivery_photo_${System.currentTimeMillis()}.jpg")
            photoFile = newFile
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", newFile)
            photoUri = uri
            cameraLauncher.launch(uri)
        }
        pendingCameraLaunch = false
    }

    val hasLocationPermission = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    val dropoffLatLng = remember(activeOrder) {
        val o = activeOrder
        if (o?.dropoffLat != null && o.dropoffLng != null) LatLng(o.dropoffLat, o.dropoffLng) else null
    }

    val pickupLatLng = remember(activeOrder) {
        val o = activeOrder
        if (o?.pickupLat != null && o.pickupLng != null) LatLng(o.pickupLat, o.pickupLng) else null
    }

    // Distance from driver to pickup in meters
    val distanceToPickup = remember(deviceLocation, pickupLatLng) {
        if (deviceLocation != null && pickupLatLng != null) {
            haversineMeters(deviceLocation!!.latitude, deviceLocation!!.longitude,
                pickupLatLng.latitude, pickupLatLng.longitude)
        } else null
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-0.1807, -78.4678), 15f)
    }

    LaunchedEffect(Unit) {
        viewModel.loadActiveOrder()
    }

    LaunchedEffect(dropoffLatLng) {
        dropoffLatLng?.let {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 15f))
        }
    }

    LaunchedEffect(activeOrder) {
        if (activeOrder == null) onOrderCompleted()
    }

    // GPS del conductor
    DisposableEffect(Unit) {
        val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as? LocationManager
        val listener = LocationListener { location: Location ->
            deviceLocation = LatLng(location.latitude, location.longitude)
        }
        try {
            if (hasLocationPermission) {
                locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000L, 2f, listener)
            }
        } catch (_: Exception) {}
        onDispose { try { locationManager?.removeUpdates(listener) } catch (_: Exception) {} }
    }

    // Navigation dialog
    if (showNavDialog && navDestination != null) {
        AlertDialog(
            onDismissRequest = { showNavDialog = false },
            icon = { Icon(Icons.Default.Navigation, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Abrir navegación", fontWeight = FontWeight.Bold) },
            text = { Text("¿Con qué aplicación quieres navegar?") },
            confirmButton = {
                TextButton(onClick = {
                    showNavDialog = false
                    val dest = navDestination!!
                    val uri = Uri.parse("google.navigation:q=${dest.latitude},${dest.longitude}&mode=d")
                    val intent = Intent(Intent.ACTION_VIEW, uri).setPackage("com.google.android.apps.maps")
                    try { context.startActivity(intent) } catch (_: Exception) {
                        // Fallback to browser
                        val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${dest.latitude},${dest.longitude}")
                        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                    }
                }) { Text("Google Maps") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showNavDialog = false
                    val dest = navDestination!!
                    val uri = Uri.parse("https://waze.com/ul?ll=${dest.latitude},${dest.longitude}&navigate=yes")
                    try { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) } catch (_: Exception) {
                        scope.launch { snackbarHostState.showSnackbar("Waze no está instalado") }
                    }
                }) { Text("Waze") }
            }
        )
    }

    // Photo confirmation dialog
    if (showPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoDialog = false },
            icon = { Icon(Icons.Default.CameraAlt, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Foto de evidencia", fontWeight = FontWeight.Bold) },
            text = { Text("Toma una foto del pedido entregado como evidencia.") },
            confirmButton = {
                Button(onClick = {
                    showPhotoDialog = false
                    val hasCameraPermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasCameraPermission) {
                        val newFile = File(context.cacheDir, "delivery_photo_${System.currentTimeMillis()}.jpg")
                        photoFile = newFile
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", newFile)
                        photoUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        pendingCameraLaunch = true
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }) { Text("Tomar foto") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPhotoDialog = false
                    // Deliver without photo
                    val order = activeOrder ?: return@TextButton
                    isDelivering = true
                    viewModel.confirmDelivery(order.id,
                        onSuccess = {
                            isDelivering = false
                            viewModel.loadActiveOrder()
                            viewModel.loadProfile()
                        },
                        onError = { msg ->
                            isDelivering = false
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    )
                }) { Text("Sin foto") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pedido Activo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (onNavigateToChat != null && activeOrder != null) {
                        IconButton(onClick = {
                            val o = activeOrder!!
                            onNavigateToChat(o.id, "Cliente")
                        }) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat con cliente")
                        }
                    }
                    IconButton(onClick = { viewModel.loadActiveOrder() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (activeOrder == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val order = activeOrder!!
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {

                // Mapa de fondo
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false),
                    properties = MapProperties(isMyLocationEnabled = hasLocationPermission)
                ) {
                    pickupLatLng?.let {
                        Marker(
                            state = MarkerState(position = it),
                            title = "Punto de recogida",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                        )
                    }
                    dropoffLatLng?.let {
                        Marker(
                            state = MarkerState(position = it),
                            title = "Entregar a cliente",
                            snippet = order.dropoffAddress,
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        )
                    }
                    deviceLocation?.let {
                        Marker(
                            state = MarkerState(position = it),
                            title = "Mi ubicación",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                        )
                    }
                }

                // FABs column (right side)
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                        .padding(bottom = 220.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Navigate button
                    val currentDest = if (order.deliveryStatus.lowercase() in listOf("picked_up", "in_transit")) dropoffLatLng else pickupLatLng
                    currentDest?.let { dest ->
                        SmallFloatingActionButton(
                            onClick = {
                                navDestination = dest
                                showNavDialog = true
                            },
                            containerColor = Color(0xFF1565C0),
                            contentColor = Color.White,
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.Navigation, "Navegar", Modifier.size(18.dp))
                        }
                    }

                    pickupLatLng?.let { pickup ->
                        SmallFloatingActionButton(
                            onClick = { scope.launch { cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(pickup, 16f)) } },
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = Color(0xFF2E7D32),
                            shape = CircleShape
                        ) { Icon(Icons.Default.Store, "Ir a recogida", Modifier.size(18.dp)) }
                    }

                    dropoffLatLng?.let { dest ->
                        SmallFloatingActionButton(
                            onClick = { scope.launch { cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(dest, 15f)) } },
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.error,
                            shape = CircleShape
                        ) { Icon(Icons.Default.LocationOn, "Centrar en destino", Modifier.size(18.dp)) }
                    }

                    deviceLocation?.let { loc ->
                        SmallFloatingActionButton(
                            onClick = { scope.launch { cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(loc, 16f)) } },
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ) { Icon(Icons.Default.MyLocation, "Mi ubicación", Modifier.size(18.dp)) }
                    }
                }

                // Panel inferior
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Pedido #${order.id}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            HorizontalDivider()
                            InfoRow("Total", "$${String.format("%.2f", order.total)}")
                            InfoRow("Tarifa", "$${String.format("%.2f", order.deliveryFee)}")
                            order.dropoffAddress?.let { InfoRow("Dirección", it) }
                        }
                    }

                    // Status messages
                    when (order.orderStatus) {
                        "driver_assigned" -> StatusBanner(
                            text = "Esperando que el establecimiento prepare el pedido",
                            icon = Icons.Default.HourglassEmpty,
                            bgColor = Color(0xFFFFF3E0),
                            contentColor = Color(0xFFE65100)
                        )
                        "ready_for_pickup" -> StatusBanner(
                            text = "¡El pedido está listo! Dirígete al establecimiento.",
                            icon = Icons.Default.CheckCircle,
                            bgColor = Color(0xFFE8F5E9),
                            contentColor = Color(0xFF2E7D32)
                        )
                    }

                    // "Ya llegué" button when near pickup and pre-pickup
                    if (order.deliveryStatus.lowercase() in listOf("accepted", "assigned") &&
                        distanceToPickup != null && distanceToPickup < 200 && !arrivedPickupSent
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.arrivedAtPickup(order.id,
                                    onSuccess = {
                                        arrivedPickupSent = true
                                        scope.launch { snackbarHostState.showSnackbar("Llegada registrada. Esperando pedido.") }
                                    },
                                    onError = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1565C0)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1565C0))
                        ) {
                            Icon(Icons.Default.LocationOn, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("YA LLEGUÉ AL ESTABLECIMIENTO", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Action buttons
                    when (order.deliveryStatus.lowercase()) {
                        "accepted", "assigned" -> {
                            SwipeToConfirmButton(
                                text = if (order.orderStatus == "ready_for_pickup") "Desliza para confirmar recogida" else "Esperando establecimiento",
                                onConfirm = {
                                    isPickingUp = true
                                    viewModel.confirmPickup(order.id,
                                        onSuccess = {
                                            isPickingUp = false
                                            viewModel.loadActiveOrder()
                                        },
                                        onError = { msg ->
                                            isPickingUp = false
                                            scope.launch { snackbarHostState.showSnackbar(msg) }
                                        }
                                    )
                                },
                                enabled = order.orderStatus == "ready_for_pickup",
                                isLoading = isPickingUp,
                                backgroundColor = if (order.orderStatus == "ready_for_pickup") Color(0xFF1565C0) else Color(0xFF9E9E9E)
                            )
                        }
                        "picked_up", "in_transit" -> {
                            SwipeToConfirmButton(
                                text = "Desliza para confirmar entrega",
                                onConfirm = { showPhotoDialog = true },
                                enabled = !isDelivering && !isUploadingPhoto,
                                isLoading = isDelivering || isUploadingPhoto,
                                backgroundColor = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBanner(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bgColor: Color,
    contentColor: Color
) {
    Surface(shape = RoundedCornerShape(12.dp), color = bgColor) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = contentColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, color = contentColor, fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(value, fontWeight = FontWeight.Bold)
    }
}

/** Haversine distance in meters between two lat/lng points. */
private fun haversineMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val R = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
    return R * 2 * atan2(sqrt(a), sqrt(1 - a))
}
