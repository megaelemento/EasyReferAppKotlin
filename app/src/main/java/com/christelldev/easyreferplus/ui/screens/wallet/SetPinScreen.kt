package com.christelldev.easyreferplus.ui.screens.wallet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.christelldev.easyreferplus.ui.theme.DesignConstants
import com.christelldev.easyreferplus.ui.viewmodel.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetPinScreen(
    viewModel: WalletViewModel = viewModel(),
    isChangingPin: Boolean = false,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()

    var currentStep by remember { mutableIntStateOf(1) }
    var tempPin1 by remember { mutableStateOf("") }
    var tempPin2 by remember { mutableStateOf("") }
    var currentPinInput by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentStep) {
        viewModel.clearError()
        localError = null
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage?.contains("PIN") == true) onSuccess()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Gradiente superior sutil
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp)
                    .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), Color.Transparent)))
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // Cabecera Premium
                TopAppBar(
                    title = {
                        Text(
                            text = if (isChangingPin) "Cambiar PIN" else "Seguridad PIN",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )

                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Icono Seguridad de Élite
                        Surface(
                            modifier = Modifier.size(90.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            tonalElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Security, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Títulos Dinámicos Premium
                        val stepTitle = when {
                            !isChangingPin && currentStep == 1 -> "Crea tu PIN"
                            !isChangingPin && currentStep == 2 -> "Confirma tu PIN"
                            isChangingPin && currentStep == 1 -> "Verifica tu Identidad"
                            isChangingPin && currentStep == 2 -> "Nuevo PIN"
                            else -> "Confirmar Nuevo PIN"
                        }
                        
                        val stepSubtitle = when {
                            !isChangingPin && currentStep == 1 -> "Tu PIN de 6 dígitos protege tus transferencias en Enfoque Refer."
                            !isChangingPin && currentStep == 2 -> "Vuelve a ingresar el código para asegurar que sea correcto."
                            isChangingPin && currentStep == 1 -> "Ingresa tu PIN actual para continuar."
                            isChangingPin && currentStep == 2 -> "Elige un código de 6 dígitos que sea fácil de recordar para ti."
                            else -> "Repite el nuevo PIN para finalizar el cambio."
                        }

                        Text(text = stepTitle, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stepSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(48.dp))
                        
                        // Indicadores de PIN Premium
                        PinInputDisplayPremium(pin = currentPinInput)
                        
                        // Errores con Estilo
                        val error = uiState.pinError ?: localError
                        AnimatedVisibility(visible = error != null, enter = fadeIn(), exit = fadeOut()) {
                            Surface(
                                modifier = Modifier.padding(top = 24.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Teclado Premium
                    PinKeyboardPremium(
                        onDigit = { digit ->
                            if (currentPinInput.length < 6) {
                                currentPinInput += digit
                                localError = null
                                if (currentPinInput.length == 6) {
                                    when {
                                        !isChangingPin && currentStep == 1 -> {
                                            tempPin1 = currentPinInput; currentPinInput = ""; currentStep = 2
                                        }
                                        !isChangingPin && currentStep == 2 -> {
                                            if (tempPin1 == currentPinInput) viewModel.setPin(tempPin1)
                                            else { localError = "Los PINs no coinciden"; currentStep = 1; tempPin1 = ""; currentPinInput = "" }
                                        }
                                        isChangingPin && currentStep == 1 -> {
                                            tempPin1 = currentPinInput; currentPinInput = ""; currentStep = 2
                                        }
                                        isChangingPin && currentStep == 2 -> {
                                            tempPin2 = currentPinInput; currentPinInput = ""; currentStep = 3
                                        }
                                        isChangingPin && currentStep == 3 -> {
                                            if (tempPin2 == currentPinInput) viewModel.changePin(tempPin1, tempPin2)
                                            else { localError = "La confirmación no coincide"; currentStep = 2; tempPin2 = ""; currentPinInput = "" }
                                        }
                                    }
                                }
                            }
                        },
                        onDelete = { if (currentPinInput.isNotEmpty()) currentPinInput = currentPinInput.dropLast(1) }
                    )
                }
            }
        }
    }
}

@Composable
fun PinInputDisplayPremium(pin: String, maxLength: Int = 6) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(maxLength) { index ->
            val isFilled = index < pin.length
            Surface(
                modifier = Modifier.size(20.dp),
                shape = CircleShape,
                color = if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = if (isFilled) 4.dp else 0.dp,
                border = if (isFilled) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {}
        }
    }
}

@Composable
fun PinKeyboardPremium(onDigit: (String) -> Unit, onDelete: () -> Unit) {
    val rows = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("", "0", "DEL"))
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                row.forEach { key ->
                    if (key.isEmpty()) {
                        Spacer(modifier = Modifier.size(72.dp))
                    } else {
                        Surface(
                            modifier = Modifier.size(72.dp).clickable { if (key == "DEL") onDelete() else onDigit(key) },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            shadowElevation = 1.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (key == "DEL") {
                                    Icon(Icons.AutoMirrored.Filled.Backspace, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                } else {
                                    Text(text = key, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
