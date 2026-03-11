package com.christelldev.easyreferplus.ui.screens.profile

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.ui.theme.AppBlue
import com.christelldev.easyreferplus.ui.viewmodel.ProfileViewModel

// Constantes de diseño elegante
private val CARD_CORNER_RADIUS = 20.dp
private val CARD_ELEVATION = 8.dp
private val CARD_MARGIN_HORIZONTAL = 16.dp
private val GradientPrimary = listOf(Color(0xFF03A9F4), Color(0xFF2196F3))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToSessions: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var isEditing by remember { mutableStateOf(false) }
    var showSelfieDialog by remember { mutableStateOf(false) }

    // Launcher para seleccionar imagen de la galería
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.uploadSelfieFromUri(context, it)
        }
    }

    // Variable para almacenar el URI de la foto mientras se solicita permiso
    var pendingPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }

    // Launcher para tomar foto con la cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        // Cuando la cámara toma la foto exitosamente, procesar el URI
        if (success) {
            pendingPhotoUri?.let { uri ->
                viewModel.setPendingSelfieUri(uri.toString())
            }
        }
        pendingPhotoUri = null
    }

    // Launcher para solicitar permiso de cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingPhotoUri?.let { uri ->
                cameraLauncher.launch(uri)
            }
        } else {
            Toast.makeText(context, "Se requiere permiso de cámara para tomar fotos", Toast.LENGTH_LONG).show()
        }
        // No limpiar pendingPhotoUri aquí, se limpia después de que la cámara tome la foto
    }

    // Diálogo para editar selfie
    if (showSelfieDialog) {
        SelfieEditDialog(
            currentSelfieUrl = uiState.selfieUrl,
            isUploading = uiState.isUploadingSelfie,
            isDeleting = uiState.isDeletingSelfie,
            onSelectFromGallery = {
                showSelfieDialog = false
                imagePickerLauncher.launch("image/*")
            },
            onTakePhoto = {
                showSelfieDialog = false
                // Crear archivo regular para la foto (no temporal para que persista)
                val timestamp = System.currentTimeMillis()
                val photoFile = java.io.File(context.cacheDir, "selfie_$timestamp.jpg")
                val photoUri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )
                // Solicitar permiso de cámara antes de tomar la foto
                pendingPhotoUri = photoUri
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onDeleteSelfie = {
                viewModel.deleteSelfie()
                showSelfieDialog = false
            },
            onDismiss = { showSelfieDialog = false }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    // Procesar pending selfie URI (cuando se toma foto con cámara)
    LaunchedEffect(uiState.pendingSelfieUri) {
        uiState.pendingSelfieUri?.let { uriString ->
            if (!uiState.isUploadingSelfie) {
                // Pequeño delay para asegurar que el archivo se haya guardado completamente
                kotlinx.coroutines.delay(1000)
                try {
                    val uri = android.net.Uri.parse(uriString)
                    viewModel.uploadSelfieFromUri(context, uri)
                    // Limpiar el URI después de procesarlo
                    viewModel.clearPendingSelfieUri()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al procesar la foto: ${e.message}", Toast.LENGTH_LONG).show()
                    viewModel.clearPendingSelfieUri()
                }
            }
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.isSuccess) {
            val message = if (uiState.isPhoneVerificationPending) {
                "Se envió un código de verificación al ${uiState.pendingPhone}"
            } else {
                uiState.successMessage ?: "Perfil actualizado"
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            isEditing = false
            viewModel.clearSuccess()
        }
    }

    // Diálogo de cambio de teléfono
    if (uiState.isChangingPhone) {
        ChangePhoneDialog(
            phone = uiState.phone,
            newPhone = uiState.newPhone,
            onNewPhoneChange = viewModel::updateNewPhone,
            verificationCode = uiState.verificationCode,
            onCodeChange = viewModel::updateVerificationCode,
            codeSent = uiState.codeSent,
            isSendingCode = uiState.isSendingCode,
            isVerifyingCode = uiState.isVerifyingCode,
            resendTimer = uiState.resendTimer,
            onSendCode = viewModel::sendVerificationCode,
            onResendCode = viewModel::resendCode,
            onConfirm = viewModel::confirmPhoneChange,
            onDismiss = viewModel::cancelChangePhone,
            errorMessage = uiState.errorMessage
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.profile),
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
                actions = {
                    IconButton(onClick = { isEditing = !isEditing }) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.CheckCircle else Icons.Default.Edit,
                            contentDescription = "Editar",
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header con avatar
                ProfileHeader(
                    nombres = uiState.nombres,
                    apellidos = uiState.apellidos,
                    isVerified = uiState.isVerified,
                    selfieUrl = uiState.selfieUrl,
                    onEditSelfie = { showSelfieDialog = true },
                    isUploading = uiState.isUploadingSelfie,
                    isDeleting = uiState.isDeletingSelfie
                )

                // Información personal
                if (isEditing) {
                    EditProfileSection(
                        nombres = uiState.nombres,
                        onNombresChange = viewModel::updateNombres,
                        apellidos = uiState.apellidos,
                        onApellidosChange = viewModel::updateApellidos,
                        email = uiState.email,
                        onEmailChange = viewModel::updateEmail,
                        phone = uiState.phone,
                        onPhoneChange = viewModel::updatePhone,
                        cedulaRuc = uiState.cedulaRuc,
                        onCedulaChange = viewModel::updateCedulaRuc,
                        isRuc = uiState.isRuc
                    )

                    Button(
                        onClick = viewModel::saveProfile,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.save_changes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // Vista de perfil
                    ProfileInfoSection(
                        icon = Icons.Default.Person,
                        title = "Información Personal",
                        items = listOf(
                            ProfileItem("Nombres", uiState.nombres),
                            ProfileItem("Apellidos", uiState.apellidos),
                            ProfileItem(if (uiState.isRuc) "RUC" else "Cédula", uiState.cedulaRuc)
                        )
                    )

                    ProfileInfoSection(
                        icon = Icons.Default.Email,
                        title = "Correo Electrónico",
                        items = listOf(
                            ProfileItem("Email", uiState.email)
                        )
                    )

                    if (!uiState.phone.isNullOrBlank()) {
                        PhoneSection(
                            phone = uiState.phone,
                            isVerified = uiState.phoneVerified,
                            onChangePhone = { viewModel.startChangePhone() }
                        )
                    }

                    if (uiState.hasCompany && !uiState.empresaNombre.isNullOrBlank()) {
                        ProfileInfoSection(
                            icon = Icons.Default.Business,
                            title = "Empresa",
                            items = listOf(
                                ProfileItem("Empresa", uiState.empresaNombre!!)
                            )
                        )
                    }

                    if (!uiState.referralCode.isNullOrBlank()) {
                        ProfileInfoSection(
                            icon = Icons.Default.Badge,
                            title = "Código de Referido",
                            items = listOf(
                                ProfileItem("Tu código", uiState.referralCode!!)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón de gestión de sesiones
                TextButton(
                    onClick = onNavigateToSessions,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = AppBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.session_management),
                        color = AppBlue
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón de cerrar sesión
                TextButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.logout),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ProfileHeader(
    nombres: String,
    apellidos: String,
    isVerified: Boolean,
    selfieUrl: String?,
    onEditSelfie: () -> Unit,
    isUploading: Boolean,
    isDeleting: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CARD_MARGIN_HORIZONTAL)
            .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(CARD_CORNER_RADIUS)),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.horizontalGradient(colors = GradientPrimary))
                .padding(24.dp)
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar con selfie o iniciales
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .clickable(enabled = !isUploading && !isDeleting) { onEditSelfie() },
                contentAlignment = Alignment.Center
            ) {
                if (selfieUrl != null) {
                    // Agregar timestamp para evitar cache
                    val timestamp = System.currentTimeMillis()
                    val selfieUrlWithTimestamp = if (selfieUrl.contains("?")) {
                        "$selfieUrl&t=$timestamp"
                    } else {
                        "$selfieUrl?t=$timestamp"
                    }

                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(selfieUrlWithTimestamp)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${nombres.firstOrNull() ?: ""}${apellidos.firstOrNull() ?: ""}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.surface
                        )
                    }
                }

                // Overlay de edición
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = if (isUploading || isDeleting) 0.5f else 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.surface,
                            strokeWidth = 3.dp
                        )
                    } else if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.surface,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Cambiar foto",
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nombre completo
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$nombres $apellidos",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                if (isVerified) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verificado",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (isVerified) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Usuario Verificado",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
            }
        }
        }
    }
}

