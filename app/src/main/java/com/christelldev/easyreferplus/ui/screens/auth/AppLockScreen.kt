package com.christelldev.easyreferplus.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.christelldev.easyreferplus.util.BiometricHelper
import kotlinx.coroutines.delay

@Composable
fun AppLockScreen(
    userName: String?,
    onBiometricSuccess: () -> Unit,
    onUsePassword: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val isDark = isSystemInDarkTheme()
    val canBiometric = remember { BiometricHelper.canAuthenticate(context) }
    var pressed by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        delay(500)
        if (canBiometric && activity != null) {
            BiometricHelper.showAppLockPrompt(
                activity = activity,
                onSuccess = onBiometricSuccess,
                onError = { errorMsg = it },
                onCancelled = { }
            )
        }
    }

    LaunchedEffect(errorMsg) {
        if (errorMsg != null) {
            delay(3000)
            errorMsg = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Gradiente Superior Premium
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Título de Marca
            Text(
                text = "Enfoque Refer",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White,
                letterSpacing = (-1).sp
            )
            Text(
                text = "SISTEMA DE REFERIDOS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.8f),
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(60.dp))

            if (!userName.isNullOrBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(bottom = 40.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Bienvenido de nuevo,",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            if (canBiometric) {
                Surface(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(scale)
                        .clickable {
                            pressed = true
                            if (activity != null) {
                                BiometricHelper.showAppLockPrompt(
                                    activity = activity,
                                    onSuccess = { pressed = false; onBiometricSuccess() },
                                    onError = { msg -> pressed = false; errorMsg = msg },
                                    onCancelled = { pressed = false }
                                )
                            }
                        },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(70.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Toca para autenticar con\nbiometría o PIN del sistema",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            AnimatedVisibility(visible = errorMsg != null, enter = fadeIn(), exit = fadeOut()) {
                Surface(
                    modifier = Modifier.padding(top = 24.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = errorMsg ?: "",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))

            Surface(
                modifier = Modifier.fillMaxWidth().height(1.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            ) {}

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = onUsePassword,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Ingresar con contraseña tradicional",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
