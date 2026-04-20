package com.christelldev.easyreferplus.ui.screens.address

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.christelldev.easyreferplus.data.model.SavedAddress
import com.christelldev.easyreferplus.ui.viewmodel.AddressListState
import com.christelldev.easyreferplus.ui.viewmodel.AddressViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedAddressesScreen(
    viewModel: AddressViewModel,
    onNavigateBack: () -> Unit,
    onAddAddress: () -> Unit,
    onEditAddress: (SavedAddress) -> Unit
) {
    val listState by viewModel.listState.collectAsState()
    val addresses by viewModel.addresses.collectAsState()
    var addressToDelete by remember { mutableStateOf<SavedAddress?>(null) }
    val isDark = isSystemInDarkTheme()
    val contentColor = if (isDark) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface

    LaunchedEffect(Unit) { viewModel.loadAddresses() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAddress,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar dirección")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Gradiente superior profundo
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
                    title = { Text("Mis Direcciones", fontWeight = FontWeight.Bold, color = contentColor) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = contentColor)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    windowInsets = WindowInsets.statusBars
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        listState is AddressListState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                        addresses.isEmpty() && listState !is AddressListState.Loading -> {
                            EmptyAddressesContent(onAddAddress = onAddAddress)
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(addresses, key = { it.id }) { address ->
                                    AddressCard(
                                        address = address,
                                        onEdit = { onEditAddress(address) },
                                        onDelete = { addressToDelete = address },
                                        onSetDefault = {
                                            viewModel.updateAddress(address.id, isDefault = true)
                                        }
                                    )
                                }
                                // Espacio para la barra de navegación y el FAB
                                item { Spacer(Modifier.navigationBarsPadding().height(80.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog de confirmación para eliminar
    addressToDelete?.let { address ->
        AlertDialog(
            onDismissRequest = { addressToDelete = null },
            title = { Text("Eliminar dirección") },
            text = { Text("¿Eliminar \"${address.label}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAddress(address.id)
                    addressToDelete = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { addressToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun AddressCard(
    address: SavedAddress,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit
) {
    val icon = when (address.label.lowercase()) {
        "casa", "home" -> Icons.Default.Home
        "trabajo", "work", "oficina" -> Icons.Default.Work
        else -> Icons.Default.Place
    }
    val borderColor by animateColorAsState(
        if (address.isDefault) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        label = "border"
    )

    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit),
        shape = RoundedCornerShape(14.dp),
        color = if (address.isDefault) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            if (address.isDefault) 2.dp else 1.dp, borderColor
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(44.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(address.label, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge)
                    if (address.isDefault) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text("Principal", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text(address.address, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (!address.isDefault) {
                    IconButton(onClick = onSetDefault, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Star, null, modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
private fun EmptyAddressesContent(onAddAddress: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Place, null, modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        Spacer(Modifier.height(16.dp))
        Text("Sin direcciones guardadas", fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text("Agrega tus direcciones favoritas para agilizar tus compras con delivery.",
            textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAddAddress, shape = RoundedCornerShape(14.dp)) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Agregar dirección", fontWeight = FontWeight.Bold)
        }
    }
}
