package com.christelldev.easyreferplus.ui.screens.companies

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.christelldev.easyreferplus.data.network.AppConfig
import com.christelldev.easyreferplus.ui.viewmodel.SlugCheckResult
import com.christelldev.easyreferplus.ui.viewmodel.StoreViewModel

private val TEMPLATE_NAMES = listOf("Moderna", "Clásica", "Minimalista", "Colorida", "Premium")
private val TEMPLATE_ICONS = listOf(
    Icons.Default.AutoAwesome, Icons.Default.Star, Icons.Default.GridView,
    Icons.Default.Palette, Icons.Default.Diamond
)
private val FONTS = listOf("inter", "poppins", "playfair", "montserrat", "raleway")
private val FONT_LABELS = listOf("Inter", "Poppins", "Playfair", "Montserrat", "Raleway")

private val COLOR_PRESETS = listOf(
    "#6366f1", "#8b5cf6", "#ec4899", "#ef4444",
    "#f97316", "#eab308", "#22c55e", "#14b8a6",
    "#3b82f6", "#06b6d4", "#000000", "#374151"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreSetupScreen(
    viewModel: StoreViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()

    LaunchedEffect(Unit) { viewModel.loadStore() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Gradiente superior
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp).background(
                    Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), Color.Transparent))
                )
            )

            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Text("Mi Tienda Web", fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null,
                                tint = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (!uiState.isValidated) {
                    NotValidatedMessage()
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))

                        // ─── Estado de la tienda ─────────────────────────────
                        StoreStatusCard(
                            storeEnabled = uiState.storeEnabled,
                            storeUrl = uiState.storeUrl,
                            productsCount = uiState.productsCount,
                            isToggling = uiState.isToggling,
                            onToggle = { viewModel.toggleStore() },
                            onVisit = { url ->
                                val fullUrl = "${AppConfig.BASE_URL_CLEAN}$url"
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl)))
                            }
                        )

                        // ─── Nombre / URL ─────────────────────────────────────
                        SectionCard(title = "Nombre de tu tienda (URL)") {
                            Text(
                                "Elige un nombre único para tu URL pública. Solo letras minúsculas, números y guiones.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = uiState.storeSlug,
                                onValueChange = { viewModel.onSlugChange(it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Nombre de tienda") },
                                placeholder = { Text("ej: mi-tienda") },
                                prefix = { Text("empresas/", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                singleLine = true,
                                trailingIcon = {
                                    when (uiState.slugCheckResult) {
                                        SlugCheckResult.Checking -> CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                        SlugCheckResult.Available -> Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF22c55e))
                                        is SlugCheckResult.Taken -> Icon(Icons.Default.Cancel, null, tint = MaterialTheme.colorScheme.error)
                                        else -> {}
                                    }
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            when (val check = uiState.slugCheckResult) {
                                SlugCheckResult.Available -> Text("Disponible", color = Color(0xFF22c55e), style = MaterialTheme.typography.labelSmall)
                                is SlugCheckResult.Taken -> Text(check.reason, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                else -> {}
                            }
                        }

                        // ─── Plantilla ────────────────────────────────────────
                        SectionCard(title = "Diseño de la tienda") {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(5) { index ->
                                    val templateId = index + 1
                                    val selected = uiState.templateId == templateId
                                    TemplateCard(
                                        id = templateId,
                                        name = TEMPLATE_NAMES[index],
                                        icon = TEMPLATE_ICONS[index],
                                        selected = selected,
                                        onClick = { viewModel.onTemplateChange(templateId) }
                                    )
                                }
                            }
                        }

                        // ─── Colores ──────────────────────────────────────────
                        SectionCard(title = "Colores") {
                            ColorPickerRow(
                                label = "Color principal",
                                selectedColor = uiState.primaryColor,
                                onColorSelected = { viewModel.onPrimaryColorChange(it) }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            ColorPickerRow(
                                label = "Color secundario",
                                selectedColor = uiState.secondaryColor,
                                onColorSelected = { viewModel.onSecondaryColorChange(it) }
                            )
                        }

                        // ─── Fuente ───────────────────────────────────────────
                        SectionCard(title = "Tipografía") {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(FONTS.indices.toList()) { i ->
                                    val font = FONTS[i]
                                    val selected = uiState.font == font
                                    FilterChip(
                                        selected = selected,
                                        onClick = { viewModel.onFontChange(font) },
                                        label = { Text(FONT_LABELS[i], fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) }
                                    )
                                }
                            }
                        }

                        // ─── Eslogan ──────────────────────────────────────────
                        SectionCard(title = "Eslogan") {
                            OutlinedTextField(
                                value = uiState.tagline,
                                onValueChange = { viewModel.onTaglineChange(it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Frase bajo el nombre") },
                                placeholder = { Text("ej: Los mejores productos de la ciudad") },
                                maxLines = 2,
                                shape = RoundedCornerShape(12.dp),
                                supportingText = { Text("${uiState.tagline.length}/300") }
                            )
                        }

                        // ─── Botón guardar ────────────────────────────────────
                        Button(
                            onClick = { viewModel.saveStore() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !uiState.isSaving && uiState.storeSlug.length >= 3
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Save, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (uiState.storeEnabled) "Guardar Cambios" else "Guardar y Activar Tienda",
                                    fontWeight = FontWeight.Black, fontSize = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StoreStatusCard(
    storeEnabled: Boolean,
    storeUrl: String?,
    productsCount: Int,
    isToggling: Boolean,
    onToggle: () -> Unit,
    onVisit: (String) -> Unit
) {
    val statusColor = if (storeEnabled) Color(0xFF22c55e) else Color(0xFFF59E0B)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = statusColor.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(10.dp), shape = CircleShape, color = statusColor) {}
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (storeEnabled) "Tienda activa" else "Tienda inactiva",
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.weight(1f))
                if (isToggling) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Switch(
                        checked = storeEnabled,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF22c55e))
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoChip(label = "Productos", value = productsCount.toString(), icon = Icons.Default.Inventory)
            }

            if (storeEnabled && storeUrl != null) {
                Button(
                    onClick = { onVisit(storeUrl) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = statusColor)
                ) {
                    Icon(Icons.Default.OpenInBrowser, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver mi tienda", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("$value $label", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}

@Composable
private fun TemplateCard(
    id: Int,
    name: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.size(width = 110.dp, height = 100.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        tonalElevation = if (selected) 4.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(28.dp),
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Text(name, style = MaterialTheme.typography.labelSmall, fontWeight = if (selected) FontWeight.Black else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            if (selected) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(modifier = Modifier.size(6.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary) {}
            }
        }
    }
}

@Composable
private fun ColorPickerRow(label: String, selectedColor: String, onColorSelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val safeColor = try { Color(android.graphics.Color.parseColor(selectedColor)) } catch (e: Exception) { Color(0xFF6366F1) }
            Surface(modifier = Modifier.size(20.dp), shape = CircleShape, color = safeColor,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {}
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.weight(1f))
            Text(selectedColor.uppercase(), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(COLOR_PRESETS) { hex ->
                val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Gray }
                val selected = selectedColor.equals(hex, ignoreCase = true)
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape)
                        .background(color)
                        .then(if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
                        .clickable { onColorSelected(hex) },
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp), tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun NotValidatedMessage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.HourglassEmpty, null, modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            Text("Empresa en validación", fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.headlineSmall)
            Text(
                "Tu empresa necesita ser validada por el administrador antes de poder activar tu tienda web.",
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
