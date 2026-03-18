package com.christelldev.easyreferplus.ui.screens.products

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Inventory2
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.christelldev.easyreferplus.data.model.CompanyProduct
import com.christelldev.easyreferplus.ui.theme.DesignConstants

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
    val isDark = isSystemInDarkTheme()

    Scaffold(
        containerColor = if (isDark) DesignConstants.BackgroundDark else DesignConstants.BackgroundLight
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Fondo de Header con Gradiente (Estilo Success para productos)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (isDark) {
                                listOf(DesignConstants.SuccessColor.copy(alpha = 0.4f), Color.Transparent)
                            } else {
                                DesignConstants.GradientSuccess
                            }
                        )
                    )
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // Espacio para el TopBar Flotante
                Spacer(modifier = Modifier.height(60.dp))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when {
                        isLoading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = DesignConstants.PrimaryColor)
                            }
                        }
                        products.isEmpty() -> {
                            EmptyProductsState(isDark)
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    horizontal = DesignConstants.CARD_MARGIN_HORIZONTAL,
                                    vertical = 16.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item {
                                    Text(
                                        text = "Catálogo de Productos",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = if (isDark) Color.White else Color.White,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                                items(products, key = { it.id ?: 0 }) { product ->
                                    ElegantProductCard(
                                        product = product,
                                        isDark = isDark,
                                        isCompanyOwner = isCompanyOwner,
                                        onClick = { onProductClick(product) },
                                        onAddToCart = { onAddToCart(product) }
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(80.dp)) }
                            }
                        }
                    }
                }
            }

            // Custom Top Bar (Floating Glass Style)
            ElegantTopBar(
                title = companyName,
                cartCount = cartCount,
                onBack = onNavigateBack,
                onCart = onNavigateToCart
            )
        }
    }
}

@Composable
private fun ElegantTopBar(
    title: String,
    cartCount: Int,
    onBack: () -> Unit,
    onCart: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.3f),
            onClick = onBack
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )

        BadgedBox(
            badge = {
                if (cartCount > 0) {
                    Badge(
                        containerColor = DesignConstants.ErrorColor,
                        contentColor = Color.White,
                        modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                    ) {
                        Text(text = if (cartCount > 9) "9+" else cartCount.toString(), fontSize = 10.sp)
                    }
                }
            }
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.3f),
                onClick = onCart
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ShoppingCart, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun ElegantProductCard(
    product: CompanyProduct,
    isDark: Boolean,
    isCompanyOwner: Boolean,
    onClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(DesignConstants.CARD_ELEVATION, RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS))
            .clickable { onClick() },
        shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS),
        color = if (isDark) DesignConstants.SurfaceCardDark else Color.White,
        tonalElevation = DesignConstants.CARD_ELEVATION
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del Producto
            Surface(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) DesignConstants.BackgroundDark else DesignConstants.BackgroundLight
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (!product.images.isNullOrEmpty()) {
                        AsyncImage(
                            model = product.images?.firstOrNull { it.isPrimary }?.imageUrl
                                ?: product.images?.firstOrNull()?.imageUrl,
                            contentDescription = product.productName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = DesignConstants.TextSecondary.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.productName ?: "Producto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) DesignConstants.TextPrimaryDark else DesignConstants.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!product.productDescription.isNullOrBlank()) {
                    Text(
                        text = product.productDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) DesignConstants.TextSecondaryDark else DesignConstants.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$${String.format(java.util.Locale.US, "%.2f", product.currentPrice ?: product.price ?: 0.0)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = if (isDark) DesignConstants.SuccessColor else DesignConstants.PrimaryDark
                    )
                    
                    if (product.offerPrice != null && product.offerPrice!! < (product.price ?: 0.0)) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$${String.format(java.util.Locale.US, "%.2f", product.price ?: 0.0)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = DesignConstants.TextSecondary,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                    }
                }
            }

            // Botón Añadir (Glass Style)
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (isCompanyOwner) 
                    DesignConstants.TextSecondary.copy(alpha = 0.1f) 
                else 
                    DesignConstants.PrimaryColor.copy(alpha = 0.1f),
                onClick = { if (!isCompanyOwner) onAddToCart() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Añadir",
                        tint = if (isCompanyOwner) 
                            DesignConstants.TextSecondary.copy(alpha = 0.5f) 
                        else 
                            DesignConstants.PrimaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyProductsState(isDark: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = if (isDark) DesignConstants.SurfaceCardDark else Color.White,
            tonalElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = DesignConstants.PrimaryColor.copy(alpha = 0.5f)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Sin productos aún",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = if (isDark) Color.White else DesignConstants.TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Esta empresa aún no ha publicado su catálogo de productos.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isDark) DesignConstants.TextSecondaryDark else DesignConstants.TextSecondary
        )
    }
}
