package com.christelldev.easyreferplus.ui.screens.auth

import android.view.WindowManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.christelldev.easyreferplus.util.BiometricHelper
import kotlinx.coroutines.delay

@Composable
fun AppLockScreen(
    userName: String?,
    onBiometricSuccess: () -> Unit,
    onPinClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? androidx.fragment.app.FragmentActivity
    val isDark = isSystemInDarkTheme()
    
    var canBiometric by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var pressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        canBiometric = BiometricHelper.canAuthenticate(context)
        if (canBiometric && activity != null) {
            BiometricHelper.showAppLockPrompt(
                activity = activity,
                onSuccess = { onBiometricSuccess() },
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

    val contentColor = if (isDark) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Gradiente Superior Premium
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
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
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Título de Marca
            Text(
                text = "Enfoque Refer",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = contentColor,
                letterSpacing = (-1).sp
            )
            Text(
                text = "SISTEMA DE REFERIDOS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor.copy(alpha = 0.8f),
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(60.dp))

            if (!userName.isNullOrBlank()) {
                Surface(
                    color = contentColor.copy(alpha = 0.1f),
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
                            color = contentColor.copy(alpha = 0.7f)
                        )
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = contentColor
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
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.3f else 1f),
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Autenticación biométrica",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Toca para usar datos biométricos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            } else {
                // Fallback a PIN si no hay biometría
                Button(
                    onClick = onPinClick,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(0.7f).height(56.dp)
                ) {
                    Icon(Icons.Default.Lock, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ingresar con PIN", fontWeight = FontWeight.Bold)
                }
            }

            errorMsg?.let { msg ->
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}
