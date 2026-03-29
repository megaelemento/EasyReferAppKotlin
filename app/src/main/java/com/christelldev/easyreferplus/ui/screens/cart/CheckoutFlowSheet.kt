package com.christelldev.easyreferplus.ui.screens.cart

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.christelldev.easyreferplus.data.model.CartItem
import com.christelldev.easyreferplus.data.model.DeliveryOption
import com.christelldev.easyreferplus.data.model.SavedAddress
import com.christelldev.easyreferplus.ui.viewmodel.CheckoutFlowState
import com.christelldev.easyreferplus.ui.viewmodel.AddressViewModel
import com.christelldev.easyreferplus.ui.viewmodel.OrderViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

private enum class CheckoutStep {
    DELIVERY_QUESTION, ADDRESS_INPUT, DELIVERY_SELECTION, ORDER_SUMMARY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutFlowSheet(
    cartItems: List<CartItem>,
    cartTotal: Double,
    orderViewModel: OrderViewModel,
    addressViewModel: AddressViewModel? = null,
    onDismiss: () -> Unit,
    onSuccess: (orderId: Int) -> Unit
) {
    val checkoutState by orderViewModel.checkoutState.collectAsState()
    val savedAddresses = addressViewModel?.let {
        val addrs by it.addresses.collectAsState()
        addrs
    } ?: emptyList()
    var step by remember { mutableStateOf(CheckoutStep.DELIVERY_QUESTION) }
    var needsDelivery by remember { mutableStateOf(false) }
    var dropoffAddress by remember { mutableStateOf("") }
    var dropoffLat by remember { mutableStateOf<Double?>(null) }
    var dropoffLng by remember { mutableStateOf<Double?>(null) }
    var selectedDelivery by remember { mutableStateOf<DeliveryOption?>(null) }
    var tipAmount by remember { mutableDoubleStateOf(0.0) }
    val context = LocalContext.current

    // Load saved addresses when opening
    LaunchedEffect(Unit) { addressViewModel?.loadAddresses() }

    // Launcher para pedir permiso de ubicación
    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { location ->
                    location?.let {
                        dropoffLat = it.latitude
                        dropoffLng = it.longitude
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
                        dropoffLat = it.latitude
                        dropoffLng = it.longitude
                    }
                }
        } else {
            locationLauncher.launch(permission)
        }
    }

    // Auto-centrar en la ubicación al entrar al paso de dirección
    LaunchedEffect(step) {
        if (step == CheckoutStep.ADDRESS_INPUT) requestGps()
    }

    // Observar estado de checkout para Processing/Success/Error
    LaunchedEffect(checkoutState) {
        when (checkoutState) {
            is CheckoutFlowState.Success -> {
                val s = checkoutState as CheckoutFlowState.Success
                onSuccess(s.orderId)
                orderViewModel.resetCheckout()
            }
            else -> {}
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Contenido según estado/step
            when {
                checkoutState is CheckoutFlowState.Processing -> ProcessingContent()

                checkoutState is CheckoutFlowState.Error -> ErrorContent(
                    message = (checkoutState as CheckoutFlowState.Error).message,
                    onRetry = { orderViewModel.resetCheckout() }
                )

                step == CheckoutStep.DELIVERY_QUESTION -> DeliveryQuestionContent(
                    onNo = {
                        needsDelivery = false
                        step = CheckoutStep.ORDER_SUMMARY
                    },
                    onYes = {
                        needsDelivery = true
                        step = CheckoutStep.ADDRESS_INPUT
                    },
                    onDismiss = onDismiss
                )

                step == CheckoutStep.ADDRESS_INPUT -> AddressInputContent(
                    address = dropoffAddress,
                    onAddressChange = { dropoffAddress = it },
                    lat = dropoffLat,
                    lng = dropoffLng,
                    savedAddresses = savedAddresses,
                    onSelectSavedAddress = { addr ->
                        dropoffAddress = addr.address
                        dropoffLat = addr.lat
                        dropoffLng = addr.lng
                    },
                    onUseGps = { requestGps() },
                    onPinChange = { lat, lng -> dropoffLat = lat; dropoffLng = lng },
                    onBack = { step = CheckoutStep.DELIVERY_QUESTION },
                    onNext = {
                        step = CheckoutStep.DELIVERY_SELECTION
                        val lat = dropoffLat ?: -0.22
                        val lng = dropoffLng ?: -78.51
                        orderViewModel.loadDeliveryOptions(lat, lng)
                    }
                )

                step == CheckoutStep.DELIVERY_SELECTION -> DeliverySelectionContent(
                    checkoutState = checkoutState,
                    selectedDelivery = selectedDelivery,
                    onSelect = { selectedDelivery = it },
                    onBack = { step = CheckoutStep.ADDRESS_INPUT },
                    onNext = { step = CheckoutStep.ORDER_SUMMARY }
                )

                step == CheckoutStep.ORDER_SUMMARY -> OrderSummaryContent(
                    cartItems = cartItems,
                    subtotal = cartTotal,
                    needsDelivery = needsDelivery,
                    selectedDelivery = selectedDelivery,
                    dropoffAddress = dropoffAddress,
                    tipAmount = tipAmount,
                    onTipChange = { tipAmount = it },
                    onBack = {
                        step = if (needsDelivery) CheckoutStep.DELIVERY_SELECTION
                               else CheckoutStep.DELIVERY_QUESTION
                    },
                    onConfirm = {
                        orderViewModel.createAndPayOrder(
                            deliveryRequired = needsDelivery,
                            deliveryCompanyId = selectedDelivery?.companyId,
                            dropoffAddress = dropoffAddress.ifBlank { null },
                            dropoffLat = dropoffLat,
                            dropoffLng = dropoffLng,
                            observations = null
                        )
                    }
                )
            }
        }
    }
}

