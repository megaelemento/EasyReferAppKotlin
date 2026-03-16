package com.christelldev.easyreferplus.ui.screens.auth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

/**
 * Pantalla de bloqueo de la aplicación.
 *
 * Se muestra cuando:
 * - El app arranca y ya existe una sesión guardada.
 * - El app regresa a primer plano tras [AppLockManager.LOCK_TIMEOUT_MS] en background.
 *
 * El usuario elige:
 * 1. Biométrico / PIN / Patrón del dispositivo → autentica con [BiometricHelper.showAppLockPrompt]
 * 2. Contraseña → navega al Login tradicional.
 */
@Composable
fun AppLockScreen(
    userName: String?,
    onBiometricSuccess: () -> Unit,
    onUsePassword: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val canBiometric = remember { BiometricHelper.canAuthenticate(context) }

    // Estado visual del botón biométrico (pulso al presionar)
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = tween(100),
        label = "biometric_scale"
    )

    // Mensaje de error breve
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Auto-lanzar el prompt biométrico al entrar (UX fluida)
    LaunchedEffect(Unit) {
        delay(350) // pequeña espera para que la pantalla se renderice
        if (canBiometric && activity != null) {
            BiometricHelper.showAppLockPrompt(
                activity = activity,
                onSuccess = onBiometricSuccess,
                onError = { msg -> errorMsg = msg },
                onCancelled = { /* usuario canceló — deja que elija manualmente */ }
            )
        }
    }

    // Limpia el error tras 3 segundos
    LaunchedEffect(errorMsg) {
        if (errorMsg != null) {
            delay(3_000)
            errorMsg = null
        }
    }

    // ── Fondo gradiente azul EasyRefer ──────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0D47A1), Color(0xFF1565C0), Color(0xFF1976D2))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
        ) {

            // ── Logo / nombre de la app ──────────────────────────────────────
            Text(
                text = "EasyRefer+",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Sistema de referidos",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // ── Saludo personalizado ─────────────────────────────────────────
            if (!userName.isNullOrBlank()) {
                Text(
                    text = "Bienvenido de nuevo,",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = userName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(40.dp))
            }

            // ── Botón biométrico ─────────────────────────────────────────────
            if (canBiometric) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    IconButton(
                        onClick = {
                            pressed = true
                            if (activity != null) {
                                BiometricHelper.showAppLockPrompt(
                                    activity = activity,
                                    onSuccess = {
                                        pressed = false
                                        onBiometricSuccess()
                                    },
                                    onError = { msg ->
                                        pressed = false
                                        errorMsg = msg
                                    },
                                    onCancelled = { pressed = false }
                                )
                            }
                        },
                        modifier = Modifier.size(120.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Autenticar con biométrico",
                            tint = Color.White,
                            modifier = Modifier.size(72.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Toca para ingresar\ncon huella, Face ID o PIN del teléfono",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }

            // ── Mensaje de error ─────────────────────────────────────────────
            if (errorMsg != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMsg!!,
                    fontSize = 13.sp,
                    color = Color(0xFFFFCDD2),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(if (canBiometric) 48.dp else 24.dp))

            // ── Divider visual ───────────────────────────────────────────────
            Text(
                text = "─────   o   ─────",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.35f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Opción contraseña ────────────────────────────────────────────
            TextButton(onClick = onUsePassword) {
                Text(
                    text = "Ingresar con usuario y contraseña",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
