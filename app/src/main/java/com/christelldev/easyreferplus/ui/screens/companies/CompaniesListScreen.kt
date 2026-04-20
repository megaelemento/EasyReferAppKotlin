package com.christelldev.easyreferplus.ui.screens.companies

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.data.model.UserCompanyResponse
import com.christelldev.easyreferplus.ui.theme.DesignConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompaniesListScreen(
    userCompanies: List<UserCompanyResponse> = emptyList(),
    publicCompanies: List<UserCompanyResponse> = emptyList(),
    isPublicMode: Boolean = false,
    onBack: () -> Unit,
    onRegisterCompany: (() -> Unit)? = null,
    onEditCompany: (Int) -> Unit = {},
    onCompanyClick: ((UserCompanyResponse) -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()
    val displayCompanies = if (isPublicMode) publicCompanies else userCompanies

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Gradiente superior profundo que ocupa toda la parte superior incluyendo status bar
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
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // Cabecera Premium con insets de status bar
                TopAppBar(
                    title = {
                        Text(
                            text = if (isPublicMode) "Comercios" else "Mis Empresas",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                null,
                                tint = if (isDark) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    windowInsets = WindowInsets.statusBars
                )

                if (displayCompanies.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (isPublicMode) EmptyPublicCompaniesState() else EmptyUserCompaniesState(onRegisterCompany)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Dashboard de Resumen (Solo para dueños)
                        if (!isPublicMode) {
                            item {
                                CompaniesDashboardCard(
                                    count = userCompanies.size,
                                    onAdd = onRegisterCompany
                                )
                            }

                            // Alerta de Inactivas
                            val inactive = userCompanies.filter { !it.isValidated || !it.isActive }
                            if (inactive.isNotEmpty()) {
                                item {
                                    Surface(
                                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                "Tienes ${inactive.size} empresa(s) pendiente(s) de validación. Algunas funciones podrían estar limitadas.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Lista de Empresas
                        items(displayCompanies, key = { it.id }) { company ->
                            CompanyListItemPremium(
                                company = company,
                                isPublic = isPublicMode,
                                onEdit = { onEditCompany(it) },
                                onClick = { onCompanyClick?.invoke(it) }
                            )
                        }

                        // Espacio para la barra de navegación al final de la lista
                        item { Spacer(modifier = Modifier.navigationBarsPadding()) }
                    }
                }
            }
        }
    }
}

@Composable
fun CompaniesDashboardCard(count: Int, onAdd: (() -> Unit)?) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        tonalElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("TOTAL EMPRESAS", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Text(
                    text = "$count ${if (count == 1) "registrada" else "registradas"}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (onAdd != null) {
                Surface(
                    modifier = Modifier.size(48.dp).clickable { onAdd() },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, null, tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun CompanyListItemPremium(
    company: UserCompanyResponse,
    isPublic: Boolean,
    onEdit: (Int) -> Unit,
    onClick: (UserCompanyResponse) -> Unit
) {
    val statusColor = if (company.isValidated) Color(0xFF10B981) else Color(0xFFF59E0B)
    
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { if (isPublic) onClick(company) },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Logo / Inicial
            if (company.logoUrl != null && company.hasLogo) {
                AsyncImage(
                    model = company.logoUrl,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp).clip(CircleShape)
                )
            } else {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(company.companyName.take(1).uppercase(), fontWeight = FontWeight.Black, color = statusColor, style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(company.companyName, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.size(8.dp), shape = CircleShape, color = statusColor) {}
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (company.isValidated) "Negocio Validado" else "En Validación",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            if (!isPublic) {
                IconButton(
                    onClick = { onEdit(company.id) },
                    modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape).size(36.dp)
                ) {
                    Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            } else {
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
fun EmptyUserCompaniesState(onRegister: (() -> Unit)?) {
    Column(modifier = Modifier.fillMaxSize().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Storefront, null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(24.dp))
        Text("No tienes empresas", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Text("Registra tu negocio para empezar a recibir pagos por QR y expandir tu red.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (onRegister != null) {
            Spacer(modifier = Modifier.height(40.dp))
            Button(onClick = onRegister, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Registrar Mi Empresa", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun EmptyPublicCompaniesState() {
    Column(modifier = Modifier.fillMaxSize().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Sin comercios aún", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Text("Estamos trabajando para traer los mejores comercios a nuestra red muy pronto.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
