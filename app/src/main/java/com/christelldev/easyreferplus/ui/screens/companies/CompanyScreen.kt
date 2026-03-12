package com.christelldev.easyreferplus.ui.screens.companies

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.ui.theme.AppBlue
import com.christelldev.easyreferplus.ui.theme.DesignConstants
import com.christelldev.easyreferplus.ui.viewmodel.CompanyViewModel
import com.christelldev.easyreferplus.data.model.CategoryInfo
import com.christelldev.easyreferplus.data.model.ServiceInfo

// Constants for elegant design
private val CARD_CORNER_RADIUS = DesignConstants.CARD_CORNER_RADIUS
private val CARD_ELEVATION = DesignConstants.CARD_ELEVATION
private val CARD_MARGIN_HORIZONTAL = DesignConstants.CARD_MARGIN_HORIZONTAL
private val GradientPrimary = DesignConstants.GradientPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyScreen(
    viewModel: CompanyViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Inicializar datos cuando la pantalla esté lista
    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    // Success Dialog
    if (uiState.isSuccess && uiState.successMessage != null) {
        CompanySuccessDialog(
            message = uiState.successMessage ?: "",
            onDismiss = onSuccess
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.register_company),
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
                    containerColor = AppBlue
                ),
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
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
                CircularProgressIndicator(color = AppBlue)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                // Required Fields Section
                SectionTitle(text = stringResource(R.string.company_info))

                Spacer(modifier = Modifier.height(12.dp))

                CompanyTextField(
                    value = uiState.companyName,
                    onValueChange = viewModel::updateCompanyName,
                    label = stringResource(R.string.company_name_label),
                    placeholder = stringResource(R.string.company_name_placeholder),
                    error = uiState.fieldErrors["company_name"],
                    isRequired = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                Spacer(modifier = Modifier.height(12.dp))

                CompanyTextField(
                    value = uiState.companyDescription,
                    onValueChange = viewModel::updateCompanyDescription,
                    label = stringResource(R.string.company_description_label),
                    placeholder = stringResource(R.string.company_description_placeholder),
                    error = uiState.fieldErrors["company_description"],
                    isRequired = true,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                Spacer(modifier = Modifier.height(12.dp))

                CompanyTextField(
                    value = uiState.productDescription,
                    onValueChange = viewModel::updateProductDescription,
                    label = stringResource(R.string.product_description_label),
                    placeholder = stringResource(R.string.product_description_placeholder),
                    error = uiState.fieldErrors["product_description"],
                    isRequired = true,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Location Section
                SectionTitle(text = stringResource(R.string.location_info))

                Spacer(modifier = Modifier.height(12.dp))

                CompanyTextField(
                    value = uiState.address,
                    onValueChange = viewModel::updateAddress,
                    label = stringResource(R.string.address_label),
                    placeholder = "Calle principal #123 (mín. 10 caracteres)",
                    error = uiState.fieldErrors["address"],
                    isRequired = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Provincia Dropdown
                ProvinceDropdown(
                    selectedProvince = uiState.province,
                    provinces = uiState.provinces,
                    onProvinceSelected = viewModel::updateProvince,
                    error = uiState.fieldErrors["province"],
                    isLoading = uiState.isLoadingLocations
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Ciudad Dropdown
                CityDropdown(
                    selectedCity = uiState.city,
                    cities = uiState.cities,
                    selectedProvince = uiState.province,
                    onCitySelected = viewModel::updateCity,
                    error = uiState.fieldErrors["city"],
                    isLoading = uiState.isLoadingLocations
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category Dropdown
                CategoryDropdown(
                    selectedCategory = uiState.selectedCategory,
                    categories = uiState.categories,
                    onCategorySelected = viewModel::selectCategory,
                    isLoading = uiState.isLoadingCategories
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Service Dropdown
                ServiceDropdown(
                    selectedService = uiState.selectedService,
                    services = uiState.services,
                    selectedCategory = uiState.selectedCategory,
                    onServiceSelected = viewModel::selectService,
                    isLoading = uiState.isLoadingCategories
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Commission Section
                SectionTitle(text = stringResource(R.string.commission_info))

                Spacer(modifier = Modifier.height(12.dp))

                CompanyTextField(
                    value = uiState.commissionPercentage,
                    onValueChange = viewModel::updateCommissionPercentage,
                    label = stringResource(R.string.commission_percentage_label),
                    placeholder = "10",
                    error = uiState.fieldErrors["commission_percentage"],
                    isRequired = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    suffix = { Text("%") }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Optional Fields Section
                SectionTitle(text = stringResource(R.string.additional_info))

                Spacer(modifier = Modifier.height(12.dp))

                CompanyTextField(
                    value = uiState.website,
                    onValueChange = viewModel::updateWebsite,
                    label = stringResource(R.string.website_label),
                    placeholder = "https://miempresa.com",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CompanyTextField(
                        value = uiState.facebookUrl,
                        onValueChange = viewModel::updateFacebookUrl,
                        label = stringResource(R.string.facebook_label),
                        placeholder = "https://facebook.com/...",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Next
                        )
                    )

                    CompanyTextField(
                        value = uiState.instagramUrl,
                        onValueChange = viewModel::updateInstagramUrl,
                        label = stringResource(R.string.instagram_label),
                        placeholder = "https://instagram.com/...",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Next
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                CompanyTextField(
                    value = uiState.whatsappNumber,
                    onValueChange = viewModel::updateWhatsappNumber,
                    label = stringResource(R.string.whatsapp_label),
                    placeholder = "593991234567",
                    error = uiState.fieldErrors["whatsapp_number"],
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Submit Button
                Button(
                    onClick = viewModel::registerCompany,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppBlue
                    ),
                    shape = RoundedCornerShape(CARD_CORNER_RADIUS)
                ) {
                    Text(
                        text = stringResource(R.string.register_company_button),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.surface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvinceDropdown(
    selectedProvince: String,
    provinces: List<String>,
    onProvinceSelected: (String) -> Unit,
    error: String?,
    isLoading: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedProvince,
                onValueChange = {},
                readOnly = true,
                label = {
                    Row {
                        Text(stringResource(R.string.province_label))
                        Text(" *", color = Color.Red)
                    }
                },
                placeholder = { Text("Seleccionar provincia") },
                trailingIcon = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                isError = error != null,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    errorBorderColor = Color.Red
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                provinces.forEach { province ->
                    DropdownMenuItem(
                        text = { Text(province) },
                        onClick = {
                            onProvinceSelected(province)
                            expanded = false
                        }
                    )
                }
            }
        }
        if (error != null) {
            Text(
                text = error,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityDropdown(
    selectedCity: String,
    cities: List<String>,
    selectedProvince: String,
    onCitySelected: (String) -> Unit,
    error: String?,
    isLoading: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        ExposedDropdownMenuBox(
            expanded = expanded && selectedProvince.isNotBlank(),
            onExpandedChange = { if (selectedProvince.isNotBlank()) expanded = it }
        ) {
            OutlinedTextField(
                value = selectedCity,
                onValueChange = {},
                readOnly = true,
                enabled = selectedProvince.isNotBlank(),
                label = {
                    Row {
                        Text(stringResource(R.string.city_label))
                        Text(" *", color = Color.Red)
                    }
                },
                placeholder = {
                    Text(
                        if (selectedProvince.isBlank()) "Seleccione provincia primero"
                        else "Seleccionar ciudad"
                    )
                },
                trailingIcon = {
                    if (isLoading && selectedProvince.isNotBlank()) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else if (selectedProvince.isNotBlank()) {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                isError = error != null,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    errorBorderColor = Color.Red,
                    disabledBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            ExposedDropdownMenu(
                expanded = expanded && selectedProvince.isNotBlank(),
                onDismissRequest = { expanded = false }
            ) {
                cities.forEach { city ->
                    DropdownMenuItem(
                        text = { Text(city) },
                        onClick = {
                            onCitySelected(city)
                            expanded = false
                        }
                    )
                }
            }
        }
        if (error != null) {
            Text(
                text = error,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    selectedCategory: CategoryInfo?,
    categories: List<CategoryInfo>,
    onCategorySelected: (CategoryInfo?) -> Unit,
    isLoading: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedCategory?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = {
                    Text(stringResource(R.string.category_label))
                },
                placeholder = { Text(stringResource(R.string.select_category)) },
                trailingIcon = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            onCategorySelected(category)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDropdown(
    selectedService: ServiceInfo?,
    services: List<ServiceInfo>,
    selectedCategory: CategoryInfo?,
    onServiceSelected: (ServiceInfo?) -> Unit,
    isLoading: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        ExposedDropdownMenuBox(
            expanded = expanded && selectedCategory != null,
            onExpandedChange = { if (selectedCategory != null) expanded = it }
        ) {
            OutlinedTextField(
                value = selectedService?.name ?: "",
                onValueChange = {},
                readOnly = true,
                enabled = selectedCategory != null,
                label = {
                    Text(stringResource(R.string.service_label))
                },
                placeholder = {
                    Text(
                        if (selectedCategory == null) stringResource(R.string.select_category_first)
                        else stringResource(R.string.select_service)
                    )
                },
                trailingIcon = {
                    if (isLoading && selectedCategory != null) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else if (selectedCategory != null) {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    disabledBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            ExposedDropdownMenu(
                expanded = expanded && selectedCategory != null,
                onDismissRequest = { expanded = false }
            ) {
                services.forEach { service ->
                    DropdownMenuItem(
                        text = { Text(service.name) },
                        onClick = {
                            onServiceSelected(service)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(CARD_ELEVATION, RoundedCornerShape(CARD_CORNER_RADIUS)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.horizontalGradient(GradientPrimary))
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
fun CompanyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    isRequired: Boolean = false,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    suffix: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Row {
                    Text(label)
                    if (isRequired) {
                        Text(" *", color = Color.Red)
                    }
                }
            },
            placeholder = { Text(placeholder) },
            modifier = Modifier
                .fillMaxWidth(),
            isError = error != null,
            maxLines = maxLines,
            singleLine = maxLines == 1,
            keyboardOptions = keyboardOptions,
            shape = RoundedCornerShape(CARD_CORNER_RADIUS),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF03A9F4),
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = Color.Red
            ),
            suffix = suffix
        )
        if (error != null) {
            Text(
                text = error,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun CompanySuccessDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = AppBlue,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = stringResource(R.string.success),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AppBlue)
            ) {
                Text(stringResource(R.string.accept))
            }
        }
    )
}
