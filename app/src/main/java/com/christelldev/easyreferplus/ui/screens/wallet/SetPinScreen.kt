package com.christelldev.easyreferplus.ui.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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

    // Estado local para manejar los pasos del wizard
    var currentStep by remember { mutableIntStateOf(1) }
    var tempPin1 by remember { mutableStateOf("") } // Para crear PIN o cambiar PIN (nuevo)
    var tempPin2 by remember { mutableStateOf("") } // Para confirmar
    var currentPinInput by remember { mutableStateOf("") } // Input actual siendo escrito
    var localError by remember { mutableStateOf<String?>(null) }

    // Limpiar errores al cambiar de paso
    LaunchedEffect(currentStep) {
        viewModel.clearError()
        localError = null
    }

    // Manejar éxito
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage?.contains("PIN") == true) {
            onSuccess()
        }
    }

    val title = if (isChangingPin) "Cambiar PIN" else "Configurar PIN"
    val primaryColor = Color(0xFF2196F3)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Lógica de Contenido según Modo y Paso
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // Mostrar error del ViewModel si existe
                uiState.pinError?.let { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                // Mostrar error local
                localError?.let { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (!isChangingPin) {
                    // --- MODO CREACIÓN DE PIN ---
                    if (currentStep == 1) {
                        IconSetup()
                        Text("Crea tu PIN de seguridad", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "Tu PIN de 6 dígitos protege tus transferencias. No uses tu contraseña de acceso.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Ingresa tu nuevo PIN", style = MaterialTheme.typography.labelLarge)
                    } else {
                        IconSetup()
                        Text("Confirma tu PIN", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Vuelve a ingresar tu PIN", style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    // --- MODO CAMBIO DE PIN ---
                    when (currentStep) {
                        1 -> {
                            Text("Verificación de seguridad", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Ingresa tu PIN actual", style = MaterialTheme.typography.labelLarge)
                        }
                        2 -> {
                            Text("Nuevo PIN", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Ingresa tu nuevo PIN", style = MaterialTheme.typography.labelLarge)
                        }
                        3 -> {
                            Text("Confirmar nuevo PIN", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Vuelve a ingresar tu nuevo PIN", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                PinInputDisplay(pin = currentPinInput)
            }

            // Teclado Numérico
            PinKeyboard(
                onDigit = { digit ->
                    if (currentPinInput.length < 6) {
                        currentPinInput += digit
                        localError = null

                        // Si se completaron 6 dígitos
                        if (currentPinInput.length == 6) {
                            // Pequeño delay para que se vea el último círculo lleno
                            // O simplemente procesar inmediatamente
                            when {
                                // CREAR PIN
                                !isChangingPin && currentStep == 1 -> {
                                    tempPin1 = currentPinInput
                                    currentPinInput = ""
                                    currentStep = 2
                                }
                                !isChangingPin && currentStep == 2 -> {
                                    tempPin2 = currentPinInput
                                    if (tempPin1 == tempPin2) {
                                        viewModel.setPin(tempPin1)
                                    } else {
                                        localError = "Los PINs no coinciden. Intenta de nuevo."
                                        currentStep = 1
                                        tempPin1 = ""
                                        tempPin2 = ""
                                        currentPinInput = ""
                                    }
                                }
                                // CAMBIAR PIN
                                isChangingPin && currentStep == 1 -> {
                                    // Validar contra viewModel? No tenemos función validarPIN especifica, 
                                    // asumimos que el botón de confirmar llama a changePin que validará internamente o nos confiamos.
                                    // Para este UI, guardamos el PIN actual y pedimos el nuevo.
                                    tempPin1 = currentPinInput // Current PIN
                                    currentPinInput = ""
                                    currentStep = 2
                                }
                                isChangingPin && currentStep == 2 -> {
                                    tempPin2 = currentPinInput // New PIN
                                    currentPinInput = ""
                                    currentStep = 3
                                }
                                isChangingPin && currentStep == 3 -> {
                                    val confirmPin = currentPinInput
                                    if (tempPin2 == confirmPin) {
                                        viewModel.changePin(tempPin1, tempPin2)
                                    } else {
                                        localError = "La confirmación no coincide."
                                        currentStep = 2
                                        tempPin2 = ""
                                        currentPinInput = ""
                                    }
                                }
                            }
                        }
                    }
                },
                onDelete = {
                    if (currentPinInput.isNotEmpty()) {
                        currentPinInput = currentPinInput.dropLast(1)
                    }
                }
            )
        }
    }
}

@Composable
private fun IconSetup() {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(Color(0xFFE3F2FD)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = Color(0xFF2196F3),
            modifier = Modifier.size(40.dp)
        )
    }
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
fun PinInputDisplay(pin: String, maxLength: Int = 6) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(maxLength) { index ->
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        if (index < pin.length) Color(0xFF2196F3)
                        else Color.Gray.copy(alpha = 0.3f)
                    )
            )
        }
    }
}

@Composable
fun PinKeyboard(
    onDigit: (String) -> Unit,
    onDelete: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "DEL")
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { key ->
                    if (key.isEmpty()) {
                        Spacer(modifier = Modifier.size(64.dp))
                    } else if (key == "DEL") {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF3F4F6))
                                .clickable { onDelete() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Borrar",
                                tint = Color(0xFF374151),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF3F4F6))
                                .clickable { onDigit(key) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                        }
                    }
                }
            }
        }
    }
}
