package com.christelldev.easyreferplus.ui.screens.delivery

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.core.content.ContextCompat
import com.christelldev.easyreferplus.data.model.UserSavedLocation
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryAddressScreen(
    deliveryLocationViewModel: com.christelldev.easyreferplus.ui.viewmodel.DeliveryLocationViewModel? = null,
    onBack: () -> Unit,
    onNext: (lat: Double, lng: Double, address: String, notes: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pinLat by remember { mutableDoubleStateOf(-0.22) }
    var pinLng by remember { mutableDoubleStateOf(-78.51) }
    var hasGps by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var detectedAddress by remember { mutableStateOf("") }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(pinLat, pinLng), 14f)
    }
    val locations by (deliveryLocationViewModel?.locations?.collectAsState()
        ?: remember { mutableStateOf(emptyList()) })

    val favorites = remember(locations) { locations.filter { it.isFavorite } }
    val recent = remember(locations) { locations.filter { !it.isFavorite }.take(3) }

    // Actualiza pin al centro del mapa cuando el usuario para de mover
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            val center = cameraPositionState.position.target
            pinLat = center.latitude
            pinLng = center.longitude
            // Reverse geocoding
            scope.launch {
                try {
                    @Suppress("DEPRECATION")
                    val results = Geocoder(context).getFromLocation(pinLat, pinLng, 1)
                    detectedAddress = results?.firstOrNull()?.getAddressLine(0) ?: ""
                } catch (_: Exception) {}
            }
        }
    }

    fun requestLocation() {
        try {
            val fused = LocationServices.getFusedLocationProviderClient(context)
            fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        pinLat = location.latitude
                        pinLng = location.longitude
                        hasGps = true
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(LatLng(pinLat, pinLng), 16f)
                            )
                        }
                    }
                }
        } catch (_: Exception) {}
    }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) requestLocation() }

    LaunchedEffect(Unit) {
        deliveryLocationViewModel?.loadLocations()
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            requestLocation()
        } else {
            locationLauncher.launch(permission)
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Capa 1: mapa ocupa toda la pantalla
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
        )

        // Capa 2: gradiente header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                            Color.Transparent
                        )
                    )
                )
                .align(Alignment.TopStart)
        )

        // Capa 3: TopAppBar transparente
        TopAppBar(
            title = { Text("¿Dónde entregamos?", color = MaterialTheme.colorScheme.onPrimary) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null,
                        tint = MaterialTheme.colorScheme.onPrimary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            modifier = Modifier.align(Alignment.TopStart)
        )

        // Capa 4: crosshair fijo en el centro
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(40.dp)
        )

        // Capa 5: FAB "Mi ubicación"
        SmallFloatingActionButton(
            onClick = { requestLocation() },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 240.dp, end = 16.dp)
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Mi ubicación",
                modifier = Modifier.size(20.dp))
        }

        // Capa 6: panel inferior fijo
        DeliveryBottomPanel(
            detectedAddress = detectedAddress,
            notes = notes,
            onNotesChange = { notes = it },
            favorites = favorites,
            recent = recent,
            onSelectLocation = { loc ->
                pinLat = loc.latitude
                pinLng = loc.longitude
                detectedAddress = loc.address
                scope.launch {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(LatLng(loc.latitude, loc.longitude), 16f)
                    )
                }
            },
            selectedLat = pinLat,
            selectedLng = pinLng,
            canContinue = detectedAddress.isNotBlank() || hasGps,
            onContinue = { onNext(pinLat, pinLng, detectedAddress, notes) },
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

@Composable
private fun DeliveryBottomPanel(
    detectedAddress: String,
    notes: String,
    onNotesChange: (String) -> Unit,
    favorites: List<UserSavedLocation>,
    recent: List<UserSavedLocation>,
    onSelectLocation: (UserSavedLocation) -> Unit,
    selectedLat: Double?,
    selectedLng: Double?,
    canContinue: Boolean,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .size(40.dp, 4.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        RoundedCornerShape(2.dp)
                    )
            )
            Spacer(modifier = Modifier.height(12.dp))
            // Título y dirección detectada
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Punto de entrega",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Place, null, modifier = Modifier.size(18.dp).padding(top = 2.dp),
                    tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = detectedAddress.ifBlank { "Mueve el mapa para seleccionar..." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    modifier = Modifier.weight(1f)
                )
            }
            // Favoritos
            if (favorites.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Favoritos", style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    favorites.forEach { loc ->
                        FilterChip(
                            selected = selectedLat == loc.latitude && selectedLng == loc.longitude,
                            onClick = { onSelectLocation(loc) },
                            label = { Text(loc.alias ?: "Fav") },
                            leadingIcon = {
                                Icon(Icons.Default.Star, null, modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary)
                            }
                        )
                    }
                }
            }
            // Recientes
            if (recent.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Recientes", style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(4.dp))
                recent.forEach { loc ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onSelectLocation(loc) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.History, null, modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(loc.alias ?: loc.address.take(32), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface, maxLines = 1,
                            modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("Instrucciones adicionales (piso, referencia...)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3,
                leadingIcon = { Icon(Icons.Default.StickyNote2, null) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onContinue,
                enabled = canContinue,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.ArrowForward, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Continuar", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}
