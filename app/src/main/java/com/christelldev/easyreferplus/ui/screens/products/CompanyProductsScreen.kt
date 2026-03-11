package com.christelldev.easyreferplus.ui.screens.products

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.ui.window.Dialog
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyProductsScreen(
    companyId: Int,
    companyName: String,
    products: List<CompanyProduct> = emptyList(),
    isLoading: Boolean = false,
    cartCount: Int = 0,
    isCompanyOwner: Boolean = false,
    onProductClick: (CompanyProduct) -> Unit = {},
    onAddToCart: (CompanyProduct) -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Productos de $companyName",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                products.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .size(120.dp)
                                .shadow(elevation = CARD_ELEVATION, shape = CircleShape),
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE3F2FD)
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(60.dp),
                                    tint = Color(0xFF03A9F4)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "No hay productos disponibles",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Pronto agregaremos nuevos productos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(products, key = { it.id ?: 0 }) { product ->
                            CompanyProductCard(
                                product = product,
                                isCompanyOwner = isCompanyOwner,
                                onClick = { onProductClick(product) },
                                onAddToCart = { onAddToCart(product) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompanyProductCard(
    product: CompanyProduct,
    isCompanyOwner: Boolean = false,
    onClick: () -> Unit = {},
    onAddToCart: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CARD_MARGIN_HORIZONTAL)
            .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(CARD_CORNER_RADIUS))
            .clickable { onClick() },
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen con gradiente de fondo
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFE8F5E9),
                                Color(0xFFC8E6C9)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!product.images.isNullOrEmpty()) {
                    AsyncImage(
                        model = product.images?.firstOrNull { it.isPrimary }?.imageUrl
                            ?: product.images?.firstOrNull()?.imageUrl,
                        contentDescription = product.productName,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(colors = GradientSuccess),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    product.productName ?: "Producto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!product.productDescription.isNullOrBlank()) {
                    Text(
                        product.productDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$${String.format("%.2f", product.currentPrice ?: product.price ?: 0.0)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF03A9F4),
                        fontWeight = FontWeight.Bold
                    )

                    if (product.offerPrice != null && product.offerPrice!! < (product.price ?: 0.0)) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "$${String.format("%.2f", product.price ?: 0.0)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Status badge con gradiente
                if (product.isActive) {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.linearGradient(colors = GradientSuccess),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Disponible",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.surface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Add to cart button - disable if user is the company owner
            Card(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(enabled = !isCompanyOwner) { onAddToCart() },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCompanyOwner)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f).copy(alpha = 0.1f)
                    else
                        Color(0xFF03A9F4).copy(alpha = 0.1f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = if (isCompanyOwner) "No puedes comprar tus productos" else "Agregar al carrito",
                        tint = if (isCompanyOwner) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else Color(0xFF03A9F4),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
