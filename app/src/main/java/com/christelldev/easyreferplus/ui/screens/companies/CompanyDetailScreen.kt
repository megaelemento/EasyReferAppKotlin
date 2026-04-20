package com.christelldev.easyreferplus.ui.screens.companies

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Surface
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.data.model.CompanyProduct
import com.christelldev.easyreferplus.data.model.UserCompanyResponse
import com.christelldev.easyreferplus.ui.theme.AppBlue
import com.christelldev.easyreferplus.ui.theme.DesignConstants
import com.christelldev.easyreferplus.ui.viewmodel.CompanyDetailViewModel

// Constants for elegant design
private val CARD_CORNER_RADIUS = DesignConstants.CARD_CORNER_RADIUS
private val CARD_ELEVATION = DesignConstants.CARD_ELEVATION
private val CARD_MARGIN_HORIZONTAL = DesignConstants.CARD_MARGIN_HORIZONTAL
private val GradientPrimary = DesignConstants.GradientPrimary
private val GradientSuccess = DesignConstants.GradientSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyDetailScreen(
    companyId: Int,
    companyName: String = "",
    viewModel: CompanyDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToProducts: () -> Unit = {},
    onProductClick: (com.christelldev.easyreferplus.data.model.CompanyProduct) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    // Load company details
    androidx.compose.runtime.LaunchedEffect(companyId) {
        viewModel.loadCompany(companyId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (uiState.errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(text = uiState.errorMessage ?: "Error", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                }
            } else {
                uiState.company?.let { company ->
                    CompanyDetailContent(
                        company = company,
                        isDark = isDark,
                        onNavigateBack = onNavigateBack,
                        onNavigateToProducts = onNavigateToProducts,
                        onProductClick = onProductClick
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompanyDetailContent(
    company: UserCompanyResponse,
    isDark: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToProducts: () -> Unit = {},
    onProductClick: (com.christelldev.easyreferplus.data.model.CompanyProduct) -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val contentTint = if (isDark) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo de Header con Gradiente que cubre status bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), Color.Transparent)
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // TopAppBar manual con insets de status bar
            TopAppBar(
                title = { Text("Detalles de Empresa", fontWeight = FontWeight.Bold, color = contentTint) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = contentTint)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                windowInsets = WindowInsets.statusBars
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {

            // Logo y Nombre (Header Flotante)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = if (isDark) DesignConstants.SurfaceCardDark else Color.White,
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp,
                    border = androidx.compose.foundation.BorderStroke(4.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (company.logoUrl != null) {
                            val logoUrlWithTimestamp = "${company.logoUrl}${if (company.logoUrl.contains("?")) "&" else "?"}t=${System.currentTimeMillis()}"
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(logoUrlWithTimestamp)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = company.companyName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp),
                                tint = DesignConstants.PrimaryColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = company.companyName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    color = contentTint
                )

                if (!company.city.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier.padding(top = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${company.city}${if (!company.province.isNullOrBlank()) ", ${company.province}" else ""}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Contenido Principal (Cards)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DesignConstants.CARD_MARGIN_HORIZONTAL),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Quick Actions (WhatsApp & Share)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionCard(
                        icon = Icons.Default.Phone,
                        label = "WhatsApp",
                        color = Color(0xFF25D366),
                        isDark = isDark,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/${company.whatsappNumber}"))
                            context.startActivity(intent)
                        }
                    )
                    ActionCard(
                        icon = Icons.Default.Share,
                        label = "Compartir",
                        color = DesignConstants.PrimaryColor,
                        isDark = isDark,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "¡Mira esta empresa en Enfoque Refer!: ${company.companyName}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Compartir"))
                        }
                    )
                }

                // Descripción
                if (!company.companyDescription.isNullOrBlank()) {
                    ElegantInfoCard(
                        title = "Acerca de la empresa",
                        content = company.companyDescription,
                        icon = Icons.Default.Business,
                        isDark = isDark
                    )
                }

                // Acceso a Productos (Elegant Style)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS),
                    color = if (isDark) DesignConstants.SurfaceCardDark else Color.White,
                    tonalElevation = 2.dp,
                    shadowElevation = 4.dp,
                    onClick = onNavigateToProducts
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = DesignConstants.PrimaryColor.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.ShoppingCart, null, tint = DesignConstants.PrimaryColor)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Productos y Servicios",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDark) DesignConstants.TextPrimaryDark else DesignConstants.TextPrimary
                            )
                            Text(
                                text = "Explora nuestro catálogo completo",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) DesignConstants.TextSecondaryDark else DesignConstants.TextSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = DesignConstants.TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Información de Contacto Detallada
                ElegantInfoCard(
                    title = "Contacto y Redes",
                    isDark = isDark,
                    content = "" // No usamos content directo aquí
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (!company.address.isNullOrBlank()) {
                            ContactItem(Icons.Default.LocationOn, "Dirección", company.address ?: "", isDark)
                        }
                        if (!company.website.isNullOrBlank()) {
                            ContactItem(Icons.Default.Language, "Sitio Web", company.website ?: "", isDark)
                        }
                        if (!company.facebookUrl.isNullOrBlank()) {
                            ContactItem(Icons.Default.Language, "Facebook", company.facebookUrl ?: "", isDark)
                        }
                        if (!company.instagramUrl.isNullOrBlank()) {
                            ContactItem(Icons.Default.Language, "Instagram", company.instagramUrl ?: "", isDark)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun ActionCard(
    icon: ImageVector,
    label: String,
    color: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) DesignConstants.SurfaceCardDark else Color.White,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDark) DesignConstants.TextPrimaryDark else DesignConstants.TextPrimary
            )
        }
    }
}

