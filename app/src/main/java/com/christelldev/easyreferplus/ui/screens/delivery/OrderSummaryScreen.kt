package com.christelldev.easyreferplus.ui.screens.delivery

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.christelldev.easyreferplus.data.model.CartItem
import com.christelldev.easyreferplus.data.model.DeliveryOption
import com.christelldev.easyreferplus.ui.viewmodel.CheckoutFlowState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderSummaryScreen(
    cartItems: List<CartItem>,
    cartSubtotal: Double,
    needsDelivery: Boolean,
    selectedDelivery: DeliveryOption?,
    dropoffAddress: String,
    checkoutState: CheckoutFlowState,
    onBack: () -> Unit,
    onConfirm: (tipAmount: Double) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val contentColor = if (isDark) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface
    var tipAmount by remember { mutableDoubleStateOf(0.0) }
    val deliveryFee = if (needsDelivery) selectedDelivery?.deliveryFee ?: 0.0 else 0.0
    val total = cartSubtotal + deliveryFee + tipAmount

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Gradient header overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), Color.Transparent)
                        )
                    )
                    .align(Alignment.TopStart)
            )
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text("Resumen del pedido", color = contentColor) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = contentColor)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )

                if (checkoutState is CheckoutFlowState.Processing) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(modifier = Modifier.size(52.dp), strokeWidth = 4.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Procesando tu pedido…", fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium)
                            Text("Un momento por favor",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Error banner
                        if (checkoutState is CheckoutFlowState.Error) {
                            item {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.errorContainer
                                ) {
                                    Row(modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ErrorOutline, null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.error)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(checkoutState.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        // Card dirección
                        if (needsDelivery && dropoffAddress.isNotBlank()) {
                            item {
                                Surface(modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                                    Row(modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Place, null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Dirección de entrega",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(dropoffAddress,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface, maxLines = 2)
                                        }
                                    }
                                }
                            }
                        }

                        // Card empresa delivery
                        if (needsDelivery && selectedDelivery != null) {
                            item {
                                Surface(modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                                    Row(modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocalShipping, null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Empresa de delivery",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(selectedDelivery.companyName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface)
                                            Text("$${String.format("%.2f", selectedDelivery.deliveryFee)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }

                        // Card productos
                        item {
                            Surface(modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Productos (${cartItems.size})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    cartItems.take(5).forEach { item ->
                                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                                            Text("${item.quantity}×",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodySmall)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(item.productName,
                                                modifier = Modifier.weight(1f),
                                                style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                            Text("$${String.format("%.2f", item.displayPrice * item.quantity)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    if (cartItems.size > 5) {
                                        Text("+ ${cartItems.size - 5} más…",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        // Card propina (solo con delivery)
                        if (needsDelivery && selectedDelivery != null) {
                            item {
                                Surface(modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Favorite, null,
                                                modifier = Modifier.size(18.dp),
                                                tint = Color(0xFFE91E63))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Propina para el repartidor",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium)
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf(0.0, 1.0, 2.0, 3.0, 5.0).forEach { amount ->
                                                val isSelected = tipAmount == amount
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = { tipAmount = amount },
                                                    modifier = Modifier.weight(1f),
                                                    label = {
                                                        Text(
                                                            if (amount == 0.0) "Sin propina"
                                                            else "$${amount.toInt()}",
                                                            fontSize = 11.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold
                                                                         else FontWeight.Normal
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Card totales
                        item {
                            Surface(modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    SummaryTotalRow("Subtotal", cartSubtotal)
                                    if (needsDelivery && selectedDelivery != null)
                                        SummaryTotalRow("Delivery (${selectedDelivery.companyName})", deliveryFee)
                                    if (tipAmount > 0) SummaryTotalRow("Propina", tipAmount)
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("TOTAL", fontWeight = FontWeight.Black,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onBackground)
                                        Text("$${String.format("%.2f", total)}",
                                            fontWeight = FontWeight.Black,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }

                        // Botón confirmar
                        item {
                            Button(
                                onClick = { onConfirm(tipAmount) },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.Payment, null)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Confirmar y Pagar",
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleMedium)
                            }
                        }

                        item { Spacer(modifier = Modifier.navigationBarsPadding().padding(bottom = 16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryTotalRow(label: String, amount: Double) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("$${String.format("%.2f", amount)}",
            style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
