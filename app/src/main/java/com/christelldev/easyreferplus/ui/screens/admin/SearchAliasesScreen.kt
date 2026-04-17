package com.christelldev.easyreferplus.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.christelldev.easyreferplus.data.model.SearchAlias
import com.christelldev.easyreferplus.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAliasesScreen(
    productViewModel: ProductViewModel,
    onNavigateBack: () -> Unit
) {
    val aliases by productViewModel.aliases.collectAsState()
    val uiState by productViewModel.uiState.collectAsState()
    val isLoading = uiState is com.christelldev.easyreferplus.ui.viewmodel.ProductUiState.Loading

    var showAddDialog by remember { mutableStateOf(false) }
    var aliasInput by remember { mutableStateOf("") }
    var termInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { productViewModel.loadAliases() }

    LaunchedEffect(uiState) {
        if (uiState is com.christelldev.easyreferplus.ui.viewmodel.ProductUiState.Success) {
            showAddDialog = false
            aliasInput = ""
            termInput = ""
            productViewModel.resetState()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(bottom = 8.dp)
            ) {
                TopAppBar(
                    title = {
                        Text("Alias de Búsqueda", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimary)
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }

            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                if (isLoading && aliases.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (aliases.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Search, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Sin alias", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Agrega sinónimos para mejorar la búsqueda", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(aliases, key = { it.id }) { alias ->
                            AliasCard(alias = alias, onDelete = { productViewModel.deleteAlias(alias.id) })
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; aliasInput = ""; termInput = "" },
            title = { Text("Nuevo Alias", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = aliasInput,
                        onValueChange = { aliasInput = it },
                        label = { Text("Alias (lo que escribe el usuario)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = termInput,
                        onValueChange = { termInput = it },
                        label = { Text("Término real a buscar") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (aliasInput.isNotBlank() && termInput.isNotBlank()) {
                            productViewModel.createAlias(aliasInput.trim(), termInput.trim())
                        }
                    },
                    enabled = aliasInput.isNotBlank() && termInput.isNotBlank() && !isLoading
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false; aliasInput = ""; termInput = "" }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun AliasCard(alias: SearchAlias, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(6.dp)) {
                        Text(
                            alias.alias,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(6.dp)) {
                        Text(
                            alias.term,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