@Composable
private fun ElegantInfoCard(
    title: String,
    content: String,
    icon: ImageVector? = null,
    isDark: Boolean,
    customContent: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS),
        color = if (isDark) DesignConstants.SurfaceCardDark else Color.White,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(icon, null, tint = DesignConstants.PrimaryColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) DesignConstants.TextPrimaryDark else DesignConstants.TextPrimary
                )
            }
            
            if (content.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) DesignConstants.TextSecondaryDark else DesignConstants.TextSecondary,
                    lineHeight = 20.sp
                )
            }
            
            if (customContent != null) {
                Spacer(modifier = Modifier.height(16.dp))
                customContent()
            }
        }
    }
}

@Composable
private fun ContactItem(icon: ImageVector, label: String, value: String, isDark: Boolean) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = DesignConstants.PrimaryColor.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = DesignConstants.PrimaryColor, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isDark) DesignConstants.TextSecondaryDark else DesignConstants.TextSecondary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDark) DesignConstants.TextPrimaryDark else DesignConstants.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    content: String,
    icon: ImageVector? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(CARD_ELEVATION, RoundedCornerShape(CARD_CORNER_RADIUS)),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(brush = Brush.linearGradient(GradientPrimary), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ContactRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ProductListItem(
    product: CompanyProduct,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Product image or placeholder
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!product.images.isNullOrEmpty()) {
                // Mostrar imagen si existe
                coil.compose.AsyncImage(
                    model = product.images?.firstOrNull { it.isPrimary }?.imageUrl
                        ?: product.images?.firstOrNull()?.imageUrl,
                    contentDescription = product.productName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Product info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.productName ?: "Producto",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            if (!product.productDescription.isNullOrBlank()) {
                Text(
                    text = product.productDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            // Price
            Text(
                text = "$${String.format("%.2f", product.currentPrice ?: product.price ?: 0.0)}",
                style = MaterialTheme.typography.bodyMedium,
                color = AppBlue,
                fontWeight = FontWeight.Bold
            )
        }

        // Add to cart icon
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = "Agregar al carrito",
            tint = AppBlue,
            modifier = Modifier.size(24.dp)
        )
    }
}
