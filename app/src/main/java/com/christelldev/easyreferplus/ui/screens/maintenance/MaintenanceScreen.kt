package com.christelldev.easyreferplus.ui.screens.maintenance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.data.network.NetworkMonitor
import com.christelldev.easyreferplus.ui.theme.DesignConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MaintenanceScreen(
    onRetry: () -> Unit
) {
    val context = LocalContext.current
    var isChecking by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Reset cache first to force a fresh check
        NetworkMonitor.resetCache()
        isChecking = true
        
        // Usar launch dentro de LaunchedEffect que ya es un CoroutineScope
        launch {
            try {
                val isAvailable = NetworkMonitor.isServerAvailable()
                isChecking = false
                
                if (isAvailable) {
                    onRetry()
                }
            } catch (e: Exception) {
                isChecking = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DesignConstants.PrimaryColor,
                        DesignConstants.PrimaryDark,
                        Color(0xFF0D47A1)
                    )
                )
            )
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignConstants.CARD_MARGIN_HORIZONTAL),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = DesignConstants.CARD_ELEVATION, shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS)),
                shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        DesignConstants.WarningColor.copy(alpha = 0.2f),
                                        DesignConstants.WarningColor.copy(alpha = 0.1f)
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = DesignConstants.WarningColor
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Mantenimiento",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = DesignConstants.TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "El servidor está en mantenimiento.\nPor favor, intente más tarde.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            NetworkMonitor.resetCache()
                            isChecking = true
                        },
                        enabled = !isChecking,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(DesignConstants.BUTTON_CORNER_RADIUS),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DesignConstants.PrimaryColor,
                            disabledContainerColor = DesignConstants.PrimaryColor.copy(alpha = 0.5f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 2.dp)
                    ) {
                        if (isChecking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.surface,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Reintentar",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
