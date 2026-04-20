package com.christelldev.easyreferplus.ui.screens.companies

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.data.model.CategoryInfo
import com.christelldev.easyreferplus.data.model.ServiceInfo
import com.christelldev.easyreferplus.ui.viewmodel.CompanyViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyScreen(
    viewModel: CompanyViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()

    LaunchedEffect(Unit) { viewModel.initialize() }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess && uiState.successMessage == null) onSuccess()
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    if (uiState.successMessage != null) {
        CompanySuccessDialog(message = uiState.successMessage ?: "", onDismiss = onSuccess)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Gradiente superior profundo (260dp para cubrir status bar)
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

            val contentTint = if (isDark) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface

            Column(modifier = Modifier.fillMaxSize()) {
                // TopAppBar con insets de status bar
                TopAppBar(
                    title = {
                        Text(
                            text = if (uiState.isEditing) "Editar Empresa" else stringResource(R.string.register_company),
                            fontWeight = FontWeight.ExtraBold,
                            color = contentTint
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                null,
                                tint = contentTint
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    windowInsets = WindowInsets.statusBars
                )

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp)
                            .verticalScroll(scrollState)
                            .imePadding()
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // SECCIÓN INFORMACIÓN BÁSICA
                        FormSectionCard(title = stringResource(R.string.company_info), icon = Icons.Default.Business) {
                            CompanyTextFieldPremium(
                                value = uiState.companyName,
                                onValueChange = viewModel::updateCompanyName,
                                label = stringResource(R.string.company_name_label),
                                placeholder = stringResource(R.string.company_name_placeholder),
                                error = uiState.fieldErrors["company_name"],
                                isRequired = true
                            )
                            CompanyTextFieldPremium(
                                value = uiState.companyDescription,
                                onValueChange = viewModel::updateCompanyDescription,
                                label = stringResource(R.string.company_description_label),
                                placeholder = stringResource(R.string.company_description_placeholder),
                                error = uiState.fieldErrors["company_description"],
                                isRequired = true,
                                maxLines = 3
                            )
                            CompanyTextFieldPremium(
                                value = uiState.productDescription,
                                onValueChange = viewModel::updateProductDescription,
                                label = stringResource(R.string.product_description_label),
                                placeholder = stringResource(R.string.product_description_placeholder),
                                error = uiState.fieldErrors["product_description"],
                                isRequired = true,
                                maxLines = 3
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // SECCIÓN UBICACIÓN Y CATEGORÍA
                        FormSectionCard(title = "Ubicación y Rubro", icon = Icons.Default.LocationOn) {
                            CompanyTextFieldPremium(
                                value = uiState.address,
                                onValueChange = viewModel::updateAddress,
                                label = stringResource(R.string.address_label),
                                placeholder = "Calle principal #123",
                                error = uiState.fieldErrors["address"],
                                isRequired = true
                            )
                            ProvinceDropdownPremium(
                                selected = uiState.province,
                                options = uiState.provinces,
                                onSelected = viewModel::updateProvince,
                                error = uiState.fieldErrors["province"],
                                isLoading = uiState.isLoadingLocations
                            )
                            CityDropdownPremium(
                                selected = uiState.city,
                                options = uiState.cities,
                                provinceSelected = uiState.province,
                                onSelected = viewModel::updateCity,
                                error = uiState.fieldErrors["city"],
                                isLoading = uiState.isLoadingLocations
                            )
                            CategoryDropdownPremium(
                                selected = uiState.selectedCategory,
                                options = uiState.categories,
                                onSelected = viewModel::selectCategory,
                                isLoading = uiState.isLoadingCategories
                            )
                            ServiceDropdownPremium(
                                selected = uiState.selectedService,
                                options = uiState.services,
                                categorySelected = uiState.selectedCategory != null,
                                onSelected = viewModel::selectService,
                                isLoading = uiState.isLoadingCategories
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // SECCIÓN COMISIÓN
                        FormSectionCard(title = stringResource(R.string.commission_info), icon = Icons.Default.Percent) {
                            CompanyTextFieldPremium(
                                value = uiState.commissionPercentage,
                                onValueChange = viewModel::updateCommissionPercentage,
                                label = stringResource(R.string.commission_percentage_label),
                                placeholder = "10",
                                error = uiState.fieldErrors["commission_percentage"],
                                isRequired = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                suffix = { Text("%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // SECCIÓN ADICIONAL
                        FormSectionCard(title = stringResource(R.string.additional_info), icon = Icons.Default.Language) {
                            CompanyTextFieldPremium(
                                value = uiState.website,
                                onValueChange = viewModel::updateWebsite,
                                label = stringResource(R.string.website_label),
                                placeholder = "https://miempresa.com",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next)
                            )
                            CompanyTextFieldPremium(
                                value = uiState.whatsappNumber,
                                onValueChange = viewModel::updateWhatsappNumber,
                                label = stringResource(R.string.whatsapp_label),
                                placeholder = "593991234567",
                                error = uiState.fieldErrors["whatsapp_number"],
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done)
                            )
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        Button(
                            onClick = if (uiState.isEditing) viewModel::updateCompany else viewModel::registerCompany,
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(if (uiState.isEditing) Icons.Default.Save else Icons.Default.CloudUpload, null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                if (uiState.isEditing) "GUARDAR CAMBIOS" else stringResource(R.string.register_company_button),
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(48.dp))
                        // Espacio para la barra de navegación
                        Spacer(modifier = Modifier.navigationBarsPadding())
                    }
                }
            }
        }
    }
}

@Composable
fun FormSectionCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
                    Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) { content() }
        }
    }
}

@Composable
fun CompanyTextFieldPremium(
    value: String, onValueChange: (String) -> Unit, label: String, placeholder: String,
    modifier: Modifier = Modifier, error: String? = null, isRequired: Boolean = false,
    maxLines: Int = 1, keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    suffix: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            label = { Row { Text(label); if (isRequired) Text(" *", color = MaterialTheme.colorScheme.error) } },
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth(),
            isError = error != null,
            maxLines = maxLines,
            singleLine = maxLines == 1,
            keyboardOptions = keyboardOptions,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            suffix = suffix
        )
        AnimatedVisibility(visible = error != null, enter = fadeIn(), exit = fadeOut()) {
            Text(text = error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 12.dp, top = 4.dp), fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvinceDropdownPremium(selected: String, options: List<String>, onSelected: (String) -> Unit, error: String?, isLoading: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected, onValueChange = {}, readOnly = true,
            label = { Row { Text(stringResource(R.string.province_label)); Text(" *", color = MaterialTheme.colorScheme.error) } },
            placeholder = { Text("Seleccionar provincia") },
            trailingIcon = { if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) else ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable, enabled = true),
            isError = error != null,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { onSelected(it); expanded = false }) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityDropdownPremium(selected: String, options: List<String>, provinceSelected: String, onSelected: (String) -> Unit, error: String?, isLoading: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded && provinceSelected.isNotBlank(), onExpandedChange = { if (provinceSelected.isNotBlank()) expanded = it }) {
        OutlinedTextField(
            value = selected, onValueChange = {}, readOnly = true, enabled = provinceSelected.isNotBlank(),
            label = { Row { Text(stringResource(R.string.city_label)); Text(" *", color = MaterialTheme.colorScheme.error) } },
            placeholder = { Text(if (provinceSelected.isBlank()) "Seleccione provincia" else "Seleccionar ciudad") },
            trailingIcon = { if (isLoading && provinceSelected.isNotBlank()) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) else ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable, enabled = true),
            isError = error != null,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
        )
        ExposedDropdownMenu(expanded = expanded && provinceSelected.isNotBlank(), onDismissRequest = { expanded = false }) {
            options.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { onSelected(it); expanded = false }) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdownPremium(selected: CategoryInfo?, options: List<CategoryInfo>, onSelected: (CategoryInfo?) -> Unit, isLoading: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.name ?: "", onValueChange = {}, readOnly = true,
            label = { Text(stringResource(R.string.category_label)) },
            placeholder = { Text(stringResource(R.string.select_category)) },
            trailingIcon = { if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) else ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable, enabled = true),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { DropdownMenuItem(text = { Text(it.name) }, onClick = { onSelected(it); expanded = false }) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDropdownPremium(selected: ServiceInfo?, options: List<ServiceInfo>, categorySelected: Boolean, onSelected: (ServiceInfo?) -> Unit, isLoading: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded && categorySelected, onExpandedChange = { if (categorySelected) expanded = it }) {
        OutlinedTextField(
            value = selected?.name ?: "", onValueChange = {}, readOnly = true, enabled = categorySelected,
            label = { Text(stringResource(R.string.service_label)) },
            placeholder = { Text(if (!categorySelected) stringResource(R.string.select_category_first) else stringResource(R.string.select_service)) },
            trailingIcon = { if (isLoading && categorySelected) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) else ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable, enabled = true),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
        )
        ExposedDropdownMenu(expanded = expanded && categorySelected, onDismissRequest = { expanded = false }) {
            options.forEach { DropdownMenuItem(text = { Text(it.name) }, onClick = { onSelected(it); expanded = false }) }
        }
    }
}

@Composable
fun CompanySuccessDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981), modifier = Modifier.size(64.dp)) },
        title = { Text(stringResource(R.string.success), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black) },
        text = { Text(message, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Medium) },
        confirmButton = { Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))) { Text(stringResource(R.string.accept), fontWeight = FontWeight.Bold) } },
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 6.dp
    )
}
