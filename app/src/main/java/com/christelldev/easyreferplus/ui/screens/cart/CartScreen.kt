package com.christelldev.easyreferplus.ui.screens.cart

import android.graphics.Bitmap
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.christelldev.easyreferplus.data.model.CartItem
import com.christelldev.easyreferplus.data.model.CheckoutQRCode
import com.christelldev.easyreferplus.ui.theme.AppBlue
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

// Constants for consistent styling - Following HomeScreen design
private val CARD_CORNER_RADIUS = 20.dp
private val CARD_ELEVATION = 8.dp
private val CARD_MARGIN_HORIZONTAL = 16.dp

// Modern color palette - Following HomeScreen design
private val GradientPrimary = listOf(Color(0xFF03A9F4), Color(0xFF2196F3))
private val GradientSuccess = listOf(Color(0xFF10B981), Color(0xFF34D399))
private val GradientWarning = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24))

// Estados para el checkout
sealed class CheckoutState {
    data object Idle : CheckoutState()
    data object Processing : CheckoutState()
    data class Success(
        val message: String,
        val orderId: String?,
        val qrCodes: List<CheckoutQRCode> = emptyList(),
        val totalItems: Int = 0,
        val totalAmount: Double = 0.0,
        val companyCount: Int = 0
    ) : CheckoutState()
    data class Error(val message: String) : CheckoutState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartItems: List<CartItem> = emptyList(),
    isLoading: Boolean = false,
    checkoutState: CheckoutState = CheckoutState.Idle,
    onAddToCart: (Int, Int) -> Unit = { _, _ -> },
    onRemoveFromCart: (Int) -> Unit = { _ -> },
    onUpdateQuantity: (Int, Int) -> Unit = { _, _ -> },
    onCheckout: () -> Unit = {},
    onClearCart: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onCheckoutDismiss: () -> Unit = {},
    onRefreshCart: () -> Unit = {}
) {
    // Variable para determinar si debe navegar al home
    val shouldNavigateToHome = remember { mutableStateOf(false) }
    // Mostrar diálogo de checkout
    when (checkoutState) {
        is CheckoutState.Success -> {
            CheckoutResultDialog(
                isSuccess = true,
                message = checkoutState.message,
                orderId = checkoutState.orderId,
                qrCodes = checkoutState.qrCodes,
                totalItems = checkoutState.totalItems,
                totalAmount = checkoutState.totalAmount,
                companyCount = checkoutState.companyCount,
                onDismiss = {
                    // Resetear estado del checkout
                    onCheckoutDismiss()
                    // Recargar el carrito para ver productos restantes
                    onRefreshCart()
                    // Solo navegar al home si el carrito está vacío
                    // (los productos se eliminan cuando las empresas registran las ventas)
                    // Por defecto nos quedamos en el carrito
                    // El usuario puede navegar manualmente con la flecha atrás
                }
            )
        }
        is CheckoutState.Error -> {
            CheckoutResultDialog(
                isSuccess = false,
                message = checkoutState.message,
                onDismiss = {
                    onCheckoutDismiss()
                }
            )
        }
        else -> {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Carrito de Compras") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (cartItems.isNotEmpty()) {
                        IconButton(onClick = onClearCart) {
                            Icon(Icons.Default.Delete, contentDescription = "Vaciar carrito")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBlue,
                    titleContentColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.surface,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                cartItems.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Tu carrito está vacío",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Agrega productos de las empresas para verlos aquí",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(cartItems, key = { it.productId }) { item ->
                                CartItemCard(
                                    item = item,
                                    onIncrement = { onUpdateQuantity(item.productId, item.quantity + 1) },
                                    onDecrement = {
                                        if (item.quantity > 1) {
                                            onUpdateQuantity(item.productId, item.quantity - 1)
                                        } else {
                                            onRemoveFromCart(item.productId)
                                        }
                                    },
                                    onRemove = { onRemoveFromCart(item.productId) }
                                )
                            }
                        }

                        // Total y Checkout - Following HomeScreen design
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(topStart = CARD_CORNER_RADIUS, topEnd = CARD_CORNER_RADIUS)),
                            shape = RoundedCornerShape(topStart = CARD_CORNER_RADIUS, topEnd = CARD_CORNER_RADIUS),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Transparent
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = GradientPrimary
                                        )
                                    )
                                    .padding(20.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Resumen
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                "Total a pagar",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                                            )
                                            Text(
                                                "${cartItems.sumOf { it.quantity }} productos",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                            )
                                        }
                                        Text(
                                            "$${String.format("%.2f", cartItems.sumOf { it.subtotal })}",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.surface
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Botón de checkout moderno
                                    Button(
                                        onClick = onCheckout,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp),
                                        enabled = checkoutState !is CheckoutState.Processing,
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        if (checkoutState is CheckoutState.Processing) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = AppBlue,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Procesando...", color = AppBlue)
                                        } else {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = AppBlue
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "Finalizar Compra",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = AppBlue,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Text(
                                        "Paga en la empresa con tu código de referido",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(CARD_CORNER_RADIUS)),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del producto
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.outline),
                contentAlignment = Alignment.Center
            ) {
                if (item.images.isNotEmpty()) {
                    AsyncImage(
                        model = item.images.firstOrNull()?.imageUrl,
                        contentDescription = item.productName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info del producto
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    item.productName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                if (item.companyName != null) {
                    Text(
                        item.companyName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Text(
                    "$${String.format("%.2f", item.displayPrice)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppBlue,
                    fontWeight = FontWeight.Bold
                )

                // Cantidad controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    IconButton(
                        onClick = onDecrement,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Reducir",
                            tint = AppBlue
                        )
                    }

                    Text(
                        "${item.quantity}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    IconButton(
                        onClick = onIncrement,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Aumentar",
                            tint = AppBlue
                        )
                    }
                }
            }

            // Subtotal
            Column(
                horizontalAlignment = Alignment.End
            ) {
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color.Red
                    )
                }
                Text(
                    "$${String.format("%.2f", item.subtotal)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CheckoutResultDialog(
    isSuccess: Boolean,
    message: String,
    orderId: String? = null,
    qrCodes: List<CheckoutQRCode> = emptyList(),
    totalItems: Int = 0,
    totalAmount: Double = 0.0,
    companyCount: Int = 0,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(CARD_CORNER_RADIUS)),
            shape = RoundedCornerShape(CARD_CORNER_RADIUS),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono de resultado
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = if (isSuccess) Color(0xFF10B981).copy(alpha = 0.1f)
                                    else Color(0xFFF44336).copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = if (isSuccess) Color(0xFF10B981) else Color(0xFFF44336)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isSuccess) "¡QR Generado!" else "Error",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Resumen de la compra
                if (isSuccess) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$totalItems",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppBlue
                            )
                            Text(
                                text = "Productos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$companyCount",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppBlue
                            )
                            Text(
                                text = "Empresas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$${String.format("%.2f", totalAmount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppBlue
                            )
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )

                    if (orderId != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Referencia: #$orderId",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f).copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mostrar un QR por cada empresa
                    Text(
                        text = "Códigos QR por Empresa",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    qrCodes.forEach { qrCode ->
                        QRCodeCard(qrCode = qrCode)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                } else {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSuccess) Color(0xFF10B981) else AppBlue
                    )
                ) {
                    Text(
                        text = if (isSuccess) "Entendido" else "Intentar de Nuevo",
                        color = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }
    }
}

@Composable
private fun QRCodeCard(qrCode: CheckoutQRCode) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF03A9F4).copy(alpha = 0.05f),
                            Color.White
                        )
                    )
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Nombre de la empresa
            Text(
                text = qrCode.companyName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AppBlue
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mostrar productos de esta empresa
            Text(
                text = "${qrCode.itemCount} producto(s) - $${String.format("%.2f", qrCode.total)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Generar y mostrar QR
            val qrBitmap = generateQRCode(qrCode.qrCode, 200)
            qrBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "QR para ${qrCode.companyName}",
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Muestra este QR a la empresa",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f).copy(alpha = 0.7f)
            )
        }
    }
}

// Función para generar código QR
private fun generateQRCode(content: String, size: Int): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}
