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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Load company details
    androidx.compose.runtime.LaunchedEffect(companyId) {
        viewModel.loadCompany(companyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.company_details),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.surface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        uiState.company?.let { company ->
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Mira esta empresa: ${company.companyName}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Compartir"))
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.share_company),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF03A9F4),
                    titleContentColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.surface,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.errorMessage ?: "Error",
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            uiState.company?.let { company ->
                CompanyDetailContent(
                    company = company,
                    modifier = Modifier.padding(paddingValues),
                    onNavigateToProducts = onNavigateToProducts,
                    onProductClick = onProductClick
                )
            }
        }
    }
}

@Composable
private fun CompanyDetailContent(
    company: UserCompanyResponse,
    modifier: Modifier = Modifier,
    onNavigateToProducts: () -> Unit = {},
    onProductClick: (com.christelldev.easyreferplus.data.model.CompanyProduct) -> Unit = {}
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = CARD_MARGIN_HORIZONTAL, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with logo and name
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(CARD_ELEVATION, RoundedCornerShape(CARD_CORNER_RADIUS)),
                shape = RoundedCornerShape(CARD_CORNER_RADIUS),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(brush = Brush.verticalGradient(GradientPrimary))
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Company Logo
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))
                                .then(
                                    Modifier.shadow(4.dp, CircleShape)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (company.logoUrl != null) {
                                // Agregar timestamp para evitar caché de la imagen
                                val logoUrlWithTimestamp = if (company.logoUrl.contains("?")) {
                                    "${company.logoUrl}&t=${System.currentTimeMillis()}"
                                } else {
                                    "${company.logoUrl}?t=${System.currentTimeMillis()}"
                                }
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
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = company.companyName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.surface
                        )

                        // Full Location: Address + City + Province
                        if (!company.address.isNullOrBlank() || !company.city.isNullOrBlank() || !company.province.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = buildString {
                                        if (!company.address.isNullOrBlank()) append(company.address)
                                        if (!company.city.isNullOrBlank()) {
                                            if (isNotEmpty()) append(", ")
                                            append(company.city)
                                        }
                                        if (!company.province.isNullOrBlank()) {
                                            if (isNotEmpty()) append(", ")
                                            append(company.province)
                                        }
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Contact Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // WhatsApp Button
                if (!company.whatsappNumber.isNullOrBlank()) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/${company.whatsappNumber}"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.whatsapp))
                    }
                }

                // Website Button
                if (!company.website.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = {
                            val url = company.website?.let {
                                if (!it.startsWith("http")) "https://$it" else it
                            }
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.website))
                    }
                }
            }
        }

        // Description
        if (!company.companyDescription.isNullOrBlank()) {
            item {
                InfoCard(
                    title = stringResource(R.string.company_info_description),
                    content = company.companyDescription
                )
            }
        }

        // Products List - Button to navigate to products
        if (!company.products.isNullOrEmpty() || !company.productDescription.isNullOrBlank()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToProducts() },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.products),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (!company.productDescription.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = company.productDescription,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    maxLines = 2
                                )
                            }
                            if (!company.products.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${company.products!!.size} productos disponibles",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppBlue
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Ver productos",
                            tint = AppBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        // Contact Information
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.contact),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // WhatsApp
                    if (!company.whatsappNumber.isNullOrBlank()) {
                        ContactRow(
                            icon = Icons.Default.Phone,
                            label = stringResource(R.string.whatsapp),
                            value = company.whatsappNumber
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Website
                    if (!company.website.isNullOrBlank()) {
                        ContactRow(
                            icon = Icons.Default.Language,
                            label = stringResource(R.string.website),
                            value = company.website
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Facebook
                    if (!company.facebookUrl.isNullOrBlank()) {
                        ContactRow(
                            icon = Icons.Default.Language,
                            label = stringResource(R.string.facebook),
                            value = company.facebookUrl
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Instagram
                    if (!company.instagramUrl.isNullOrBlank()) {
                        ContactRow(
                            icon = Icons.Default.Language,
                            label = stringResource(R.string.instagram),
                            value = company.instagramUrl
                        )
                    }
                }
            }
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