@Composable
fun ProfileInfoSection(
    icon: ImageVector,
    title: String,
    items: List<ProfileItem>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CARD_MARGIN_HORIZONTAL)
            .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(CARD_CORNER_RADIUS)),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(brush = Brush.linearGradient(colors = GradientPrimary)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppBlue
                )
            }

            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = item.value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

data class ProfileItem(
    val label: String,
    val value: String
)

@Composable
fun PhoneSection(
    phone: String,
    isVerified: Boolean,
    onChangePhone: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CARD_MARGIN_HORIZONTAL)
            .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(CARD_CORNER_RADIUS)),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(brush = Brush.linearGradient(colors = GradientPrimary)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Teléfono",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppBlue
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = phone,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isVerified) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verificado",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Verificado",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF4CAF50)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "No verificado",
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "No verificado",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFF9800)
                            )
                        }
                    }
                }

                Button(
                    onClick = onChangePhone,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppBlue
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isVerified) "Cambiar" else "Verificar",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
fun ChangePhoneDialog(
    phone: String,
    newPhone: String,
    onNewPhoneChange: (String) -> Unit,
    verificationCode: String,
    onCodeChange: (String) -> Unit,
    codeSent: Boolean,
    isSendingCode: Boolean,
    isVerifyingCode: Boolean,
    resendTimer: Int,
    onSendCode: () -> Unit,
    onResendCode: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    errorMessage: String?
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (codeSent) "Verificar Teléfono" else "Cambiar Teléfono",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppBlue
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!codeSent) {
                    Text(
                        text = "Ingresa tu nuevo número de teléfono para verificarlo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { value ->
                            // Solo permitir dígitos y máximo 10 caracteres
                            val filtered = value.filter { it.isDigit() }.take(10)
                            onNewPhoneChange(filtered)
                        },
                        label = { Text("Teléfono") },
                        placeholder = { Text("0987654321") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            cursorColor = AppBlue
                        ),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = AppBlue
                            )
                        }
                    )
                } else {
                    // Código de verificación enviado
                    Text(
                        text = "Se envió un código de verificación al número $newPhone",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = verificationCode,
                        onValueChange = { value ->
                            val filtered = value.filter { it.isDigit() }.take(6)
                            onCodeChange(filtered)
                        },
                        label = { Text("Código de verificación") },
                        placeholder = { Text("1234") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            cursorColor = AppBlue
                        ),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        )
                    )

                    // Timer para reenvío
                    if (resendTimer > 0) {
                        Text(
                            text = "Reenviar código en ${resendTimer}s",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        TextButton(
                            onClick = onResendCode,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                text = "Reenviar código",
                                color = AppBlue
                            )
                        }
                    }
                }

                // Mensaje de error
                if (!errorMessage.isNullOrBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFC62828),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Loading indicator
                if (isSendingCode || isVerifyingCode) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = AppBlue
                    )
                }
            }
        },
        confirmButton = {
            if (!codeSent) {
                Button(
                    onClick = onSendCode,
                    enabled = !isSendingCode && newPhone.length == 10,
                    colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSendingCode) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Enviar Código")
                    }
                }
            } else {
                Button(
                    onClick = onConfirm,
                    enabled = !isVerifyingCode && verificationCode.length >= 4,
                    colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isVerifyingCode) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Verificar")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun EditProfileSection(
    nombres: String,
    onNombresChange: (String) -> Unit,
    apellidos: String,
    onApellidosChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    cedulaRuc: String,
    onCedulaChange: (String) -> Unit,
    isRuc: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Editar Información",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppBlue
            )

            OutlinedTextField(
                value = nombres,
                onValueChange = onNombresChange,
                label = { Text("Nombres") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = AppBlue
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = apellidos,
                onValueChange = onApellidosChange,
                label = { Text("Apellidos") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = AppBlue
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = AppBlue
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = AppBlue
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = cedulaRuc,
                onValueChange = onCedulaChange,
                label = { Text(if (isRuc) "RUC" else "Cédula") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = AppBlue
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                )
            )
        }
    }
}

@Composable
fun SelfieEditDialog(
    currentSelfieUrl: String?,
    isUploading: Boolean,
    isDeleting: Boolean,
    onSelectFromGallery: () -> Unit,
    onTakePhoto: () -> Unit,
    onDeleteSelfie: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Foto de Perfil",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppBlue
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = AppBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Subiendo foto...")
                } else if (isDeleting) {
                    CircularProgressIndicator(color = AppBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Eliminando foto...")
                } else {
                    Text(
                        text = if (currentSelfieUrl != null)
                            "Cambia tu foto de perfil"
                        else
                            "Agrega una foto de perfil",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botón para tomar foto con cámara
                    Button(
                        onClick = onTakePhoto,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tomar Selfie")
                    }

                    // Botón para seleccionar de galería
                    OutlinedButton(
                        onClick = onSelectFromGallery,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Elegir de Galería")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            if (!isUploading && !isDeleting) {
                Row {
                    if (currentSelfieUrl != null) {
                        TextButton(
                            onClick = onDeleteSelfie,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Eliminar")
                        }
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}
