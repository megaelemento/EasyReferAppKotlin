package com.christelldev.easyreferplus.ui.screens.delivery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.christelldev.easyreferplus.data.model.DeliveryOption
import com.christelldev.easyreferplus.ui.viewmodel.CheckoutFlowState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliverySelectionScreen(
    checkoutState: CheckoutFlowState,
    selectedDelivery: DeliveryOption?,
    dropoffAddress: String = "",
    onSelect: (DeliveryOption) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val contentColor = if (isDark) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Gradient header overlay que ocupa toda la parte superior incluyendo status bar
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
                    .align(Alignment.TopStart)
            )
            Column(modifier = Modifier.fillMaxSize()) {
                // TopAppBar manual con padding de status bar
                TopAppBar(
                    title = { Text("Elige empresa de entrega", color = contentColor) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = contentColor)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    windowInsets = WindowInsets.statusBars
                )

                if (dropoffAddress.isNotBlank()) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Place, null, modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(dropoffAddress.take(60), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (checkoutState) {
                        is CheckoutFlowState.LoadingOptions -> {
                            Column(modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Buscando empresas disponibles…",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        is CheckoutFlowState.OptionsLoaded -> {
                            val options = checkoutState.options
                            if (options.isEmpty()) {
                                Column(modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center) {
                                    Icon(Icons.Default.LocalShipping, null,
                                        modifier = Modifier.size(56.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Sin empresas disponibles en tu zona",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                val cheapestId = options.filter { it.available }
                                    .minByOrNull { it.deliveryFee }?.companyId
                                Column(modifier = Modifier.fillMaxSize()) {
                                    LazyColumn(
                                        modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                                        contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(options) { option ->
                                            DeliveryCompanyCard(
                                                option = option,
                                                selected = selectedDelivery?.companyId == option.companyId,
                                                isCheapest = option.companyId == cheapestId,
                                                onClick = { if (option.available) onSelect(option) }
                                            )
                                        }
                                    }
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        tonalElevation = 4.dp
                                    ) {
                                        Column {
                                            Button(
                                                onClick = onNext,
                                                enabled = selectedDelivery != null,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp)
                                                    .height(52.dp),
                                                shape = RoundedCornerShape(14.dp)
                                            ) {
                                                Text("Continuar", fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(Icons.Default.ArrowForward, null)
                                            }
                                            // Espacio para la barra de navegación
                                            Spacer(modifier = Modifier.navigationBarsPadding())
                                        }
                                    }
                                }
                            }
                        }
                        is CheckoutFlowState.Error -> {
                            Column(modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center) {
                                Icon(Icons.Default.ErrorOutline, null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(checkoutState.message, color = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = onBack) { Text("Volver") }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun DeliveryCompanyCard(
    option: DeliveryOption,
    selected: Boolean,
    isCheapest: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(enabled = option.available, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = when {
            !option.available -> MaterialTheme.colorScheme.surfaceVariant
            selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)
            else -> MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocalShipping, null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(option.companyName, style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (!option.available) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSurface)
                    if (isCheapest) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF4CAF50).copy(alpha = 0.15f)) {
                            Text("Más barato",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
                Text(
                    text = if (option.available)
                        "${option.vehiclesCount} vehículos · ${String.format("%.1f", option.distanceKm)} km"
                    else "Sin disponibilidad",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (!option.available) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("$${String.format("%.2f", option.deliveryFee)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = if (!option.available) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.primary)
            if (selected) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.CheckCircle, null,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
