package com.christelldev.easyreferplus.ui.screens.orders

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.christelldev.easyreferplus.data.model.DispatchDetail
import com.christelldev.easyreferplus.data.model.DispatchItem
import com.christelldev.easyreferplus.ui.viewmodel.DispatchViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDispatchScreen(
    orderId: Int,
    viewModel: DispatchViewModel,
    onNavigateToChat: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(orderId) {
        viewModel.loadDispatchDetail(orderId)
    }

    LaunchedEffect(state.successMessage, state.errorMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    val isDark = isSystemInDarkTheme()
    val contentTint = if (isDark) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Despacho", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadDispatchDetail(orderId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                state.selectedDetail?.let { detail ->
                    DetailContent(
                        detail = detail,
                        onMarkReady = { viewModel.markOrderReadyPickup(orderId) },
                        onMarkDispatched = { viewModel.markOrderDispatched(orderId) },
                        onChatClick = { onNavigateToChat(orderId) }
                    )
                } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se pudo cargar la información del despacho")
                }
            }
        }
    }
}

@Composable
fun DetailContent(
    detail: DispatchDetail,
    onMarkReady: () -> Unit,
    onMarkDispatched: () -> Unit,
    onChatClick: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Información del Cliente
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Información del Cliente", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Nombre: ${detail.buyer.name}", fontWeight = FontWeight.Medium)
                    detail.buyer.phone?.let {
                        Text("Teléfono: $it")
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onChatClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Chat con Comprador")
                    }
                }
            }
        }

        // Productos
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Productos del Pedido", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    detail.items.forEach { item ->
                        OrderItemRow(item)
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total del pedido:", fontWeight = FontWeight.Bold)
                        Text(String.format(Locale.US, "$%.2f", detail.total), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Ubicación / Delivery
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (detail.deliveryRequired) Icons.Default.LocalShipping else Icons.Default.Store,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (detail.deliveryRequired) "Dirección de Entrega" else "Retiro en Establecimiento",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    if (detail.deliveryRequired) {
                        Text(detail.dropoffAddress ?: "Dirección no disponible")
                        if (detail.dropoffLat != null && detail.dropoffLng != null) {
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    val gmmIntentUri = Uri.parse("geo:${detail.dropoffLat},${detail.dropoffLng}?q=${detail.dropoffAddress}")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                    mapIntent.setPackage("com.google.android.apps.maps")
                                    context.startActivity(mapIntent)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Map, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Ver en Google Maps")
                            }
                        }
                    } else {
                        Text("El cliente pasará a retirar el pedido personalmente.")
                    }
                    detail.observations?.let {
                        Spacer(Modifier.height(8.dp))
                        Text("Observaciones:", fontWeight = FontWeight.Bold)
                        Text(it, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Acciones
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!detail.deliveryRequired && detail.status == "paid") {
                    Button(
                        onClick = onMarkReady,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Marcar como Listo para Retiro", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                } else if (detail.deliveryRequired && (detail.status == "paid_pending_driver" || detail.status == "driver_assigned" || detail.status == "ready_for_pickup")) {
                    Button(
                        onClick = onMarkDispatched,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Marcar como Despachado", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Text(
                    "Estado Actual: ${detail.status.uppercase()}",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun OrderItemRow(item: DispatchItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.productName, fontWeight = FontWeight.Medium)
            Text("Cantidad: ${item.quantity}", style = MaterialTheme.typography.bodySmall)
        }
        Text(
            String.format(Locale.US, "$%.2f", item.unitPrice * item.quantity),
            fontWeight = FontWeight.Bold
        )
    }
}
