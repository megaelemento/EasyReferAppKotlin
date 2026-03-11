package com.christelldev.easyreferplus.ui.screens.companies

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.data.model.UserCompanyResponse
import com.christelldev.easyreferplus.ui.theme.AppBlue
import com.christelldev.easyreferplus.ui.theme.DesignConstants
import coil.compose.AsyncImage

// Constants for elegant design
private val CARD_CORNER_RADIUS = DesignConstants.CARD_CORNER_RADIUS
private val CARD_ELEVATION = DesignConstants.CARD_ELEVATION
private val CARD_MARGIN_HORIZONTAL = DesignConstants.CARD_MARGIN_HORIZONTAL
private val GradientPrimary = DesignConstants.GradientPrimary
private val GradientSuccess = DesignConstants.GradientSuccess
private val GradientWarning = DesignConstants.GradientWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompaniesListScreen(
    userCompanies: List<com.christelldev.easyreferplus.data.model.UserCompanyResponse> = emptyList(),
    publicCompanies: List<com.christelldev.easyreferplus.data.model.UserCompanyResponse> = emptyList(),
    isPublicMode: Boolean = false,
    onBack: () -> Unit,
    onRegisterCompany: (() -> Unit)? = null,
    onEditCompany: (Int) -> Unit = {},
    onCompanyClick: ((com.christelldev.easyreferplus.data.model.UserCompanyResponse) -> Unit)? = null
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis Empresas",
                        color = MaterialTheme.colorScheme.surface,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF03A9F4)
                ),
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // En modo público, usar publicCompanies
            val displayCompanies = if (isPublicMode) publicCompanies else userCompanies

            when {
                displayCompanies.isEmpty() -> {
                    if (isPublicMode) {
                        // En modo público, mensaje diferente
                        EmptyPublicCompaniesContent(
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        EmptyCompaniesContent(
                            onRegisterCompany = onRegisterCompany,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                else -> {
                    // Lista de empresas
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Solo mostrar advertencia en modo usuario (no público)
                        if (!isPublicMode) {
                            val hasInactiveCompany = userCompanies.any { company ->
                                company.status != "validated" || !company.isActive
                            }
                            if (hasInactiveCompany) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = CARD_MARGIN_HORIZONTAL, vertical = 8.dp)
                                        .shadow(CARD_ELEVATION, RoundedCornerShape(CARD_CORNER_RADIUS)),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFFFF3E0)
                                    ),
                                    shape = RoundedCornerShape(CARD_CORNER_RADIUS)
                                ) {
                                    Text(
                                        text = "Tu empresa está pendiente de activación o está suspendida. No podrás generar códigos QR hasta que esté activa.",
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFE65100)
                                    )
                                }
                            }
                        }

                        CompaniesListContent(
                            companies = displayCompanies,
                            onRegisterCompany = if (isPublicMode) null else onRegisterCompany,
                            onEditCompany = if (isPublicMode) null else onEditCompany,
                            onCompanyClick = if (isPublicMode) onCompanyClick else null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyPublicCompaniesContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Business,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = AppBlue.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No hay empresas disponibles",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Pronto habrá empresas asociadas para que puedas hacer tus compras.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun EmptyCompaniesContent(
    onRegisterCompany: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Business,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = AppBlue.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No tienes empresas registradas",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Registra tu empresa para comenzar a recibir pagos con códigos QR",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // Solo mostrar botón de registrar si onRegisterCompany no es null
        if (onRegisterCompany != null) {
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRegisterCompany,
                colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.register_company),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun CompaniesListContent(
    companies: List<UserCompanyResponse>,
    onRegisterCompany: (() -> Unit)?,
    onEditCompany: ((Int) -> Unit)?,
    onCompanyClick: ((UserCompanyResponse) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = CARD_MARGIN_HORIZONTAL),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header con botón de registrar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(CARD_ELEVATION, RoundedCornerShape(CARD_CORNER_RADIUS)),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(CARD_CORNER_RADIUS)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(colors = GradientPrimary)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${companies.size} ${if (companies.size == 1) "Empresa" else "Empresas"}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.surface
                            )
                            Text(
                                text = "Gestiona tus empresas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                            )
                        }
                        if (onRegisterCompany != null) {
                            Button(
                                onClick = onRegisterCompany,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color(0xFF03A9F4),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.size(4.dp))
                                Text(
                                    "Nueva",
                                    color = Color(0xFF03A9F4),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Lista de empresas
        items(companies) { company ->
            CompanyCard(company = company, onEdit = onEditCompany, onCompanyClick = onCompanyClick)
        }
    }
}

@Composable
private fun CompanyCard(
    company: UserCompanyResponse,
    onEdit: ((Int) -> Unit)?,
    onCompanyClick: ((UserCompanyResponse) -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(CARD_ELEVATION, RoundedCornerShape(CARD_CORNER_RADIUS))
            .then(
                if (onCompanyClick != null) {
                    Modifier.clickable { onCompanyClick(company) }
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo o Avatar de la empresa
            if (company.logoUrl != null && company.hasLogo) {
                // Agregar timestamp para evitar caché de la imagen
                val logoUrlWithTimestamp = if (company.logoUrl.contains("?")) {
                    "${company.logoUrl}&t=${System.currentTimeMillis()}"
                } else {
                    "${company.logoUrl}?t=${System.currentTimeMillis()}"
                }
                AsyncImage(
                    model = logoUrlWithTimestamp,
                    contentDescription = "Logo de ${company.companyName}",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = if (company.isValidated) GradientSuccess else GradientWarning
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = company.companyName.first().toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.surface
                    )
                }
            }

            Spacer(modifier = Modifier.size(16.dp))

            // Info de la empresa
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = company.companyName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Estado de validación
                    if (company.isValidated) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = "Validada",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4CAF50)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Pending,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFFFA000)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = "En validación",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFA000)
                        )
                    }
                }
            }

            // Botón de editar - solo mostrar si onEdit no es null
            if (onEdit != null) {
                IconButton(onClick = { onEdit(company.id) }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar empresa",
                        tint = AppBlue
                    )
                }
            }
        }
    }
}
