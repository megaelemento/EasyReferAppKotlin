package com.christelldev.easyreferplus.ui.screens.companies

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.data.model.CategoryInfo
import com.christelldev.easyreferplus.data.model.ServiceInfo
import com.christelldev.easyreferplus.ui.theme.AppBlue
import com.christelldev.easyreferplus.ui.theme.DesignConstants
import com.christelldev.easyreferplus.ui.viewmodel.CompanyViewModel

// Constants for elegant design
private val CARD_CORNER_RADIUS = DesignConstants.CARD_CORNER_RADIUS
private val CARD_ELEVATION = DesignConstants.CARD_ELEVATION
private val CARD_MARGIN_HORIZONTAL = DesignConstants.CARD_MARGIN_HORIZONTAL
private val GradientPrimary = DesignConstants.GradientPrimary
private val GradientSuccess = DesignConstants.GradientSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCompanyScreen(
    viewModel: CompanyViewModel,
    companyId: Int,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Cargar datos de la empresa al iniciar
    // Always load fresh data when the screen is shown
    // Use initialize to also load categories and services
    LaunchedEffect(companyId) {
        viewModel.initialize()
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            Toast.makeText(context, uiState.successMessage ?: "Empresa actualizada", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    // Show success messages for logo
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.edit_company),
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Logo de la empresa
                LogoUploadSection(
                    logoUrl = uiState.logoUrl,
                    hasLogo = uiState.hasLogo,
                    isUploading = uiState.isUploadingLogo,
                    isDeleting = uiState.isDeletingLogo,
                    onLogoSelected = { file ->
                        viewModel.uploadLogo(file)
                    },
                    onDeleteLogo = {
                        viewModel.deleteLogo()
                    }
                )

                // Sección: Información Básica
                EditSectionCard(
                    title = "Información Básica",
                    icon = Icons.Default.Business
                ) {
                    EditTextField(
                        value = uiState.companyName,
                        onValueChange = viewModel::updateCompanyName,
                        label = stringResource(R.string.company_name),
                        placeholder = "Nombre de tu empresa",
                        error = uiState.fieldErrors["company_name"],
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    EditTextField(
                        value = uiState.companyDescription,
                        onValueChange = viewModel::updateCompanyDescription,
                        label = stringResource(R.string.company_description),
                        placeholder = "Describe tu empresa...",
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    EditTextField(
                        value = uiState.productDescription,
                        onValueChange = viewModel::updateProductDescription,
                        label = stringResource(R.string.product_description),
                        placeholder = "Productos o servicios...",
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Sección: Ubicación
                EditSectionCard(
                    title = "Ubicación",
                    icon = Icons.Default.LocationOn
                ) {
                    EditTextField(
                        value = uiState.address,
                        onValueChange = viewModel::updateAddress,
                        label = stringResource(R.string.address),
                        placeholder = "Dirección de tu empresa",
                        error = uiState.fieldErrors["address"],
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProvinceDropdown(
                            value = uiState.province,
                            onValueChange = viewModel::updateProvince,
                            provinces = uiState.provinces,
                            isLoading = uiState.isLoadingLocations,
                            modifier = Modifier.weight(1f)
                        )

                        CityDropdown(
                            value = uiState.city,
                            onValueChange = viewModel::updateCity,
                            cities = uiState.cities,
                            isLoading = uiState.isLoadingLocations,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Sección: Categoría y Servicio
                EditSectionCard(
                    title = "Categoría y Servicio",
                    icon = Icons.Default.Business
                ) {
                    CategoryServiceDropdown(
                        selectedCategory = uiState.selectedCategory,
                        categories = uiState.categories,
                        selectedService = uiState.selectedService,
                        services = uiState.services,
                        isLoading = uiState.isLoadingCategories,
                        onCategorySelected = viewModel::selectCategory,
                        onServiceSelected = viewModel::selectService
                    )
                }

                // Sección: Redes Sociales y Contacto
                EditSectionCard(
                    title = "Redes y Contacto",
                    icon = Icons.Default.Language
                ) {
                    EditTextField(
                        value = uiState.website,
                        onValueChange = viewModel::updateWebsite,
                        label = stringResource(R.string.website_optional),
                        placeholder = "https://tuempresa.com",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        EditTextField(
                            value = uiState.facebookUrl,
                            onValueChange = viewModel::updateFacebookUrl,
                            label = "Facebook",
                            placeholder = "facebook.com/tuempresa",
                            modifier = Modifier.weight(1f)
                        )

                        EditTextField(
                            value = uiState.instagramUrl,
                            onValueChange = viewModel::updateInstagramUrl,
                            label = "Instagram",
                            placeholder = "@tuempresa",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    EditTextField(
                        value = uiState.whatsappNumber,
                        onValueChange = viewModel::updateWhatsappNumber,
                        label = stringResource(R.string.whatsapp_number),
                        placeholder = "+593987654321",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Sección: Comisiones
                EditSectionCard(
                    title = "Comisiones",
                    icon = Icons.Default.Star
                ) {
                    EditTextField(
                        value = uiState.commissionPercentage,
                        onValueChange = viewModel::updateCommissionPercentage,
                        label = stringResource(R.string.commission_percentage),
                        placeholder = "10",
                        error = uiState.fieldErrors["commission_percentage"],
                        suffix = "%",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Porcentaje de comisión para referidos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón de guardar
                Button(
                    onClick = viewModel::updateCompany,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(CARD_ELEVATION, RoundedCornerShape(CARD_CORNER_RADIUS)),
                    shape = RoundedCornerShape(CARD_CORNER_RADIUS),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03A9F4))
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.save_changes),
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


@Composable
fun LogoUploadSection(
    logoUrl: String?,
    hasLogo: Boolean,
    isUploading: Boolean,
    isDeleting: Boolean,
    onLogoSelected: (java.io.File) -> Unit,
    onDeleteLogo: () -> Unit
) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Convert URI to File
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val fileName = "logo_${System.currentTimeMillis()}.jpg"
                val file = java.io.File(context.cacheDir, fileName)
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                onLogoSelected(file)
            } catch (e: Exception) {
                Toast.makeText(context, "Error al procesar imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(CARD_ELEVATION, RoundedCornerShape(CARD_CORNER_RADIUS)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title with gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = Brush.horizontalGradient(GradientPrimary))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Logo de tu Empresa",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.surface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Añade un logo para que tus clientes te reconozcan fácilmente",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Logo area
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(
                        brush = if (hasLogo && !logoUrl.isNullOrBlank())
                            Brush.linearGradient(GradientSuccess)
                        else
                            Brush.linearGradient(GradientPrimary)
                    )
                    .border(
                        width = 3.dp,
                        color = if (hasLogo && !logoUrl.isNullOrBlank()) Color(0xFF10B981) else Color(0xFF03A9F4),
                        shape = CircleShape
                    )
                    .clickable(enabled = !isUploading && !isDeleting) {
                        launcher.launch("image/*")
                    },
                contentAlignment = Alignment.Center
            ) {
                when {
                    isUploading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = AppBlue,
                            strokeWidth = 3.dp
                        )
                    }
                    isDeleting -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = MaterialTheme.colorScheme.error,
                            strokeWidth = 3.dp
                        )
                    }
                    hasLogo && !logoUrl.isNullOrBlank() -> {
                        // Use remember with logoUrl as key to force recalculation
                        val logoUrlWithTimestamp = remember(logoUrl) {
                            val baseUrl = logoUrl.substringBefore("?")
                            val separator = if (logoUrl.contains("?")) "&" else "?"
                            "$baseUrl${separator}t=${System.currentTimeMillis()}"
                        }
                        // Show existing logo
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(logoUrlWithTimestamp)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Logo de la empresa",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        // Show add icon
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = "Agregar logo",
                                modifier = Modifier.size(48.dp),
                                tint = AppBlue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Subir Logo",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppBlue
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Button change/upload logo
                Button(
                    onClick = { launcher.launch("image/*") },
                    enabled = !isUploading && !isDeleting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (hasLogo) "Cambiar" else "Subir Logo")
                    }
                }

                // Delete button (only if has logo)
                if (hasLogo) {
                    Button(
                        onClick = onDeleteLogo,
                        enabled = !isUploading && !isDeleting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Eliminar")
                        }
                    }
                }
            }

            // Additional information
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Formatos soportados: JPG, PNG, WebP\nTamaño máximo: 5MB",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EditCompanyHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppBlue.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(AppBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Editar Empresa",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppBlue
                )
                Text(
                    text = "Actualiza la información de tu empresa",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EditSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(CARD_ELEVATION, RoundedCornerShape(CARD_CORNER_RADIUS)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = Brush.horizontalGradient(GradientPrimary))
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.surface
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun EditTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    error: String? = null,
    maxLines: Int = 1,
    suffix: String? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(CARD_CORNER_RADIUS)),
            singleLine = maxLines == 1,
            maxLines = maxLines,
            shape = RoundedCornerShape(CARD_CORNER_RADIUS),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF03A9F4),
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = Color(0xFF03A9F4),
                cursorColor = Color(0xFF03A9F4)
            ),
            isError = error != null,
            suffix = suffix?.let { { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        )
        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvinceDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    provinces: List<String>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.province)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                enabled = !isLoading
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                provinces.forEach { province ->
                    DropdownMenuItem(
                        text = { Text(province) },
                        onClick = {
                            onValueChange(province)
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
fun CityDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    cities: List<String>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.city)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                enabled = !isLoading && cities.isNotEmpty()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                cities.forEach { city ->
                    DropdownMenuItem(
                        text = { Text(city) },
                        onClick = {
                            onValueChange(city)
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
fun PaymentFrequencyDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val frequencies = listOf("Quincenal", "Mensual")

    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = value.ifEmpty { "Seleccionar" },
                onValueChange = {},
                readOnly = true,
                label = { Text("Frecuencia de pago") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
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
                frequencies.forEach { frequency ->
                    DropdownMenuItem(
                        text = { Text(frequency) },
                        onClick = {
                            onValueChange(frequency)
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
fun CategoryServiceDropdown(
    selectedCategory: CategoryInfo?,
    categories: List<CategoryInfo>,
    selectedService: ServiceInfo?,
    services: List<ServiceInfo>,
    isLoading: Boolean,
    onCategorySelected: (CategoryInfo?) -> Unit,
    onServiceSelected: (ServiceInfo?) -> Unit
) {
    var categoryExpanded by remember { mutableStateOf(false) }
    var serviceExpanded by remember { mutableStateOf(false) }

    // Filter services based on selected category
    val filteredServices = remember(selectedCategory, services) {
        if (selectedCategory == null) {
            services
        } else {
            services.filter { it.categoryId == selectedCategory.id }
        }
    }

    Column {
        // Category Dropdown
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedCategory?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Categoría") },
                placeholder = { Text("Seleccionar categoría") },
                trailingIcon = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
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
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            onCategorySelected(category)
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Service Dropdown
        ExposedDropdownMenuBox(
            expanded = serviceExpanded,
            onExpandedChange = { if (selectedCategory != null) serviceExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedService?.name ?: "",
                onValueChange = {},
                readOnly = true,
                enabled = selectedCategory != null,
                label = { Text("Servicio") },
                placeholder = {
                    Text(
                        if (selectedCategory == null) "Selecciona una categoría primero"
                        else "Seleccionar servicio"
                    )
                },
                trailingIcon = {
                    if (isLoading && selectedCategory != null) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else if (selectedCategory != null) {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceExpanded)
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
                expanded = serviceExpanded && selectedCategory != null,
                onDismissRequest = { serviceExpanded = false }
            ) {
                filteredServices.forEach { service ->
                    DropdownMenuItem(
                        text = { Text(service.name) },
                        onClick = {
                            onServiceSelected(service)
                            serviceExpanded = false
                        }
                    )
                }
            }
        }
    }
}
