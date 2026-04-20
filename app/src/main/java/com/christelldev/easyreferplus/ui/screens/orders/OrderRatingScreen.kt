package com.christelldev.easyreferplus.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.christelldev.easyreferplus.ui.viewmodel.OrderRatingViewModel

private val tagLabels = mapOf(
    "rapido" to "Rápido",
    "amable" to "Amable",
    "profesional" to "Profesional",
    "puntual" to "Puntual",
    "lento" to "Lento",
    "descortes" to "Descortés",
    "buena_comida" to "Buena comida",
    "bien_empacado" to "Bien empacado",
    "pedido_correcto" to "Pedido correcto",
    "frio" to "Frío",
    "incompleto" to "Incompleto",
    "mal_empacado" to "Mal empacado",
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OrderRatingScreen(
    viewModel: OrderRatingViewModel,
    orderId: Int,
    deliveryFee: Double = 0.0,
    driverName: String? = null,
    driverSelfieUrl: String? = null,
    companyName: String? = null,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(orderId) {
        viewModel.loadData(orderId, deliveryFee, driverName, driverSelfieUrl, companyName)
    }

    LaunchedEffect(state.saved) {
        if (state.saved) onNavigateBack()
    }

    val isDark = isSystemInDarkTheme()
    val contentTint = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
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
                // TopAppBar manual con padding de status bar
                TopAppBar(
                    title = { Text("Calificar pedido", fontWeight = FontWeight.Bold, color = contentTint) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = contentTint)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    windowInsets = WindowInsets.statusBars
                )

                if (state.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Header
                        Text(
                            "¿Cómo estuvo tu pedido #$orderId?",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // ─── Driver Rating Section ──────────────────────────
                        if (state.driverName != null) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                tonalElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            Modifier.size(44.dp).clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (state.driverSelfieUrl != null) {
                                                AsyncImage(
                                                    model = state.driverSelfieUrl,
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                                )
                                            } else {
                                                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text("Repartidor", style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(state.driverName!!, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Stars
                                    StarRating(
                                        rating = state.driverRating,
                                        onRatingChanged = { viewModel.setDriverRating(it) }
                                    )

                                    // Tags
                                    if (state.driverTagOptions.isNotEmpty()) {
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            state.driverTagOptions.forEach { tag ->
                                                val selected = tag in state.selectedDriverTags
                                                FilterChip(
                                                    selected = selected,
                                                    onClick = { viewModel.toggleDriverTag(tag) },
                                                    label = { Text(tagLabels[tag] ?: tag, fontSize = 12.sp) },
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ─── Company Rating Section ─────────────────────────
                        if (state.companyName != null) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                tonalElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Store, null,
                                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text("Establecimiento", style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(state.companyName!!, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    StarRating(
                                        rating = state.companyRating,
                                        onRatingChanged = { viewModel.setCompanyRating(it) }
                                    )

                                    if (state.companyTagOptions.isNotEmpty()) {
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            state.companyTagOptions.forEach { tag ->
                                                val selected = tag in state.selectedCompanyTags
                                                FilterChip(
                                                    selected = selected,
                                                    onClick = { viewModel.toggleCompanyTag(tag) },
                                                    label = { Text(tagLabels[tag] ?: tag, fontSize = 12.sp) },
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ─── Comment ────────────────────────────────────────
                        OutlinedTextField(
                            value = state.comment,
                            onValueChange = { viewModel.setComment(it) },
                            label = { Text("Comentario (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // ─── Tip Section ────────────────────────────────────
                        if (state.driverName != null) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF1565C0).copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        "¿Quieres dejar propina a ${state.driverName}?",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    // Suggested percentages
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        state.tipSuggestedPercentages.forEach { pct ->
                                            val amount = (state.deliveryFee * pct / 100.0)
                                            val isSelected = state.tipAmount > 0 && kotlin.math.abs(state.tipAmount - amount) < 0.01
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    viewModel.setTipAmount(if (isSelected) 0.0 else amount)
                                                },
                                                label = {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text("$pct%", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                        Text("$${String.format("%.2f", amount)}", fontSize = 11.sp)
                                                    }
                                                },
                                                modifier = Modifier.weight(1f),
                                            )
                                        }
                                        // Custom amount
                                        FilterChip(
                                            selected = state.tipAmount > 0 && state.tipSuggestedPercentages.none { pct ->
                                                kotlin.math.abs(state.tipAmount - state.deliveryFee * pct / 100.0) < 0.01
                                            },
                                            onClick = { /* handled by text field */ },
                                            label = { Text("Otro", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                            modifier = Modifier.weight(1f),
                                        )
                                    }

                                    // Custom amount input
                                    OutlinedTextField(
                                        value = if (state.tipAmount > 0) String.format("%.2f", state.tipAmount) else "",
                                        onValueChange = { text ->
                                            val amount = text.toDoubleOrNull()
                                            if (amount != null && amount >= 0) viewModel.setTipAmount(amount)
                                            else if (text.isEmpty()) viewModel.setTipAmount(0.0)
                                        },
                                        label = { Text("Monto propina") },
                                        prefix = { Text("$") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    if (state.tipMinutesRemaining != null) {
                                        Text(
                                            "Puedes modificar la propina por ${state.tipMinutesRemaining} min más",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else if (state.existingTip == null) {
                                        Text(
                                            "Tienes ${state.tipAdjustmentMinutes} min después de la entrega para ajustar",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // ─── Error ──────────────────────────────────────────
                        if (state.error != null) {
                            Text(state.error!!, color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall)
                        }

                        // ─── Submit Button ──────────────────────────────────
                        Button(
                            onClick = { viewModel.submit() },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            enabled = !state.isSaving && (state.driverRating > 0 || state.companyRating > 0),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Send, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Enviar calificación", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(Modifier.navigationBarsPadding().padding(bottom = 16.dp))
                    }
                }
            }
        }
    }
}

// ─── Star Rating Composable ─────────────────────────────────────────────────

@Composable
private fun StarRating(
    rating: Float,
    onRatingChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..5) {
            Icon(
                imageVector = when {
                    i <= rating -> Icons.Default.Star
                    i - 0.5f <= rating -> Icons.Default.StarHalf
                    else -> Icons.Default.StarBorder
                },
                contentDescription = "Estrella $i",
                tint = if (i <= rating) Color(0xFFFFC107) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onRatingChanged(i.toFloat()) }
            )
        }
        if (rating > 0) {
            Spacer(Modifier.width(12.dp))
            Text(
                String.format("%.0f", rating),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFFC107)
            )
        }
    }
}
