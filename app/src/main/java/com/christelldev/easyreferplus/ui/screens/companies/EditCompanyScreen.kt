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
import com.google.android.gms.location.LocationServices
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EditCompanyScreen(
    viewModel: CompanyViewModel,
    companyId: Int,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    // Estado de permisos de ubicación
    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()

    // Referencia al estado de la cámara del mapa
    val cameraPositionState = com.google.maps.android.compose.rememberCameraPositionState()

    // Centrar cámara en coordenadas cargadas
    LaunchedEffect(uiState.latitude, uiState.longitude) {
        if (uiState.latitude != 0.0) {
            cameraPositionState.position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
                LatLng(uiState.latitude, uiState.longitude), 17f
            )
        }
    }

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
                            // Mapa con PIN Central Fijo
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            ) {
                                val Ecuador = com.google.android.gms.maps.model.LatLng(-1.8312, -78.1834)
                                val cameraPositionState = com.google.maps.android.compose.rememberCameraPositionState {
                                    position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
                                        if (uiState.latitude != 0.0) com.google.android.gms.maps.model.LatLng(uiState.latitude, uiState.longitude) else Ecuador,
                                        if (uiState.latitude != 0.0) 17f else 6f
                                    )
                                }

                                // Capturar movimiento del mapa
                                LaunchedEffect(cameraPositionState.isMoving) {
                                    if (!cameraPositionState.isMoving) {
                                        viewModel.updateLocation(
                                            cameraPositionState.position.target.latitude,
                                            cameraPositionState.position.target.longitude
                                        )
                                    }
                                }

                                com.google.maps.android.compose.GoogleMap(
                                    modifier = Modifier.fillMaxSize(),
                                    cameraPositionState = cameraPositionState,
                                    uiSettings = com.google.maps.android.compose.MapUiSettings(
                                        zoomControlsEnabled = false,
                                        myLocationButtonEnabled = false
                                    )
                                )

                                // PIN Central Fijo (Icono de Tienda)
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        modifier = Modifier.size(44.dp).offset(y = (-22).dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape,
                                        shadowElevation = 8.dp
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Storefront,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }

                                // Botón Mi Ubicación
                                SmallFloatingActionButton(
                                    onClick = {
                                        if (locationPermissionsState.allPermissionsGranted) {
                                            try {
                                                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                                    location?.let {
                                                        scope.launch {
                                                            cameraPositionState.animate(
                                                                CameraUpdateFactory.newLatLngZoom(
                                                                    LatLng(it.latitude, it.longitude),
                                                                    17f
                                                                )
                                                            )
                                                            viewModel.updateLocation(it.latitude, it.longitude)
                                                        }
                                                    }
                                                }
                                            } catch (e: SecurityException) {
                                                Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            locationPermissionsState.launchMultiplePermissionRequest()
                                        }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(16.dp),
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.primary
                                ) {
                                    Icon(
                                        imageVector = if (locationPermissionsState.allPermissionsGranted) 
                                            Icons.Default.MyLocation else Icons.Default.LocationOff,
                                        contentDescription = "Mi ubicación"
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            CompanyTextFieldPremium(
                                value = uiState.address,
                                onValueChange = viewModel::updateAddress,
                                label = "Dirección Física (Referencia)",
                                placeholder = "Ej: Frente al parque central...",
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
