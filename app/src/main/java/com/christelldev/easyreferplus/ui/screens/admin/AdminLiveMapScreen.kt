package com.christelldev.easyreferplus.ui.screens.admin

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.christelldev.easyreferplus.data.model.AdminDriverLocation
import com.christelldev.easyreferplus.ui.utils.MarkerUtils
import com.christelldev.easyreferplus.ui.viewmodel.AdminDeliveryViewModel
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLiveMapScreen(
    viewModel: AdminDeliveryViewModel,
    token: String,
    onNavigateBack: () -> Unit
) {
    val driverLocations by viewModel.driverLocations.collectAsState()
    val context = LocalContext.current

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-0.1807, -78.4678), 12f)
    }

    LaunchedEffect(Unit) {
        viewModel.loadDriverLocations()
        viewModel.startLiveDriverUpdates(token)
        try { MapsInitializer.initialize(context) } catch (_: Exception) {}
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopLiveDriverUpdates() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Conductores en Vivo", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Surface(color = Color(0xFF2E7D32), shape = CircleShape) {
                            Text(
                                "EN VIVO",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadDriverLocations() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {

            // Mapa principal
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = true)
            ) {
                driverLocations.forEach { driver ->
                    if (driver.lat != null && driver.lng != null) {
                        val icon = remember(driver.driverId, driver.isOnDuty, driver.name) {
                            MarkerUtils.createDriverMarker(driver.name, driver.isOnDuty)
                        }
                        Marker(
                            state = MarkerState(position = LatLng(driver.lat, driver.lng)),
                            title = driver.name ?: "Conductor #${driver.driverId}",
                            snippet = when {
                                driver.isBusy -> "Con pedido #${driver.activeOrderId}"
                                driver.isOnDuty -> "Disponible"
                                else -> "Fuera de turno"
                            },
                            icon = icon
                        )
                    }
                }
            }

            // Tarjeta de conteo activos – esquina superior derecha
            val onDutyCount = driverLocations.count { it.isOnDuty }
            val busyCount = driverLocations.count { it.isBusy }
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(9.dp).background(Color(0xFF27AE60), CircleShape))
                        Spacer(Modifier.width(6.dp))
                        Text("$onDutyCount en turno", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        if (driverLocations.isNotEmpty()) {
                            Text(" / ${driverLocations.size}", fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (busyCount > 0) {
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(9.dp).background(Color(0xFFE65100), CircleShape))
                            Spacer(Modifier.width(6.dp))
                            Text("$busyCount con pedido", fontSize = 12.sp,
                                color = Color(0xFFBF360C), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Panel inferior – estado en tiempo real de cada conductor
            if (driverLocations.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            "Estado de conductores",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(driverLocations, key = { it.driverId }) { driver ->
                                DriverStatusChip(driver)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverStatusChip(driver: AdminDriverLocation) {
    val bgColor by animateColorAsState(
        targetValue = when {
            driver.isBusy -> Color(0xFFFFF3E0)
            driver.isOnDuty -> Color(0xFFE8F5E9)
            else -> Color(0xFFF5F5F5)
        },
        label = "chipBg_${driver.driverId}"
    )
    val dotColor by animateColorAsState(
        targetValue = when {
            driver.isBusy -> Color(0xFFE65100)
            driver.isOnDuty -> Color(0xFF27AE60)
            else -> Color(0xFF9E9E9E)
        },
        label = "dotColor_${driver.driverId}"
    )
    val labelColor by animateColorAsState(
        targetValue = when {
            driver.isBusy -> Color(0xFFBF360C)
            driver.isOnDuty -> Color(0xFF1B5E20)
            else -> Color(0xFF757575)
        },
        label = "labelColor_${driver.driverId}"
    )
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(9.dp).background(dotColor, CircleShape))
            Spacer(Modifier.width(7.dp))
            Column {
                Text(
                    driver.name ?: "#${driver.driverId}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    when {
                        driver.isBusy -> "Con pedido #${driver.activeOrderId}"
                        driver.isOnDuty -> "Disponible"
                        else -> "Fuera de turno"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor
                )
                driver.vehiclePlate?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
