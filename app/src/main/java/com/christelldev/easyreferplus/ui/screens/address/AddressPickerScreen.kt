package com.christelldev.easyreferplus.ui.screens.address

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.christelldev.easyreferplus.data.model.SavedAddress
import com.christelldev.easyreferplus.ui.viewmodel.AddressSaveState
import com.christelldev.easyreferplus.ui.viewmodel.AddressViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private data class LabelOption(val key: String, val display: String, val icon: ImageVector)

private val labelOptions = listOf(
    LabelOption("Casa", "Casa", Icons.Default.Home),
    LabelOption("Trabajo", "Trabajo", Icons.Default.Work),
    LabelOption("Otro", "Otro", Icons.Default.Place)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressPickerScreen(
    viewModel: AddressViewModel,
    editingAddress: SavedAddress? = null,
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit
) {
    val saveState by viewModel.saveState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isEditing = editingAddress != null
    val isDark = isSystemInDarkTheme()
    val contentColor = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White

    var selectedLabel by remember { mutableStateOf(editingAddress?.label ?: "Casa") }
    var customLabel by remember { mutableStateOf("") }
    var addressText by remember { mutableStateOf(editingAddress?.address ?: "") }
    var lat by remember { mutableStateOf(editingAddress?.lat) }
    var lng by remember { mutableStateOf(editingAddress?.lng) }
    var isDefault by remember { mutableStateOf(editingAddress?.isDefault ?: false) }
    var isReverseGeocoding by remember { mutableStateOf(false) }

    // Determine the label to use
    val isCustomLabel = remember(editingAddress) {
        editingAddress != null && labelOptions.none { it.key.equals(editingAddress.label, ignoreCase = true) }
    }
    LaunchedEffect(isCustomLabel) {
        if (isCustomLabel && editingAddress != null) {
            selectedLabel = "Otro"
            customLabel = editingAddress.label
        }
    }

    val hasCoords = lat != null && lng != null
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(lat ?: -0.22, lng ?: -78.51), if (hasCoords) 16f else 12f
        )
    }

    // Sync camera when coordinates change
    LaunchedEffect(lat, lng) {
        if (lat != null && lng != null) {
            cameraState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(lat!!, lng!!), 16f))
        }
    }

    // Reverse geocode when pin moves
    fun reverseGeocode(latitude: Double, longitude: Double) {
        scope.launch {
            isReverseGeocoding = true
            try {
                val result = withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    val geocoder = Geocoder(context, Locale("es", "EC"))
                    geocoder.getFromLocation(latitude, longitude, 1)
                }
                result?.firstOrNull()?.let { addr ->
                    val parts = buildList {
                        addr.thoroughfare?.let { add(it) }
                        addr.subThoroughfare?.let { add(it) }
                        addr.locality?.let { add(it) }
                        addr.adminArea?.let { add(it) }
                    }
                    if (parts.isNotEmpty()) {
                        addressText = parts.joinToString(", ")
                    }
                }
            } catch (_: Exception) { /* geocoding failed silently */ }
            isReverseGeocoding = false
        }
    }

    // GPS permission
    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { location ->
                    location?.let {
                        lat = it.latitude
                        lng = it.longitude
                        reverseGeocode(it.latitude, it.longitude)
                    }
                }
        }
    }

    fun requestGps() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { location ->
                    location?.let {
                        lat = it.latitude
                        lng = it.longitude
                        reverseGeocode(it.latitude, it.longitude)
                    }
                }
        } else {
            locationLauncher.launch(permission)
        }
    }

    // Auto-request GPS if creating new (no existing coordinates)
    LaunchedEffect(Unit) {
        if (!isEditing) requestGps()
    }

    // Observe save state
    LaunchedEffect(saveState) {
        if (saveState is AddressSaveState.Success) {
            viewModel.resetSaveState()
            onSaved()
        }
    }

    Scaffold(
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
                    title = {
                        Text(
                            if (isEditing) "Editar dirección" else "Nueva dirección",
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = contentColor)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    windowInsets = WindowInsets.statusBars
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                ) {
                    // ── Map ──────────────────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraState,
                            onMapClick = { latLng ->
                                lat = latLng.latitude
                                lng = latLng.longitude
                                reverseGeocode(latLng.latitude, latLng.longitude)
                            },
                            uiSettings = MapUiSettings(
                                zoomControlsEnabled = false,
                                myLocationButtonEnabled = false
                            )
                        ) {
                            if (hasCoords) {
                                Marker(
                                    state = MarkerState(position = LatLng(lat!!, lng!!)),
                                    title = "Ubicación seleccionada",
                                    draggable = true
                                )
                            }
                        }

                        if (!hasCoords) {
                            Box(
                                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        color = Color.White,
                                        strokeWidth = 3.dp
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Obteniendo ubicación...",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        // GPS button
                        SmallFloatingActionButton(
                            onClick = { requestGps() },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp),
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary,
                            elevation = FloatingActionButtonDefaults.elevation(4.dp)
                        ) {
                            Icon(Icons.Default.MyLocation, "Centrar en mi ubicación", modifier = Modifier.size(20.dp))
                        }

                        // Hint overlay
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 12.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            shadowElevation = 4.dp
                        ) {
                            Text(
                                "Toca el mapa o arrastra el pin",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        // ── Label chips ──────────────────────────────────────────
                        Text(
                            "Etiqueta",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            labelOptions.forEach { option ->
                                val isSelected = selectedLabel == option.key
                                Surface(
                                    modifier = Modifier
                                        .clickable { selectedLabel = option.key }
                                        .weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            option.icon, null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary
                                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            option.display,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Custom label field (when "Otro" is selected)
                        if (selectedLabel == "Otro") {
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = customLabel,
                                onValueChange = { customLabel = it },
                                label = { Text("Nombre personalizado") },
                                placeholder = { Text("Ej: Gym, Universidad...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Edit, null) }
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // ── Address text ─────────────────────────────────────────
                        OutlinedTextField(
                            value = addressText,
                            onValueChange = { addressText = it },
                            label = { Text("Dirección / Referencias") },
                            placeholder = { Text("Ej: Av. Principal y Calle 2, edificio azul") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 3,
                            leadingIcon = { Icon(Icons.Default.Place, null) },
                            trailingIcon = if (isReverseGeocoding) {
                                { CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp) }
                            } else null
                        )

                        Spacer(Modifier.height(16.dp))

                        // ── Default toggle ───────────────────────────────────────
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isDefault = !isDefault }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    null,
                                    tint = if (isDefault) MaterialTheme.colorScheme.primary
                                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        "Dirección principal",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        "Se usará por defecto en tus compras",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isDefault,
                                    onCheckedChange = { isDefault = it }
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // ── Save button ──────────────────────────────────────────
                        val finalLabel = if (selectedLabel == "Otro") customLabel.ifBlank { "Otro" } else selectedLabel
                        val canSave = addressText.isNotBlank() && hasCoords && finalLabel.isNotBlank()
                        val isSaving = saveState is AddressSaveState.Saving

                        Button(
                            onClick = {
                                if (isEditing) {
                                    viewModel.updateAddress(
                                        addressId = editingAddress!!.id,
                                        label = finalLabel,
                                        address = addressText,
                                        lat = lat,
                                        lng = lng,
                                        isDefault = isDefault
                                    )
                                } else {
                                    viewModel.createAddress(
                                        label = finalLabel,
                                        address = addressText,
                                        lat = lat!!,
                                        lng = lng!!,
                                        isDefault = isDefault
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            enabled = canSave && !isSaving
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(10.dp))
                            }
                            Icon(
                                if (isEditing) Icons.Default.Check else Icons.Default.Add,
                                null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (isEditing) "Guardar cambios" else "Guardar dirección",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Error message
                        if (saveState is AddressSaveState.Error) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                (saveState as AddressSaveState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(Modifier.height(24.dp))
                        Spacer(Modifier.navigationBarsPadding())
                    }
                }
            }
        }
    }
}
