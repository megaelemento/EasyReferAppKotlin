package com.christelldev.easyreferplus.ui.screens.companies

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.data.model.CategoryInfo
import com.christelldev.easyreferplus.data.model.ServiceInfo
import com.christelldev.easyreferplus.data.model.UserCompanyResponse
import com.christelldev.easyreferplus.ui.viewmodel.CompanyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCompanyScreen(
    viewModel: CompanyViewModel,
    companyId: Int,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()

    LaunchedEffect(companyId) {
        viewModel.initialize()
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess && uiState.successMessage != null && !uiState.successMessage!!.contains("logo", ignoreCase = true)) {
            Toast.makeText(context, uiState.successMessage, Toast.LENGTH_SHORT).show()
            onSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Gradiente superior profundo
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
                // TopAppBar manual con insets de status bar
                TopAppBar(
                    title = { Text("Editar Empresa", fontWeight = FontWeight.ExtraBold, color = contentTint) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = contentTint)
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

                        // SECCIÓN LOGO
                        LogoUploadSection(
                            logoUrl = uiState.logoUrl,
                            hasLogo = uiState.hasLogo,
                            isUploading = uiState.isUploadingLogo,
                            isDeleting = uiState.isDeletingLogo,
                            onLogoSelected = { viewModel.uploadLogo(it) },
                            onDeleteLogo = { viewModel.deleteLogo() }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // SECCIÓN INFORMACIÓN BÁSICA
                        FormSectionCard(title = "Información General", icon = Icons.Default.Business) {
                            CompanyTextFieldPremium(
                                value = uiState.companyName,
                                onValueChange = viewModel::updateCompanyName,
                                label = "Nombre de la Empresa",
                                placeholder = "Ej: Mi Negocio S.A.",
                                error = uiState.fieldErrors["company_name"],
                                isRequired = true
                            )
                            CompanyTextFieldPremium(
                                value = uiState.companyDescription,
                                onValueChange = viewModel::updateCompanyDescription,
                                label = "Descripción",
                                placeholder = "Cuenta de qué trata tu empresa...",
                                maxLines = 3,
                                isRequired = true
                            )
                            CompanyTextFieldPremium(
                                value = uiState.productDescription,
                                onValueChange = viewModel::updateProductDescription,
                                label = "Productos/Servicios",
                                placeholder = "Detalla lo que ofreces...",
                                maxLines = 3,
                                isRequired = true
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // SECCIÓN UBICACIÓN
                        FormSectionCard(title = "Ubicación", icon = Icons.Default.LocationOn) {
                            CompanyTextFieldPremium(
                                value = uiState.address,
                                onValueChange = viewModel::updateAddress,
                                label = "Dirección Física",
                                placeholder = "Av. Principal #123",
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
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // SECCIÓN CATEGORÍA
                        FormSectionCard(title = "Rubro de Negocio", icon = Icons.Default.Category) {
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

                        // SECCIÓN CONTACTO
                        FormSectionCard(title = "Contacto y Redes", icon = Icons.Default.Language) {
                            CompanyTextFieldPremium(
                                value = uiState.website,
                                onValueChange = viewModel::updateWebsite,
                                label = "Sitio Web",
                                placeholder = "https://empresa.com"
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                CompanyTextFieldPremium(
                                    value = uiState.facebookUrl,
                                    onValueChange = viewModel::updateFacebookUrl,
                                    label = "Facebook",
                                    placeholder = "user",
                                    modifier = Modifier.weight(1f)
                                )
                                CompanyTextFieldPremium(
                                    value = uiState.instagramUrl,
                                    onValueChange = viewModel::updateInstagramUrl,
                                    label = "Instagram",
                                    placeholder = "@user",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            CompanyTextFieldPremium(
                                value = uiState.whatsappNumber,
                                onValueChange = viewModel::updateWhatsappNumber,
                                label = "WhatsApp de Negocio",
                                placeholder = "593991234567",
                                error = uiState.fieldErrors["whatsapp_number"],
                                isRequired = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // SECCIÓN COMISIÓN
                        FormSectionCard(title = "Comisión", icon = Icons.Default.Percent) {
                            CompanyTextFieldPremium(
                                value = uiState.commissionPercentage,
                                onValueChange = viewModel::updateCommissionPercentage,
                                label = "Porcentaje de Comisión",
                                placeholder = "10",
                                error = uiState.fieldErrors["commission_percentage"],
                                isRequired = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                                suffix = { Text("%", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary) }
                            )
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        Button(
                            onClick = viewModel::updateCompany,
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(Icons.Default.Save, null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("GUARDAR CAMBIOS", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                        }

                        Spacer(modifier = Modifier.height(48.dp))
                        Spacer(modifier = Modifier.navigationBarsPadding())
                    }
                }
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

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.AddAPhoto, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "Logo de tu Empresa", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                    .clickable(enabled = !isUploading && !isDeleting) { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (isUploading || isDeleting) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                } else if (hasLogo && !logoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(logoUrl).crossfade(true).build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Business, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = { launcher.launch("image/*") }, enabled = !isUploading && !isDeleting) {
                    Text(if (hasLogo) "Cambiar Logo" else "Subir Logo", fontWeight = FontWeight.Bold)
                }
                if (hasLogo) {
                    TextButton(onClick = onDeleteLogo, enabled = !isUploading && !isDeleting, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Text("Eliminar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
