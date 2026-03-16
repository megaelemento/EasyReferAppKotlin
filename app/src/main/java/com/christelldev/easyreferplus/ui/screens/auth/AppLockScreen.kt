package com.christelldev.easyreferplus.ui.screens.auth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
 * Pantalla de bloqueo de la aplicación — soporta modo claro y oscuro.
 *
 * Se muestra cuando:
 * - La app arranca con sesión guardada existente.
 * - La app regresa a primer plano tras el timeout de background.
 *
 * El usuario elige:
 * 1. Biométrico / PIN / Patrón del dispositivo → [BiometricHelper.showAppLockPrompt]
 * 2. Contraseña → navega al Login tradicional.
 */
@Composable
fun AppLockScreen(
    userName: String?,
    onBiometricSuccess: () -> Unit,
    onUsePassword: () -> Unit
) {
    val context  = LocalContext.current
    val activity = context as? FragmentActivity
    val isDark   = isSystemInDarkTheme()
    val canBiometric = remember { BiometricHelper.canAuthenticate(context) }

    // ── Paleta adaptativa ────────────────────────────────────────────────────
    val background: Brush
    val titleColor:    Color
    val subtitleColor: Color
    val welcomeColor:  Color
    val nameColor:     Color
    val bioBg:         Color
    val bioIcon:       Color
    val hintColor:     Color
    val dividerColor:  Color
    val pwdColor:      Color
    val errorColor:    Color

    if (isDark) {
        background    = Brush.verticalGradient(
            listOf(Color(0xFF060D1A), Color(0xFF0A1628), Color(0xFF0F2040))
        )
        titleColor    = Color.White
        subtitleColor = Color(0xFF94A3B8)
        welcomeColor  = Color.White.copy(alpha = 0.70f)
        nameColor     = Color.White
        bioBg         = Color.White.copy(alpha = 0.12f)
        bioIcon       = Color.White
        hintColor     = Color.White.copy(alpha = 0.65f)
        dividerColor  = Color.White.copy(alpha = 0.20f)
        pwdColor      = Color.White
        errorColor    = Color(0xFFFFCDD2)
    } else {
        background    = Brush.verticalGradient(
            listOf(Color(0xFFEEF4FF), Color(0xFFF5F8FF), Color(0xFFFFFFFF))
        )
        titleColor    = Color(0xFF0D47A1)
        subtitleColor = Color(0xFF6B7A99)
        welcomeColor  = Color(0xFF334155)
        nameColor     = Color(0xFF1A2340)
        bioBg         = Color(0xFFDCEEFB)
        bioIcon       = Color(0xFF1565C0)
        hintColor     = Color(0xFF64748B)
        dividerColor  = Color(0xFFCBD5E1)
        pwdColor      = Color(0xFF1565C0)
        errorColor    = Color(0xFFB91C1C)
    }

    // ── Estado del botón biométrico ──────────────────────────────────────────
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = tween(120),
        label = "biometric_scale"
    )

    // ── Mensaje de error ─────────────────────────────────────────────────────
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Auto-lanzar el prompt biométrico al entrar
    LaunchedEffect(Unit) {
        delay(350)
        if (canBiometric && activity != null) {
            BiometricHelper.showAppLockPrompt(
                activity   = activity,
                onSuccess  = onBiometricSuccess,
                onError    = { msg -> errorMsg = msg },
                onCancelled = { }
            )
        }
    }

    // Auto-limpiar error tras 3 s
    LaunchedEffect(errorMsg) {
        if (errorMsg != null) {
            delay(3_000)
            errorMsg = null
        }
    }

    // ── Layout ───────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
        ) {

            // ── Nombre de la app ─────────────────────────────────────────────
            Text(
                text       = "Enfoque Refer",
                fontSize   = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = titleColor,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text      = "Sistema de referidos",
                fontSize  = 13.sp,
                color     = subtitleColor,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(52.dp))

            // ── Saludo personalizado ─────────────────────────────────────────
            if (!userName.isNullOrBlank()) {
                Text(
                    text     = "Bienvenido de nuevo,",
                    fontSize = 15.sp,
                    color    = welcomeColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text       = userName,
                    fontSize   = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color      = nameColor
                )
                Spacer(modifier = Modifier.height(44.dp))
            }

            // ── Botón biométrico ─────────────────────────────────────────────
            if (canBiometric) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(bioBg)
                ) {
                    IconButton(
                        onClick = {
                            pressed = true
                            if (activity != null) {
                                BiometricHelper.showAppLockPrompt(
                                    activity    = activity,
                                    onSuccess   = { pressed = false; onBiometricSuccess() },
                                    onError     = { msg -> pressed = false; errorMsg = msg },
                                    onCancelled = { pressed = false }
                                )
                            }
                        },
                        modifier = Modifier.size(120.dp)
                    ) {
                        Icon(
                            imageVector     = Icons.Default.Fingerprint,
                            contentDescription = "Autenticar con biométrico",
                            tint            = bioIcon,
                            modifier        = Modifier.size(72.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text       = "Toca para ingresar\ncon huella, Face ID o PIN del teléfono",
                    fontSize   = 13.sp,
                    color      = hintColor,
                    textAlign  = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }

            // ── Error ────────────────────────────────────────────────────────
            if (errorMsg != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text      = errorMsg!!,
                    fontSize  = 13.sp,
                    color     = errorColor,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(if (canBiometric) 48.dp else 24.dp))

            // ── Divider ──────────────────────────────────────────────────────
            Text(
                text     = "─────   o   ─────",
                fontSize = 12.sp,
                color    = dividerColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Opción contraseña ────────────────────────────────────────────
            TextButton(onClick = onUsePassword) {
                Text(
                    text       = "Ingresar con usuario y contraseña",
                    color      = pwdColor,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
