package com.christelldev.easyreferplus.ui.screens.profile

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.ui.theme.DesignConstants
import com.christelldev.easyreferplus.ui.viewmodel.ProfileViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToSessions: () -> Unit = {},
    onNavigateToSavedAddresses: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    var isEditing by remember { mutableStateOf(false) }
    var showSelfieDialog by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()

    // Launcher Galería
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.uploadSelfieFromUri(context, it) }
    }

    // Launcher Cámara
    var pendingPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) pendingPhotoUri?.let { viewModel.setPendingSelfieUri(it.toString()) }
        pendingPhotoUri = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) pendingPhotoUri?.let { cameraLauncher.launch(it) }
        else Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_LONG).show()
    }

    LaunchedEffect(Unit) { viewModel.loadProfile() }

    // Procesar foto tomada
    LaunchedEffect(uiState.pendingSelfieUri) {
        uiState.pendingSelfieUri?.let { uriString ->
            if (!uiState.isUploadingSelfie) {
                kotlinx.coroutines.delay(500)
                viewModel.uploadSelfieFromUri(context, android.net.Uri.parse(uriString))
                viewModel.clearPendingSelfieUri()
            }
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            isEditing = false
            viewModel.clearSuccess()
        }
    }

    // DIÁLOGOS
    if (showSelfieDialog) {
        SelfieEditDialogPremium(
            currentSelfieUrl = uiState.selfieUrl,
            isUploading = uiState.isUploadingSelfie,
            isDeleting = uiState.isDeletingSelfie,
            onSelectFromGallery = { showSelfieDialog = false; imagePickerLauncher.launch("image/*") },
            onTakePhoto = {
                showSelfieDialog = false
                val photoFile = java.io.File(context.cacheDir, "selfie_${System.currentTimeMillis()}.jpg")
                val photoUri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
                pendingPhotoUri = photoUri
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onDeleteSelfie = { viewModel.deleteSelfie(); showSelfieDialog = false },
            onDismiss = { showSelfieDialog = false }
        )
    }

    if (uiState.isChangingPhone) {
        ChangePhoneDialogPremium(
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
                            text = "Mi Perfil",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                null,
                                tint = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { isEditing = !isEditing }) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.CheckCircle else Icons.Default.Edit,
                                contentDescription = null,
                                tint = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White
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
                            .imePadding(),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // AVATAR SECTION
                        ProfileHeaderPremium(
                            nombres = uiState.nombres,
                            apellidos = uiState.apellidos,
                            isVerified = uiState.isVerified,
                            selfieUrl = uiState.selfieUrl,
                            onEditSelfie = { showSelfieDialog = true },
                            isUploading = uiState.isUploadingSelfie,
                            isDeleting = uiState.isDeletingSelfie
                        )

                        if (isEditing) {
                            EditProfileSectionPremium(
                                nombres = uiState.nombres,
                                onNombresChange = viewModel::updateNombres,
                                apellidos = uiState.apellidos,
                                onApellidosChange = viewModel::updateApellidos,
                                email = uiState.email,
                                onEmailChange = viewModel::updateEmail,
                                phone = uiState.phone ?: "",
                                onPhoneChange = viewModel::updatePhone,
                                cedulaRuc = uiState.cedulaRuc,
                                onCedulaChange = viewModel::updateCedulaRuc,
                                isRuc = uiState.isRuc,
                                onSave = viewModel::saveProfile
                            )
                        } else {
                            ProfileInfoSectionPremium(
                                icon = Icons.Default.Person,
                                title = "Identidad",
                                items = listOf(
                                    ProfileItem("Nombres", uiState.nombres),
                                    ProfileItem("Apellidos", uiState.apellidos),
                                    ProfileItem(if (uiState.isRuc) "RUC" else "Cédula", uiState.cedulaRuc)
                                )
                            )

                            ProfileInfoSectionPremium(
                                icon = Icons.Default.Email,
                                title = "Contacto",
                                items = listOf(ProfileItem("Email", uiState.email))
                            )

                            PhoneSectionPremium(
                                phone = uiState.phone ?: "No registrado",
                                isVerified = uiState.phoneVerified,
                                onChangePhone = { viewModel.startChangePhone() }
                            )

                            if (uiState.hasCompany) {
                                ProfileInfoSectionPremium(
                                    icon = Icons.Default.Business,
                                    title = "Empresa",
                                    items = listOf(ProfileItem("Negocio", uiState.empresaNombre ?: "Cargando..."))
                                )
                            }

                            // SAVED ADDRESSES
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToSavedAddresses() },
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Place, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        "Mis Direcciones",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
                                }
                            }

                            // SECURITY ACTIONS
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToSessions() },
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Security, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        "Gestión de Sesiones",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
                                }
                            }

                            TextButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Cerrar Sesión", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                        }
                        // Espacio para la barra de navegación
                        Spacer(modifier = Modifier.navigationBarsPadding().padding(bottom = 24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHeaderPremium(nombres: String, apellidos: String, isVerified: Boolean, selfieUrl: String?, onEditSelfie: () -> Unit, isUploading: Boolean, isDeleting: Boolean) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 12.dp,
            border = androidx.compose.foundation.BorderStroke(4.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (selfieUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(selfieUrl).crossfade(true).build(),
                        contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                    )
                } else {
                    Text("${nombres.take(1)}${apellidos.take(1)}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                }
                
                if (isUploading || isDeleting) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
                    }
                } else {
                    IconButton(
                        onClick = onEditSelfie,
                        modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp).size(32.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$nombres $apellidos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            if (isVerified) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Verified, null, tint = Color(0xFF10B981), modifier = Modifier.size(24.dp))
            }
        }
        if (isVerified) Text("Usuario Verificado", style = MaterialTheme.typography.labelMedium, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ProfileInfoSectionPremium(icon: ImageVector, title: String, items: List<ProfileItem>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(36.dp), shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
                    Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            items.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(item.label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(item.value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PhoneSectionPremium(phone: String, isVerified: Boolean, onChangePhone: () -> Unit) {
    val displayPhone = if (phone.startsWith("+593")) {
        "0" + phone.removePrefix("+593")
    } else {
        phone
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(36.dp), shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("Teléfono", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(displayPhone, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val color = if (isVerified) Color(0xFF10B981) else Color(0xFFFF9800)
                        Icon(if (isVerified) Icons.Default.CheckCircle else Icons.Default.Warning, null, tint = color, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isVerified) "Verificado" else "Sin verificar", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
                    }
                }
                Button(onClick = onChangePhone, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.primary)) {
                    Text(if (isVerified) "Cambiar" else "Verificar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ChangePhoneDialogPremium(
    phone: String?, newPhone: String, onNewPhoneChange: (String) -> Unit,
    verificationCode: String, onCodeChange: (String) -> Unit,
    codeSent: Boolean, isSendingCode: Boolean, isVerifyingCode: Boolean,
    resendTimer: Int, onSendCode: () -> Unit, onResendCode: () -> Unit,
    onConfirm: () -> Unit, onDismiss: () -> Unit, errorMessage: String?
) {
    val displayNewPhone = if (newPhone.startsWith("+593")) {
        "0" + newPhone.removePrefix("+593")
    } else {
        newPhone
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (codeSent) "Verificar Número" else "Cambiar Teléfono",
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (!codeSent) {
                    Text(
                        "Ingresa tu número en formato local (Ej: 0956674789). El sistema lo convertirá automáticamente.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = displayNewPhone,
                        onValueChange = { onNewPhoneChange(it.filter { c -> c.isDigit() }.take(10)) },
                        label = { Text("Número de teléfono") },
                        placeholder = { Text("Ej: 0956674789") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Send),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        leadingIcon = { Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.primary) },
                        singleLine = true
                    )
                } else {
                    Text(
                        "Hemos enviado un código de 6 dígitos al número:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        displayNewPhone,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    OutlinedTextField(
                        value = verificationCode,
                        onValueChange = { onCodeChange(it.filter { c -> c.isDigit() }.take(6)) },
                        label = { Text("Código OTP") },
                        placeholder = { Text("XXXXXX") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                    if (resendTimer > 0) {
                        Text(
                            "Reenviar código en ${resendTimer}s",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        TextButton(
                            onClick = onResendCode,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Reenviar nuevo código", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                if (!errorMessage.isNullOrBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                if (isSendingCode || isVerifyingCode) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = if (codeSent) onConfirm else onSendCode,
                enabled = if (codeSent) verificationCode.length == 6 else newPhone.length == 10,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text(if (codeSent) "Verificar" else "Enviar Código", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", fontWeight = FontWeight.SemiBold)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    )
}

@Composable
fun EditProfileSectionPremium(nombres: String, onNombresChange: (String) -> Unit, apellidos: String, onApellidosChange: (String) -> Unit, email: String, onEmailChange: (String) -> Unit, phone: String, onPhoneChange: (String) -> Unit, cedulaRuc: String, onCedulaChange: (String) -> Unit, isRuc: Boolean, onSave: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Editar Información", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(value = nombres, onValueChange = onNombresChange, label = { Text("Nombres") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words))
            OutlinedTextField(value = apellidos, onValueChange = onApellidosChange, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words))
            OutlinedTextField(value = email, onValueChange = onEmailChange, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            OutlinedTextField(value = cedulaRuc, onValueChange = onCedulaChange, label = { Text(if (isRuc) "RUC" else "Cédula") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Default.Save, null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Guardar Perfil", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun SelfieEditDialogPremium(currentSelfieUrl: String?, isUploading: Boolean, isDeleting: Boolean, onSelectFromGallery: () -> Unit, onTakePhoto: () -> Unit, onDeleteSelfie: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Foto de Perfil", fontWeight = FontWeight.Black) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (isUploading || isDeleting) CircularProgressIndicator()
                else {
                    Button(onClick = onTakePhoto, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.CameraAlt, null); Spacer(Modifier.width(8.dp)); Text("Tomar Selfie")
                    }
                    OutlinedButton(onClick = onSelectFromGallery, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.PhotoLibrary, null); Spacer(Modifier.width(8.dp)); Text("Elegir Galería")
                    }
                    if (currentSelfieUrl != null) {
                        TextButton(onClick = onDeleteSelfie, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                            Icon(Icons.Default.Delete, null); Spacer(Modifier.width(8.dp)); Text("Eliminar Actual")
                        }
                    }
                }
            }
        },
        confirmButton = {}, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        shape = RoundedCornerShape(28.dp)
    )
}
data class ProfileItem(val label: String, val value: String)
