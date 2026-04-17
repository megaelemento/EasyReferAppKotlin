package com.christelldev.easyreferplus.ui.screens.companies

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.painter.ColorPainter
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.data.model.UserCompanyResponse
import com.christelldev.easyreferplus.ui.theme.DesignConstants
import com.christelldev.easyreferplus.ui.viewmodel.PublicCompaniesViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicCompaniesScreen(
    viewModel: PublicCompaniesViewModel,
    onNavigateBack: () -> Unit,
    onCompanyClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()

    LaunchedEffect(Unit) {
        viewModel.loadCompanies()
        viewModel.loadProvinces()
        viewModel.loadCategories()
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
                            text = "Directorio de Comercios",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )

                // Sección de Búsqueda y Filtros Premium
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            placeholder = { Text("Buscar comercios por nombre...") },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                        Icon(Icons.Default.Clear, null, tint = MaterialTheme.colorScheme.outline)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                        )
                        
                        FilterExpansionSection(uiState, viewModel)
                    }
                }

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (uiState.companies.isEmpty()) {
                    EmptyPublicSearchState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.companies, key = { it.id }) { company ->
                            PublicCompanyCardPremium(
                                company = company,
                                onClick = { onCompanyClick(company.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterExpansionSection(uiState: com.christelldev.easyreferplus.ui.viewmodel.PublicCompaniesUiState, viewModel: PublicCompaniesViewModel) {
    var showFilters by remember { mutableStateOf(false) }
    
    Column {
        TextButton(
            onClick = { showFilters = !showFilters },
            modifier = Modifier.align(Alignment.End),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Icon(if (showFilters) Icons.Default.FilterListOff else Icons.Default.FilterList, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (showFilters) "Ocultar filtros" else "Más filtros", fontWeight = FontWeight.Bold)
        }

        AnimatedVisibility(visible = showFilters) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProvinceSelector(uiState, viewModel, Modifier.weight(1f))
                    CitySelector(uiState, viewModel, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CategorySelector(uiState, viewModel, Modifier.weight(1f))
                    ServiceSelector(uiState, viewModel, Modifier.weight(1f))
                }
                if (uiState.selectedProvince != null || uiState.selectedCity != null || uiState.selectedCategory != null) {
                    TextButton(onClick = viewModel::clearFilters, modifier = Modifier.fillMaxWidth()) {
                        Text("Limpiar filtros", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProvinceSelector(uiState: com.christelldev.easyreferplus.ui.viewmodel.PublicCompaniesUiState, viewModel: PublicCompaniesViewModel, modifier: Modifier) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = uiState.selectedProvince ?: "Provincia", onValueChange = {}, readOnly = true,
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true), shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
            textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            uiState.provinces.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { viewModel.onProvinceSelected(it); expanded = false }) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CitySelector(uiState: com.christelldev.easyreferplus.ui.viewmodel.PublicCompaniesUiState, viewModel: PublicCompaniesViewModel, modifier: Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val enabled = uiState.selectedProvince != null
    ExposedDropdownMenuBox(expanded = expanded && enabled, onExpandedChange = { if (enabled) expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = uiState.selectedCity ?: "Ciudad", onValueChange = {}, readOnly = true, enabled = enabled,
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true), shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
            textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        )
        ExposedDropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
            uiState.cities.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { viewModel.onCitySelected(it); expanded = false }) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySelector(uiState: com.christelldev.easyreferplus.ui.viewmodel.PublicCompaniesUiState, viewModel: PublicCompaniesViewModel, modifier: Modifier) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = uiState.selectedCategory?.name ?: "Categoría", onValueChange = {}, readOnly = true,
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true), shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
            textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            uiState.categories.forEach { DropdownMenuItem(text = { Text(it.name) }, onClick = { viewModel.onCategorySelected(it); expanded = false }) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceSelector(uiState: com.christelldev.easyreferplus.ui.viewmodel.PublicCompaniesUiState, viewModel: PublicCompaniesViewModel, modifier: Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val enabled = uiState.selectedCategory != null
    ExposedDropdownMenuBox(expanded = expanded && enabled, onExpandedChange = { if (enabled) expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = uiState.selectedService?.name ?: "Servicio", onValueChange = {}, readOnly = true, enabled = enabled,
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true), shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
            textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        )
        ExposedDropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
            uiState.services.forEach { DropdownMenuItem(text = { Text(it.name) }, onClick = { viewModel.onServiceSelected(it); expanded = false }) }
        }
    }
}

@Composable
fun PublicCompanyCardPremium(
    company: UserCompanyResponse,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Logo
            Surface(
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                if (company.logoUrl != null && company.hasLogo) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(company.logoUrl).crossfade(true).build(),
                        contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
                        placeholder = ColorPainter(Color(0xFFE0E0E0)),
                        error = ColorPainter(Color(0xFFEEEEEE))
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Text(company.companyName.take(1).uppercase(), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(company.companyName, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = company.companyDescription ?: "Sin descripción",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${company.city ?: "Ciudad"}, ${company.province ?: "Provincia"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun EmptyPublicSearchState() {
    Column(modifier = Modifier.fillMaxSize().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Storefront, null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(24.dp))
        Text("No se encontraron comercios", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Text("Prueba con otra búsqueda o ajusta los filtros de ubicación y categoría.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
