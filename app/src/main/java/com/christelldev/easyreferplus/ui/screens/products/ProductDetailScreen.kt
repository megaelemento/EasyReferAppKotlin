package com.christelldev.easyreferplus.ui.screens.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.christelldev.easyreferplus.data.model.CompanyProduct
import com.christelldev.easyreferplus.ui.theme.AppBlue
import com.christelldev.easyreferplus.ui.theme.DesignConstants

// Constantes de diseño elegante
private val CARD_CORNER_RADIUS = DesignConstants.CARD_CORNER_RADIUS
private val CARD_ELEVATION = DesignConstants.CARD_ELEVATION
private val CARD_MARGIN_HORIZONTAL = DesignConstants.CARD_MARGIN_HORIZONTAL
private val GradientPrimary = DesignConstants.GradientPrimary
private val GradientSuccess = DesignConstants.GradientSuccess
private val GradientPurple = DesignConstants.GradientPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: CompanyProduct,
    cartCount: Int = 0,
    isProductOwner: Boolean = false,
    onAddToCart: (Int) -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    var quantity by remember { mutableIntStateOf(1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        product.productName ?: "Producto",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                                .padding(8.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                actions = {
                    // Botón del carrito con badge
                    BadgedBox(
                        badge = {
                            if (cartCount > 0) {
                                Badge(
                                    containerColor = Color(0xFFF44336)
                                ) {
                                    Text(
                                        text = if (cartCount > 99) "99+" else cartCount.toString(),
                                        color = MaterialTheme.colorScheme.surface
                                    )
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = onNavigateToCart) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = "Ver carrito",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.surface,
                    actionIconContentColor = Color.White
                ),
                modifier = Modifier.background(
                    brush = Brush.horizontalGradient(colors = GradientPrimary)
                )
            )
        },
        bottomBar = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(topStart = CARD_CORNER_RADIUS, topEnd = CARD_CORNER_RADIUS)),
                shape = RoundedCornerShape(topStart = CARD_CORNER_RADIUS, topEnd = CARD_CORNER_RADIUS),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quantity selector con estilo mejorado
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(4.dp)
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            enabled = quantity > 1,
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    color = if (quantity > 1) Color(0xFF03A9F4) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f).copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Disminuir",
                                tint = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = quantity.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = Color(0xFF1E293B)
                        )
                        IconButton(
                            onClick = { quantity++ },
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    brush = Brush.linearGradient(colors = GradientPrimary),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Aumentar",
                                tint = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Add to cart button - disable if user is the product owner
                    Button(
                        onClick = { onAddToCart(quantity) },
                        enabled = !isProductOwner,
                        modifier = Modifier
                            .height(48.dp)
                            .weight(1f)
                            .background(
                                brush = if (!isProductOwner)
                                    Brush.horizontalGradient(colors = GradientPrimary)
                                else
                                    Brush.horizontalGradient(colors = listOf(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isProductOwner) "No puedes comprar" else "Agregar al Carrito",
                            color = MaterialTheme.colorScheme.surface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Product images — carrusel si hay múltiples
            val sortedImages = remember(product.images) {
                product.images?.sortedWith(compareByDescending<com.christelldev.easyreferplus.data.model.ProductImage> { it.isPrimary }) ?: emptyList()
            }

            if (sortedImages.isEmpty()) {
                // Sin imágenes
                Box(
                    modifier = Modifier.fillMaxWidth().height(280.dp)
                        .background(Brush.verticalGradient(listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB)))),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.size(140.dp).shadow(elevation = CARD_ELEVATION, shape = CircleShape),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Image, null, modifier = Modifier.size(70.dp), tint = Color(0xFF03A9F4))
                        }
                    }
                }
            } else if (sortedImages.size == 1) {
                // Una sola imagen
                AsyncImage(
                    model = sortedImages.first().imageUrl,
                    contentDescription = product.productName,
                    modifier = Modifier.fillMaxWidth().height(320.dp).clip(RoundedCornerShape(0.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Carrusel múltiples imágenes
                val pagerState = rememberPagerState { sortedImages.size }
                Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        AsyncImage(
                            model = sortedImages[page].imageUrl,
                            contentDescription = product.productName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    // Indicadores (puntos)
                    Row(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(sortedImages.size) { i ->
                            val selected = pagerState.currentPage == i
                            Surface(
                                modifier = Modifier.size(if (selected) 8.dp else 6.dp),
                                shape = CircleShape,
                                color = if (selected) Color.White else Color.White.copy(alpha = 0.5f)
                            ) {}
                        }
                    }
                    // Contador "1 / N"
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.45f)
                    ) {
                        Text(
                            "${pagerState.currentPage + 1} / ${sortedImages.size}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            // Product info
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Name
                Text(
                    text = product.productName ?: "Producto",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Price con tarjeta
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Precio",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$${String.format("%.2f", product.currentPrice ?: product.price ?: 0.0)}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color(0xFF03A9F4),
                                    fontWeight = FontWeight.Bold
                                )

                                if (product.offerPrice != null && product.offerPrice!! < (product.price ?: 0.0)) {
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "$${String.format("%.2f", product.price ?: 0.0)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                    )
                                }
                            }
                        }

                        // Badge de descuento si hay oferta
                        if (product.offerPrice != null && product.offerPrice!! < (product.price ?: 0.0)) {
                            val discount = ((1 - (product.offerPrice!! / (product.price ?: 1.0))) * 100).toInt()
                            Box(
                                modifier = Modifier
                                    .background(
                                        brush = Brush.linearGradient(colors = GradientSuccess),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "-$discount%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.surface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                if (!product.productDescription.isNullOrBlank()) {
                    Text(
                        text = "Descripción",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = product.productDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Product details
                if (!product.size.isNullOrBlank() || !product.weight.isNullOrBlank() || !product.dimensions.isNullOrBlank()) {
                    Text(
                        text = "Detalles del Producto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(CARD_CORNER_RADIUS)),
                        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (!product.size.isNullOrBlank()) {
                                DetailRow("Tamaño", product.size)
                            }
                            if (!product.weight.isNullOrBlank()) {
                                DetailRow("Peso", product.weight)
                            }
                            if (!product.dimensions.isNullOrBlank()) {
                                DetailRow("Dimensiones", product.dimensions)
                            }
                            if (product.quantity != null && product.quantity > 0) {
                                DetailRow("Stock", "${product.quantity} unidades", isStock = true)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Commission info
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, isStock: Boolean = false) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isStock)
                Color(0xFF10B981).copy(alpha = 0.1f)
            else
                Color(0xFFF1F5F9)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
        }
    }
}
