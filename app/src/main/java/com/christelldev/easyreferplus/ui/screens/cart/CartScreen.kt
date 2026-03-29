package com.christelldev.easyreferplus.ui.screens.cart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.christelldev.easyreferplus.data.model.CartItem
import com.christelldev.easyreferplus.ui.viewmodel.AddressViewModel
import com.christelldev.easyreferplus.ui.viewmodel.OrderViewModel

sealed class CheckoutState {
    object Idle : CheckoutState()
    object Processing : CheckoutState()
    data class Success(
        val message: String,
        val orderId: Int,
        val qrCodes: List<String> = emptyList(),
        val totalItems: Int = 0,
        val totalAmount: Double = 0.0,
        val companyCount: Int = 0
    ) : CheckoutState()
    data class Error(val message: String) : CheckoutState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartItems: List<CartItem>,
    isLoading: Boolean,
    checkoutState: CheckoutState,
    orderViewModel: OrderViewModel,
    addressViewModel: AddressViewModel? = null,
    onAddToCart: (Int, Int) -> Unit,
    onRemoveFromCart: (Int) -> Unit,
    onUpdateQuantity: (Int, Int) -> Unit,
    onCheckout: () -> Unit,
    onClearCart: () -> Unit,
    onNavigateBack: () -> Unit,
    onCheckoutDismiss: () -> Unit,
    onRefreshCart: () -> Unit,
    onCheckoutSuccess: (orderId: Int) -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val totalAmount = remember(cartItems) { cartItems.sumOf { it.price * it.quantity } }
    var showDeliverySheet by remember { mutableStateOf(false) }

    if (checkoutState is CheckoutState.Success) {
        CheckoutSuccessDialog(
            state = checkoutState,
            onDismiss = {
                onCheckoutDismiss()
                onCheckoutSuccess(checkoutState.orderId)
            }
        )
    }

    if (showDeliverySheet) {
        CheckoutFlowSheet(
            cartItems = cartItems,
            cartTotal = totalAmount,
            orderViewModel = orderViewModel,
            addressViewModel = addressViewModel,
            onDismiss = { showDeliverySheet = false },
            onSuccess = { orderId ->
                showDeliverySheet = false
                onRefreshCart()
                onCheckoutSuccess(orderId)
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Gradiente superior sutil
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp)
                    .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), Color.Transparent)))
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // Cabecera Premium
                TopAppBar(
                    title = {
                        Text(
                            text = "Mi Carrito",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White)
                        }
                    },
                    actions = {
                        if (cartItems.isNotEmpty()) {
                            IconButton(onClick = onClearCart) {
                                Icon(Icons.Default.DeleteSweep, null, tint = if (isDark) MaterialTheme.colorScheme.error else Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )

                if (isLoading && cartItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (cartItems.isEmpty()) {
                    EmptyCartState(onNavigateBack)
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(cartItems, key = { it.id }) { item ->
                                CartItemPremium(
                                    item = item,
                                    onUpdateQuantity = { onUpdateQuantity(item.productId, it) },
                                    onRemove = { onRemoveFromCart(item.productId) }
                                )
                            }
                        }

                        // Resumen de Pago Premium
                        CheckoutSummaryCard(
                            subtotal = totalAmount,
                            total = totalAmount,
                            onCheckout = { showDeliverySheet = true },
                            isLoading = checkoutState is CheckoutState.Processing
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemPremium(
    item: CartItem,
    onUpdateQuantity: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Imagen
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                AsyncImage(
                    model = item.images.firstOrNull()?.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(item.productName, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = "$${String.format(java.util.Locale.US, "%.2f", item.price)} c/u",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(32.dp).clickable { if (item.quantity > 1) onUpdateQuantity(item.quantity - 1) },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Remove, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                    }
                    
                    Text(text = item.quantity.toString(), modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black)
                    
                    Surface(
                        modifier = Modifier.size(32.dp).clickable { onUpdateQuantity(item.quantity + 1) },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            IconButton(onClick = onRemove, modifier = Modifier.align(Alignment.Top)) {
                Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun CheckoutSummaryCard(
    subtotal: Double,
    total: Double,
    onCheckout: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 16.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Subtotal", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$${String.format(java.util.Locale.US, "%.2f", subtotal)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text("Total a pagar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text("$${String.format(java.util.Locale.US, "%.2f", total)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onCheckout,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else {
                    Icon(Icons.Default.ShoppingCartCheckout, null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Finalizar Compra", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

@Composable
fun EmptyCartState(onContinueShopping: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.RemoveShoppingCart, null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Tu carrito está vacío", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Text("Parece que aún no has añadido nada. ¡Explora los comercios y encuentra lo que buscas!", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(40.dp))
        Button(onClick = onContinueShopping, shape = RoundedCornerShape(16.dp)) { Text("Seguir Comprando", fontWeight = FontWeight.Black) }
    }
}

@Composable
fun CheckoutSuccessDialog(state: CheckoutState.Success, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981), modifier = Modifier.size(64.dp)) },
        title = { Text("¡Compra Realizada!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(state.message, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                if (state.qrCodes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Presenta tus códigos QR en cada establecimiento para recibir tus productos.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) { Text("Aceptar", fontWeight = FontWeight.Bold) } },
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 6.dp
    )
}
