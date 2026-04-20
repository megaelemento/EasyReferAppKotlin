package com.christelldev.easyreferplus.ui.screens.sessions

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.data.model.SessionInfo
import com.christelldev.easyreferplus.ui.theme.DesignConstants
import com.christelldev.easyreferplus.ui.viewmodel.SessionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionManagementScreen(
    viewModel: SessionViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()
    val snackbarHostState = remember { SnackbarHostState() }
    var showRevokeAllDialog by remember { mutableStateOf(false) }
    var sessionToRevoke by remember { mutableStateOf<SessionInfo?>(null) }

    // Cargar sesiones
    LaunchedEffect(Unit) {
        viewModel.loadSessions()
    }

    // Handlers para mensajes
    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    // Diálogos Estilizados
    sessionToRevoke?.let { session ->
        ElegantRevokeDialog(
            deviceName = session.deviceInfo ?: "Dispositivo",
            isDark = isDark,
            onConfirm = {
                viewModel.invalidateSession(session.sessionId)
                sessionToRevoke = null
            },
            onDismiss = { sessionToRevoke = null }
        )
    }

    if (showRevokeAllDialog) {
        ElegantRevokeAllDialog(
            sessionCount = uiState.sessions.size,
            isDark = isDark,
            onConfirm = {
                viewModel.logoutAllExceptCurrent()
                showRevokeAllDialog = false
            },
            onDismiss = { showRevokeAllDialog = false }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Header con Gradiente que ocupa toda la parte superior incluyendo status bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                Color.Transparent
                            )
                        )
                    )
            )

            val contentColor = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White

            Column(modifier = Modifier.fillMaxSize()) {
                // TopAppBar manual con padding de status bar
                TopAppBar(
                    title = {
                        Text(
                            text = "Gestión de Sesiones",
                            fontWeight = FontWeight.ExtraBold,
                            color = contentColor
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                null,
                                tint = contentColor
                            )
                        }
                    },
                    actions = {
                        if (uiState.sessions.size > 1) {
                            IconButton(onClick = { showRevokeAllDialog = true }) {
                                Icon(
                                    Icons.Default.DeleteSweep,
                                    null,
                                    tint = contentColor
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    windowInsets = WindowInsets.statusBars
                )

                if (uiState.isLoading && uiState.sessions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (uiState.sessions.isEmpty()) {
                    EmptySessionsState(isDark = isDark, onRetry = { viewModel.loadSessions() })
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = DesignConstants.CARD_MARGIN_HORIZONTAL,
                            vertical = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header info flotante
                        item {
                            SessionHeaderCard(
                                totalSessions = uiState.sessions.size,
                                maxSessions = uiState.maxSessions,
                                isDark = isDark
                            )
                        }

                        item {
                            Text(
                                text = "Dispositivos Conectados",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }

                        // Listado de sesiones
                        items(uiState.sessions) { session ->
                            ElegantSessionCard(
                                session = session,
                                isDark = isDark,
                                onRevoke = { sessionToRevoke = session },
                                isRevoking = uiState.isRevoking
                            )
                        }
                        
                        // Espacio para la barra de navegación al final de la lista
                        item { Spacer(modifier = Modifier.navigationBarsPadding().height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionHeaderCard(totalSessions: Int, maxSessions: Int, isDark: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS),
        color = if (isDark) DesignConstants.SurfaceCardDark else Color.White,
        tonalElevation = 8.dp,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = DesignConstants.PrimaryColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = DesignConstants.PrimaryColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = "Sesiones Activas",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isDark) DesignConstants.TextSecondaryDark else DesignConstants.TextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "$totalSessions / $maxSessions",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = if (isDark) Color.White else DesignConstants.TextPrimary
                )
                Text(
                    text = "Dispositivos autorizados",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) DesignConstants.TextSecondaryDark else DesignConstants.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun ElegantSessionCard(
    session: SessionInfo,
    isDark: Boolean,
    onRevoke: () -> Unit,
    isRevoking: Boolean
) {
    val isCurrent = session.isCurrent
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS),
        color = if (isDark) DesignConstants.SurfaceCardDark else Color.White,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de dispositivo
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = if (isCurrent) DesignConstants.PrimaryColor.copy(alpha = 0.1f) 
                        else if (isDark) Color.White.copy(alpha = 0.05f) 
                        else Color.Black.copy(alpha = 0.05f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getDeviceIcon(session.deviceInfo),
                        contentDescription = null,
                        tint = if (isCurrent) DesignConstants.PrimaryColor 
                               else if (isDark) DesignConstants.TextSecondaryDark 
                               else DesignConstants.TextSecondary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info de sesión
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = session.deviceInfo?.ifBlank { "Dispositivo Desconocido" } ?: "Dispositivo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) DesignConstants.TextPrimaryDark else DesignConstants.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isCurrent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = DesignConstants.SuccessColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "ESTA SESIÓN",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = DesignConstants.SuccessColor,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                Text(
                    text = session.ipAddress ?: "IP no disponible",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) DesignConstants.TextSecondaryDark else DesignConstants.TextSecondary
                )

                val dateStr = remember(session.lastUsed) {
                    try {
                        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.forLanguageTag("es-EC"))
                        sdf.format(Date(session.lastUsed * 1000))
                    } catch (e: Exception) { "Recientemente" }
                }

                Text(
                    text = "Última actividad: $dateStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) DesignConstants.TextSecondaryDark.copy(alpha = 0.6f) 
                            else DesignConstants.TextSecondary.copy(alpha = 0.6f)
                )
            }

            if (!isCurrent) {
                IconButton(
                    onClick = onRevoke,
                    enabled = !isRevoking
                ) {
                    if (isRevoking) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Cerrar sesión",
                            tint = DesignConstants.ErrorColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySessionsState(isDark: Boolean, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = if (isDark) DesignConstants.SurfaceCardDark else Color.White,
            tonalElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = DesignConstants.PrimaryColor.copy(alpha = 0.3f)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Sin sesiones activas",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = if (isDark) Color.White else DesignConstants.TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No se encontraron sesiones para tu cuenta.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isDark) DesignConstants.TextSecondaryDark else DesignConstants.TextSecondary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(DesignConstants.BUTTON_CORNER_RADIUS),
            colors = ButtonDefaults.buttonColors(containerColor = DesignConstants.PrimaryColor)
        ) {
            Text("Reintentar", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ElegantRevokeDialog(
    deviceName: String,
    isDark: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Cerrar Sesión",
                fontWeight = FontWeight.Black,
                color = if (isDark) Color.White else DesignConstants.TextPrimary
            )
        },
        text = {
            Text(
                "¿Estás seguro de que deseas cerrar la sesión en $deviceName? Tendrás que volver a iniciar sesión en ese dispositivo.",
                color = if (isDark) DesignConstants.TextSecondaryDark else DesignConstants.TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = DesignConstants.ErrorColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = if (isDark) Color.White.copy(alpha = 0.6f) else DesignConstants.TextSecondary)
            }
        },
        containerColor = if (isDark) DesignConstants.SurfaceCardDark else Color.White,
        shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS)
    )
}

@Composable
private fun ElegantRevokeAllDialog(
    sessionCount: Int,
    isDark: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Cerrar Todas las Sesiones",
                fontWeight = FontWeight.Black,
                color = if (isDark) Color.White else DesignConstants.TextPrimary
            )
        },
        text = {
            Text(
                "Se cerrarán las $sessionCount sesiones activas en otros dispositivos. Esta sesión actual permanecerá abierta.",
                color = if (isDark) DesignConstants.TextSecondaryDark else DesignConstants.TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = DesignConstants.ErrorColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cerrar Todas", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = if (isDark) Color.White.copy(alpha = 0.6f) else DesignConstants.TextSecondary)
            }
        },
        containerColor = if (isDark) DesignConstants.SurfaceCardDark else Color.White,
        shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS)
    )
}

private fun getDeviceIcon(deviceInfo: String?): ImageVector {
    val info = deviceInfo?.lowercase() ?: ""
    return when {
        info.contains("android") || info.contains("phone") || info.contains("mobile") -> Icons.Default.PhoneAndroid
        info.contains("iphone") || info.contains("ios") -> Icons.Default.PhoneIphone
        info.contains("tablet") || info.contains("ipad") -> Icons.Default.TabletAndroid
        else -> Icons.Default.Computer
    }
}