// ─── Step 1: ¿Necesitas delivery? ─────────────────────────────────────────────

@Composable
private fun DeliveryQuestionContent(
    onNo: () -> Unit,
    onYes: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.ShoppingBag,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "¿Tu compra necesita delivery?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            "Si los productos son físicos y requieren envío a domicilio, selecciona una empresa de entrega.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onNo,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Store, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Retiro en tienda", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onYes,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.LocalShipping, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Con delivery", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.navigationBarsPadding())
    }
}

// ─── Step 2: Dirección ────────────────────────────────────────────────────────

@Composable
private fun AddressInputContent(
    address: String,
    onAddressChange: (String) -> Unit,
    lat: Double?,
    lng: Double?,
    savedAddresses: List<SavedAddress> = emptyList(),
    onSelectSavedAddress: (SavedAddress) -> Unit = {},
    onUseGps: () -> Unit,
    onPinChange: (Double, Double) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val hasCoords = lat != null && lng != null
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(lat ?: -0.22, lng ?: -78.51), if (hasCoords) 15f else 12f
        )
    }

    // Sincronizar cámara cuando cambian las coordenadas
    LaunchedEffect(lat, lng) {
        if (lat != null && lng != null) {
            cameraState.animate(
                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 15f)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        SheetHeader(title = "Dirección de entrega", onBack = onBack)
        Spacer(Modifier.height(12.dp))

        // Saved address chips
        if (savedAddresses.isNotEmpty()) {
            Text(
                "Mis direcciones",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                savedAddresses.take(3).forEach { addr ->
                    val icon = when (addr.label.lowercase()) {
                        "casa", "home" -> Icons.Default.Home
                        "trabajo", "work", "oficina" -> Icons.Default.Work
                        else -> Icons.Default.Place
                    }
                    val isActive = lat == addr.lat && lng == addr.lng
                    FilterChip(
                        selected = isActive,
                        onClick = { onSelectSavedAddress(addr) },
                        label = { Text(addr.label, maxLines = 1) },
                        leadingIcon = { Icon(icon, null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = address,
            onValueChange = onAddressChange,
            label = { Text("Referencias (ej: Calle principal, casa azul)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            maxLines = 3,
            leadingIcon = { Icon(Icons.Default.Home, null) }
        )

        Spacer(Modifier.height(12.dp))

        // Mapa para confirmar/ajustar pin
        Text(
            "Toca el mapa para ajustar el pin de entrega",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraState,
                onMapClick = { latLng -> onPinChange(latLng.latitude, latLng.longitude) },
                uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
            ) {
                if (hasCoords) {
                    Marker(
                        state = MarkerState(position = LatLng(lat!!, lng!!)),
                        title = "Punto de entrega"
                    )
                }
            }
            if (!hasCoords) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Obteniendo ubicación…",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            // Botón centrar en mi ubicación (dentro del mapa)
            SmallFloatingActionButton(
                onClick = onUseGps,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Centrar en mi ubicación",
                    modifier = Modifier.size(20.dp))
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = address.isNotBlank() && hasCoords
        ) {
            Text("Ver empresas de delivery", fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, null)
        }
        Spacer(Modifier.navigationBarsPadding().padding(bottom = 8.dp))
    }
}

// ─── Step 3: Selección empresa delivery ───────────────────────────────────────

@Composable
private fun DeliverySelectionContent(
    checkoutState: CheckoutFlowState,
    selectedDelivery: DeliveryOption?,
    onSelect: (DeliveryOption) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        SheetHeader(title = "Elige empresa de entrega", onBack = onBack)
        Spacer(Modifier.height(12.dp))

        when (checkoutState) {
            is CheckoutFlowState.LoadingOptions -> {
                Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text("Buscando empresas disponibles…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            is CheckoutFlowState.OptionsLoaded -> {
                val options = checkoutState.options
                if (options.isEmpty()) {
                    Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.LocalShipping, null, modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            Spacer(Modifier.height(12.dp))
                            Text("No hay empresas disponibles en tu zona.",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    // Determine badges
                    val availableOpts = options.filter { it.available }
                    val cheapestId = availableOpts.minByOrNull { it.deliveryFee }?.companyId
                    val fastestId = if (availableOpts.size > 1) {
                        availableOpts.minByOrNull { it.distanceKm }?.companyId
                    } else null

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(options) { option ->
                            val badge = when (option.companyId) {
                                fastestId -> if (fastestId != cheapestId) "Más rápido" else null
                                cheapestId -> "Más barato"
                                else -> null
                            }
                            DeliveryOptionCard(
                                option = option,
                                selected = selectedDelivery?.companyId == option.companyId,
                                badge = badge,
                                onClick = { if (option.available) onSelect(option) }
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onNext,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        enabled = selectedDelivery != null
                    ) {
                        Text("Continuar", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, null)
                    }
                }
            }
            is CheckoutFlowState.Error -> {
                Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null, modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Text(checkoutState.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    }
                }
            }
            else -> {}
        }
        Spacer(Modifier.navigationBarsPadding().padding(bottom = 8.dp))
    }
}

@Composable
private fun DeliveryOptionCard(
    option: DeliveryOption,
    selected: Boolean,
    badge: String? = null,
    onClick: () -> Unit
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val bgColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)
                  else MaterialTheme.colorScheme.surface

    Surface(
        modifier = Modifier.fillMaxWidth().clickable(enabled = option.available, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (!option.available) MaterialTheme.colorScheme.surfaceVariant else bgColor,
        tonalElevation = if (selected) 2.dp else 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 2.dp else 1.dp, color = borderColor
        )
    ) {
        Column {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(44.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.LocalShipping, null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(option.companyName, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (!option.available) MaterialTheme.colorScheme.onSurfaceVariant
                                    else MaterialTheme.colorScheme.onSurface)
                        if (badge != null) {
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (badge == "Más barato") Color(0xFF4CAF50).copy(alpha = 0.15f)
                                        else Color(0xFFF57C00).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    badge,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (badge == "Más barato") Color(0xFF2E7D32) else Color(0xFFE65100)
                                )
                            }
                        }
                    }
                    Text(
                        if (option.available) "${String.format("%.1f", option.distanceKm)} km · ${option.vehiclesCount} vehículos"
                        else "Sin disponibilidad ahora",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (!option.available) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "$${String.format("%.2f", option.deliveryFee)}",
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (!option.available) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.primary
                )
                if (selected) {
                    Spacer(Modifier.width(10.dp))
                    Icon(Icons.Default.CheckCircle, null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

// ─── Step 4: Resumen y confirmar ──────────────────────────────────────────────

@Composable
private fun OrderSummaryContent(
    cartItems: List<CartItem>,
    subtotal: Double,
    needsDelivery: Boolean,
    selectedDelivery: DeliveryOption?,
    dropoffAddress: String,
    tipAmount: Double = 0.0,
    onTipChange: (Double) -> Unit = {},
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    val deliveryFee = selectedDelivery?.deliveryFee ?: 0.0
    val total = subtotal + deliveryFee + tipAmount

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)
    ) {
        SheetHeader(title = "Confirmar pedido", onBack = onBack)
        Spacer(Modifier.height(16.dp))

        // Artículos
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Column(Modifier.padding(14.dp)) {
                Text("Artículos (${cartItems.size})", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                cartItems.take(3).forEach { item ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                        Text("${item.quantity}×", color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.width(6.dp))
                        Text(item.productName, modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall, maxLines = 1)
                        Text("$${String.format("%.2f", item.price * item.quantity)}",
                            style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                }
                if (cartItems.size > 3) {
                    Text("+ ${cartItems.size - 3} más…",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Delivery info
        if (needsDelivery && selectedDelivery != null) {
            Surface(shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalShipping, null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(selectedDelivery.companyName, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium)
                    }
                    if (dropoffAddress.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Place, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp).padding(top = 2.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(dropoffAddress, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Propina (solo si hay delivery)
        if (needsDelivery && selectedDelivery != null) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, null,
                            tint = Color(0xFFE91E63), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Propina para el repartidor", fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val tipOptions = listOf(0.0, 1.0, 2.0, 3.0, 5.0)
                        tipOptions.forEach { amount ->
                            val isSelected = tipAmount == amount
                            FilterChip(
                                selected = isSelected,
                                onClick = { onTipChange(amount) },
                                label = {
                                    Text(
                                        if (amount == 0.0) "Sin propina" else "$${String.format("%.0f", amount)}",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Totales
        Surface(shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
            Column(Modifier.padding(16.dp)) {
                SummaryRow("Subtotal", subtotal)
                if (needsDelivery) SummaryRow("Delivery (${selectedDelivery?.companyName ?: ""})", deliveryFee)
                if (tipAmount > 0) SummaryRow("Propina", tipAmount)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                    Text("$${String.format("%.2f", total)}", fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Payment, null)
            Spacer(Modifier.width(10.dp))
            Text("Confirmar y Pagar", fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.navigationBarsPadding().padding(bottom = 8.dp))
    }
}

@Composable
private fun SummaryRow(label: String, amount: Double) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("$${String.format("%.2f", amount)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

// ─── Processing / Error ───────────────────────────────────────────────────────

@Composable
private fun ProcessingContent() {
    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(modifier = Modifier.size(52.dp), strokeWidth = 4.dp)
            Spacer(Modifier.height(16.dp))
            Text("Procesando tu pedido…", fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium)
            Text("Un momento por favor", color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.ErrorOutline, null, modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(12.dp))
        Text("Ocurrió un error", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(20.dp))
        Button(onClick = onRetry, shape = RoundedCornerShape(12.dp)) {
            Text("Volver a intentar", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.navigationBarsPadding())
    }
}

// ─── Header reutilizable ──────────────────────────────────────────────────────

@Composable
private fun SheetHeader(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
        }
        Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
    }
}
