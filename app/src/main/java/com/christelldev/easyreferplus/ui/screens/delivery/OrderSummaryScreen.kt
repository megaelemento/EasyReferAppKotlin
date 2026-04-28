package com.christelldev.easyreferplus.ui.screens.delivery

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.christelldev.easyreferplus.data.model.CartItem
import com.christelldev.easyreferplus.data.model.DeliveryOption
import com.christelldev.easyreferplus.ui.components.PayPalCheckoutButton
import com.christelldev.easyreferplus.ui.viewmodel.CheckoutFlowState
import com.christelldev.easyreferplus.ui.viewmodel.PayPalViewModel
import com.christelldev.easyreferplus.ui.viewmodel.PayPalUiState
import com.christelldev.easyreferplus.util.AppLockManager

import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderSummaryScreen(
    cartItems: List<CartItem>,
    cartSubtotal: Double,
    needsDelivery: Boolean,
    selectedDelivery: DeliveryOption?,
    dropoffAddress: String,
    checkoutState: CheckoutFlowState,
    payPalViewModel: PayPalViewModel,
    onBack: () -> Unit,
    onConfirm: (tipAmount: Double, notes: String) -> Unit,
    onPayPalSuccess: (orderId: Int, companyId: Int?, status: String) -> Unit,
    onCancelOrder: () -> Unit = {}
) {
    Log.d("PayPalFlow", "OrderSummaryScreen - checkoutState: $checkoutState")
    val isDark = isSystemInDarkTheme()
    val contentColor = if (isDark) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface
    var tipAmount by remember { mutableDoubleStateOf(0.0) }
    var notes by remember { mutableStateOf("") }
    val deliveryFee = if (needsDelivery) selectedDelivery?.deliveryFee ?: 0.0 else 0.0
    val total = cartSubtotal + deliveryFee + tipAmount

    // Timer de expiración para pending_payment
    var secondsPending by remember { mutableIntStateOf(0) }
    val isPendingPayment = checkoutState is CheckoutFlowState.Success && checkoutState.status == "pending_payment"
    LaunchedEffect(isPendingPayment) {
        if (isPendingPayment) {
            secondsPending = 0
            while (true) {
                kotlinx.coroutines.delay(1000)
                secondsPending++
            }
        }
    }
    val pendingMinutes = secondsPending / 60
    val isExpired = secondsPending > 300 // 5 minutos

    // Observar éxito de PayPal
    val payPalState by payPalViewModel.uiState.collectAsState()
    Log.d("PayPalFlow", "OrderSummaryScreen - payPalState: $payPalState")
    var lastProcessedPaymentId by remember { mutableIntStateOf(-1) }

    LaunchedEffect(payPalState) {
        if (payPalState is PayPalUiState.Success) {
            val success = payPalState as PayPalUiState.Success
            val paymentId = success.response.paymentId ?: 0
            Log.d("PayPalFlow", "PayPal Success detected - paymentId: $paymentId")
            if (paymentId != lastProcessedPaymentId) {
                lastProcessedPaymentId = paymentId

                // Extraer información del estado de la orden (checkoutState)
                val orderId = (checkoutState as? CheckoutFlowState.Success)?.orderId ?: 0
                val companyId = (checkoutState as? CheckoutFlowState.Success)?.companyId

                Log.d("PayPalFlow", "Navigating to success - orderId: $orderId, companyId: $companyId")
                val finalStatus = if (needsDelivery) "paid_pending_driver" else "ready_for_pickup"
                onPayPalSuccess(orderId, companyId, finalStatus)
            }
        }
    }

    // Si el SDK de PayPal no procesó el deep link automáticamente (ej: browser externo),
    // detectamos el retorno y disparamos la captura manualmente.
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            if (AppLockManager.consumeReturnFromPayPal()) {
                val paypalOrderId = AppLockManager.getPendingPaypalOrderId()
                val currentState = payPalState
                Log.d("PayPalFlow", "Resumed after PayPal return. pendingPaypalId=$paypalOrderId, currentState=$currentState")
                if (paypalOrderId != null && currentState is PayPalUiState.OrderCreated) {
                    Log.d("PayPalFlow", "SDK did not fire callback, triggering capture manually")
                    payPalViewModel.captureOrder(paypalOrderId)
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Gradient header overlay que ocupa toda la parte superior incluyendo status bar
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
                    .align(Alignment.TopStart)
            )
            Column(modifier = Modifier.fillMaxSize()) {
                // TopAppBar manual con padding de status bar
                TopAppBar(
                    title = { Text("Resumen del pedido", color = contentColor) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = contentColor)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    windowInsets = WindowInsets.statusBars
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
                        modifier = Modifier.fillMaxSize().imePadding(),
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

                        // Campo instrucciones de entrega
                        if (needsDelivery) {
                            item {
                                OutlinedTextField(
                                    value = notes,
                                    onValueChange = { notes = it },
                                    label = { Text("Instrucciones adicionales (piso, referencia...)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    maxLines = 3,
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.StickyNote2, null) }
                                )
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

                        // Botón confirmar o PayPal
                        item {
                            if (checkoutState is CheckoutFlowState.Success && checkoutState.status == "pending_payment") {
                                // 1. Pre-crear la orden de PayPal en nuestro backend al llegar a este estado
                                LaunchedEffect(checkoutState.orderId) {
                                    if (payPalViewModel.uiState.value is PayPalUiState.Idle) {
                                        val companyId = cartItems.firstOrNull()?.companyId
                                        payPalViewModel.setPaymentContext(checkoutState.orderId, needsDelivery, companyId)
                                        payPalViewModel.startPayPalCheckout(
                                            amount = checkoutState.total,
                                            orderId = checkoutState.orderId,
                                            notes = notes.ifBlank { null }
                                        )
                                    }
                                }

                                Column(modifier = Modifier.fillMaxWidth()) {
                                    val isPayPalReady = payPalState is PayPalUiState.OrderCreated
                                    val isPayPalLoading = payPalState is PayPalUiState.Loading || payPalState is PayPalUiState.Idle

                                    Text(
                                        if (isPayPalReady) "¡Listo! Haz clic en el botón de PayPal para pagar:" 
                                        else "Preparando PayPal...",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isPayPalReady) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    
                                    if (isPayPalLoading) {
                                        Box(modifier = Modifier.fillMaxWidth().height(65.dp), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                        }
                                    } else {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            // Botón nativo de PayPal
                                            PayPalCheckoutButton(
                                                viewModel = payPalViewModel,
                                                modifier = Modifier.fillMaxWidth().height(60.dp)
                                            )
                                            
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            // Botón de reintentar PayPal si el SDK no cargó
                                            OutlinedButton(
                                                onClick = {
                                                    // Reiniciar el flujo de PayPal
                                                    payPalViewModel.startPayPalCheckout(
                                                        amount = checkoutState.total,
                                                        orderId = checkoutState.orderId,
                                                        notes = notes.ifBlank { null }
                                                    )
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(Icons.Default.Refresh, null)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Reintentar PayPal")
                                            }
                                        }
                                        
                                        if (payPalState is PayPalUiState.Error) {
                                            Text(
                                                "Error: ${(payPalState as PayPalUiState.Error).message}",
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                            Button(
                                                onClick = { 
                                                    payPalViewModel.startPayPalCheckout(
                                                        amount = checkoutState.total,
                                                        orderId = checkoutState.orderId,
                                                        notes = notes.ifBlank { null }
                                                    ) 
                                                },
                                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                            ) {
                                                Text("Reintentar PayPal")
                                            }
                                        }

                                        // Aviso de expiración y botón cancelar
                                        if (isExpired) {
                                            Spacer(Modifier.height(12.dp))
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(Modifier.padding(12.dp)) {
                                                    Text(
                                                        "El pago lleva pendiente más de 5 minutos. Si el pago con PayPal no funciona, puedes cancelar este pedido.",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onErrorContainer
                                                    )
                                                    Spacer(Modifier.height(8.dp))
                                                    OutlinedButton(
                                                        onClick = onCancelOrder,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = ButtonDefaults.outlinedButtonColors(
                                                            contentColor = MaterialTheme.colorScheme.error
                                                        )
                                                    ) {
                                                        Icon(Icons.Default.Cancel, null, modifier = Modifier.size(18.dp))
                                                        Spacer(Modifier.width(6.dp))
                                                        Text("Cancelar pedido")
                                                    }
                                                }
                                            }
                                        } else if (pendingMinutes >= 2) {
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                "El pago lleva $pendingMinutes minuto(s) pendiente. Si no completa en breve, aparecerá la opción de cancelar.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { onConfirm(tipAmount, notes) },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    enabled = checkoutState !is CheckoutFlowState.Processing
                                ) {
                                    Icon(Icons.Default.Payment, null)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        "Confirmar y Pagar",
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
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
